package org.sustudio.concise.core.collocation;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.wordlister.Lemma;

/**
 * 把字詞用lemma取代。 <br />
 * TODO 有詞性的時候還有問題。
 * 
 * @author Kuan-ming Su
 *
 */
public class LemmaFilter extends TokenFilter {

	private CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	
	public LemmaFilter(TokenStream input) {
		super(input);
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (input.incrementToken()) {
			if (CCPrefs.LEMMA_LIST != null && 
				!CCPrefs.LEMMA_LIST.isEmpty()) 
			{
				// checking lemma_list
				Lemma lemma = CCPrefs.LOWERCASE_ENABLED ? 
						CCPrefs.LEMMA_LIST.getLemmaWithFormIgnoreCase(termAtt.toString()) :
						CCPrefs.LEMMA_LIST.getLemmaWithForm(termAtt.toString());
				if (lemma != null) {
					termAtt.setEmpty();
					termAtt.append(lemma.getWord());
				}
			}
			return true;
		}
		return false;
	}
}
