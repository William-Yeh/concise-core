package org.sustudio.concise.core.corpus;

public class ConciseDocument {

	public int docID;
	
	public String title;
	
	public long numWords = -1;
	
	public long numParagraphs = -1;
	
	public String filepath;
	
	public boolean isTokenized = false;
	
	
	public String toString() {
		return filepath;
	}
}
