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

	private File workpath;
	private File indexDir;
	private File indexDirRef;
	private File dicDir;
	private File originalDocs;
	private File originalRefs;
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
		
		openIndexReader();
	}
	
	/**
	 * create default folder if not exists
	 * @param folderName folder name (relative to workpath) 
	 */
	private File createFolderIfNotExists(String folderName) {
		File folder = new File(workpath, folderName);
		if (!folder.exists()) {
			folder.mkdir();
		}
		return folder;
	}
	
	/**
	 * 傳回Workspace的路徑
	 * @return Workspace的路徑
	 */
	public File getFile() {
		return workpath;
	}
	
	public String toString() {
		return getClass().getSimpleName() + " {" + workpath.toString() + "}";
	}
	
	protected void openIndexReader() throws IOException {
		try {
			indexDirectory = FSDirectory.open(indexDir);
			indexReader = DirectoryReader.open(indexDirectory);
			
		} catch (IndexNotFoundException e) {
			// eat...	
		}
	}
	
	public IndexReader getIndexReader() throws IOException {
		if (indexReader == null) {
			openIndexReader();
		}
		return indexReader;
	}
	
	public IndexReader reopenIndexReader() throws IOException {
		closeIndexReader();
		openIndexReader();
		return indexReader;
	}
	
	public void closeIndexReader() throws IOException {
		if (indexReader != null) { 
			indexReader.close();
			indexReader = null;
		}
		if (indexDirectory != null) {
			indexDirectory.close();
			indexDirectory = null;
		}
	}
	
	protected void openIndexReaderRef() throws IOException {
		try {
			
			indexDirectoryRef = FSDirectory.open(indexDirRef);
			indexReaderRef = DirectoryReader.open(indexDirectoryRef);
			
		} catch (IndexNotFoundException e) {
			// eat...
		}
	}
	
	public IndexReader getIndexReaderRef() throws IOException {
		if (indexReaderRef == null) {
			openIndexReaderRef();
		}
		return indexReaderRef;
	}
	
	public IndexReader reopenIndexReaderRef() throws IOException {
		closeIndexReaderRef();
		openIndexReaderRef();
		return indexReaderRef;
	}
	
	public void closeIndexReaderRef() throws IOException {
		if (indexReaderRef != null) { 
			indexReaderRef.close();
			indexReaderRef = null;
		}
		if (indexDirectoryRef != null) {
			indexDirectoryRef.close();
			indexDirectoryRef = null;
		}
	}
	
	public void close() throws IOException {
		closeIndexReader();
		closeIndexReaderRef();
	}
	
	
	/**
	 * 傳回儲存 index 的 {@link File} 物件
	 * @return 儲存 index 的 {@link File} 物件
	 */
	public File getIndexDir() {
		return indexDir;
	}
	
	/**
	 * 傳回儲存 index (reference) 的 {@link File} 物件
	 * @return 儲存 index (reference) 的 {@link File} 物件
	 */
	public File getIndexDirRef() {
		return indexDirRef;
	}
	
	/**
	 * 傳回儲存字典（分詞用）的 {@link File} 物件
	 * @return 儲存字典（分詞用）的 {@link File} 物件
	 */
	public File getDictionaryDir() {
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
	public File getOriginalDocFolder() {
		return originalDocs;
	}
	
	/**
	 * 傳回原始參照文件檔案目錄
	 * @return 原始參照文件檔案目錄
	 */
	public File getOriginalRefFolder() {
		return originalRefs;
	}
}
