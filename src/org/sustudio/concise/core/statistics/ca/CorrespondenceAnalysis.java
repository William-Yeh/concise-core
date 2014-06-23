package org.sustudio.concise.core.statistics.ca;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class CorrespondenceAnalysis {
	
	public static final double EPS = 1.0e-8;

	private final int n;
	private final int m;
	private final double[] rowmass;
	private final double[] colmass;
	private final double[][] data;
	
	private double trace;						// trace
	private transient double[][] CP;			// sums of squares and cross-products matrix to be diagonalized
	private transient double[] EigenValues;		// eigenvalues
	private transient double[][] EigenVectors;	// eigenvectors
	private transient double[] rates;			// rates of inertia associated with eigenvectors
	
	private transient double[][] rowproj;	// row projections	
	private transient double[][] colproj;	// column projections
	private transient double[][] rowcntr;	// row contributions
	private transient double[][] colcntr;	// column contributions
	private transient double[][] rowcorr;	// row correlations
	private transient double[][] colcorr;	// column correlations
	
	
	public CorrespondenceAnalysis(Imatrix principal) {
		
		n		= principal.getRowDimension();
		m		= principal.getColumnDimension();
		rowmass	= principal.getRowMass();
		colmass	= principal.getColumnMass();
		data	= principal.getArray();
		
		EigenValues 	= new double[m];
        EigenVectors 	= new double[m][m];
        rates			= new double[m];
		
		// Form matrix of cross-products to be analyzed, i.e. diagonalized.
		CP = new double[m][m];	// cross-products, e.g. Burt table.
        for (int j1 = 0; j1 < m; j1++) {
            for (int j2 = 0; j2 < m; j2++) {
                CP[j1][j2] = 0.0;
                for (int i = 0; i < n; i++)
                    CP[j1][j2] += ( data[i][j1] * data[i][j2] ) /
                            ( rowmass[i] * Math.sqrt(colmass[j1]*colmass[j2]) );
            }
        }

        // We will use Jama matrix class because it has the methods needed.
        Matrix cp = new Matrix(CP);
        
        
        // Diagonalization or Eigen Decomposition (Eigen Reduction)
        
        // Eigen decomposition
        EigenvalueDecomposition evaldec = cp.eig();
        Matrix evecs = evaldec.getV();
        double[] evals = evaldec.getRealEigenvalues();

        // Trace is adjusted by a value 1.0 because always in CA,
        // the first eigenvalue is trivially 1-valued.
        trace = cp.trace() - 1.0;

        // evecs contains the cols. ordered right to left.
        // EigenVectors is the more natural order with cols. ordered left to right.
        // So to repeat: leftmost col. of EigenVectors is assoc'd. with largest EigenValues.
        // EigenValues and EigenVectors ordered from left to right.
        
        // Reverse order of evals into Evals.
        for (int j = 0; j < m; j++) EigenValues[j] = evals[m - j - 1];

        // Reverse order of Matrix evecs into Matrix Evecs.
        double[][] tempold = evecs.getArray();
        for (int j1 = 0; j1 < m; j1++) {
            for (int j2 = 0; j2 < m; j2++)
                EigenVectors[j1][j2] = tempold[j1][m - j2 - 1]/
                        Math.sqrt(colmass[j1]);
        }
        
        // Low index in following = 1 to exclude first trivial eval.
        rates[0] = 0.0;
        for (int j = 1; j < EigenValues.length; j++) {
            rates[j] = EigenValues[j]/trace;
        }
		
	}
	
	/**
	 * calculate projections
	 */
	private void calculateProjections() {
		
        rowproj = new double[n][m];
		colproj = new double[m][m];
		
		// Projections on factors - row, and column
		// Row projections in new space, X U  Dims: (n x m) x (m x m)
		for (int i = 0; i < n; i++) {
			for (int j1 = 0; j1 < m; j1++) {
				rowproj[i][j1] = 0.0;
				for (int j2 = 0; j2 < m; j2++) {
					rowproj[i][j1] += data[i][j2] * EigenVectors[j2][j1];
				}
				if (rowmass[i] >= EPS)	rowproj[i][j1] /= rowmass[i];
				if (rowmass[i] < EPS)		rowproj[i][j1] = 0.0;
			}
		}
		
		// Column projections
		for (int j1 = 0; j1 < m; j1++) {
			for (int j2 = 0; j2 < m; j2++) {
				colproj[j1][j2] = 0.0;
				for (int j3 = 0; j3 < m; j3++) {
					colproj[j1][j2] += CP[j1][j3] * EigenVectors[j3][j2] * Math.sqrt(colmass[j3]);
				}
				if (colmass[j1] >= EPS && EigenValues[j2] >= EPS)
					colproj[j1][j2] /= Math.sqrt(EigenValues[j2] * colmass[j1]);
				if (colmass[j1] < EPS && EigenValues[j2] < EPS)
					colproj[j1][j2] = 0.0;
			}
		}
	}
	
	/**
	 * calculate contributions
	 */
	private void calculateContributions() {
		
		if (rowproj == null || colproj == null) {
			calculateProjections();
		}
		
		rowcntr = new double[n][m];
		colcntr = new double[m][m];
		
		// Contributions of factors - row, and column
		double rowconColsum;
		for (int j = 0; j < m; j++) {
			rowconColsum = 0.0;
			for (int i = 0; i < n; i++) {
				rowcntr[i][j] = rowmass[i] * Math.pow(rowproj[i][j], 2.0);
				rowconColsum += rowcntr[i][j];
			}
			// Normalize so that sum of contributions for a factor equals 1
			for (int i = 0; i < n; i++) {
				if (rowconColsum > EPS)	rowcntr[i][j] /= rowconColsum;
				if (rowconColsum <= EPS)	rowcntr[i][j] = 0.0;
			}
		}
		
		double colconColsum;
		for (int j1 = 0; j1 < m; j1++) {
			colconColsum = 0.0;
			for (int j2 = 0; j2 < m; j2++) {
				colcntr[j2][j1] = colmass[j2] * Math.pow(colproj[j2][j1], 2.0);
				colconColsum += colcntr[j2][j1];
			}
			// Normalize so that sum of contributions for a factor sum to 1
			for (int j2 = 0; j2 < m; j2++) {
				if (colconColsum > EPS)	colcntr[j2][j1] /= colconColsum;
				if (colconColsum <= EPS)	colcntr[j2][j1] = 0.0;
			}
		}
	}
	
	/**
	 * calculate correlations
	 */
	private void calculateCorrelations() {
		
		if (rowproj == null || colproj == null) {
			calculateProjections();
		}
		
		rowcorr = new double[n][m];
		colcorr = new double[m][m];
		
		// Correlations with factors - rows and column.
		// Return to this later: we may want to restrict to the
		// calculation of 7 factors.
		// We're assuming here that n > mnew >= 7
		
		// First rows.
		double distsq;
		for (int i = 0; i < n; i++) {
			distsq = 0.0;
			for (int j = 0; j < m; j++) {
				distsq += Math.pow((data[i][j]/rowmass[i] - colmass[j]), 2.0) / colmass[j];
			}
			for (int j = 0; j < m; j++) {
				rowcorr[i][j] = Math.pow(rowproj[i][j], 2.0)/distsq;
			}
		}
		
		// Now columns.
		// Return to this later: we may want to restrict to 7 factos.
		// We're assuming here that n > mnew >=7
		for (int j1 = 0; j1 < m; j1++) {
			distsq = 0.0;
			for (int i = 0; i < n; i++) {
				distsq += Math.pow((data[i][j1]/colmass[j1] - rowmass[i]), 2.0) / rowmass[i];
			}
			for (int j2 = 0; j2 < m; j2++) {
				colcorr[j1][j2] = Math.pow(colproj[j1][j2], 2.0) / distsq;
			}
		}
	}

	
	public double getTrace() {
		return trace;
	}
	
	/**
	 * returns matrix of cross-products to be analyzed
	 * @return
	 */
	public double[][] getCP() {
		return CP;
	}
	
	/**
	 * returns EigenValues
	 * @return
	 */
	public double[] getEigenValues() {
		return EigenValues;
	}
	
	/**
	 * returns EigenVectors
	 * @return
	 */
	public double[][] getEigenVectors() {
		return EigenVectors;
	}
	
	/**
	 * returns rates of inertia associated with eigenvectors
	 * @return
	 */
	public double[] getRates() {
		return rates;
	}
	
	/**
	 * returns row projections
	 * @return
	 */
	public double[][] getRowProjections() {
		if (rowproj == null) {
			calculateProjections();
		}
		return rowproj;
	}
	
	/**
	 * returns column projections
	 * @return
	 */
	public double[][] getColumnProjections() {
		if (colproj == null) {
			calculateProjections();
		}
		return colproj;
	}
	
	/**
	 * returns a copy of row contributions
	 * @return
	 */
	public double[][] getRowContributions() {
		if (rowcntr == null) {
			calculateContributions();
		}
		return rowcntr.clone();
	}
	
	/**
	 * returns column contributions
	 * @return
	 */
	public double[][] getColumnContributions() {
		if (colcntr == null) {
			calculateContributions();
		}
		return colcntr;
	}
	
	/**
	 * returns row correlations
	 * @return
	 */
	public double[][] getRowCorrelations() {
		if (rowcorr == null) {
			calculateCorrelations();
		}
		return rowcorr;
	}
	
	/**
	 * returns column correlations
	 * @return
	 */
	public double[][] getColumnCorrelations() {
		if (colcorr == null) {
			calculateCorrelations();
		}
		return colcorr;
	}
	
	/**
	 * 清除資料
	 */
	public void clear() {
		CP = null;
		EigenValues = null;
		EigenVectors = null;
		rates = null;
		rowproj = null;
		colproj = null;
		rowcntr = null;
		colcntr = null;
		rowcorr = null;
		colcorr = null;

		// run the java garbage collector
		Runtime.getRuntime().gc();
	}
		
}
