package org.sustudio.concise.core.concordance;

import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

public enum ConcField {
	
	/** Node word field. */
	NODE("node", Store.YES),
	
	/** Left span field. */
	LEFT("left", Store.YES),
	
	/** Right span field. */
	RIGHT("right", Store.YES),
	
	/** Full concordance field. */
	TEXT("text", Store.NO),
	
	/** Original Document ID in the index. */
	DOC("doc", Store.YES);
	
	private final String field;
	private final Store store;
		
	ConcField(String field, Store store) {
		this.field = field;
		this.store = store;
	}
	
	public String field() {
		return field;
	}
	
	public Store store() {
		return store;
	}
	
	public TextField getTextField(String content) {
		return new TextField(field, content, store);
	}
	
	/**
	 * Returns a {@link StringField}.
	 * @param content
	 * @return
	 */
	public StringField getStringField(String content) {
		return new StringField(field, content, store);
	}
	
	public IntField getIntField(int number) {
		return new IntField(field, number, store);
	}

}
