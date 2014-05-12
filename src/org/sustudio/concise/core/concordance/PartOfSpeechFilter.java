package org.sustudio.concise.core.concordance;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.sustudio.concise.core.Config;

public class PartOfSpeechFilter extends TokenFilter {
	
	private boolean showPartOfSpeech;
	private CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
	private OffsetAttribute offAttr = addAttribute(OffsetAttribute.class);
	private PartOfSpeechAttribute posAttr = addAttribute(PartOfSpeechAttribute.class);
	
	public PartOfSpeechFilter(TokenStream input, boolean showPartOfSpeech) {
		super(input);
		this.showPartOfSpeech = showPartOfSpeech;
	}
	
	public boolean incrementToken() throws IOException {
		if (input.incrementToken()) {
			String term = termAttr.toString();
			int seperatorOffset = term.indexOf(Config.SYSTEM_POS_SEPERATOR);
			if (seperatorOffset != -1) 
			{
				// add part-of-speech attribute
				String pos = term.substring(seperatorOffset + Config.SYSTEM_POS_SEPERATOR.length(), term.length());
				posAttr.setPartOfSpeech(pos);
				posAttr.setWord(term.substring(0, seperatorOffset));
				if (!showPartOfSpeech) {
					termAttr.setLength(seperatorOffset);
					int startOffset = offAttr.startOffset();
					offAttr.setOffset(startOffset, startOffset + seperatorOffset);
				}
			}
			return true;
		}
		return false;
	}
	
}
