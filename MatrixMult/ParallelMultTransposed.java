import java.util.concurrent.*;

/**
 * Implements a parallel algorithm for matrix multiplication with 
 * transposition.
 */
class ParallelMultTransposed implements MatrixMultiplicator {
    int numThreads;

    ParallelMultTransposed() {
	numThreads = Runtime.getRuntime().availableProcessors();
	if (numThreads == 64) {
	    // NB: Hardcode the nr of physical cores (this particular machine
	    // is using hyperthreading with 2 logical cores per physical core).
	    numThreads = 32;
	}
	System.out.println("Algo 2: nThreads = " + numThreads);
    }

    public static void main(String[] args) {
	// Test the 'transpose' method.
	System.out.println("* Test the 'transposed' method *");
	ParallelMultTransposed p = new ParallelMultTransposed();
	
	double[][] a = {
	    {1, 2, 3},
	    {4, 5, 6},
	    {7, 8, 9}
	};

	p.transpose(a);
	System.out.println("Matrix after transposing.");
	for (int i = 0; i < a.length; i++) {
	    for (int j = 0; j < a.length; j++) {
		System.out.print(a[i][j] + " ");
	    }
	    System.out.println();
	}
    } // end main

    /**
     * Calculate the product of two matrices a and b.
     * @param a: 1st input matrix.
     * @param b: 2nd input matrix.
     * @return: the result matrix.
     */
    public double[][] mult(double[][] a, double[][] b) {
	int n = a.length;
	double[][] c = new double[n][n];
	int startRow, stopRow, len;
	Thread[] myThreads = new Thread[numThreads];

	// Transpose the b matrix.
	transpose(b);

	len = n/numThreads;
	startRow = 0;

	Thread t;
	int i;
	// Start all threads but the last.
	for (i = 0; i < numThreads - 1; i++) {
	    stopRow = startRow + len;
	    t = new Thread(new Para(startRow, stopRow, a, b, c));
	    t.start();
	    myThreads[i] = t;
	    startRow = stopRow;
	}

	// Start the last thread.
	t = new Thread(new Para(startRow, n, a, b, c));
	t.start();
	myThreads[i] = t;

	// Wait for threads to finish.
	for (i = 0; i < numThreads; i++) {
	    try {
		myThreads[i].join();
	    } catch (InterruptedException e) {
		System.out.println(e.toString());
		System.exit(1);
	    }
	}	 
	return c;
    } // end mult

    /**
     * Return a string identifying the algorithm.
     */   
    public String toString() {
	return "parMultTransposed";
    }

    /* Thread class */
    class Para implements Runnable {
	int startRow, stopRow;
	double[][] a, b; // The matrices to be multiplied.
	double[][] c;      // The result matrix.
	int n;

	Para(int startRow, int stopRow, double[][] a, double[][] b, double[][] c) {
	    this.startRow = startRow;
	    this.stopRow = stopRow;
	    this.a = a;
	    this.b = b;
	    this.c = c;
	    n = a.length;
	}

	public void run() {
	    for (int i = startRow; i < stopRow; i++) {
		for (int j = 0; j < n; j++) {
		    double tmp = 0.0;
		    for (int k = 0; k < n; k++) {
			tmp += a[i][k]*b[j][k];
		    }
		    c[i][j] = tmp;
		}
	    }
	}
    } // end inner class Para

    /**
     * Transpose a matrix.
     */
    void transpose(double[][] b) {
	int n = b.length;
	double tmp;
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < i; j++) {
		tmp = b[i][j];
		b[i][j] = b[j][i];
		b[j][i] = tmp;
	    }
	}
    } // end transpose
} // end class ParallelMultTransposed
