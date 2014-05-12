package org.sustudio.concise.core.wordlister;

import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.sustudio.concise.core.CCPrefs;

/**
 * Filter word string by preference (using regex). <br />
 * 同時也過濾 Stop Word （在 {@link CCPrefs} 裡頭設定）
 * 
 * @author Kuan-ming Su
 *
 */
public class WordFilter {

	public Pattern pattern;
	
	public WordFilter() {
		pattern = filterPattern();
	}
	
	public Pattern filterPattern() {
		// build token filters
		String patternString = "\\s";
		
		if (!CCPrefs.SHOW_TOKEN_LETTER)
			patternString += "\\p{L}";
		
		if (!CCPrefs.SHOW_TOKEN_NUMBER)
			patternString += "\\p{N}";
		
		if (!CCPrefs.SHOW_TOKEN_SYMBOL)
			patternString += "\\p{S}";
		
		if (!CCPrefs.SHOW_TOKEN_MARK)
			patternString += "\\p{M}";
		
		if (!CCPrefs.SHOW_TOKEN_PUNCTUATION)
			patternString += "\\p{P}";
		
		return Pattern.compile("[" + patternString + "]+.*?");
	}
	
	public boolean accept(String word) {
		// Stop word filter
		if (CCPrefs.STOP_WORDS_ENABLED && 
			CCPrefs.stopWords != null && 
			CCPrefs.stopWords.length > 0) 
		{
			// skip stop words.
			if (ArrayUtils.contains(CCPrefs.stopWords, word)) {
				return false;
			}
		}
		
		return 	!word.isEmpty() && 
				!pattern.matcher(word).matches();
	}
	
	public String toString() {
		return filterPattern().pattern();
	}
	
}
