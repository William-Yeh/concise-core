package org.sustudio.concise.core.collocation;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.wordlister.WordFilter;

/**
 * Filter TokenStream with {@link WordFilter}, which reads token settings in {@link CCPrefs}.  
 * 
 * @author Kuan-ming Su.
 *
 */
public class ConciseTokenFilter extends TokenFilter {
	
	WordFilter wordFilter;
	CharTermAttribute termAttr;
	PositionIncrementAttribute posIncrAttr;
	
	public ConciseTokenFilter(TokenStream input) {
		super(input);
		wordFilter = new WordFilter();
		termAttr = input.addAttribute(CharTermAttribute.class);
		posIncrAttr = input.addAttribute(PositionIncrementAttribute.class);
	}
	
	public boolean incrementToken() throws IOException {
		int extraIncrement = 0;
		while (true) {
			boolean hasNext = input.incrementToken();
			if (hasNext) {
				if (!wordFilter.accept(termAttr.toString())) {
					extraIncrement++;	// filter this term out
					continue;
				}
				if (extraIncrement > 0) {
					posIncrAttr.setPositionIncrement(posIncrAttr.getPositionIncrement() + extraIncrement);
				}
			}
			return hasNext;
		}
	}
	
}
