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
import org.sustudio.concise.core.Workspace.INDEX;
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
		return sumTotalTermFreq(workspace, INDEX.DOCUMENT, showPartOfSpeech);
	}
	
	/**
	 * 僅計算全部詞彙的數量（用在計算 reference corpus 的時候）
	 * @param workspace
	 * @param indexType
	 * @param showPartOfSpeech
	 * @return
	 * @throws Exception
	 */
	public static long sumTotalTermFreq(Workspace workspace, INDEX indexType, boolean showPartOfSpeech) throws Exception {
		long sum = 0L;
		for (Word word : new WordIterator(workspace.getIndexReader(indexType), showPartOfSpeech, true)) {
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
	
	private LemmaList lemmaList;
	
	/**
	 * 讀取 {@link Workspace} 索引中的詞彙
	 * @param workspace
	 * @param showPartOfSpeech
	 * @throws Exception
	 */
	public WordIterator(Workspace workspace, boolean showPartOfSpeech) throws Exception {
		this(workspace, INDEX.DOCUMENT, showPartOfSpeech);
	}
	
	/**
	 * 用來讀參照語料庫的索引
	 * @param reader
	 * @param showPartOfSpeech
	 * @throws Exception
	 */
	public WordIterator(Workspace workspace, INDEX indexType, boolean showPartOfSpeech) throws Exception {
		this(workspace.getIndexReader(indexType), showPartOfSpeech, false);
	}
	
	protected WordIterator(final IndexReader reader, boolean showPartOfSpeech, boolean countSumTotalOnly) throws Exception {
		this.reader = reader;
		this.countSumTotalOnly = countSumTotalOnly;
		
		Terms terms = MultiFields.getTerms(reader, ConciseField.CONTENT.field());
		if (terms != null) 
		{
			if (CCPrefs.LEMMA_ENABLED && CCPrefs.LEMMA_LIST != null) {
				lemmaList = (LemmaList) CCPrefs.LEMMA_LIST.clone();
			} else {
				lemmaList = new LemmaList();
			}
			
			
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
				if (CCPrefs.LEMMA_ENABLED && lemmaList != null) {
					Lemma lemma = CCPrefs.LEMMA_LIST.get(term);
					lemmaList.remove(lemma);
					if (lemma != null) {
						word = buildLemma(word, lemma);
					}
				}
				
				nextWord = word;
				return nextWord;
			}
		}
		
		// 處理沒有在詞彙中的 Lemma
		if (lemmaList.size() > 0) {
			Lemma lemma = lemmaList.remove(0);
			Word word = new Word(lemma.getWord(), 0, 0);
			word = buildLemma(word, lemma);
			nextWord = word;
		}
		return nextWord;
	}
	
	private Word buildLemma(Word word, Lemma lemma) throws Exception {
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
		return word;
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
		throw new UnsupportedOperationException(
				getClass().getSimpleName() + " cannot perform remove()");
	}
	
}
