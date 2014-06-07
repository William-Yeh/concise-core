package org.sustudio.concise.core.corpus;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.sustudio.concise.core.ConciseFile;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.Workspace.INDEX;
import org.sustudio.concise.core.corpus.importer.ConciseField;
import org.sustudio.concise.core.corpus.importer.ConciseFileUtils;
import org.sustudio.concise.core.corpus.importer.ImportPOSAnalyzer;

/**
 * 寫入、刪除文件用
 * 
 * @author Kuan-ming Su
 */
public class DocumentWriter extends IndexWriter {
	
	protected final Workspace workspace;
	protected final INDEX indexType;
	protected final ConciseFile indexDir;
	protected final ConciseFile originalFolder;
	
	public DocumentWriter(Workspace workspace) throws IOException {
		this(workspace, INDEX.DOCUMENT);
	}
	
	public DocumentWriter(Workspace workspace, INDEX indexType) throws IOException {
		super(FSDirectory.open(workspace.getIndexDir(indexType)), 
			  new IndexWriterConfig(Config.LUCENE_VERSION,
					  				new ImportPOSAnalyzer(Config.LUCENE_VERSION)));
		this.workspace = workspace;
		this.indexType = indexType;
		this.indexDir = workspace.getIndexDir(indexType);
		this.originalFolder = workspace.getOriginalDocFolder(indexType);
		workspace.closeIndexReader(indexType);		
	}
	
	public void close() throws IOException {
		super.close();
		workspace.reopenIndexReader(indexType);
	}
	
	
	public void deleteAll() throws IOException {
		super.deleteAll();
		forceMerge(1);
		// 刪掉原始檔案的資料夾再重建
		FileUtils.deleteDirectory(originalFolder);
		originalFolder.mkdir();
	}
	
	/**
	 * 依據 {@link ConciseDocument} 來刪除語料庫中的文件。
	 * @param docs
	 * @throws IOException
	 */
	public void deleteDocuments(ConciseDocument...docs) throws IOException {
		Term[] terms = new Term[docs.length];
		for (int i = 0; i < docs.length; i++) {
			terms[i] = new Term(ConciseField.FILENAME.field(), docs[i].filename);
			docs[i].documentFile.delete();
		}
		deleteDocuments(terms);
		forceMerge(1);
	}
	
	
	/**
	 * 自別的 {@link ConciseDocument} 複製到目標的 Index 中。
	 * 應該是自其他 workspace 匯入時使用。
	 * @param docs
	 * @throws IOException 
	 */
	public void addConciseDocuments(Iterable<ConciseDocument> docs) throws IOException {
		for (ConciseDocument cd : docs) {
			// copy file to original documents dir
			File targetFile = ConciseFileUtils.getUniqueFile(
								new File(originalFolder, cd.filename));
			FileUtils.copyFile(cd.documentFile, targetFile);
			
			// update Document FILEPATH
			Document d = cd.document();
			d.removeField(ConciseField.FILENAME.field());
			d.add(new StringField(ConciseField.FILENAME.field(), targetFile.getName(), Store.YES));
			addDocument(d);
		}
		forceMerge(1);
	}
}
