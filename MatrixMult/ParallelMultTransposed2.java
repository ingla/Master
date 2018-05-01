import java.util.concurrent.*;

/**
 * Implement a parallel algorithm for matrix multiplication where
 * both the multiplication and the transposition is performed in
 * parallel.
 * PS: Currently not part of the scope of the master thesis.
 */
class ParallelMultTransposed2 implements MatrixMultiplicator {
    int numThreads;
    CyclicBarrier synchBar;

    ParallelMultTransposed2() {
	numThreads = Runtime.getRuntime().availableProcessors();
	if (numThreads == 64) {
	    // NB: Hardcode the nr of physical cores (this particular machine
	    // is using hyperthreading with 2 logical cores per physical core).
	    numThreads = 32;
	}	
	synchBar = new CyclicBarrier(numThreads);
    }

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

	len = n/numThreads;
	startRow = 0;

	Thread t;
	int i;
	// Start all threads but the last.
	for (i = 0; i < numThreads - 1; i++) {
	    stopRow = startRow + len;
	    t = new Thread(new Para(i, startRow, stopRow, a, b, c));
	    t.start();
	    myThreads[i] = t;
	    startRow = stopRow;
	}

	// Start the last thread.
	t = new Thread(new Para(i, startRow, n, a, b, c));
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
	return "parMultTransposed2";
    }

    /* Thread class */
    class Para implements Runnable {
	int id;
	int startRow, stopRow;
	double[][] a, b;   // The matrices to be multiplied.
	double[][] c;      // The result matrix.
	int n;

	Para(int id, int startRow, int stopRow, double[][] a, double[][] b, double[][] c) {
	    this.id = id;
	    this.startRow = startRow;
	    this.stopRow = stopRow;
	    this.a = a;
	    this.b = b;
	    this.c = c;
	    n = a.length;
	}

	public void run() {
	    // Transpose the b matrix.
	    double tmp;
	    for (int i = startRow; i < stopRow; i++) {
		for (int j = 0; j < i; j++) {
		    tmp = b[i][j];
		    b[i][j] = b[j][i];
		    b[j][i] = tmp;
		}
	    }
	    synch();

	    // Multiply a and b.	    
	    for (int i = startRow; i < stopRow; i++) {
		for (int j = 0; j < n; j++) {
		    tmp = 0.0;
		    for (int k = 0; k < n; k++) {
			tmp += a[i][k]*b[j][k];
		    }
		    c[i][j] = tmp;
		}
	    }
	} // end run

	void synch() {
	    try {
		synchBar.await();
	    } catch (InterruptedException ie) {
		System.out.println(ie.toString());
		System.exit(1);
	    } catch (BrokenBarrierException be) {
		System.out.println(be.toString());
		System.exit(1);
	    }
	}
    } // end inner class Para
} // end class ParallelMultTransposed
