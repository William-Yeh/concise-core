package org.sustudio.concise.core;

import org.apache.lucene.util.Version;

/**
 * Concise 的基本設定，理論上不應當變動
 * 
 * @author Kuan-ming Su
 *
 */
public class Config {

	public static final String VERSION = "0.3.7";
	
	
	/** current Lucene version */
	public static final Version LUCENE_VERSION = Version.LUCENE_48;
	
	/** index folder */
	public static final String INDEX_FOLDER = "idx.conciseindex";
	
	/** reference index folder */
	public static final String REF_INDEX_FOLDER = "ridx.conciseindex";
	
	/** Dictionary Folder */
	public static final String DIC_FOLDER = "dic.concisedic";
	
	/** default part-of-speech separator (for index) */
	public static final String SYSTEM_POS_SEPERATOR = "_POS_";
}
