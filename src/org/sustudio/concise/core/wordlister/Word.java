package org.sustudio.concise.core.wordlister;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.index.Term;
import org.sustudio.concise.core.corpus.importer.ConciseField;

public class Word implements Serializable, Cloneable {
	
	private static final long serialVersionUID = -8377773690203209945L;
	
	/** the word */
	public String word;
	public long docFreq = 0;
	public long totalTermFreq = 0;
	
	private List<Word> children = new ArrayList<Word>();
	
	public Word(String word, long docFreq, long totalTermFreq) {
		this.word = word;
		this.docFreq = docFreq;
		this.totalTermFreq = totalTermFreq;
	}
	
	public void setWord(final String word) {
		this.word = word;
	}
	
	public String getWord() {
		return word;
	}
	
	public Term getTerm() {
		return new Term(ConciseField.CONTENT.field(), word);
	}
	
	public void setDocFreq(long docFreq) {
		this.docFreq = docFreq;
	}
	
	public long getDocFreq() {
		return docFreq;
	}
	
	public void setTotalTermFreq(long totalTermFreq) {
		this.totalTermFreq = totalTermFreq;
	}
	
	public long getTotalTermFreq() {
		return totalTermFreq;
	}
	
	public void addChild(final Word word) {
		children.add(word);
	}
	
	public Word[] getChildren() {
		Collections.sort(children, new Comparator<Word>() {

			@Override
			public int compare(Word w1, Word w2) {
				if (w1.getTotalTermFreq() > w2.getTotalTermFreq()) return -1;
				return 0;
			}
			
		});
		return children.toArray(new Word[0]);
	}
	
	public int getChildrenCount() {
		return children.size();
	}
	
	public String getChildrenToString() {
		StringBuilder sb = new StringBuilder();
		for (Word word : getChildren()) {
			sb.append(sb.length() > 0 ? ", " : "");
			sb.append(word.getWord() + " [" + word.getTotalTermFreq() + "]");
		}
		return sb.toString();
	}
	
	public byte[] getChildrenByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(children);
		oos.close();
		bos.close();
		return bos.toByteArray();
	}
	
	@SuppressWarnings("unchecked")
	public void setChildrenByteArray(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bis);
		children = (List<Word>) ois.readObject();
		ois.close();
		bis.close();
	}

	public String toString() {
		return word + "\t docFreq:" + docFreq + "\t totalTermFreq:" + totalTermFreq + " " + getChildrenToString(); 
	}
	
	protected Word clone() throws CloneNotSupportedException {
		return (Word) super.clone();
	}
}
