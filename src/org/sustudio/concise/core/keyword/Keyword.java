package org.sustudio.concise.core.keyword;

import java.io.Serializable;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

public class Keyword implements Serializable {

	private static final long serialVersionUID = -1724788465933854058L;
	public String word;
	public long f1;
	public long f2;
	public double p1;
	public double p2;
	public double ll = 0.0;
	public double YatesChiSquare = 0.0;
	
	/**
	 * Create an empty keyword instance
	 */
	public Keyword() {
		
	}
	
	/**
	 * Constructor.
	 * @param word the key word.
	 * @param f1 the frequency of the keyword in the corpus.
	 * @param f2 the frequency of the keyword in the reference corpus.
	 * @param n1 the size of the corpus.
	 * @param n2 the size of the reference corpus.
	 */
	public Keyword(String word, long f1, long f2, long n1, long n2) {
		//double c1 = 0.0;
		//double c2 = 0.0;
		double e1 = (double)n1 * (double) (f1+f2) / (double) (n1+n2);
		double e2 = (double)n2 * (double) (f1+f2) / (double) (n1+n2);
		//if (f1 > 0)
		double c1 = (double) f1 * Math.log((double) f1 / e1 + (f1 == 0 ? 1.0 : 0));
		//if (f2 > 0)
		double c2 = (double) f2 * Math.log((double) f2 / e2 + (f2 == 0 ? 1.0 : 0));
		this.ll = 2 * (c1 + c2);
				
		this.YatesChiSquare = ((double)(n1+n2) * Math.pow( (Math.abs( (f1*(n2-f2)) - (f2*(n1-f1)) ) - ((double)(n1+n2)/2.0)), 2.0) )/( (double)(f1+f2) * (double)(n1-f1+n2-f2) * (double)n1 * (double)n2);
				
		this.f1 = f1;
		this.f2 = f2;
		this.p1 = (float) f1 / (float) n1;
		this.p2 = (float) f2 / (float) n2;
		this.word = word;
	}
	
	public String getWord() {
		return this.word;
	}
	
	public long getFreq() {
		return f1;
	}
	
	public long getRefFreq() {
		return f2;
	}
	
	public double getPercent() {
		return p1;
	}
	
	public double getRefPercent() {
		return p2;
	}
	
	/**
	 * return log-likelihood
	 * @return
	 * see http://ucrel.lancs.ac.uk/llwizard.html
	 */
	public double getLL() {
		return ll;
	}
	
	public double getLLPvalue() throws Exception {
		return pValueOf(ll);
	}
	
	/**
	 * return Yates' chi-square
	 * @return chi-square value
	 * see http://en.wikipedia.org/wiki/Yates%27_correction_for_continuity
	 */
	public double getChiSquare() {
		return YatesChiSquare;
	}
	
	public double getChiSquarePvalue() throws Exception {
		return pValueOf(YatesChiSquare);
	}
	
	/**
	 * Compute p-value of x
	 * @param x
	 * @return
	 * @throws Exception
	 */
	private double pValueOf(double x) throws Exception {
		double p = 0.0;
		ChiSquaredDistribution cqd = new ChiSquaredDistribution(1);
		p = 1 - cqd.cumulativeProbability(x);
		return p;
	}

}
