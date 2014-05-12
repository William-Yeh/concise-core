package org.sustudio.concise.core.wordlister;

import java.util.Iterator;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.concordance.AllDocsCollector;
import org.sustudio.concise.core.corpus.importer.ConciseField;

/**
 * iterate words from lucene index
 * 
 * @author Kuan-ming Su
 *
 */
public class WordIterator implements Iterator<Word>, Iterable<Word> {

	/**
	 * 僅計算全部詞彙的數量
	 * @param workspace
	 * @param showPartOfSpeech
	 * @return
	 * @throws Exception
	 */
	public static long sumTotalTermFreq(Workspace workspace, boolean showPartOfSpeech) throws Exception {
		return sumTotalTermFreq(workspace.getIndexReader(), showPartOfSpeech);
	}
	
	/**
	 * 僅計算全部詞彙的數量（用在計算 reference corpus 的時候）
	 * @param reader
	 * @param showPartOfSpeech
	 * @return
	 * @throws Exception
	 */
	public static long sumTotalTermFreq(IndexReader reader, boolean showPartOfSpeech) throws Exception {
		long sum = 0L;
		for (Word word : new WordIterator(reader, showPartOfSpeech, true)) {
			sum += word.getTotalTermFreq();
		}
		return sum;
	}
	
	private final IndexReader reader;
	private final boolean countSumTotalOnly;
	
	private TermsEnum termsEnum;
	private Word nextWord;
	
	/** 做一個標記，看看 TermsEnum 是不是還在繼續 */
	private boolean termsEnumContinues = true;
	
	/**
	 * 讀取 {@link Workspace} 索引中的詞彙
	 * @param workspace
	 * @param showPartOfSpeech
	 * @throws Exception
	 */
	public WordIterator(Workspace workspace, boolean showPartOfSpeech) throws Exception {
		this(workspace.getIndexReader(), showPartOfSpeech);
	}
	
	/**
	 * 用來讀參照語料庫的索引
	 * @param reader
	 * @param showPartOfSpeech
	 * @throws Exception
	 */
	public WordIterator(final IndexReader reader, boolean showPartOfSpeech) throws Exception {
		this(reader, showPartOfSpeech, false);
	}
	
	protected WordIterator(final IndexReader reader, boolean showPartOfSpeech, boolean countSumTotalOnly) throws Exception {
		this.reader = reader;
		this.countSumTotalOnly = countSumTotalOnly;
		
		Terms terms = MultiFields.getTerms(reader, ConciseField.CONTENT.field());
		if (terms != null) 
		{
			boolean lemmatize = countSumTotalOnly ? false : CCPrefs.LEMMA_ENABLED;
			// ConciseTermsEnum 是過濾後的 TermsEnum
			termsEnum = new ConciseTermsEnum(terms.iterator(null), showPartOfSpeech, lemmatize);
			nextWord = readNextWord();
		}
	}

	private Word readNextWord() throws Exception {
		nextWord = null;
		if (termsEnumContinues) {	// 如果 TermsEnum 結束後還繼續，會出現錯誤
			while (termsEnumContinues = termsEnum.next() != null) {
				String term = termsEnum.term().utf8ToString();
				term = CustomPartOfSpeechEncoder.encode(term);
				
				long docFreq = termsEnum.docFreq();
				long totalTermFreq = termsEnum.totalTermFreq();
				Word word = new Word( term, docFreq, totalTermFreq );
				
				if (countSumTotalOnly) {
					nextWord = word;
					break;
				}
				
				// build lemma
				if (CCPrefs.LEMMA_ENABLED && CCPrefs.LEMMA_LIST != null) {
					Lemma lemma = CCPrefs.LEMMA_LIST.get(term);
					if (lemma != null) {
						word.addChild(word.clone());
						word.setDocFreq(0);
						StringBuilder queryWords = new StringBuilder();
						queryWords.append("\"" + word.getWord() + "\"");
						for (String form : lemma.getForms()) {
							form = CustomPartOfSpeechDecoder.decode(form);
							// get frequency
							Term formTerm = new Term(ConciseField.CONTENT.field(), form);
							long formDocFreq = reader.docFreq(formTerm);
							long formTotalTermFreq = reader.totalTermFreq(formTerm);
							Word formWord = new Word( CustomPartOfSpeechEncoder.encode(form), formDocFreq, formTotalTermFreq );
							word.addChild(formWord);
							word.setTotalTermFreq(word.getTotalTermFreq() + formTotalTermFreq);
							queryWords.append(" OR \"" + formWord.getWord() + "\"");
						}
						
						// query to get total docFreq
						QueryParser parser = new QueryParser(Config.LUCENE_VERSION, ConciseField.CONTENT.field(), new WhitespaceAnalyzer(Config.LUCENE_VERSION));
						Query q = parser.parse(queryWords.toString());
						IndexSearcher searcher = new IndexSearcher(reader);
						AllDocsCollector allDocs = new AllDocsCollector();
						searcher.search(q, allDocs);
						word.setDocFreq(allDocs.allDocs().length);
					}
				}
				
				nextWord = word;
				break;
			}
		}
		return nextWord;
	}
	
	@Override
	public Iterator<Word> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return nextWord != null;
	}

	@Override
	public Word next() {
		Word returnWord = nextWord;
		try {
			nextWord = readNextWord();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnWord;
	}
	

	/** Do nothing. Do not use this one. */
	public void remove() {
		// Do nothing
	}
	
}
