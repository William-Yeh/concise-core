package org.sustudio.concise.core;

import java.io.File;
import java.io.Serializable;

import org.sustudio.concise.core.corpus.importer.AnalyzerEnum;
import org.sustudio.concise.core.wordlister.LemmaList;

public class CCPrefs implements Serializable {

	private static final long serialVersionUID = 6050653409688390871L;
	
	// Default Span size
	public static int SPAN_SIZE_LEFT = 4;
	public static int SPAN_SIZE_RIGHT = 4;
	
	public static File[] userDictionaries = null;
	
	public static AnalyzerEnum rawDocAnalyzer = AnalyzerEnum.MMSegComplex;
	
	/** 預設的 pos tagger 模型路徑 */
	public static final String DEFAULT_POS_TAGGER_MODEL = "models/chinese-distsim.tagger";
	
	/** pos tagger 的模型路徑 */
	public static String POS_TAGGER_MODEL = DEFAULT_POS_TAGGER_MODEL;
	
	/** 預設的 pos tagger 用的 separator */
	public static final String DEFAULT_POS_TAGGER_SEPARATOR = "#";
	
	/** pos tagger 用的 separator */
	public static String POS_TAGGER_SEPARATOR = DEFAULT_POS_TAGGER_SEPARATOR;
	
	/** enable lowercase */
	public static boolean LOWERCASE_ENABLED = false;
	
	
	/** 顯示用的詞性分隔標籤 （預設是 _POS_） */
	public static String POS_SEPARATOR = Config.SYSTEM_POS_SEPERATOR;
	
	/** show letter token */
	public static boolean SHOW_TOKEN_LETTER = true;
	/** show number token */
	public static boolean SHOW_TOKEN_NUMBER = true;
	/** show symbol token */
	public static boolean SHOW_TOKEN_SYMBOL = false;
	/** show mark token */
	public static boolean SHOW_TOKEN_MARK = false;
	/** show punctuation token */
	public static boolean SHOW_TOKEN_PUNCTUATION = false;

	
	
	/** enable stop words */
	public static boolean STOP_WORDS_ENABLED = false;
	
	/** StopWorder's stop words */
	public static String[] stopWords;
	
	
	/** enable lemma */
	public static boolean LEMMA_ENABLED = false;
	
	public static LemmaList LEMMA_LIST = new LemmaList();
	
	
}
