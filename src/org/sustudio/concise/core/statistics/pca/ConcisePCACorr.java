package org.sustudio.concise.core.statistics.pca;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.corpus.DocumentIterator;
import org.sustudio.concise.core.statistics.ConciseMultivariate;
import org.sustudio.concise.core.statistics.DocumentPlotData;
import org.sustudio.concise.core.statistics.WordPlotData;
import org.sustudio.concise.core.wordlister.Word;
import org.sustudio.concise.core.wordlister.WordUtils;

public class ConcisePCACorr extends ConciseMultivariate {

	private final ArrayList<ConciseDocument> docs = new ArrayList<ConciseDocument>();
	private final ArrayList<Word> wordList = new ArrayList<Word>();
	private final List<WordPlotData> rowProjectionData = new ArrayList<WordPlotData>();
	private double[] eigenValues = null;
	
	public ConcisePCACorr(Workspace workspace, boolean showPartOfSpeech) {
		super(workspace, showPartOfSpeech);
	}
	
	public void setWords(List<String> words) throws Exception {
		// gathering info of documents (x-Axis)
		for (ConciseDocument cd : new DocumentIterator(workspace)) {
			docs.add(cd);
		}
		
		// gather info of words (y-Axis)
		for (String strWord : words) {
			Word w = WordUtils.getWordInCorpus(workspace, strWord);
			if (w.totalTermFreq > 0) {
				wordList.add(w);
			}
		}
		
		transform();
	}
	
	protected void transform() throws Exception {
		// build observations array
		double[][] observations = new double[wordList.size()][docs.size()];
		for (int i=0; i<wordList.size(); i++) {
			Word word = wordList.get(i);
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
		for (int i=0; i<wordList.size(); i++) {
			Word word = wordList.get(i);
			double[] pc = principalComponents[i];
			rowProjectionData.add(new WordPlotData(word, pc[0], pc[1]));
		}
		pca.clear();
		docs.clear();
	}
	
	public List<WordPlotData> getRowProjectionData() {
		return rowProjectionData;
	}
	
	public List<DocumentPlotData> getColProjectionData() {
		return null;
	}

	/**
	 * returns the EigenValues of the principal components analysis
	 * @return
	 */
	public double[] getEigenValues() {
		return eigenValues;
	}
	
	/**
	 * returns the percentage explained by each dimension
	 * @return
	 */
	public double[] getRatesOfInertia() {
		double total = 0.0;
		for (double eig : eigenValues) {
			total += eig;
		}
		double[] rates = new double[eigenValues.length];
		for (int i = 0; i < rates.length; i++) {
			rates[i] = eigenValues[i] / total;
		}
		return rates;
	}
	
}
