package org.sustudio.concise.core.concordance;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.wordlister.CustomPartOfSpeechEncoder;

/**
 * 允許使用者從 {@link CCPrefs#POS_SEPARATOR} 自訂詞性的分隔標籤
 * 
 * @author Kuan-ming Su
 *
 */
public class PartOfSpeechSeparatorFilter extends TokenFilter {

	CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
	//PartOfSpeechAttribute posAttr = addAttribute(PartOfSpeechAttribute.class);
	
	public PartOfSpeechSeparatorFilter(TokenStream input) {
		super(input);
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (input.incrementToken()) {
			//if (posAttr.getWord() == null) 
			//	return true;
			
			String word = termAttr.toString();
			if (word.contains(Config.SYSTEM_POS_SEPERATOR)) {
				termAttr.setEmpty();
				termAttr
				.append(CustomPartOfSpeechEncoder.encode(word));
				//.append(posAttr.getWord())
				//.append(CCPrefs.POS_SEPARATOR)
				//.append(posAttr.getPartOfSpeech());
			}
			return true;
		}
		return false;
	}

}
