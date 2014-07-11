package org.sustudio.concise.core.statistics.ca;

import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.wordlister.Word;

public class ResultCA {

	private final CorrespondenceAnalysis ca;
	private final Imatrix principal;
	private final int n;		// row
	private final int m;		// column
	//private final int nfactor;	// number of factor to show
	private final Word[] rowlabs;
	private final ConciseDocument[] collabs;
	
	private transient double[] rinertia;
	private transient double[] cinertia;
	private transient double[] rquality;
	private transient double[] cquality;
	
	public ResultCA(CorrespondenceAnalysis ca, Word[] rowlabs, ConciseDocument[] collabs) {
		this.rowlabs = rowlabs;
		this.collabs = collabs;
		principal = ca.getPrincipalMatrix();
		n = principal.getRowDimension();
		m = principal.getColumnDimension();
		double[][] data = principal.getArray();
		double[] rowmass = principal.getRowMass();
		double[] colmass = principal.getColumnMass();
		
		double trace = ca.getTrace();
		
		this.ca = ca;
		int nfactor = Math.min(n, m) - 1;
		
		// First determine inertias of observations (rows) and
        // variables (columns), and qualities of representation
        // in the new factor space.
        rinertia = new double[n];       // relative inertia
        cinertia = new double[m];
        rquality = new double[n];       // quality
        cquality = new double[m];
        for (int i = 0; i < n; i++) {
            rinertia[i] = 0.0;
            rquality[i] = 0.0;
            for (int j = 0; j < m; j++) {
                rinertia[i] +=
                        Math.pow( (data[i][j]/rowmass[i]-colmass[j]),2.0 )/
                                colmass[j];
            }
            rinertia[i] = rowmass[i]*rinertia[i]/trace;
            for (int k = 1; k <= nfactor; k++) {
                rquality[i] += ca.getRowCorrelations()[i][k];
            }
        }

        for (int j = 0; j < m; j++) {
            cinertia[j] = 0.0;
            cquality[j] = 0.0;
            for (int i = 0; i < n; i++) {
                cinertia[j] +=
                        Math.pow( (data[i][j]/colmass[j]-rowmass[i]),2.0 )/
                                rowmass[i];
            }
            cinertia[j] = colmass[j]*cinertia[j]/trace;
            for (int k = 1; k <= nfactor; k++) {
                cquality[j] += ca.getColumnCorrelations()[j][k];
            }
        }

	}
	
	public double[] getRowQuality() {
		return rquality;
	}
	
	public double[] getRowMass() {
		return principal.getRowMass();
	}
	
	public double[] getRowInertia() {
		return rinertia;
	}
	
	public double[] getColumnQuality() {
		return cquality;
	}
	
	public double[] getColumnMass() {
		return principal.getColumnMass();
	}
	
	public double[] getColumnInertia() {
		return cinertia;
	}
	
	public double[][] getRowProjections() {
		return ca.getRowProjections();
	}
	
	public double[][] getRowCorrelations() {
		return ca.getRowCorrelations();
	}
	
	public double[][] getRowContributions() {
		return ca.getRowContributions();
	}
	
	public double[][] getColumnProjections() {
		return ca.getColumnProjections();
	}
	
	public double[][] getColumnCorrelations() {
		return ca.getColumnCorrelations();
	}
	
	public double[][] getColumnContributions() {
		return ca.getColumnContributions();
	}
	
	public double[] getRatesOfInertia() {
		return ca.getRates();
	}
	
	public double[] getCumulativeInertia() {
		double tot = 0.0;
		for (double val : ca.getRates()) {
			tot += val;
		}
		double[] p = new double[ca.getRates().length];
		for (int i = 0; i < p.length; i++) {
			p[i] = ca.getRates()[i] / tot;
			if (i > 0)
				p[i] += p[i-1];
		}
		return p;
	}
	
	public double[] getLambda() {
		return ca.getEigenvalues();
	}
	
	public double getTrace() {
		return ca.getTrace();
	}
	
	public int getRowDimension() {
		return n;
	}
	
	public int getColumnDimension() {
		return m;
	}
	
	public int getMaxDimension() {
		return Math.min(n, m) - 1;
	}
	
	public Word[] getWords() {
		return rowlabs;
	}
	
	public ConciseDocument[] getDocs() {
		return collabs;
	}
}
