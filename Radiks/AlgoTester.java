import java.util.Random;
import java.util.Arrays;
import java.lang.Runtime;
import java.lang.Math;

/**
 * An interface for the classes implementing the radix algorithm.
 */
interface RadixAlgo {
    void radix(int[] a, int numDigits);
}

class AlgoTester {
    final int NTESTS = 5; // Find median running time of NTESTS tests.
    final boolean TOFILE = false;
    static int numDigits;
    static int testType;  // 1: S = T(n,1)/Tp(n,k), 2: S = T(n,k)/Tp(n,k)
    String fileInfo;

    public static void main(String[] args) {
	if (args.length > 1) {
	    numDigits = Integer.valueOf(args[0]);
	    testType = Integer.valueOf(args[1]);
	} else {
	    System.out.println("How to run: >java AlgoTester x y\n" +
			       "x: Nr of digits to sort on\n" +
			       "y: The type of speedup to be generated (1 or 2) \n" +
			       "(1: S = T(n,1)/Tp(n,k), 2: S = T(n,k)/Tp(n,k)");
	    return;
	}
	AlgoTester tester = new AlgoTester();
	//tester.testRadix();
	tester.verifyParallelRadix();
    } // end main

    /**
     * Perform empirical analysis of the parallel radix algorithm.
     */
    void testRadix() {
	if (testType == 1) {
	    fileInfo = "speedup1_radix" + Integer.toString(numDigits);
	} else if (testType == 2) {
	    fileInfo = "speedup2_radix" + Integer.toString(numDigits);
	}
	// Create the sizes to test the algo with.
	int maxExponent, nSizes;
	int[] sizes; // The problem sizes to be tested.
	maxExponent = (numDigits == 1) ? 7 : 8;

	// We test with one value for each logarithmic "step" and one between
	// each step.
	nSizes = maxExponent + (maxExponent - 1);
	sizes = new int[nSizes];

	// Fill the sizes array.
	int sizeInd = 0;
	for (int i = 1; i < maxExponent; i++) {
	    sizes[sizeInd++] = (int) Math.pow(10, i); 
	    sizes[sizeInd++] = ((int) Math.pow(10, i+1) - (int) Math.pow(10, i))/2;
	}

	sizes[sizeInd] = (int) Math.pow(10, maxExponent);

	// Median running times.
	double[] tPar = new double[sizes.length];
	double[] tSeq = new double[sizes.length];  
	RadixAlgo seqAlgo = new SequentialRadix();
	RadixAlgo parAlgo = new ParallelRadix();

	for (int i = sizes.length - 1; i >= 0; i--) {
	    int s = sizes[i];
	    if (testType == 1) {
		tSeq[i] = testAlgo(seqAlgo, s, 1);
	    } else if (testType == 2) {
		tSeq[i] = testAlgo(seqAlgo, s);
	    }
	    tPar[i] = testAlgo(parAlgo, s);
	}
	printSpeedup(tSeq, tPar, sizes);
    } // end testRadix

    /**
     * Verify that the parallel radix algorithm sorts the array
     * correctly.
     */
    void verifyParallelRadix() {
	int[] a = new int[1000];
	fillArray(a);
	ParallelRadix par = new ParallelRadix();
	par.radix(a, numDigits);
	verifySorted(a, true);
    } // end verifyParallelRadix

    /**
     * Calculate and print speedup. Write speedup to file if TOFILE is true.
     * @param tSeq: The times for a sequential algorithm.
     * @param tPar: The times for a parallel algorithm.
     * @param sizes: The tested input sizes in ascending order.
     */
    void printSpeedup(double[] tSeq, double[] tPar, int[] sizes) {
	System.out.println("++ Speedup ++");
	System.out.println("Nr of digits: " + numDigits + "\n" +
			   "Max value in a: n");
	
	System.out.format("%-11s%-10s\n",
			  "Array size", "Speedup");

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
	    String folder = "../Kjoretider/Radix/";
	    String filename1 = folder + fileInfo + "_s_" +
		new Integer(nProcs).toString() + ".txt";
	    String filename2 = folder + fileInfo + "_n" + ".txt";
	    w.printToFile(filename1, speedups);
	    w.printToFile(filename2, sizes);
	    System.out.println("Sizes and speedups are written to file.");
	}
    } // end printSpeedup.

    /**
     * Run the algorithm for a given size NTESTS times.
     * @param algo: An implementation of the RadixAlgo interface.
     * @param size: The input size to test the algorithm with.
     * @return: The median value of the running times.
     */
    double testAlgo(RadixAlgo algo, int size) {
	int[] a;
	double[] times = new double[NTESTS];
	long startTime, stopTime;
	double diff;

	for (int i = 0; i < NTESTS; i++) {
	    a = new int[size];
	    fillArray(a, size);  // (size - 1) is the max value in a.

	    // Measure the running time.
	    startTime = System.nanoTime();
	    algo.radix(a, numDigits);
	    stopTime = System.nanoTime();
	    verifySorted(a, false);
	    
	    // Get the difference in ms.
	    diff = (stopTime - startTime)/1000000.0;
	    times[i] = diff;
	}
	return getMedian(times);
    } // end testAlgo

    /**
     * Run the algorithm for a given size NTESTS times.
     * @param algo: An implementation of the RadixAlgo interface.
     * @param size: The input size to test the algorithm with.
     * @numDigits: The number of digits to sort with.
     * return: The median value of the running times.
     */
    double testAlgo(RadixAlgo algo, int size, int numDigits) {
	int[] a;
	double[] times = new double[NTESTS];
	long startTime, stopTime;
	double diff;

	for (int i = 0; i < NTESTS; i++) {
	    a = new int[size];
	    fillArray(a, size);  // (size - 1) is the max value in a.

	    // Measure the running time.
	    startTime = System.nanoTime();
	    algo.radix(a, numDigits);
	    stopTime = System.nanoTime();
	    verifySorted(a, false);
	    
	    // Get the difference in ms.
	    diff = (stopTime - startTime)/1000000.0;
	    times[i] = diff;
	}
	return getMedian(times);
    } // end testAlgo

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
     * Fill an array with random values betweeen
     * 0 and 10 mill.
     * @param a: The array to be filled.
     */
    void fillArray(int[] a) {
	Random random = new Random();
	int maxVal = 10000000;  // 10 mill
	for (int i = 0; i < a.length; i++) {
	    a[i] = random.nextInt(maxVal);
	}
    } // end fillArray

    /**
     * Fill an array with random values between 0 
     * and a specified maximum value.
     * @param a: The array to be filled.
     * @param maxVal: Maximum value for an array element.
     */ 
    void fillArray(int[] a, int maxVal) {
	Random random = new Random();
	for (int i = 0; i < a.length; i++) {
	    a[i] = random.nextInt(maxVal);
	}
    } // end fillArray

    /**
     * Verify that an array is sorted.
     * @param a: The array.
     * @param verbose: If true, print feedback to user.
     */
    void verifySorted(int[] a, boolean verbose) {
	if (verbose) {
	System.out.println("-- Verify that the array is sorted.");
	}

	for (int i = 1; i <= a.length - 1; i++) {
	    if (a[i-1] > a[i]) {
		System.out.println("Feil!" + "\n" +
				   "a[" + (i-1) + "] = " + a[i-1] +
				   ", a[" + i + "] = " + a[i]);
		System.exit(1);
	    }
	}
	if (verbose) {
	    System.out.println("-- Success");
	}
    } // end verifySorted
} // end class AlgoTester
