package org.sustudio.concise.core.statistics.ca;

import Jama.Matrix;

/**
 * Imatrix
 <P>
 DataAnalysisJava is a first version of a data analysis package for
 Java. Class Imatrix is a labeled matrix, or matrix with identifier,
 class, which extends the JAMA Matrix class.
 <P>
 @author Fionn Murtagh, fmurtagh@acm.org
 @version 0.0, 2003-March-15
 */


public class Imatrix extends Matrix {

    private static final long serialVersionUID = 5139070126913747084L;
    
    double[] rowMass;                    // Row masses or weights.
    double[] columnMass;                 // Column masses or weights.
    double total;                        // Total of data values.

    public Imatrix (double[][] a, int n, int m, 
                    double[] rowMass, double[] columnMass, double total)
    {
        super (a, n, m);
        this.rowMass = rowMass;
        this.columnMass = columnMass;
        this.total = total;
    }


    /* -------------------
      Public Methods
     * ------------------- */
    
    public double[] getRowMass () {
        return rowMass;
    }

    public double[] getColumnMass () {
        return columnMass;
    }

    public double getTotal () {
        return total;
    }

}


