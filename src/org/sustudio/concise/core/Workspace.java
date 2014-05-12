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
		
		indexDir = new File(workpath, Config.INDEX_FOLDER);
		if (!indexDir.exists()) {
			indexDir.mkdir();
		}
		indexDirRef = new File(workpath, Config.REF_INDEX_FOLDER);
		if (!indexDirRef.exists()) {
			indexDirRef.mkdir();
		}
		dicDir = new File(workpath, Config.DIC_FOLDER);
		if (!dicDir.exists()) {
			dicDir.mkdir();
		}
		
		
		openIndexReader();
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
	 * 傳回自訂的詞典檔案
	 * @return 自訂的詞典檔案
	 */
	public File[] getDictionaryFiles() {
		return dicDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && !pathname.isHidden();
			}
			
		});
	}
}
