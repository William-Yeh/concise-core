package org.sustudio.concise.core.wordlister;

import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.Config;

/**
 * 將自訂的 {@link CCPrefs#POS_SEPARATOR} 標籤換回系統的標籤，使用 {@link CustomPartOfSpeechEncoder} 換回來
 * 
 * @author Kuan-ming Su
 *
 */
public class CustomPartOfSpeechDecoder {

	public static String decode(String word) {
		if (!CCPrefs.POS_SEPARATOR.equals(Config.SYSTEM_POS_SEPERATOR)) {
			word = word.replace(CCPrefs.POS_SEPARATOR, Config.SYSTEM_POS_SEPERATOR);
		}
		return word;
	}
	
}
