package org.sustudio.concise.core.cluster;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.sustudio.concise.core.Config;

public class TemporaryClusterIndexer {

	public static final String FIELD = "cluster";
	
	private final IndexWriter writer;
	
	public TemporaryClusterIndexer(Directory indexDirectory) throws Exception {
		Analyzer analyzer = new KeywordAnalyzer();	// KeywordAnalyzer does nothing
		IndexWriterConfig config = new IndexWriterConfig(Config.LUCENE_VERSION, analyzer);
		writer = new IndexWriter(indexDirectory, config);
	}
	
	public void commit() throws Exception {
		writer.commit();
	}
	
	public void closeWriter() throws Exception {
		writer.close();
	}
	
	public void addDocumentWithString(String str) throws Exception {
		Document doc = new Document();
		doc.add(new StringField(FIELD, str, Store.NO));
		writer.addDocument(doc);
	}
}
