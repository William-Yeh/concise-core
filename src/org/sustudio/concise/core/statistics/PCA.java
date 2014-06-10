package org.sustudio.concise.core.statistics;

/*
 * Implement Principal Components Analysis (PCA)
 * 
 * Author : Peter Foley, 07.10.2010
 * https://code.google.com/p/geoviz/source/browse/trunk/statistics/src/main/java/ncg/statistics/PCAException.java?r=751
 * 
 * Method taken from 'Multivariate Statistical Methods : A Primer'
 * by Bryan F.J. Manly (3rd edition), Chapman & Hall/CRC 2005.
 * See chapter 6 for details
 */

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixDimensionMismatchException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.Covariance;

public class PCA {
	
	// attributes
	private transient RealMatrix observations = null;
	private transient RealMatrix principalComponents = null;
	private transient RealMatrix eigenVectors = null;
	private transient RealVector eigenValues = null;
	
	// empty constructor
	public PCA() {};
    
	// reset all member variables
	public void clear() {
		
		observations = null;
		principalComponents = null;
		eigenVectors = null;
		eigenValues = null;
		
		// run the java garbage collector
		Runtime.getRuntime().gc();
	}
	
	/**
	 * check to see if the observations attribute has been set
	 * 
	 * @throws PCAException throws a new PCAException if observations is not set
	 */
	public void validateObservations() throws PCAException {
		if ( observations == null ) {
			throw new PCAException("input observations not set");
		}
	}
    
	/**
	 * check to see if the principalComponents attribute has been set
	 * 
	 * @throws PCAException throws a new PCAException if principalComponents is not set
	 */
	public void validatePrincipalComponents() throws PCAException {
		if ( principalComponents == null ) {
			throw new PCAException("output principalcomponents not set");
		}
	}
       
	/**
	 * check to see if the eigenValues attribute has been set
	 * 
	 * @throws PCAException throws a new PCAException if eigenValues is not set
	 */
	public void validateEigenValues() throws PCAException { 
		if ( eigenValues == null ) {
			throw new PCAException("output eigen values not set");
		}
	}
	
	/**
	 * check to see if the eigenVectors attribute has been set
	 * 
	 * @throws PCAException throws a new PCAException if eigenVectors is not set
	 */
	public void validateEigenVectors() throws PCAException {
		if ( eigenVectors == null ) {
			throw new PCAException("eigen vectors not set");
		}
	} 
	
	
    /**
     * set the observations matrix   
     *  
     * @param observations	the observations matrix
     * @param standardize	If standardize is set to true, z scores are computed
     * 						for each variable. Each row of the observations matrix 
     * 						refers to an observation and each column, a variable.
     */
	public void setObservations(double[][] observations, boolean standardize) {
		try {
			// standardize the observations array if required
			if ( standardize == true ) {
				observations = ConciseStatUtils.standardize(observations);
			}
			this.observations =
					MatrixUtils.createRealMatrix(observations);
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			this.observations = null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			this.observations = null;
		}
	}   

	/**
	 * return a copy of the observations matrix as a 2d array of doubles
	 * 
	 * @return a copy of the observations matrix as a 2d array of doubles
	 * @throws PCAException throws a new PCAException if observations is not set
	 */
    public double[][] getObservations() throws PCAException {
    	validateObservations();
    	return observations.getData();
    }
    
    /**
     * returns a copy of the principalComponents matrix as a 2d array of doubles
     * 
     * @return a copy of the principalComponents matrix as a 2d array of doubles
     * @throws PCAException throws a new PCAException if principalComponents is not set
     */
    public double[][] getPrincipalComponents() throws PCAException {
    	validatePrincipalComponents();
    	return principalComponents.getData();
    }
       
    /**
     * returns a copy of sorted (real) eigenValues vector
     * 
     * @return a copy of sorted (real) eigenValues vector
     * @throws PCAException throws a new PCAException if eigenValues is not set
     */
    public double[] getEigenValues() throws PCAException {
    	validateEigenValues();
        double[] result = eigenValues.toArray().clone();
        Arrays.sort(result);
        ArrayUtils.reverse(result);
        return result;
    }
    
    /**
	 * returns the percentage explained by dimension (starts from 1)
	 * 
	 * @param dimension dimension starts from 1
	 * @return
	 */
	public double getExplainedByDimension(int dimension) throws PCAException {
		validateEigenValues();
		double total = 0.0;
		for (double eig : getEigenValues()) {
			total += eig;
		}
		return getEigenValues()[dimension - 1] / total;
	}
	
	/**
	 * returns a copy of the EigenVectors matrix as a 2d array of doubles
	 * 
	 * @return a copy of the EigenVectors matrix as a 2d array of doubles
	 * @throws PCAException throws a new PCAException if eigenVectors is not set
	 */
	public double[][] getEigenVectors() throws PCAException {
		validateEigenVectors();
        return eigenVectors.getData();
	}
	
	/**
	 * returns the covariance matrix for the observations matrix
	 * 
	 * @return returns the covariance matrix for the observations matrix; returns null if an error occurs
	 * @throws PCAException throws a new PCAException if observations is not set
	 */
	private RealMatrix getCovarianceMatrix() throws PCAException {
		// validate the observations matrix
        validateObservations();
        
        RealMatrix covMatrix = null;
        
        try {
        	if ( observations.getColumnDimension() > 1) {
        		
        		// compute covariance matrix if we have more than 1 attribute
        		Covariance c = new Covariance(observations);
        		covMatrix = c.getCovarianceMatrix();
        		
        	} else {
        		
        		// if we only have one attribute calculate the variance instead
        		covMatrix = MatrixUtils.createRealMatrix(1,1);
        		covMatrix.setEntry(0, 0, StatUtils.variance(observations.getColumn(0)));
        	}
        } catch (OutOfRangeException e) {
        	e.printStackTrace();
        	covMatrix = null;
        } catch (IllegalArgumentException e) {
        	e.printStackTrace();
        	covMatrix = null;
        } catch (NullPointerException e) {
        	e.printStackTrace();
        	covMatrix = null;
        }
        
        return covMatrix;
	}
    
	/**
	 * compute the eigen values, eigen vectors for the covariance matrix
	 * of the observations matrix and transform observations
	 * 
	 * @throws PCAException throws a new PCAException if observations is not set
	 */
	public void transform() throws PCAException {
		 // check to see if the observations matrix has been set
        validateObservations();
        
        // compute the covariance matrix
        RealMatrix covMatrix = getCovarianceMatrix();
        
        try {
        	
        	// get the eigenvalues and eigenvectors of the covariance matrixE
        	EigenDecomposition eDecomp = new EigenDecomposition(covMatrix);
        	
        	// set the eigenVectors matrix
        	// the columns of the eigenVectors matrix are the eigenVectors of
        	// the covariance matrix
        	eigenVectors = eDecomp.getV();
        	
        	// set the eigenValues vector
        	eigenValues = new ArrayRealVector(eDecomp.getRealEigenvalues());
        	
        	//transform the data
        	principalComponents = observations.multiply(eigenVectors);
        	
        } catch (MatrixDimensionMismatchException e) {
        	e.printStackTrace();
        } catch (IllegalArgumentException e) {
        	e.printStackTrace();
        } catch (NullPointerException e) {
        	e.printStackTrace();
        } 
	}
}
