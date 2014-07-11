package org.sustudio.concise.core.statistics.pca;

import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.wordlister.Word;

public class ResultPCA {

	private final Word[] words;
	private final ConciseDocument[] docs;
	private final double[] eigenvalues;
	private final double[] singularvalues;
	private final double[][] rowPC;
	private final double[][] colPC;
	
	public ResultPCA(PCACorr pca, Word[] words, ConciseDocument[] docs) {
		this.words = words;
		this.docs = docs;
		rowPC = pca.getRowPrincipalComponents();
		colPC = pca.getColumnPrincipalComponents();
		eigenvalues = pca.getEigenvalues();
		singularvalues = new double[eigenvalues.length];
		for (int i = 0; i < eigenvalues.length; i++) {
			singularvalues[i] = Math.sqrt(eigenvalues[i]);
		}
	}
	
	public int getRowDimension() {
		return words.length;
	}
	
	public int getColumnDimension() {
		return docs.length;
	}
	
	public double[] getSingularvalues() {
		return singularvalues;
	}
	
	public double[] getEigenvalues() {
		return eigenvalues;
	}
	
	public double[][] getRowPrincipalComponents() {
		return rowPC;
	}
	
	public double[][] getColumnPrincipalComponents() {
		return colPC;
	}
	
	public Word[] getWords() {
		return words;
	}
	
	public ConciseDocument[] getDocs() {
		return docs;
	}
	
}
