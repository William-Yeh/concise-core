package org.sustudio.concise.core.statistics.ca;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.corpus.DocumentIterator;
import org.sustudio.concise.core.wordlister.WordUtils;

public class ConciseCA {

	private final Workspace workspace;
	private final ArrayList<ConciseDocument> docs = new ArrayList<ConciseDocument>();
	
	private int nclusters = 3;  // Number of clusters to be analyzed
    private int nclusattr; 		// Number of attribute/variable clusters to anal.
    
    private int n = 0;			// Number of rows (words)
    private int m = 0;			// Number of cols (docs);
    
    private String[] collabs;	// Column labels
    private String[] rowlabs;	// Row labels
    
    private Imatrix principal;
	private CorrespondenceAnalysis ca;
    
    public ConciseCA(Workspace workspace, boolean showPartOfSpeech) throws Exception {
		this.workspace = workspace;
		ArrayList<String> collabs = new ArrayList<String>();
		for (ConciseDocument cd : new DocumentIterator(workspace)) {
			docs.add(cd);
			collabs.add(cd.title);
		}
		this.collabs = collabs.toArray(new String[0]);
		collabs.clear();
		m = docs.size();
		nclusattr = m;
	}
	
	public void setWords(List<String> words) throws Exception {
		double[][] indat = new double[words.size()][docs.size()];
		ArrayList<String> rowlabs = new ArrayList<String>();
		for (int i = 0; i < words.size(); i++) {
			String word = words.get(i);
			rowlabs.add(word);
			Map<ConciseDocument, Integer> wordMap = WordUtils.wordFreqByDocs(workspace, word, docs);
			for (int j = 0; j < docs.size(); j++) {
				indat[i][j] = wordMap.get(docs.get(j));
			}
			wordMap.clear();
		}
		this.rowlabs = rowlabs.toArray(new String[0]);
		rowlabs.clear();
		n = words.size();
		
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
        // data, nrows, ncols, row-masses, col-masses, grand total,
        // row-labels, col-labels.
        
        principal = new Imatrix(indat, n, m, 
                rowsums, colsums, total, this.rowlabs, this.collabs);
	}
	
	public void analyze() {
		ca = new CorrespondenceAnalysis(principal);
	}
	
	/**
	 * 傳回文字的投影坐標（僅看 Factor1(x) 和 Factor2(y) 的投影）
	 * @return
	 */
	public List<PlotData> getRowProjectionData() {
		List<PlotData> data = new ArrayList<PlotData>();
		double[][] rowproj = ca.getRowProjections();
		int xAxisIndex = 1;
		int yAxisIndex = 2;
		for (int i = 0; i < rowlabs.length; i++) {
			PlotData d = new PlotData(rowlabs[i], rowproj[i][xAxisIndex], rowproj[i][yAxisIndex]);
			data.add(d);
		}
		return data;
	}
	
	/**
	 * 傳回文件的投影坐標（僅看 Factor1(x) 和 Factor2(y) 的投影）
	 * @return
	 */
	public List<PlotData> getColProjectionData() {
		List<PlotData> data = new ArrayList<PlotData>();
		double[][] colproj = ca.getColumnProjections();
		int xAxisIndex = 1;
		int yAxisIndex = 2;
		for (int i = 0; i < collabs.length; i++) {
			PlotData d = new PlotData(collabs[i], colproj[i][xAxisIndex], colproj[i][yAxisIndex]);
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
