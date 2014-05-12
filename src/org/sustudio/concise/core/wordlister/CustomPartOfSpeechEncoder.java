package org.sustudio.concise.core.wordlister;

import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.Config;

/**
 * 將系統的詞性標籤換成 {@link CCPrefs#POS_SEPARATOR} 的自訂標籤，使用 {@link CustomPartOfSpeechDecoder} 換回來
 * 
 * @author Kuan-ming Su
 *
 */
public class CustomPartOfSpeechEncoder {

	public static String encode(String word) {
		if (!CCPrefs.POS_SEPARATOR.equals(Config.SYSTEM_POS_SEPERATOR)) {
			word = word.replace(Config.SYSTEM_POS_SEPERATOR, CCPrefs.POS_SEPARATOR);
		}
		return word;
	}
	
}
