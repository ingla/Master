import java.util.Random;
import java.util.Arrays;
import java.lang.Runtime;
import java.lang.Math;

/**
 * An interface for the classes implementing the matrix multiplication 
 * algorithm.
 */
interface MatrixMultiplicator {
    public double[][] mult(double[][] a, double[][] b);
    public String toString();
}

/**
 * A class for running empirical analysis on implementations of the 
 * matrix multiplication algorithm.
 */ 
class AlgoTester {
    // Find median running time of NTESTS tests for each input size.
    final int NTESTS = 5;
    final boolean TOFILE = true;

    public static void main(String[] args) {
	AlgoTester tester = new AlgoTester();

	// Verify the implementations.
	tester.verifyPrograms();

	// Get empirical running times.
	tester.analyzePrograms();
    } // end main

    /**
     * Verify all the implementations of the MatrixMultiplicator interface.
     */
    void verifyPrograms() {
	MatrixMultiplicator[] algos = {
	    new SequentialMult(),
	    new ParallelMult(),
	    new ParallelMultTransposed(),
	    new ParallelMultTransposed2()
	};
	MatrixMultiplicator m;
	
	for (int i = 0; i < algos.length; i++) {
	    m = algos[i];
	    verifyMult(m);
	    verifyMult2(m);
	    System.out.println();
	}
    } // end verifyPrograms

    /**
     * Verify an implementation of the MatrixMultiplicator interface.
     * Multiply two matrices of only 1s, and verify the result.
     */
    void verifyMult(MatrixMultiplicator m) {
	System.out.println("-> Verify algorithm " + m.toString());
	// Create and fill two matrices with 1-values.
	int n;
	double[][] a, b;
	double[][] c; // Result
	
	// Create and fill two matrices with 1-values.	
	n = 3;
	a = new double[n][n];
	b = new double[n][n];
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		a[i][j] = 1;
		b[i][j] = 1;
	    }
	}

	// Do matrix multiplication.
	c = m.mult(a, b);

	// Verify the result matrix. All cells should be n.
	if (c == null) {
	    System.out.println("Error! \n" +
			       m.toString() + 
			       ": Result is null");
	    System.exit(1);
	}

	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		if (c[i][j] != n) {
		    System.out.println("Error!" + "\n" +
				       m.toString() + ": c[" + i + 
				       "][" + j + 
				       "] = " + c[i]);
		    System.exit(1);
		}
	    }
	}
	System.out.println("Correct result.");
    } // end verifyMult

    /**
     * Verify an implementation of the MatrixMultiplicator interface.
     */
    void verifyMult2(MatrixMultiplicator m) {
	System.out.println("-> Verify algorithm " + m.toString() + " 2nd time");
	int n;
	double[][] d;

	// Generate input.
	double[][] a = {
	    {1, 2, 3},
	    {4, 5, 6},
	    {7, 8, 9}
	};

	double[][] b = {
	    {2, 3, 4},
	    {5, 6, 7},
	    {8, 9, 10}
	};
	
	// Generate correct result.
	double[][] c = {
	    {36, 42, 48},
	    {81, 96, 111},
	    {126, 150, 174}
	};
	
	// Get nr of rows and columns in the matrices.
	n = a.length;
	// Run the algorithm.
	d = m.mult(a, b);
	
	// Verify result.
	if (d == null) {
	    System.out.println("Error! result matrix is null.");
	    System.exit(1);
	}
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		if (c[i][j] != d[i][j]) {
		    String msg = "Error! \n" + m.toString() + 
			": d[" + i + "][" + j + "] = " + 
			d[i][j] + 
			", but c[" + i + "][" + j + "] =" +
			c[i][j];
		    System.out.println(msg);
		    System.exit(1);
		}
	    }
	}
	System.out.println("Correct result.");
    } // end verifyMult2

    /**
     * Analyze the two parallel programs for matrix multiplication.
     */
    void analyzePrograms() {
	MatrixMultiplicator seqAlgo;
	MatrixMultiplicator[] parAlgos = {
	    new ParallelMult(),
	    new ParallelMultTransposed()
	};
	double[] tSeq, tPar;
	int nSizes, sizeMax, sizeMin, interval;
	int[] sizes;  // The problem sizes to be tested.

	nSizes = 20;
	int nProcs = Runtime.getRuntime().availableProcessors();
	if (nProcs == 4) {
	    sizeMax = 3000;
	} else if (nProcs == 64) {
	    sizeMax = 4000;
	} else {
	    System.out.println("Set maximum input size to default=3000.");
	    sizeMax = 3000;
	}
	sizeMin = 10;
	interval = (sizeMax - sizeMin)/nSizes;
	sizes = new int[nSizes];
	
	// Fill the sizes array.
	int s = 10;
	for (int i = 0; i < sizes.length; i++) {
	    sizes[i] = s;
	    s += interval;
	}

	// Analyze sequential algorithm.
	seqAlgo = new SequentialMult();
	tSeq = testAlgo(seqAlgo, sizes);

	// Analyze each parallel algorithm in array 'parAlgos'.
	MatrixMultiplicator tmp;
	for (int i = 0; i < parAlgos.length; i++) {
	    tmp = parAlgos[i];
	    tPar = testAlgo(tmp, sizes);
	    printSpeedup(tSeq, tPar, sizes, tmp.toString());
	}
    } // end analyzePrograms

    /**
     * Get the running time for an algorithm for a number of input sizes.
     * @param m: An implementation of the MatrixMultiplicator interface.
     * @param sizes: The input sizes to test the algorithm with.
     */
    double[] testAlgo(MatrixMultiplicator m, int[] sizes) {
	System.out.println("* Analyze algorithm " + m.toString() + " *");

	double[] times = new double[sizes.length];
	int s;
	for (int i = sizes.length - 1; i >= 0; i--) {
	    s = sizes[i];
	    times[i] = testAlgo(m, s);
	}
	return times;
    } // end testAlgo

    /**
     * Get the running time for an algorithm for a specific input size.
     * @param algo: An implementation of the MatrixMultiplicator interface.
     * @param size: The input size to test the algorithm with.
     * NB: We run the algorithm NTESTS times.
     */
    double testAlgo(MatrixMultiplicator algo, int size) {
	double[][] a, b;
	double[] times = new double[NTESTS];
	long startTime, stopTime;
	double diff;

	for (int i = 0; i < NTESTS; i++) {
	    a = new double[size][size];
	    b = new double[size][size];
	    fillMatrix(a);
	    fillMatrix(b);

	    // Measure the running time.
	    startTime = System.nanoTime();
	    algo.mult(a, b);
	    stopTime = System.nanoTime();
	    
	    // Get the difference in ms.
	    diff = (stopTime - startTime)/1000000.0;
	    times[i] = diff;
	}
	return getMedian(times);
    } // end testAlgo

    /**
     * Calculate and print speedup. Write speedup to file if TOFILE is true.
     * @param tSeq: The times for a sequential algorithm.
     * @param tPar: The times for a parallel algorithm.
     * @param sizes: The tested input sizes in ascending order.
     * @param algoName: A string identifying the parallell algorithm.
     */
    void printSpeedup(double[] tSeq, double[] tPar, int[] sizes, String algoName) {
	System.out.println("++ Speedup ++");
	
	System.out.format("%-11s%-10s\n",
			  "n", "Speedup");

	double[] speedups = new double[sizes.length];
	double a, b, s;
	for (int i = 0; i < sizes.length; i++) {
	    System.out.format("%10d|", sizes[i]);
	    a = tSeq[i];
	    b = tPar[i];
	    s = a/b;
	    System.out.format("%-10.8f\n", s);
	    speedups[i] = s;
	}
	System.out.println("\n");

	if (TOFILE) {
	    MyFileWriter w = new MyFileWriter();
	    int nProcs = Runtime.getRuntime().availableProcessors();
	    String folder = "../Kjoretider/MatrixMult/";
	    String filename1 = folder + algoName + "_s_" +
		new Integer(nProcs).toString() + ".txt";
	    String filename2 = folder + algoName + "_n_" + 
		new Integer(nProcs).toString() + ".txt";
	    w.printToFile(filename1, speedups);
	    w.printToFile(filename2, sizes);
	    System.out.println("Sizes and speedup are written to file.");
	}
    } // end printSpeedup.

    /**
     * Return the median value of an array.
     */
    double getMedian(double[] t) {
	Arrays.sort(t);
	
	double median;
	int midInd;

	if (t.length%2 != 0) {   // Odd number of elements.
	    midInd = t.length/2;
	    median = t[midInd];
	} else {                 // Even number of elements.
	    midInd = t.length/2;
	    median = (t[midInd] + t[midInd-1])/2; // Find avg.
	}

	return median;  
    } // end getMedian

    /**
     * Fill a matrix with random values between 0.0 and 1.0.
     * @param m: The matrix to be filled.
     */
    void fillMatrix(double[][] m) {
	Random rand = new Random(1234);
	int n = m.length;

	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		m[i][j] = rand.nextDouble();
	    }
	}
    } // end fillMatrix
} // end class AlgoTester
