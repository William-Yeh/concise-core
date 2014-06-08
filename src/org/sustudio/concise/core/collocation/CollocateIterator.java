package org.sustudio.concise.core.collocation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.sustudio.concise.core.concordance.Conc;

public abstract class CollocateIterator implements Iterator<Collocate>, Iterable<Collocate> {

	/** node separator (for phrase search) */
	protected static final String _NODE_SEPARATOR = "_NODE_";
	
	/** 暫存的工作目錄，CollocateIterator 結束後應該刪除 */
	protected Directory temporaryDirectory;
	protected final Map<CollocateMeasurement, Double> filters;
	protected Collocate nextCollocate;
		
	public CollocateIterator(Conc conc, Map<CollocateMeasurement, Double> filters) {
		if (filters == null) {
			filters = new HashMap<CollocateMeasurement, Double>();
		}
		this.filters = filters;
		this.temporaryDirectory = conc.workspace.getTempDirectory();
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
	 * 關閉並刪除暫存目錄
	 * @throws IOException
	 */
	protected void closeTemporaryDirectory() throws IOException {
		if (temporaryDirectory instanceof FSDirectory) {
			File tmpdir = ((FSDirectory) temporaryDirectory).getDirectory();
			FileUtils.deleteDirectory(tmpdir);
		}
		temporaryDirectory.close();
	}

	
	/**
	 * DO NOT USE.
	 */
	public void remove() {
		// Do not use
		throw new UnsupportedOperationException("remove() is not supported.");
	}

}
