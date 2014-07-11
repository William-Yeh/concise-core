package org.sustudio.concise.core.corpus;

import java.io.File;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.util.Bits;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.Workspace.INDEX;

/**
 * 顯示語料庫中的文件資訊。
 * 
 * @author Kuan-ming Su
 *
 */
public class DocumentIterator implements Iterator<ConciseDocument>, Iterable<ConciseDocument> {
	
	private final IndexReader reader;
	private final Bits liveDocs;
	private final File originalFolder;
	private int docID = 0;
	private ConciseDocument nextDocument;
	
	public DocumentIterator(Workspace workspace) throws Exception {
		this(workspace, INDEX.DOCUMENT);
	}
	
	public DocumentIterator(Workspace workspace, INDEX indexType) throws Exception {
		this.reader = workspace.getIndexReader(indexType);
		this.originalFolder = workspace.getOriginalDocFolder(indexType);
		this.liveDocs = reader == null ? null : MultiFields.getLiveDocs(reader);
		nextDocument = readNextDocument();
	}

	private ConciseDocument readNextDocument() throws Exception 
	{
		if (reader != null) {
			ConciseDocument cd = null;
			while (docID < reader.maxDoc()) 
			{				
				// check if document is deleted
				// see LUCENE-2600 at https://lucene.apache.org/core/4_0_0/MIGRATE.html
				if (liveDocs != null && !liveDocs.get(docID)) {
					docID++;
					continue;
				}
				
				Document doc = reader.document(docID);
				cd = new ConciseDocument(doc);
				cd.docID = docID;
				cd.documentFile = new File(originalFolder, cd.filename);
				docID++;
				return cd;
			}
		}
		return null;
	}
	
	@Override
	public Iterator<ConciseDocument> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return nextDocument != null;
	}

	@Override
	public ConciseDocument next() {
		ConciseDocument doc = nextDocument;
		try {
			nextDocument = readNextDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}

	// DO NOT USE.
	public void remove() {
		throw new UnsupportedOperationException("remove() is not supported.");
	}
	
}
