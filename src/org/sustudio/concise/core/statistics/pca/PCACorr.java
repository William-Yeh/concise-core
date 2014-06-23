package org.sustudio.concise.core.statistics.pca;

import org.apache.commons.lang3.ArrayUtils;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * PCAcorr - Principal Components Analysis on correlations<p>
 * Jama Matrix class package, "JAMA: A Java Matri Package" is used.<br>
 * see: <a href="http://math.nist.gov/javanumerics/jama">http://math.nist.gov/javanumerics/jama</a><p>
 * This code was modified from F. Murtagh, f.murtagh@qub.ac.uk <br>
 * see <a href="http://www.classification-society.org/csna/mda-sw/">http://www.classification-society.org/csna/mda-sw/</a><p>
 * 
 * @author Kuan-ming Su
 *
 */
public class PCACorr {
	
	private transient Matrix observations = null;
	private transient Matrix rowProjections = null;
	private transient Matrix colProjections = null;
	private transient Matrix eigenVectors = null;
	private transient double[] eigenValues = null;
	
	public PCACorr() {}
	
	/**
	 * clear all member variables
	 */
	public void clear() {
		observations = null;
		rowProjections = null;
		colProjections = null;
		eigenVectors = null;
		eigenValues = null;
		
		// run the java garbage collector
		Runtime.getRuntime().gc();
	}
	
	/**
	 * set the observations matrix
	 * @param observations
	 */
	public void setObservations(double[][] observations) {
		// standardization
		observations = standardize(observations);
		this.observations = new Matrix(observations);
	}
	
	/**
	 * returns a copy of the observations matrix as a 2d array of doubles
	 * @return
	 */
	public double[][] getObservations() {
		return observations.getArrayCopy();
	}
	
	/**
	 * returns a copy of the row projections matrix as a 2d array of doubles
	 * @return
	 */
	public double[][] getRowProjections() {
		return rowProjections.getArrayCopy();
	}
	
	/**
	 * returns a copy of the column projections matrix as a 2d array of doubles
	 * @return
	 */
	public double[][] getColProjections() {
		return colProjections.getArrayCopy();
	}
	
	/**
	 * returns a copy of the (real) eigenValues vector
	 * @return
	 */
	public double[] getEigenValues() {
		return eigenValues.clone();
	}
	
	/**
	 * returns a copy of the EigenVectors matrix as a 2d array of doubles
	 * @return
	 */
	public double[][] getEigenVectors() {
		return eigenVectors.getArrayCopy();
	}
	
	public void transform() {
		// Sums of squares and cross-products matrix
		Matrix Xprime = observations.transpose();
		Matrix SSCP = Xprime.times(observations);
		// Note the following:
		// - with no preprocessing of the input data, we have an SSCP matrix
		// - with centering of columns (i.e. each col. has col. mean
		//   [vector in row-space] subtracted) we have variances/covariances
		// - with centering and reduction to unit variance [i.e. centered
		//   cols. are divided by std. dev.] we have correlations
		// Note: the current version supports correlations only
		
		//---------------------------------------------------
		// Eigen decomposition
		EigenvalueDecomposition evaldec = SSCP.eig();
		Matrix evecs = evaldec.getV();
		double[] evals = evaldec.getRealEigenvalues();
		
		// reverse order of evals into Evals
		eigenValues = evals.clone();
		ArrayUtils.reverse(eigenValues);
		
		// reverse order of Matrix evecs into Matrix Evecs
		double[][] tempold = evecs.getArray();
		double[][] tempnew = new double[observations.getColumnDimension()][observations.getColumnDimension()];
		for (int j1=0; j1 < tempnew.length; j1++) 
		{
			for (int j2 = 0; j2 < tempnew.length; j2++)
			{
				tempnew[j1][j2] = tempold[j1][tempnew.length - j2 - 1];
			}
		}
		eigenVectors = new Matrix(tempnew);
		
		//---------------------------------------------------
		// Principal Components - row projections in new space, X U  Dims: (n x m) x (m x m)
		rowProjections = observations.times(eigenVectors);
		
		// Col projections (X'X) U    (4x4) x4  And col-wise div. by sqrt(evals)
		colProjections = SSCP.times(eigenVectors);
		
		// We need to leave colProjections Matrix class and instead use double array
		double[][] ynew = colProjections.getArray();
		int m = observations.getColumnDimension();
		for (int j1 = 0; j1 < m; j1++) {
			for (int j2 = 0; j2 < m; j2++) {
				if (eigenValues[j2] > 0.00005) {
					ynew[j1][j2] = ynew[j1][j2]/Math.sqrt(eigenValues[j2]);
				}
				if (eigenValues[j2] <= 0.00005) {
					ynew[j1][j2] = 0.0;
				}
			}
		}
		colProjections = new Matrix(ynew);
	}
	
	/**
	 * Method for standardizing the input data <p>
	 * Note the formalas used (since these very between implementations): <br>
	 * reduction: (vect - meanvect)/sqrt(nrow)*colstdev <br>
	 * colstdev: sum_cols ((vect - meanvect)^2/nrow) <br>
	 * if colstdev is close to 0, then set it to 1. <p>
	 * Rewrite standardize to meet your requirement <br>
	 * 
	 * @param A input matrix values
	 * @return standardized matrix
	 */
	protected double[][] standardize(double[][] A) {
		int nrow = A.length;
		int ncol = A[0].length;
		double[] colmeans = new double[ncol];
		double[] colstdevs = new double[ncol];
		// Adat will contain the standardized data and will be returned
		double[][] Adat = new double[nrow][ncol];
		double[] tempcol = new double[nrow];
		double tot;
		
		// determine means and standard deviations of variables/columns
		for (int j=0; j<ncol; j++) 
		{
			tot = 0.0;
			for (int i=0; i<nrow; i++) 
			{
				tempcol[i] = A[i][j];
				tot += tempcol[i];
			}
			
			// for this col, det mean
			colmeans[j] = tot/(double)nrow;
			for (int i=0; i<nrow; i++) {
				colstdevs[j] += Math.pow(tempcol[i]-colmeans[j], 2.0);
			}
			colstdevs[j] = Math.sqrt(colstdevs[j]/((double) nrow));
			if (colstdevs[j] < 0.0001) {
				colstdevs[j] = 1.0;
			}
		}
		
		// now center to zero mean, and reduce to unit standard deviation
		for (int j=0; j<ncol; j++) 
		{
			for (int i=0; i<nrow; i++)
			{
				Adat[i][j] = (A[i][j] - colmeans[j]) / (Math.sqrt((double)nrow) * colstdevs[j]);
			}
		}
		return Adat;
	}
}
