package org.sustudio.concise.core.statistics.ca;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.corpus.DocumentIterator;
import org.sustudio.concise.core.statistics.ConciseMultivariate;
import org.sustudio.concise.core.statistics.DocumentPlotData;
import org.sustudio.concise.core.statistics.WordPlotData;
import org.sustudio.concise.core.wordlister.Word;
import org.sustudio.concise.core.wordlister.WordUtils;


public class ConciseCA extends ConciseMultivariate {

	private final ArrayList<ConciseDocument> docs = new ArrayList<ConciseDocument>();
	
	//private int nclusters = 3;  // Number of clusters to be analyzed
    //private int nclusattr; 		// Number of attribute/variable clusters to anal.
    
    private int n = 0;			// Number of rows (words)
    private int m = 0;			// Number of cols (docs);
    
    private ConciseDocument[] collabs;	// Column labels
    private Word[] rowlabs;				// Row labels (contain global word frequency)
    
    private Imatrix principal;
	private CorrespondenceAnalysis ca;
    
    public ConciseCA(Workspace workspace, boolean showPartOfSpeech) {
    	super(workspace, showPartOfSpeech);
	}
	
	public void setWords(List<String> words) throws Exception {
		
		ArrayList<ConciseDocument> collabs = new ArrayList<ConciseDocument>();
		for (ConciseDocument cd : new DocumentIterator(workspace)) {
			docs.add(cd);
			collabs.add(cd);
		}
		this.collabs = collabs.toArray(new ConciseDocument[0]);
		collabs.clear();
		m = docs.size();
		//nclusattr = m;
		
		// TODO remove after debug
		System.err.println(words);
		double[][] indat = new double[words.size()][docs.size()];
		ArrayList<Word> rowlabs = new ArrayList<Word>();
		for (int i = 0; i < words.size(); i++) {
			Word word = WordUtils.getWordInCorpus(workspace, words.get(i));
			rowlabs.add(word);
			Map<ConciseDocument, Integer> wordMap = WordUtils.wordFreqByDocs(workspace, word.getWord(), docs);
			for (int j = 0; j < docs.size(); j++) {
				indat[i][j] = 0.0;
				if (wordMap.get(docs.get(j)) != null)
					indat[i][j] = wordMap.get(docs.get(j));
			}
			wordMap.clear();
		}
		this.rowlabs = rowlabs.toArray(new Word[0]);
		rowlabs.clear();
		n = words.size();
		
		// 必須要先檢查 row sums 和 column sums，兩者都必須 > 0
		indat = checkAvailability(indat);
		
		/* -------------------------------------------------------------
	     * principal array.
	     * ------------------------------------------------------------- */
		
		double[] rowsums = new double[n];
        double[] colsums = new double[m];
        double total = 0.0;
        
        // Row sums and overall total
        for (int i = 0; i < n; i++) {
            rowsums[i] = 0.0;
            for (int j = 0; j < m; j++) {
                rowsums[i] += indat[i][j];
                total += indat[i][j];
            }
        }

        // Col sums
        for (int j = 0; j < m; j++) {
            colsums[j] = 0.0;
            for (int i = 0; i <n; i++) colsums[j] += indat[i][j];
        }

        // Finalize normalization to provide masses by dividing by total
        for (int i = 0; i < n; i++) rowsums[i] /= total;
        for (int j = 0; j < m; j++) colsums[j] /= total;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) indat[i][j] /= total;
        }
        
        
        /* ------------------------------------------------------------
         * Set up "principal", an object of class Imatrix.
         * ------------------------------------------------------------- */
        
        // Now set up "principal" object of class Imatrix:
        // this will be the input data for principal rows and columns.
        // Parameters for Imatrix:
        // data, nrows, ncols, row-masses, col-masses, grand total
        
        principal = new Imatrix(indat, n, m, 
                				rowsums, colsums, total);
        
        // doing analysis
        analyze();
	}
	
	/**
	 * Check row sums and column sums. Both must be > 0.
	 * @param indat
	 * @return
	 * @throws Exception 
	 */
	private double[][] checkAvailability(double[][] indat) throws Exception {
		
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
        		rowlabs = ArrayUtils.remove(rowlabs, i);
        	}
        }
        
        // column sums check
        for (int j = m-1; j >= 0; j--) {
        	if (colsums[j] == 0.0) {
        		docs.remove(j);
        	}
        }
        
        if (rowlabs.length != n || docs.size() != m) {
        	// recreate matrix
        	n = rowlabs.length;
        	m = docs.size();
        	collabs = new ConciseDocument[m];
        	for (int j = 0; j < docs.size(); j++) {
        		collabs[j] = docs.get(j);
        	}
        	// TODO remove after debug
        	System.err.println("Recreate matrix with " + n + " x " + m + " .");
        	indat = new double[n][m];
        	for (int i = 0; i < rowlabs.length; i++) {
    			Word word = rowlabs[i];
    			Map<ConciseDocument, Integer> wordMap = WordUtils.wordFreqByDocs(workspace, word.getWord(), docs);
    			for (int j = 0; j < docs.size(); j++) {
    				indat[i][j] = 0.0;
    				if (wordMap.get(docs.get(j)) != null)
    					indat[i][j] = wordMap.get(docs.get(j));
    			}
    			wordMap.clear();
    		}
        }
        
        return indat;
	}
	
	public void analyze() {
		ca = new CorrespondenceAnalysis(principal);
	}
	
	/**
	 * 傳回文字的投影坐標（僅看 Factor1(x) 和 Factor2(y) 的投影）
	 * @return
	 */
	public List<WordPlotData> getRowProjectionData() {
		List<WordPlotData> data = new ArrayList<WordPlotData>();
		double[][] rowproj = ca.getRowProjections();
		int xAxisIndex = 1;
		int yAxisIndex = 2;
		for (int i = 0; i < rowlabs.length; i++) {
			WordPlotData d = new WordPlotData(rowlabs[i], rowproj[i][xAxisIndex], rowproj[i][yAxisIndex]);
			data.add(d);
		}
		return data;
	}
	
	/**
	 * 傳回文件的投影坐標（僅看 Factor1(x) 和 Factor2(y) 的投影）
	 * @return
	 */
	public List<DocumentPlotData> getColProjectionData() {
		List<DocumentPlotData> data = new ArrayList<DocumentPlotData>();
		double[][] colproj = ca.getColumnProjections();
		int xAxisIndex = 1;
		int yAxisIndex = 2;
		for (int i = 0; i < collabs.length; i++) {
			DocumentPlotData d = new DocumentPlotData(collabs[i], colproj[i][xAxisIndex], colproj[i][yAxisIndex]);
			data.add(d);
		}
		return data;
	}
	
	public double[] getEigenValues() {
		return ca.getEigenValues();
	}
	
	public double[] getRatesOfInertia() {
		return ca.getRates();
	}
	
}
