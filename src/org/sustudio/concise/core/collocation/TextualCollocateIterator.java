package org.sustudio.concise.core.collocation;

import java.util.Map;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.BytesRef;
import org.sustudio.concise.core.collocation.TemporaryCollocateIndexer.CIField;
import org.sustudio.concise.core.concordance.Conc;


/**
 * textual 方式的 CollocateIterator，以句子為邊界
 * 
 * @author Kuan-ming Su
 *
 */
public class TextualCollocateIterator extends CollocateIterator {
	
	/** 文本的範圍	 */
	public enum BOUNDARY { 
		/** 句子 */
		SENTENCE, 
		/** 段落 */
		PARAGRAPH 
	}
	
	private IndexReader reader;
	private long numberOfTexts;
	private long nodeMarginalFrequency;
	private TermsEnum nodeTermsEnum;
	private TermsEnum textTermsEnum;
	
	public TextualCollocateIterator(Conc conc, BOUNDARY boundary) throws Exception {
		this(conc, boundary, null);
	}
	
	public TextualCollocateIterator(Conc conc, BOUNDARY boundary, Map<CollocateMeasurement, Double> filters) throws Exception 
	{
		super(conc, filters);
		
		TemporaryCollocateIndexer ci = new TemporaryCollocateIndexer(temporaryDirectory, 0, 0);
		numberOfTexts = 0;
		nodeMarginalFrequency = 0;
		
		// collecting data
		for (ScoreDoc scoreDoc : conc.hitDocs()) 
		{
			TextualHighlighter highlighter;
			switch (boundary) {
			case PARAGRAPH:
				highlighter = new TextualParagraphHighlighter(conc, scoreDoc.doc);
				break;
			case SENTENCE:
			default:
				highlighter = new TextualSentenceHighlighter(conc, scoreDoc.doc);
			}
			String[] texts = highlighter.getAllTextsWithHighlight();
			
			// Iterate text
			for (String text : texts) {
				if (text.contains(Conc.preNodeTag) && text.contains(Conc.postNodeTag)) {
					nodeMarginalFrequency++;
				}
				ci.addDocumentWithTextualSentence(text, Conc.preNodeTag, Conc.postNodeTag);
			}
			ci.commit();
		}
		ci.closeWriter();
				
		reader = DirectoryReader.open(temporaryDirectory);
		numberOfTexts = reader.numDocs();
		
		Terms terms = MultiFields.getTerms(reader, CIField.NODE.name());
		Terms textTerms = MultiFields.getTerms(reader, CIField.TEXT.name());
		if (terms != null && textTerms != null) {
			nodeTermsEnum = terms.iterator(null);
			textTermsEnum = textTerms.iterator(null);
			nextCollocate = readNextCollocate();
		}
		
	}
	
	protected Collocate readNextCollocate() throws Exception {
		reader = DirectoryReader.open(temporaryDirectory);
		BytesRef term = null;
		while ((term = nodeTermsEnum.next()) != null) {
			String word = term.utf8ToString();
			int coOccurrenceCount = nodeTermsEnum.docFreq();
			int collocateMarginalFrequency = textTermsEnum.seekExact(term) ? textTermsEnum.docFreq() : 0;
			if (word.startsWith(Conc.preNodeTag) && word.endsWith(Conc.postNodeTag)) {
				// 因為 node word 在處理的時候會包上tag，所以找 TEXT 那邊應該會找不到，
				// 這時候要去掉 tag 去找
				BytesRef node = new BytesRef(word.replace(Conc.preNodeTag, "").replace(Conc.postNodeTag, "").replace(CollocateIterator._NODE_SEPARATOR, " "));
				collocateMarginalFrequency += textTermsEnum.seekExact(node) ? textTermsEnum.docFreq() : 0;
			}
			
			// 這邊不需要再處理 Lemma 的問題
			// 應該都已經透過 Highlighter 的 Encoder 處理了
			Collocate collocate =  new Collocate(word.replace(Conc.preNodeTag, "").replace(Conc.postNodeTag, "").replace(CollocateIterator._NODE_SEPARATOR, " "),
												 coOccurrenceCount,
												 nodeMarginalFrequency,
												 collocateMarginalFrequency,
												 numberOfTexts);
			if (word.startsWith(Conc.preNodeTag) && word.endsWith(Conc.postNodeTag)) {
				collocate.setNodeFreq(nodeMarginalFrequency);
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
			if (skip) {
				continue;
			}
			
			return collocate;
		}
		
		reader.close();
		closeTemporaryDirectory();
		
		return null;
	}
	
}
