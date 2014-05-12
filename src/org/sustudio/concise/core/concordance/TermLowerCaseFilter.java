package org.sustudio.concise.core.concordance;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharacterUtils;
import org.apache.lucene.util.Version;
import org.sustudio.concise.core.Config;

/**
 * Normalizes token text to lower case.
 * <a name="version"/>
 * <p>You must specify the required {@link Version}
 * compatibility when creating LowerCaseFilter:
 * <ul>
 *   <li> As of 3.1, supplementary characters are properly lowercased.
 * </ul>
 */
public final class TermLowerCaseFilter extends TokenFilter {
	private final CharacterUtils charUtils;
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	  
	/**
	 * Create a new LowerCaseFilter, that normalizes token text to lower case.
	 * 
	 * @param matchVersion See <a href="#version">above</a>
	 * @param in TokenStream to filter
	 */
	public TermLowerCaseFilter(Version matchVersion, TokenStream in) {
	  super(in);
	  charUtils = CharacterUtils.getInstance(matchVersion);
	}
	  
	@Override
	public final boolean incrementToken() throws IOException {
		if (input.incrementToken()) {
			// take part-of-speech into consideration
	    	int length = termAtt.toString().indexOf(Config.SYSTEM_POS_SEPERATOR);
	    	charUtils.toLowerCase(termAtt.buffer(), 0, length == -1 ? termAtt.length() : length);
	    	return true;
	    }
	    return false;
	}
}