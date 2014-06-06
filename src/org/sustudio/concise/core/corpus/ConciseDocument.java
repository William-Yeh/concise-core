package org.sustudio.concise.core.corpus;

import java.io.File;

import org.apache.lucene.document.Document;
import org.sustudio.concise.core.corpus.importer.ConciseField;

public class ConciseDocument {

	/** 在 Lucene IndexReader 中的 docID ，每一次開啓不見得會相同 */
	public int docID;
	
	/** 文件標題 （應該就是檔案名稱） */
	public String title;
	
	/** 詞彙數 （沒有調整過的話，包括了標點和其他符號） */
	public int numWords = -1;
	
	/** 段落數，用 <tt>\n</tt> 作為段落的符號 */
	public int numParagraphs = -1;
	
	/** 儲存在 Original Documents 中的檔案名稱 */
	public String filename;
	
	/** 匯入時是否經過分詞處理 */
	public boolean isTokenized = false;
	
	/** 檔案的位置 */
	public File documentFile;
	
	/** Lucene 的 Document，可能是 null */
	private Document document;
	
	
	/** 預設的 constructor */
	public ConciseDocument() {};
	
	/** 運用 Lucene 的 Document 物件來建構 */
	public ConciseDocument(Document document) {
		this.document = document;
		title = 
			document.get(ConciseField.TITLE.field());
		filename = 
			document.get(ConciseField.FILENAME.field());
		numWords = 
			document.getField(ConciseField.NUM_WORDS.field())
						.numericValue().intValue();
		numParagraphs = 
			document.getField(ConciseField.NUM_PARAGRAPHS.field())
						.numericValue().intValue();
		isTokenized = 
			document.getField(ConciseField.IS_TOKENIZED.field())
						.numericValue().intValue() == 1;
	}
	
	/**
	 * 傳回 Lucene 的 Document 物件，可能是 null
	 * @return Lucene 的 Document 物件，可能是 null
	 */
	public Document document() {
		return document;
	}
	
	public String toString() {
		return getClass().getSimpleName() + " {" + documentFile.toString() + "}";
	}
}
