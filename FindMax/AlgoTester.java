import java.util.Random;
import java.util.Arrays;
import java.lang.Runtime;
import java.lang.Math;

/**
 * Class for executing empirical analysis on the find max algorithm.
 */
class AlgoTester {
    final int MAXVAL = 10000;  // In the array.
    final int NTESTS = 5; // Find median running time of NTESTS tests.
    final boolean TOFILE = false;
    static int nProcs;

    public static void main(String[] args) {
	nProcs = Runtime.getRuntime().availableProcessors();
	System.out.println("nr. processors = " + nProcs);
	AlgoTester tester = new AlgoTester();

	/* Verify the implementations. */
	//tester.verifyAll();

	/* Test algo1 and algo2. */
	tester.analyzeAll();

	/* Test algo1 with 1, n and n² threads. */
	//tester.testThreadCost();
    }  // end main

    /**
     * Verify the three main algorithms for the Find Max problem:
     * 1. Sequential algorithm.
     * 2. Wilkinson's algorithm, algo 1.
     * 3. A more realistic algorithm, algo 2.
     */
    void verifyAll() {
	System.out.println("--- Verifying algorithms ---");
	FindMax[] algos = {
	    new SeqAlgo(),
	    new Algo1(),
	    new Algo2(),
	    new Algo1_1Thread(),
	    new Algo1_nThreads()
	};

	int[] a = new int[1000];
	fillArray(a, 1000); // All elements are at most 1000.
	a[403] = 1002; // Now 1002 is the max value.

	int m;
	for (FindMax tmp : algos) {
	    System.out.println("Verifying " + tmp.toString());
	    m = tmp.getMax(a);
	    if (m == 1002) {
		System.out.println("Success\n");
	    } else {
		System.out.println("Fail\n");
		System.exit(1);
	    }
	}
    } // end verifyAll

    /**
     * Run empirical analysis for the two main parallel algorithms.
     * 1. Wilkinson's algorithm, algo 1.
     * 2. A more realistic algorithm, algo 2.
     */
    void analyzeAll() {
	System.out.println("--- Run empirical analysis ---");	
	analyzeAlgo1();
	analyzeAlgo2();
    } // end analyzeAll

    /**
     * Analyze Wilkinson's algorithm. 
     */
    void analyzeAlgo1() {
	FindMax parAlgo;
	String fileInfo, folder;
	int[] sizes;

	parAlgo = new Algo1();
	fileInfo = "algo1";
	folder = "../Kjoretider/Algo1/";

	/* Generate the input sizes to test with.
	   PS: The max value of n before the system crashes is approximately 150.
	   We test with one value for each logarithmic "step" and three between
	   each step + three after the last step, 10^2. */
	int maxExp, nSizes, nBetween; // maxExp: Max exponent.
	maxExp = 2;
	nSizes = (maxExp + 1) + 3*maxExp + 2;
	sizes = new int[nSizes];
	nBetween = 3;
	
	// Fill the sizes array.
	int sizeInd = 0;
	for (int i = 0; i < maxExp; i++) {
	    sizes[sizeInd++] = (int) Math.pow(10, i);
	    int interval = ((int) Math.pow(10, i + 1) - (int) Math.pow(10, i) - 1)/(nBetween + 1);
	    int count = 0;
	    for (int j = 1; j <= nBetween; j++) {
		sizes[sizeInd++] = (int) Math.pow(10, i) + j*interval;
	    }
	}
	sizes[sizeInd++] = 100;
	sizes[sizeInd++] = 125;
	sizes[sizeInd++] = 150;

	analyzeAlgo(parAlgo, sizes, fileInfo, folder);	
    } // end analyzeAlgo1

    /**
     * Analyze algo 2.
    */
    void analyzeAlgo2() {
	FindMax parAlgo;
	String fileInfo, folder;
	int[] sizes;

	parAlgo = new Algo2();
	fileInfo = "algo2";
	folder = "../Kjoretider/Algo2/";	

	/* Generate to input sizes to test with.
	   We test with one value for each logarithmic "step" and one
	   between each step. */
	int maxExp, nSizes; // max exponent
	maxExp = 8;
	nSizes = maxExp + (maxExp - 1);
	sizes = new int[nSizes];
	
	// Fill the sizes array.
	int sizeInd = 0;
	for (int i = 1; i < maxExp; i++) {
	    sizes[sizeInd++] = (int) Math.pow(10, i);
	    int interval = ((int) Math.pow(10, i+1) - (int) Math.pow(10, i) - 1)/2;
	    sizes[sizeInd++] = (int) Math.pow(10, i) + interval;
	}
	sizes[sizeInd] = (int) Math.pow(10, maxExp);

	analyzeAlgo(parAlgo, sizes, fileInfo, folder);
    } // end analyzeAlgo2

    /**
     * Compare the Wilkinson's algorithm with two modified versions:
     * one with n threads and one with 1 thread.
     */
    void testThreadCost() {
	FindMax algo, algo_1Thread, algo_nThreads;
	String fileInfo, folder;
	int[] sizes = {1, 2, 4, 6, 8, 10, 25, 50, 75, 100};
	int[] sizes2 = {1, 2, 4, 6, 8, 10, 25, 50, 75, 100, 250,
			500, 750, 1000};
	int[] sizes3 = {1, 2, 4, 6, 8, 10, 25, 50, 75, 100, 250,
			500, 750, 1000, 10000, 100000};

	algo = new Algo1();
	algo_1Thread = new Algo1_1Thread();
	algo_nThreads = new Algo1_nThreads();
	
	folder = "../Kjoretider/Algo2/";

	fileInfo = "algo1_n_squared_threads";
	analyzeAlgo(algo, sizes, fileInfo, folder);
	fileInfo = "algo1_n_threads";
	analyzeAlgo(algo_nThreads, sizes2, fileInfo, folder);
	fileInfo = "algo1_1_thread";
	analyzeAlgo(algo_1Thread, sizes3, fileInfo, folder);	
    } // end testThreadCost

    /**
     * Run empirical analysis of a parallel algorithm for a given set
     * of input sizes.
     * @param parAlgo: An instance of the FindMax interface.
     * @param sizes: The sizes to test the algorithm with.
     * @param fileInfo: The filename to which the speedup is written, if
     * TOFILE is true.
     * @param folder: The folder to which the speedup is written, if
     * TOFILE is true.
     */
    void analyzeAlgo(FindMax parAlgo, int[] sizes, String fileInfo, String folder) {
	FindMax seqAlgo;
	double[] tPar, tSeq;
	System.out.println("Analyze " + parAlgo.toString());
	seqAlgo = new SeqAlgo();
	tPar = new double[sizes.length];
	tSeq = new double[sizes.length];	

	for (int i = sizes.length - 1; i >= 0; i--) {
	    int s = sizes[i];
	    tSeq[i] = analyzeAlgo(seqAlgo, s);
	    tPar[i] = analyzeAlgo(parAlgo, s);
	}
	printSpeedup(tSeq, tPar, sizes, fileInfo, folder);	
    } // end analyzeAlgo

     /**
      * Run an algorithm NTESTS time for a given input size.
      * @param algo: An implementation of the FindMax interface.
      * @param size: The input size to test the algorithm with.
      * @return the median running time for NTESTS tests.
     */
    double analyzeAlgo(FindMax algo, int size) {
	int[] a;	
	int max;

	double[] times = new double[NTESTS];
	long startTime, stopTime;
	double diff;

	for (int i = 0; i < NTESTS; i++) {
	    a = new int[size];
	    fillArray(a, MAXVAL);
	    
	    // Measure the running time.
	    startTime = System.nanoTime();
	    max = algo.getMax(a);
	    stopTime = System.nanoTime();

	    // Get the difference in ms.
	    diff = (stopTime - startTime)/1000000.0;
	    times[i] = diff;
	}
	// Find median running time.
	return getMedian(times);	    
    } // end analyzeAlgo  

    /**
     * Get the median value in an array.
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
     * Print the speedups for a parallel algorithm. Write the speedups to
     * file if TOFILE is true.
     * @param times1: The running times for a sequential algorithm.
     * @param times2: The running times for a parallel algorithm.
     * @param sizes: The input sizes that the algorithms have been tested
     * with.
     * @param fileInfo: The file to write the results to.
     * @param folder: The folder to write the results to.
     */
    void printSpeedup(double[] times1, double[] times2, 
		      int[] sizes, String fileInfo, String folder) {
	System.out.println("++ Speedup ++");
	// Table title
	System.out.format("%-11s%-10s\n", 
			  "Array size", "Speedup");

	double[] speedups = new double[sizes.length];
	double a, b, s;

	for (int ind = 0; ind < sizes.length; ind++) {
	    System.out.format("%10d|", sizes[ind]);
	    a = times1[ind];
	    b = times2[ind];
	    s = a/b; 
	    System.out.format("%-10.8f\n", s);
	    speedups[ind] = s;
	}
	System.out.println("\n");

	if (TOFILE) {
	    MyFileWriter w = new MyFileWriter();
	    String filename1 = folder + fileInfo + "_s_" + 
		new Integer(nProcs).toString() +
		".txt";
	    String filename2 = folder + fileInfo + "_n_" + 
		new Integer(nProcs).toString() + ".txt";
	    w.printToFile(filename1, speedups);
	    w.printToFile(filename2, sizes);
	    System.out.println("Sizes and speedups written to file");
	}
    } // end printSpeedup

    /**
     * Fill an array with random values betweeen
     * 0 and a maximum value.
     * @param a: The array to be filled.
     * @param maxVal: Maximum value for an array element.
     */
    void fillArray(int[] a, int maxVal) {
	Random random = new Random();

	for (int i = 0; i < a.length; i++) {
	    a[i] = random.nextInt(maxVal);
	}
    } // end fillArray
} // end AlgoTester
