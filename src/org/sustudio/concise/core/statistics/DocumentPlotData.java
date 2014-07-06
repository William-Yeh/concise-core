package org.sustudio.concise.core.statistics;

import org.sustudio.concise.core.corpus.ConciseDocument;

public class DocumentPlotData extends PlotData<ConciseDocument> {
	
	public DocumentPlotData(ConciseDocument doc, double xCoordinate, double yCoordinate) {
		super(doc, xCoordinate, yCoordinate);
	}
	
	public ConciseDocument getDoc() {
		return get();
	}
	
	public String toString() {
		return getDoc().title + " (" + getX() + "," + getY() + ")";
	}

}
