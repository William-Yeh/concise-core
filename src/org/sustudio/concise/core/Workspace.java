package org.sustudio.concise.core;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Concise資料夾的基礎，開啓 {@link Workspace} 後會建立相關的檔案結構，並且開啓預設的 {@link IndexReader}。
 * 
 * @author Kuan-ming Su
 *
 */
public class Workspace {

	/** 定義 INDEX 的類型 */
	public enum INDEX { 
		/** 語料庫文件 */
		DOCUMENT, 
		
		/** 參照語料庫文件 */
		REFERENCE,
		;
	}
	
	private File workpath;
	private ConciseFile indexDir;
	private ConciseFile indexDirRef;
	private ConciseFile dicDir;
	private ConciseFile originalDocs;
	private ConciseFile originalRefs;
	private IndexReader indexReader;
	private IndexReader indexReaderRef;
	private Directory indexDirectory;
	private Directory indexDirectoryRef;
	
	
	public Workspace(File workpath) throws IOException {
		
		this.workpath = workpath;
		
		// check structure
		if (!workpath.exists()) {
			workpath.mkdir();
		}
		
		// create default folders
		indexDir 		= createFolderIfNotExists(Config.INDEX_FOLDER);
		indexDirRef		= createFolderIfNotExists(Config.REF_INDEX_FOLDER);
		dicDir 			= createFolderIfNotExists(Config.DIC_FOLDER);
		originalDocs	= createFolderIfNotExists(Config.ORIGINAL_DOC_FOLDER);
		originalRefs	= createFolderIfNotExists(Config.ORIGINAL_REF_FOLDER);
		
		openIndexReader(INDEX.DOCUMENT);
	}
	
	/**
	 * create default folder if not exists
	 * @param folderName folder name (relative to workpath) 
	 */
	private ConciseFile createFolderIfNotExists(String folderName) {
		ConciseFile folder = new ConciseFile(workpath, folderName, this);
		if (!folder.exists()) {
			folder.mkdir();
		}
		return folder;
	}
	
	/**
	 * 傳回Workspace的 File 物件
	 * @return Workspace的 File 物件
	 */
	public File getFile() {
		return workpath;
	}
	
	public String toString() {
		return getClass().getSimpleName() + " {" + workpath.toString() + "}";
	}
	
	protected void openIndexReader(INDEX indexType) throws IOException {
		try {
			switch (indexType) {
			default:
			case DOCUMENT:
				indexDirectory = FSDirectory.open(indexDir);
				indexReader = DirectoryReader.open(indexDirectory);
				break;
				
			case REFERENCE:
				indexDirectoryRef = FSDirectory.open(indexDirRef);
				indexReaderRef = DirectoryReader.open(indexDirectoryRef);
				break;
			}
			
		} catch (IndexNotFoundException e) {
			// eat...	
		}
	}
	
	public IndexReader getIndexReader(INDEX indexType) throws IOException {
		switch (indexType) {
		default:
		case DOCUMENT:
			if (indexReader == null) {
				openIndexReader(indexType);
			}
			return indexReader;
			
		case REFERENCE:
			if (indexReaderRef == null) {
				openIndexReader(indexType);
			}
			return indexReaderRef;
		}
	}
	
	public IndexReader reopenIndexReader(INDEX indexType) throws IOException {
		closeIndexReader(indexType);
		openIndexReader(indexType);
		switch (indexType) {
		default:
		case DOCUMENT:		return indexReader;
		case REFERENCE:		return indexReaderRef;
		}
		
	}
	
	public void closeIndexReader(INDEX indexType) throws IOException {
		switch (indexType) {
		default:
		case DOCUMENT:
			if (indexReader != null) { 
				indexReader.close();
				indexReader = null;
			}
			if (indexDirectory != null) {
				indexDirectory.close();
				indexDirectory = null;
			}
			break;
			
		case REFERENCE:
			if (indexReaderRef != null) { 
				indexReaderRef.close();
				indexReaderRef = null;
			}
			if (indexDirectoryRef != null) {
				indexDirectoryRef.close();
				indexDirectoryRef = null;
			}
			break;
		}
	}
	
	public void close() throws IOException {
		closeIndexReader(INDEX.DOCUMENT);
		closeIndexReader(INDEX.REFERENCE);
	}
	

	/**
	 * 傳回儲存 index 的 {@link File} 物件
	 * @param indexType DOCUMENT or REFERENCE
	 * @return
	 */
	public ConciseFile getIndexDir(INDEX indexType) {
		switch (indexType) {
		default:
		case DOCUMENT:	return indexDir;
		case REFERENCE:	return indexDirRef;
		}
	}
	
	/**
	 * 傳回儲存字典（分詞用）的 {@link File} 物件
	 * @return 儲存字典（分詞用）的 {@link File} 物件
	 */
	public ConciseFile getDictionaryDir() {
		return dicDir;
	}
	
	/**
	 * 傳回自訂的詞典檔案目錄
	 * @return 自訂的詞典檔案目錄
	 */
	public File[] getDictionaryFiles() {
		return dicDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && !pathname.isHidden();
			}
			
		});
	}
	
	/**
	 * 傳回原始文件檔案目錄
	 * @return 原始文件檔案目錄
	 */
	public ConciseFile getOriginalDocFolder(INDEX indexType) {
		switch (indexType) {
		default:
		case DOCUMENT:	return originalDocs;
		case REFERENCE:	return originalRefs;
		}
	}
	
}
