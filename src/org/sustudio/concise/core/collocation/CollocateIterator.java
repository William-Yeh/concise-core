package org.sustudio.concise.core.collocation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public abstract class CollocateIterator implements Iterator<Collocate>, Iterable<Collocate> {

	/** node separator (for phrase search) */
	protected static final String _NODE_SEPARATOR = "_NODE_";
	
	public Directory indexDirectory = new RAMDirectory();
	protected final Map<CollocateMeasurement, Double> filters;
	protected Collocate nextCollocate;
		
	public CollocateIterator(Map<CollocateMeasurement, Double> filters) {
		if (filters == null) {
			filters = new HashMap<CollocateMeasurement, Double>();
		}
		this.filters = filters;
	}
	
	public void setTemporaryDirectory(Directory temporaryDirectory) {
		this.indexDirectory = temporaryDirectory;
	}
			
	protected abstract Collocate readNextCollocate() throws Exception;
	
	
	@Override
	public Iterator<Collocate> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return nextCollocate != null;
	}

	@Override
	public Collocate next() {
		Collocate collocate = nextCollocate;
		try {
			nextCollocate = readNextCollocate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return collocate;
	}

	
	/**
	 * DO NOT USE.
	 */
	public void remove() {
		// Do not use
		throw new UnsupportedOperationException("remove() is not supported.");
	}

}
