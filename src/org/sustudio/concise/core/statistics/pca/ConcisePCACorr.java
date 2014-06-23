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
	private final List<DocumentPlotData> colProjectionData = new ArrayList<DocumentPlotData>();
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
	
	/**
	 * Check row sums and column sums. Both must be > 0.
	 * @param indat
	 * @return
	 * @throws Exception 
	 */
	private double[][] checkAvailability(double[][] indat) throws Exception {
		
		int n = indat.length;
		int m = indat[0].length;
		
		double[] rowsums = new double[n];
        double[] colsums = new double[m];
        
        // Row sums and overall total
        for (int i = 0; i < n; i++) {
            rowsums[i] = 0.0;
            for (int j = 0; j < m; j++) {
                rowsums[i] += indat[i][j];
            }
        }

        // Col sums
        for (int j = 0; j < m; j++) {
            colsums[j] = 0.0;
            for (int i = 0; i <n; i++) colsums[j] += indat[i][j];
        }
        
        
        // row sums check
        for (int i = n-1; i >= 0; i--) {
        	if (rowsums[i] == 0.0) {
        		wordList.remove(i);
        	}
        }
        
        // column sums check
        for (int j = m-1; j >= 0; j--) {
        	if (colsums[j] == 0.0) {
        		docs.remove(j);
        	}
        }
        
        if (wordList.size() != n || docs.size() != m) {
        	// recreate matrix
        	n = wordList.size();
        	m = docs.size();
        	
        	// TODO remove after debug
        	System.err.println("Recreate matrix with " + n + " x " + m + " .");
        	indat = new double[n][m];
        	for (int i=0; i<wordList.size(); i++) {
    			Word word = wordList.get(i);
    			Map<ConciseDocument, Integer> countMap = WordUtils.wordFreqByDocs(workspace, word.getWord(), docs);
    			for (int j=0; j<docs.size(); j++) {
    				Integer f = countMap.get(docs.get(j));
    				double freq = f == null ? 0.0 : Double.valueOf(f);
    				indat[i][j] = freq;
    			}
    			countMap.clear();
    		}
        }
        
        return indat;
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
			countMap.clear();
		}
		
		observations = checkAvailability(observations);
		
		// start Principal Components Analysis
		PCACorr pca = new PCACorr();
		pca.setObservations(observations);
		pca.transform();
		eigenValues = pca.getEigenValues();
		double[][] rowproj = pca.getRowProjections();
		for (int i=0; i<wordList.size(); i++) {
			Word word = wordList.get(i);
			double[] pc = rowproj[i];
			rowProjectionData.add(new WordPlotData(word, pc[0], pc[1]));
		}
		double[][] colproj = pca.getColProjections();
		for (int j=0; j<docs.size(); j++) {
			ConciseDocument doc = docs.get(j);
			double[] pc = colproj[j];
			colProjectionData.add(new DocumentPlotData(doc, pc[0], pc[1]));
		}
		pca.clear();
		docs.clear();
	}
	
	public List<WordPlotData> getRowProjectionData() {
		return rowProjectionData;
	}
	
	public List<DocumentPlotData> getColProjectionData() {
		return colProjectionData;
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
