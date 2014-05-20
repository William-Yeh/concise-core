package org.sustudio.concise.core.corpus;

import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.util.Bits;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.corpus.importer.ConciseField;

/**
 * 顯示語料庫中的文件資訊。
 * 
 * @author Kuan-ming Su
 *
 */
public class DocumentIterator implements Iterator<ConciseDocument>, Iterable<ConciseDocument> {
	
	private final IndexReader reader;
	private final Bits liveDocs;
	private int docID = 0;
	private ConciseDocument nextDocument;
	
	public DocumentIterator(Workspace workspace) throws Exception {
		this(workspace.getIndexReader());
	}
	
	public DocumentIterator(final IndexReader reader) throws Exception {
		this.reader = reader;
		this.liveDocs = MultiFields.getLiveDocs(reader);
		nextDocument = readNextDocument();
	}

	private ConciseDocument readNextDocument() throws Exception 
	{
		ConciseDocument ccDoc = null;
		while (docID < reader.maxDoc()) 
		{				
			// check if document is deleted
			// see LUCENE-2600 at https://lucene.apache.org/core/4_0_0/MIGRATE.html
			if (liveDocs != null && !liveDocs.get(docID)) {
				docID++;
				continue;
			}
			
			Document doc = reader.document(docID);
			ccDoc = new ConciseDocument();
			ccDoc.docID = docID;
			ccDoc.title = doc.get(ConciseField.TITLE.field());
			ccDoc.filepath = doc.get(ConciseField.FILEPATH.field());
			//ccDoc.isTokenized = doc.get(ConciseField.IS_TOKENIZED.field());
			
			/*
			StringReader content = new StringReader(doc.get(ConciseField.CONTENT.field()));
			int wordsCount = 0;
			TokenStream tokenStream = analyzer.tokenStream(null, content);
			CharTermAttribute termAttr = tokenStream.addAttribute(CharTermAttribute.class);
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				String word = termAttr.toString();
				if (wordFilter.accept(word)) {
					if (CCPrefs.STOP_WORDS_ENABLED && ArrayUtils.contains(CCPrefs.stopWords, word)) {
						continue;
					}
					wordsCount++;
				}
			}
			tokenStream.end();
			tokenStream.close();
			content.close();
			*/
			String content = doc.get(ConciseField.CONTENT.field());
			StringTokenizer st = new StringTokenizer(content, " \n");
			ccDoc.numWords = st.countTokens();
			st = null;
			
			IndexableField paraField = doc.getField(ConciseField.NUM_PARAGRAPHS.field());
			if (paraField != null) {
				ccDoc.numParagraphs = paraField.numericValue().longValue();
			}
			docID++;
			
			return ccDoc;
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
