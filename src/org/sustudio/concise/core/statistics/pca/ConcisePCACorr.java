package org.sustudio.concise.core.statistics.pca;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.corpus.DocumentIterator;
import org.sustudio.concise.core.wordlister.Word;
import org.sustudio.concise.core.wordlister.WordIterator;
import org.sustudio.concise.core.wordlister.WordUtils;

public class ConcisePCACorr {

	private final Workspace workspace;
	private final ArrayList<ConciseDocument> docs = new ArrayList<ConciseDocument>();
	private final ArrayList<Word> words = new ArrayList<Word>();
	private final ArrayList<PC> pcs = new ArrayList<PC>();
	private double[] eigenValues = null;
	
	public ConcisePCACorr(Workspace workspace, boolean showPartOfSpeech) throws Exception {
		this.workspace = workspace;
		
		// gathering info of documents (x-Axis)
		for (ConciseDocument cd : new DocumentIterator(workspace)) {
			docs.add(cd);
		}
		
		// gather info of words (y-Axis)
		for (Word w : new WordIterator(workspace, showPartOfSpeech)) {
			words.add(w);
		}
		
		transform();
	}
	
	protected void transform() throws Exception {
		// build observations array
		double[][] observations = new double[words.size()][docs.size()];
		for (int i=0; i<words.size(); i++) {
			Word word = words.get(i);
			Map<ConciseDocument, Integer> countMap = WordUtils.wordFreqByDocs(workspace, word.getWord(), docs);
			for (int j=0; j<docs.size(); j++) {
				Integer f = countMap.get(docs.get(j));
				double freq = f == null ? 0.0 : Double.valueOf(f);
				observations[i][j] = freq;
			}
		}
		
		// start Principal Components Analysis
		PCACorr pca = new PCACorr();
		pca.setObservations(observations);
		pca.transform();
		eigenValues = pca.getEigenValues();
		double[][] principalComponents = pca.getPrincipalComponents();
		for (int i=0; i<words.size(); i++) {
			Word word = words.get(i);
			double[] pc = principalComponents[i];
			pcs.add(new PC(word, pc));
		}
		pca.clear();
	}
	
	public List<PC> getPrincipalComponents() {
		return pcs;
	}
	
	/**
	 * returns the EigenValues of the principal components analysis
	 * @return
	 */
	public double[] getEigenValues() {
		return eigenValues;
	}
	
	/**
	 * returns the percentage explained by dimension (starts from 1)
	 * @param dimension dimension starts from 1
	 * @return
	 */
	public double getExplainedByDimension(int dimension) {
		double total = 0.0;
		for (double eig : eigenValues) {
			total += eig;
		}
		return eigenValues[dimension - 1] / total;
	}
	
	public void clear() {
		words.clear();
		docs.clear();
		pcs.clear();
	}
	
	public class PC {
		private final Word word;
		private final double[] principalComponents;
		
		PC(Word word, double[] principalComponents) {
			this.word = word;
			this.principalComponents = principalComponents;
		}
		
		public Word getWord() {
			return word;
		}
		
		/**
		 * returns the projection (principal component) of the dimension
		 * @param dimension dimension start from 1.
		 * @return
		 */
		public double getPC(int dimension) {
			return principalComponents[dimension - 1];
		}
	}
}
