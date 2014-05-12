package org.sustudio.concise.core.corpus.importer;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.concordance.PartOfSpeechAttribute;

public class ImportPOSFilter extends TokenFilter {
	
	private final String posSeperator;
	private CharTermAttribute termAttr;
	private OffsetAttribute offAttr;
	private PartOfSpeechAttribute posAttr;
	
	private boolean hasOriginal = false;
	
	public ImportPOSFilter(TokenStream input) {
		this(input, Config.SYSTEM_POS_SEPERATOR);
	}
	
	public ImportPOSFilter(TokenStream input, final String posSeperator) {
		super(input);
		this.posSeperator = posSeperator;
		termAttr = input.addAttribute(CharTermAttribute.class);
		offAttr = input.addAttribute(OffsetAttribute.class);
		posAttr = input.addAttribute(PartOfSpeechAttribute.class);
	}
	
	public boolean incrementToken() throws IOException {
		if (hasOriginal) {
			String term = termAttr.toString();
			int splitterPosition = term.indexOf(posSeperator);
			if (splitterPosition != -1) 
			{
				// add part-of-speech attribute
				String pos = term.substring(splitterPosition + posSeperator.length(), term.length());
				posAttr.setPartOfSpeech(pos);
				termAttr.setLength(splitterPosition);
				int startOffset = offAttr.startOffset();
				offAttr.setOffset(startOffset, startOffset + splitterPosition);
			}
			hasOriginal = false;
			return true;
		}
		if (input.incrementToken()) {
			hasOriginal = true;
			return true;
		}
		return false;
	}
	
}
