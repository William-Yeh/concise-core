package org.sustudio.concise.core.wordlister;

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.Workspace.INDEX;
import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.corpus.importer.ConciseField;

public class DocumentWordIterator implements Iterator<Word>, Iterable<Word> {

	public static long sumTotalTermFreq(Workspace workspace, ConciseDocument document, boolean showPartOfSpeech) throws Exception {
		long sum = 0;
		for (Word word : new DocumentWordIterator(workspace, document, showPartOfSpeech)) {
			sum += word.getTotalTermFreq();
		}
		return sum;
	}
	
	
	private final IndexReader reader;
	private final ConciseDocument document;
	private final WordIterator wordIterator;
	private Word nextWord = null;
	
	/**
	 * 讀取某個文件中的詞表
	 * @param workspace
	 * @param document
	 * @param showPartOfSpeech
	 * @throws Exception
	 */
	public DocumentWordIterator(Workspace workspace, ConciseDocument document, boolean showPartOfSpeech) throws Exception {
		this(workspace, INDEX.DOCUMENT, document, showPartOfSpeech);
	}
	
	/**
	 * 用來讀參照語料庫的索引，應該用不上才是
	 * @param workspace
	 * @param indexType
	 * @param document
	 * @param showPartOfSpeech
	 * @throws Exception
	 */
	public DocumentWordIterator(Workspace workspace, INDEX indexType, ConciseDocument document, boolean showPartOfSpeech) throws Exception {
		this.reader = workspace.getIndexReader(indexType);
		this.document = document;
		wordIterator = new WordIterator(workspace, indexType, showPartOfSpeech);
		readNext();
	}
	
	void readNext() throws IOException {
		nextWord = null;
		while (wordIterator.hasNext()) {
			Word lookupWord = wordIterator.next();
			DocsEnum de = MultiFields.getTermDocsEnum(
								reader, 
								MultiFields.getLiveDocs(reader), 
								ConciseField.CONTENT.field(), 
								lookupWord.getTerm().bytes());
			if (de.advance(document.docID) != DocsEnum.NO_MORE_DOCS &&
				de.docID() == document.docID)
			{
				nextWord = new Word(lookupWord.word, 1L, de.freq());
				addChildren(lookupWord, nextWord); // add lemma forms as children
				return;
			}
		}
	}
	
	/**
	 * 找看看有沒有需要新增 children （有 lemma 的狀況）
	 * @param lookupWord	原本詞單中的詞
	 * @param word			要傳回的詞
	 * @throws IOException 
	 */
	void addChildren(Word lookupWord, Word word) throws IOException {
		if (lookupWord.getChildrenCount() > 0) {
			int freq = 0;
			for (Word child : lookupWord.getChildren()) {
				DocsEnum de = MultiFields.getTermDocsEnum(
									reader, 
									MultiFields.getLiveDocs(reader), 
									ConciseField.CONTENT.field(), 
									child.getTerm().bytes());
				if (de.advance(document.docID) != DocsEnum.NO_MORE_DOCS) {
					word.addChild( new Word(child.word, 1L, de.freq()) );
					freq += de.freq();
				}
			}
			word.setTotalTermFreq(freq);	
		}
	}
	
	
	@Override
	public Iterator<Word> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return nextWord != null;
	}

	@Override
	public Word next() {
		Word word = nextWord;
		try {
			readNext();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return word;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				getClass().getSimpleName() + " cannot perform remove()");
	}

}
