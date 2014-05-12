package org.sustudio.concise.core.concordance;

import org.apache.lucene.util.AttributeImpl;

public class PartOfSpeechAttributeImpl extends AttributeImpl implements PartOfSpeechAttribute {

	private String pos = null;
	private String word = null;
	
	public void setPartOfSpeech(String pos) {
		this.pos = pos;
	}
	
	public String getPartOfSpeech() {
		return pos;
	}
	
	public void setWord(String word) {
		this.word = word;
	}
	
	public String getWord() {
		return word;
	}
	
	public void clear() {
		pos = null;
		word = null;
	}
	
	public void copyTo(AttributeImpl target) {
		((PartOfSpeechAttribute) target).setPartOfSpeech(pos);
		((PartOfSpeechAttribute) target).setWord(word);
	}
	
}
