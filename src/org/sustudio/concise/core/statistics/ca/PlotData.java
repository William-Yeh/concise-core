package org.sustudio.concise.core.statistics.ca;

public class PlotData {
	
	private final String word;
	private final double xCoordinate;
	private final double yCoordinate;

	public PlotData(String word, double xCoordinate, double yCoordinate) {
		this.word = word;
		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
	}
	
	public String getWord() {
		return word;
	}
	
	public double getX() {
		return xCoordinate;
	}
	
	public double getY() {
		return yCoordinate;
	}
	
	public String toString() {
		return word + " (" + xCoordinate + "," + yCoordinate + ")";
	}

}
