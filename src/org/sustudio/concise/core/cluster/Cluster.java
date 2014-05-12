package org.sustudio.concise.core.cluster;

import java.io.Serializable;

public class Cluster implements Serializable {
	
	private static final long serialVersionUID = -8377773690203209945L;
	public String word;
	public long freq = 0;
	
	public Cluster(String word, long freq) {
		this.word = word;
		this.freq = freq;
	}
	
	public String getWord() {
		return word;
	}
	
	public long getFreq() {
		return freq;
	}

	public String toString() {
		return word + "\t" + freq; 
	}
}
