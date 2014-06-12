package org.sustudio.concise.core.statistics.pca;

public class ConciseStatUtils {

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
	public static double[][] standardize(double[][] A) {
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
