package org.sustudio.concise.core.corpus;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.corpus.importer.ConciseField;
import org.sustudio.concise.core.corpus.importer.ImportPOSAnalyzer;

/**
 * 寫入、刪除文件用
 * 
 * @author Kuan-ming Su
 */
public class DocumentWriter extends IndexWriter {
	
	protected final Workspace workspace;
	protected final File indexDir;
	
	public DocumentWriter(Workspace workspace) throws IOException {
		this(workspace, workspace.getIndexDir());
	}
	
	public DocumentWriter(Workspace workspace, File indexDir) throws IOException {
		super(FSDirectory.open(indexDir), 
			  new IndexWriterConfig(Config.LUCENE_VERSION,
					  				new ImportPOSAnalyzer(Config.LUCENE_VERSION)));
		this.workspace = workspace;
		this.indexDir = indexDir;
		if (indexDir.equals(workspace.getIndexDir()))
			workspace.closeIndexReader();
		else
			workspace.closeIndexReaderRef();
	}
	
	public void close() throws IOException {
		super.close();
		if (workspace.getIndexDir().equals(indexDir))
			workspace.reopenIndexReader();
		else
			workspace.reopenIndexReaderRef();
	}
	
	/**
	 * 依據檔案路徑刪除語料庫中的文件。
	 * @param path 檔案路徑
	 * @throws IOException
	 */
	public void deleteDocument(String...path) throws IOException {
		Term[] terms = new Term[path.length];
		for (int i = 0; i < path.length; i++) {
			terms[i] = new Term(ConciseField.FILEPATH.field(), path[i]);
		}
		deleteDocuments(terms);
		forceMerge(1);
	}
	
	/**
	 * 依據 {@link ConciseDocument} 來語料庫中的刪除文件。
	 * @param doc
	 * @throws IOException
	 */
	public void deleteDocument(ConciseDocument...doc) throws IOException {
		String[] path = new String[doc.length];
		for (int i = 0; i < doc.length; i++) {
			path[i] = doc[i].filepath;
		}
		deleteDocument(path);
	}
}
