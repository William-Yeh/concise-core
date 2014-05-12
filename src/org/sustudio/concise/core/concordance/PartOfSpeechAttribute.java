package org.sustudio.concise.core.concordance;

import org.apache.lucene.util.Attribute;

public interface PartOfSpeechAttribute extends Attribute {

	/**
	 * 設定詞性
	 * @param pos 詞性
	 */
	public void setPartOfSpeech(String pos);
	
	/**
	 * 取得詞性
	 * @return
	 */
	public String getPartOfSpeech();
	
	/**
	 * 設定字詞（沒有詞性的）
	 * @param word 詞
	 */
	public void setWord(String word);
	
	/**
	 * 傳回詞（沒有詞性的）
	 * @return 詞（沒有詞性的）
	 */
	public String getWord();
}
