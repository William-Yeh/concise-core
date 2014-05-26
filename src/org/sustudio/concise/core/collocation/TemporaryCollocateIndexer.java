package org.sustudio.concise.core.collocation;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.concordance.ConcLine;

/**
 * Temporary Lucene Document formats for Collocation Analysis.
 * 
 * @author Kuan-ming Su
 *
 */
public class TemporaryCollocateIndexer {
	
	public enum CIField {
		LEFT, NODE, RIGHT, TEXT;
	}
	
	private final int left_span_size;
	private final int right_span_size;
	private final IndexWriter writer;
	
	public TemporaryCollocateIndexer(Directory indexDirectory, int leftSpanSize, int rightSpanSize) throws Exception {
		this.left_span_size = leftSpanSize;
		this.right_span_size = rightSpanSize;
		
		Analyzer analyzer = new WhitespaceAnalyzer(Config.LUCENE_VERSION);
		IndexWriterConfig config = new IndexWriterConfig(Config.LUCENE_VERSION, analyzer);
		writer = new IndexWriter(indexDirectory, config);
	}
	
	public void commit() throws Exception {
		writer.commit();
	}
	
	public void closeWriter() throws Exception {
		writer.close();
	}
		
	public void addDocumentWithConcLine(ConcLine concLine) throws Exception {
		
		Document doc = new Document();
		doc.add(new TextField(CIField.LEFT.name(), concLine.getLeft(), Store.NO));
		//doc.add(new TextField(CIField.NODE.name(), concLine.getNode(), Store.NO));
		// replace with Specific Field
		FieldType type = new FieldType();
				  type.setIndexed(true);
				  type.setTokenized(false);	// node 不做 tokenize
				  type.freeze();
		doc.add(new Field(CIField.NODE.name(), concLine.getNode(), type));
		doc.add(new TextField(CIField.RIGHT.name(), concLine.getRight(), Store.NO));
		doc.add(new TextField(CIField.TEXT.name(), concLine.toString(), Store.NO));
		
		// add collocation specific field
		// position field
		String[] leftWords = leftWords(concLine);
		ArrayUtils.reverse(leftWords);
		doc = addTextFields(doc, "L", leftWords);
		doc = addTextFields(doc, "R", rightWords(concLine));
		
		writer.addDocument(doc);
	}
	
	
	/**
	 * 用在 textual collocation ，輸入句子
	 * @param sentence	沒有經過處理的句子，可能包含標籤
	 * @param preTag	前輟標籤
	 * @param postTag	後輟標籤
	 */
	public void addDocumentWithTextualSentence(String sentence, String preTag, String postTag) throws Exception {
		Document doc = new Document();
		if (sentence.contains(preTag) && sentence.contains(postTag)) {
			doc.add(new TextField(CIField.NODE.name(), sentence, Store.NO));
		}
		doc.add(new TextField(CIField.TEXT.name(), sentence, Store.NO));
		writer.addDocument(doc);
	}
	
	private Document addTextFields(Document doc, String prefix, String[] words) {
		for (int i=0; i<words.length; i++) {
			final String field = prefix + String.valueOf(i+1);
			doc.add(new TextField(field, words[i], Store.NO));
		}
		return doc;
	}
	
	private String[] rightWords(ConcLine concLine) {
		String[] words = Arrays.copyOf(
			concLine.getRight().split("\\s+", right_span_size), 
			right_span_size);
		fillNull(words);
		return words;
	}
	
	private String[] leftWords(ConcLine concLine) {
		String[] words = concLine.getLeft().split("\\s+");
		if (words.length < left_span_size) {
			String[] tmp = new String[left_span_size];
			System.arraycopy(words, 0, tmp, left_span_size-words.length, words.length);
			words = tmp;
		}
		fillNull(words);
		return words;
	}

	private void fillNull(String[] words) {
		for (int i=0; i<words.length; i++) {
			words[i] = words[i] == null ? "" : words[i];
		}
	}	
	
}
