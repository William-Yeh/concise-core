package org.sustudio.concise.core.statistics;

import org.sustudio.concise.core.corpus.ConciseDocument;

public class DocumentPlotData {
	
	private final ConciseDocument doc;
	private final double xCoordinate;
	private final double yCoordinate;

	public DocumentPlotData(ConciseDocument doc, double xCoordinate, double yCoordinate) {
		this.doc = doc;
		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
	}
	
	public ConciseDocument getDoc() {
		return doc;
	}
	
	public double getX() {
		return xCoordinate;
	}
	
	public double getY() {
		return yCoordinate;
	}
	
	public String toString() {
		return doc.title + " (" + xCoordinate + "," + yCoordinate + ")";
	}

}
