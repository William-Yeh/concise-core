package org.sustudio.concise.core.collocation;

import java.util.HashSet;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.collocation.TemporaryCollocateIndexer.CIField;
import org.sustudio.concise.core.concordance.Conc;
import org.sustudio.concise.core.concordance.ConcLine;
import org.sustudio.concise.core.corpus.importer.ConciseField;
import org.sustudio.concise.core.wordlister.Lemma;
import org.sustudio.concise.core.wordlister.WordIterator;

/**
 * surface 方式的 CollocateIterator，以句子為邊界，需要設定左右跨距（從 {@link Conc} 設定）
 * 
 * @author Kuan-ming Su
 *
 */
public class SurfaceCollocateIterator extends CollocateIterator {
	
	private Conc conc;
	private long corpusSumTotalTermFreq = -1;
	
	private IndexReader reader;
	private Fields fields;
	private TermsEnum termsEnum;
	private TermsEnum corpusTermsEnum;
	private HashSet<BytesRef> nodesToSkip = new HashSet<BytesRef>();
	
	/** corpus sum total term freq */
	private long Nc;
	
	/** node freq */
	private long fn;
	
	/** sample sum total term freq */
	private long Ns;
	
	private static final String _NODE_SEPARATOR = "_NODE_";
	
	public SurfaceCollocateIterator(Conc conc) throws Exception {
		this(conc, null);
	}
	
	public SurfaceCollocateIterator(Conc conc, Map<CollocateMeasurement, Double> filters) throws Exception 
	{
		super(filters);
		this.conc = conc;
		
		ScoreDoc[] hitDocs = conc.hitDocs();
		if (hitDocs.length > 0) {
			TemporaryCollocateIndexer ci = new TemporaryCollocateIndexer(indexDirectory, 
																		 conc.left_span_size,
																		 conc.right_span_size);
			for (ScoreDoc scoreDoc : hitDocs) 
			{
				TokenSentenceHighlighter highlighter = new TokenSentenceHighlighter(conc, scoreDoc.doc);
				String[] sentences = highlighter.getHighlightSpans();
				
				// Iterate Sentence
				for (String sentence : sentences)
				{					
					int nodeOffset = sentence.indexOf(Conc.preNodeTag);
					if (sentence.isEmpty() || nodeOffset == -1) continue;
					
					StringBuilder sb = new StringBuilder(sentence);
					int nodeEndOffset = 0;
						
					nodeEndOffset = sb.indexOf(Conc.postNodeTag, nodeOffset);
					ConcLine concLine = new ConcLine();
					String node = sb.substring(nodeOffset + Conc.preNodeTag.length(), nodeEndOffset).trim();
					node = node.replace(" ", _NODE_SEPARATOR);
					concLine.setNode(node);
					concLine.setLeft(sb.substring(0, nodeOffset).trim());
					concLine.setRight(sb.substring(nodeEndOffset + Conc.postNodeTag.length(), sb.length()).trim());
					
					// node 會有標籤來標示
					ci.addDocumentWithConcLine(concLine);
					sb.setLength(0);
				}
				ci.commit();
			}
			ci.closeWriter();

			
			Nc = getCorpusSumTotalTermFreq();
			
			// read collocate(s) from tmp index dir
			reader = DirectoryReader.open(indexDirectory);
			IndexSearcher searcher = new IndexSearcher(reader);
			CollectionStatistics stats = searcher.collectionStatistics(CIField.TEXT.name());
			Ns = stats.sumTotalTermFreq();
			fn = stats.docCount();
			
			Terms corpusTerms = MultiFields.getTerms(conc.workspace.getIndexReader(), ConciseField.CONTENT.field());
			corpusTermsEnum = corpusTerms.iterator(null);
			
			Terms terms = MultiFields.getTerms(reader, CIField.TEXT.name());
			fields = MultiFields.getFields(reader);
			if (terms != null) {
				
				// 先把 node 的字眼記起來，下面要跳過去
				TermsEnum te = terms.iterator(null);
				while (te.next() != null) {
					String word = te.term().utf8ToString();
					if (word.startsWith(Conc.preNodeTag) || word.endsWith(Conc.postNodeTag)) {
						word = word.replace(Conc.preNodeTag, "").replace(Conc.postNodeTag, "");
						BytesRef node = new BytesRef(word);
						nodesToSkip.add(node);
					}
				}
				
				termsEnum = terms.iterator(null);
				nextCollocate = readNextCollocate();
			}
		}
	}
	
	protected Collocate readNextCollocate() throws Exception {
		if (termsEnum != null)
		while (termsEnum.next() != null) 
		{
			BytesRef term = termsEnum.term();
			if (nodesToSkip.contains(term)) {
				continue;
			}
			
			long fnc = termsEnum.totalTermFreq();			
			long fc = conc.workspace.getIndexReader()
									.totalTermFreq(new Term(ConciseField.CONTENT.field(), term));
			
			String word = term.utf8ToString();
			if (fc == 0) {
				// 這應該是 node，因為在 collocate 中掛上了標籤，要去掉標籤再找
				word = word.replace(Conc.preNodeTag, "").replace(Conc.postNodeTag, "");
				BytesRef bytes = new BytesRef(word);
				fc += conc.workspace.getIndexReader()
									.totalTermFreq(new Term(ConciseField.CONTENT.field(), bytes));
				
				// fnc 也會漏掉，所以也得加上
				fnc += reader.totalTermFreq(new Term(CIField.TEXT.name(), word));
			}
			
			if (CCPrefs.LEMMA_ENABLED) {
				// lemma
				Lemma lemmaToCheck = null;
				if (CCPrefs.LEMMA_LIST != null) {
					// 檢查的時候要記得去掉 node 的標籤
					lemmaToCheck = CCPrefs.LEMMA_LIST.get(word);
					
					// 應該不會有 form ， 因為在 Highlighter 裡面已經被取代掉了
				}
				if (lemmaToCheck != null) {
					for (String form : lemmaToCheck.getForms()) {
						if (corpusTermsEnum.seekExact(new BytesRef(form))) {
							fc += corpusTermsEnum.totalTermFreq();
						}
					}
				}
			}
			
			// 這個會輸出 <node> 的標簽，暫時不用
			//Collocate collocate = new Collocate(term.utf8ToString(), fn, fc, fnc, Nc, Ns);
			Collocate collocate = new Collocate(word.replace(_NODE_SEPARATOR, " "), fn, fc, fnc, Nc, Ns);
			
			collocate.setLeftFreq( reader.totalTermFreq(new Term(CIField.LEFT.name(),  word)));
			collocate.setRightFreq(reader.totalTermFreq(new Term(CIField.RIGHT.name(), word)));
			collocate.setNodeFreq( reader.totalTermFreq(new Term(CIField.NODE.name(),  word)));
			
			// put position vectors
			for (String field : fields) {
				if (field.matches("[LR]\\d+")) {
					//long value = reader.totalTermFreq(new Term(field, term));
					long value = reader.totalTermFreq(new Term(field, word));
					collocate.setPositionVector(field, value);
				}
			}
			
			// check filters
			boolean skip = false;
			if (filters != null) {
				for (Map.Entry<CollocateMeasurement, Double> entry : filters.entrySet()) {
					if (entry.getValue().doubleValue() > entry.getKey().getValue(collocate)) {
						skip = true;
						break;
					}
				}
			}
			if (skip) continue;
			
			return collocate;
		}
		
		termsEnum = null;
		fields = null;
		reader.close();
		indexDirectory.close();
		if (indexDirectory instanceof FSDirectory) {
			FileUtils.deleteDirectory(((FSDirectory) indexDirectory).getDirectory());
		}
		
		return null;
	}
	
	protected long getCorpusSumTotalTermFreq() throws Exception {
		if (corpusSumTotalTermFreq == -1) {
			corpusSumTotalTermFreq = WordIterator.sumTotalTermFreq(conc.workspace, 
																	  conc.showPartOfSpeech);
		}
		return corpusSumTotalTermFreq;
	}
	
}
