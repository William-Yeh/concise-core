package org.sustudio.concise.core.keyword;

import java.util.Iterator;

import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.wordlister.Word;
import org.sustudio.concise.core.wordlister.WordIterator;

public class KeywordIterator implements Iterator<Keyword>, Iterable<Keyword> {
	
	private Workspace workspace;
	private boolean showPartOfSpeech;
	private WordIterator wordIterator;
	private WordIterator wordIteratorRef;
	/* remove after debug
	private final IndexReader reader;
	private final IndexReader referenceReader;
	private final WordFilter wordFilter = new WordFilter();
	private TermsEnum termsEnum;
	private TermsEnum referenceTermsEnum;
	*/
	private long corpusSumTotalTermFreq = -1;
	private long referenceSumTotalTermFreq = -1;
	private Keyword nextKeyword = null;
	
	public KeywordIterator(final Workspace workspace, boolean showPartOfSpeech) throws Exception {
		
		this.workspace = workspace;
		this.showPartOfSpeech = showPartOfSpeech;
		/*
		this.reader = workspace.getIndexReader();
		this.referenceReader = workspace.getIndexReaderRef();
		
		Terms terms = MultiFields.getTerms(reader, 
										   CCField.CONTENT.field());
		Terms referenceTerms = MultiFields.getTerms(referenceReader, 
													CCField.CONTENT.field());
		
		if (terms != null && referenceTerms != null) {
			termsEnum = terms.iterator(null);
			referenceTermsEnum = referenceTerms.iterator(null);
			nextKeyword = readNextKeyword();
		}
		*/
		wordIterator = new WordIterator(workspace.getIndexReader(), showPartOfSpeech);
		wordIteratorRef = new WordIterator(workspace.getIndexReaderRef(), showPartOfSpeech);
		if (wordIterator != null && wordIteratorRef != null) {
			nextKeyword = readNextKeyword();
		}
	}
	
	private Keyword readNextKeyword() {
		try 
		{
			while (wordIterator.hasNext()) 
			{
				Word word = wordIterator.next();
				long f1 = word.getTotalTermFreq();
				long f2 = workspace.getIndexReaderRef().totalTermFreq(word.getTerm());
				
				return new Keyword(word.getWord(), 
									 f1, 
									 f2, 
									 getCorpusSumTotalTermFreq(), 
									 getReferenceSumTotalTermFreq());
			}
			
			while (wordIteratorRef.hasNext()) {
				Word word = wordIteratorRef.next();
				long f1 = workspace.getIndexReader().totalTermFreq(word.getTerm());
				if (f1 > 0) {
					continue;	// keyword already exists
				}
				
				long f2 = word.getTotalTermFreq();
				return new Keyword(word.getWord(), 
									 f1, 
									 f2, 
									 getCorpusSumTotalTermFreq(), 
									 getReferenceSumTotalTermFreq());
			}
			/*
			BytesRef term;
			while ( (term = termsEnum.next()) != null ) 
			{
				String word = term.utf8ToString();
				if (wordFilter.accept(word)) 
				{
					long f1 = termsEnum.totalTermFreq();
					long f2 = referenceReader.totalTermFreq(new Term(CCField.CONTENT.field(), term));
					
					return new Keyword(word, 
										 f1, 
										 f2, 
										 getCorpusSumTotalTermFreq(), 
										 getReferenceSumTotalTermFreq());
				}
			}
			
			while ( (term = referenceTermsEnum.next()) != null ) 
			{
				String word = term.utf8ToString();
				if (wordFilter.accept(word)) 
				{
					long f1 = reader.totalTermFreq(new Term(CCField.CONTENT.field(), term));
					if (f1 > 0) {
						continue;	// keyword already exists
					}
					
					long f2 = referenceTermsEnum.totalTermFreq();
					return new Keyword(word, 
										 f1, 
										 f2, 
										 getCorpusSumTotalTermFreq(), 
										 getReferenceSumTotalTermFreq());
				}
			}
			
			termsEnum = null;
			referenceTermsEnum = null;
			*/
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public long getCorpusSumTotalTermFreq() throws Exception {
		if (corpusSumTotalTermFreq == -1) {
			// force to do a word lister
			corpusSumTotalTermFreq = WordIterator.sumTotalTermFreq(workspace, showPartOfSpeech);
		}
		return corpusSumTotalTermFreq;
	}
	
	public long getReferenceSumTotalTermFreq() throws Exception {
		if (referenceSumTotalTermFreq == -1) {
			// force to do a word lister
			referenceSumTotalTermFreq = WordIterator.sumTotalTermFreq(workspace.getIndexReaderRef(), showPartOfSpeech);
		}
		return referenceSumTotalTermFreq;
	}

	
	public Iterator<Keyword> iterator() {
		return this;
	}

	
	public boolean hasNext() {
		return nextKeyword != null;
	}

	
	public Keyword next() {
		Keyword returnKeyword = nextKeyword;
		nextKeyword = readNextKeyword();
		return returnKeyword;
	}

	
	/**
	 * DO NOT USE.
	 */
	public void remove() {
		throw new UnsupportedOperationException("remove() is unsupported.");
	}
	
	
}
