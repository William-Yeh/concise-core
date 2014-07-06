package org.sustudio.concise.core.statistics;

import org.sustudio.concise.core.wordlister.Word;

public class WordPlotData extends PlotData<Word> {

	public WordPlotData(Word type, double xCoordinate, double yCoordinate) {
		super(type, xCoordinate, yCoordinate);
	}	
	
	public Word getWord() {
		return get();
	}
	
	public String toString() {
		return getWord().word + " (" + getX() + "," + getY() + ")";
	}
}
