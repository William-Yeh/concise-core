package org.sustudio.concise.core.corpus.importer;

/**
 * Predefined Field for Lucene
 * 
 * @author Kuan-ming Su
 *
 */
public enum ConciseField {

	/** 標題欄位（檔案名稱） */
	TITLE("title"),
	
	/** 內容欄位，文件的實際文本內容 */
	CONTENT("content"),
	
	/** 檔案名稱，從0.3.7之後只存檔案名稱，然後從 Workspace 去完成相對路徑的轉換 */
	FILENAME("filename"),
	
	/** 詞彙數，含標點和其他符號 */
	NUM_WORDS("num_words"),
	
	/** 段落數，在輸入文件的時候便計算出來，用換行符號<tt>\n</tt>作為段落 */
	NUM_PARAGRAPHS("num_paras"),
	
	/** 原本的文件是否已經分詞處理過 */
	IS_TOKENIZED("is_tokenized"),
	;
	
	private String field;
	
	ConciseField(String defaultField) {
		this.field = defaultField;
	}
	
	public void setField(String field) {
		this.field = field;
	}
	
	/** 傳回欄位名稱 */
	public String field() {
		return field;
	}
	
}
