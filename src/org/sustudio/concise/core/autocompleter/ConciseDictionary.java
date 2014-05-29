package org.sustudio.concise.core.autocompleter;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.suggest.InputIterator;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.wordlister.ConciseTermsEnum;

/**
 * Concise Dictionary: terms taken from the given field
 * of a Lucene index, and perform an filter.
 */
public class ConciseDictionary implements Dictionary {

	private IndexReader reader;
	private String field;
	private boolean showPartOfSpeech;
	
	/**
	 * Creates a new Dictionary, pulling source terms from
	 * the specified <code>field</code> in the provided <code>reader</code>.
	 */
	public ConciseDictionary(IndexReader reader, String field, boolean showPartOfSpeech) {
		this.reader = reader;
		this.field = field;
		this.showPartOfSpeech = showPartOfSpeech;
	}
	
	@Override
	public InputIterator getEntryIterator() throws IOException {
		final Terms terms = MultiFields.getTerms(reader, field);
		if (terms != null) {
			TermsEnum termsEnum = new ConciseTermsEnum(terms.iterator(null), showPartOfSpeech, CCPrefs.LEMMA_ENABLED);
			return new InputIterator.InputIteratorWrapper(termsEnum);
		} else {
			return InputIterator.EMPTY;
		}
	}

}
