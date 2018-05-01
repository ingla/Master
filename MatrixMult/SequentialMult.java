/**
 * Implements the sequential algorithm for matrix multiplication.
 */
class SequentialMult implements MatrixMultiplicator {

    /**
     * Calculate the product of two matrices.
     * @param a: 1st input matrix.
     * @param b: 2nd input matrix.
     * @return: the result matrix.
     */
    public double[][] mult(double[][] a, double[][] b) {
	double[][] c;
	int n;

	n = a.length;
	c = new double[n][n];

	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		double tmp = 0.0;
		for (int k = 0; k < n; k++) {
		    tmp += a[i][k]*b[k][j];
		}
		c[i][j] = tmp;
	    }
	}	
	return c;
    } // end mult

    /**
     * Return a string identifying the algorithm.
     */   
    public String toString() {
	return "seqMult";
    }
} // end SequentialMult
