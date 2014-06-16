package org.sustudio.concise.core.statistics.ca;

import org.sustudio.concise.core.wordlister.Word;

public class WordPlotData {
	
	private final Word word;
	private final double xCoordinate;
	private final double yCoordinate;

	public WordPlotData(Word word, double xCoordinate, double yCoordinate) {
		this.word = word;
		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
	}
	
	public Word getWord() {
		return word;
	}
	
	public double getX() {
		return xCoordinate;
	}
	
	public double getY() {
		return yCoordinate;
	}
	
	public String toString() {
		return word.getWord() + " (" + xCoordinate + "," + yCoordinate + ")";
	}

}
