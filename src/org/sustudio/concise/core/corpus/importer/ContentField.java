package org.sustudio.concise.core.corpus.importer;

import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;

/**
 * This class is made to access TermVector in old Lucene (see 
 * <a href="http://stackoverflow.com/questions/11945728/how-to-use-termvector-lucene-4-0">http://stackoverflow.com/questions/11945728/how-to-use-termvector-lucene-4-0</a> for detailed discussion)
 * 
 * @author kuanming
 *
 */
public class ContentField extends Field {

	/** Indexed, tokenized, not stored. */
	public static final FieldType TYPE_NOT_STORED = new FieldType();
	
	/** Indexed, tokenized, stored. */
	public static final FieldType TYPE_STORED = new FieldType();
	
	static {
		TYPE_NOT_STORED.setIndexed(true);
	    TYPE_NOT_STORED.setTokenized(true);
	    TYPE_NOT_STORED.setStoreTermVectors(true);
	    TYPE_NOT_STORED.setStoreTermVectorOffsets(true);
	    TYPE_NOT_STORED.setStoreTermVectorPositions(true);
	    TYPE_NOT_STORED.freeze();

	    TYPE_STORED.setIndexed(true);
	    TYPE_STORED.setTokenized(true);
	    TYPE_STORED.setStored(true);
	    TYPE_STORED.setStoreTermVectors(true);
	    TYPE_STORED.setStoreTermVectorOffsets(true);
	    TYPE_STORED.setStoreTermVectorPositions(true);
	    TYPE_STORED.freeze();
	}
	
	public ContentField(String name, Reader reader, Field.Store store) {
		super(name, reader, store == Field.Store.YES ? TYPE_STORED : TYPE_NOT_STORED);
	}
	
	public ContentField(String name, String value, Field.Store store) {
		super(name, value, store == Field.Store.YES ? TYPE_STORED : TYPE_NOT_STORED);
	}
	
	public ContentField(String name, TokenStream stream) {
		super(name, stream, TYPE_NOT_STORED);
	}
	
	/*
	static FieldType type = new FieldType();
	static {
		type.setIndexed(true);
		type.setStored(true); // it needs to be stored to be properly highlighted
		type.setTokenized(true);
		type.setIndexOptions(FieldInfo.IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS); // necessary for PostingsHighlighter
	}
	
	public ContentField(String name, Reader reader) {
		super(name, reader, type);
	}
	
	public ContentField(String name, String value) {
		super(name, value, type);
	}
	
	public ContentField(String name, TokenStream stream) {
		super(name, stream, type);
	}
	*/ 
}
