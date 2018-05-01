import java.util.concurrent.*;

/**
 * Implement the parallel algorithm for radix sort.
 */
class ParallelRadix implements RadixAlgo {
    int numThreads;
    CyclicBarrier synchBar;
    int[] localMaxArray;    // Used for step a: find max.
    int[][] allCount;       // Used for step b and c.

    public static void main(String[] args) {
	// Simple test.
	ParallelRadix pr = new ParallelRadix();
	int[] a = {10, 3, 1, 4, 8};
	pr.radix(a, 3);
	
	for (int e : a) {
	    System.out.print(e + " ");
	}
	System.out.println();
    } // end main

    ParallelRadix() {
	numThreads = Runtime.getRuntime().availableProcessors();
	if (numThreads == 64) {
	    // NB: Hardcode the nr of physical cores (this particular machine
	    // is using hyperthreading with 2 logical cores per physical core).
	    numThreads = 32;
	}	
	System.out.println("numThreads = " + numThreads);
	synchBar = new CyclicBarrier(numThreads);
	localMaxArray = new int[numThreads];
	allCount = new int[numThreads + 1][];
    }
    
    /**
     * Start the threads which will be performing radix sort.
     * @param a: The array to be sorted.
     * @param numDigits: Number of digits to sort the elements with.
     */
    public void radix(int[] a, int numDigits) {
	int n = a.length;
	int len = n/numThreads;
	// Delimit each thread's part of the array.
	int startInd = 0, stopInd;
	Thread[] myThreads = new Thread[numThreads];
	Thread t;
	int[] b = new int[n];

	// Start all threads but the last.
	int i;
	for (i = 0; i < numThreads - 1; i++) {
	    stopInd = startInd + len;
	    t = new Thread(new Para(i, a, b, startInd, stopInd, numDigits));
	    t.start();
	    myThreads[i] = t;
	    startInd = stopInd;
	}
	// Start the last thread.
	t = new Thread(new Para(i, a, b, startInd, n, numDigits));
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
    } // end radix

    /* Thread class */
    class Para implements Runnable {
	int id, startInd, stopInd;
	int[] a, b;
	int numDigits;

	Para(int id, int[] a, int[] b, int start, int stop, int numDigits) {
	    this.id = id;
	    this.a = a; this.b = b;
	    startInd = start; stopInd = stop;
	    this.numDigits = numDigits;
	}
	
	public void run() {
	    int max;
	    int numBits = 1, n = a.length;
	    
	    // a1) Find max value in a.
	    max = findMax();

	    // a2) Find number of bits in max.
	    while (max >> numBits != 0) {
		numBits++;
	    }

	    if (numDigits == 1) {
		radixSort(a, b, numBits, 0);
		synch();
		copy(b, a);

	    } else if (numDigits == 2) {
		// Find number of bits in 1st and 2nd digit.
		int bit1 = numBits/2;
		int bit2 = numBits - bit1;
		
		radixSort(a, b, bit1, 0);    // Sort on 1st digit.
		synch();
		radixSort(b, a, bit2, bit1); // Sort on 2nd digit.
	    
	    } else if (numDigits == 3) {
		int bit1 = numBits/3;
		int bit2 = bit1;
		int bit3 = numBits - (2*bit1);
		radixSort(a, b, bit1, 0);    // Sort on 1st digit.
		synch();
		radixSort(b, a, bit2, bit1); // Sort on 2nd digit.
		synch();
		radixSort(a, b, bit3, (bit1 + bit2));  // Sort on 3rd digit.
		synch();
		copy(b, a);

	    } else if (numDigits == 4) {
		int bit1 = numBits/4;
		int bit4 = numBits - (3*bit1);
		radixSort(a, b, bit1, 0);     // Sort on 1st digit.
		synch();
		radixSort(b, a, bit1, bit1);  // Sort on 2nd digit.
		synch();
		radixSort(a, b, bit1, (2*bit1)); // Sort on 3rd digit.
		synch();
		radixSort(b, a, bit4, (3*bit1)); // Sort on 4th digit.
	    }
	} // end run.

	/**
	 * Sort the array with respect to a specified digit.
	 * @param a: The array to sort.
	 * @param b: The result array.
	 * @param maskLen: Number of bits in the digit to sort on.
	 * @param shift: Number of shifts in order to obtain the digit
	 * to sort on.
	 */
	void radixSort(int[] a, int[] b, int maskLen, int shift) {
	    int n = a.length;
	    int mask = (1 << maskLen) - 1; // The last 'maskLen' bits set to 1.
	    int[] count = new int[mask+1];

	    /* b) count = frequency of each radix value in the thread's part 
	       of a. */
	    for (int i = startInd; i < stopInd; i++) {
		count[(a[i] >> shift) & mask]++;
	    }
	    allCount[id] = count;
	    // The last row will store the sums of the columns.
	    if (id == 0) allCount[numThreads] = new int[mask+1];
	    synch();

	    /* c) Accumulate the frequencies in 'count'. */
	    count = stepC(mask + 1);

	    /* d) Move numbers in sorted order from a to b. */
	    for (int i = startInd; i < stopInd; i++) {
		b[count[(a[i]>>shift) & mask]++] = a[i];
	    }
	} // end radixSort
	
	/**
	 * Step a) Find the maximum element in array 'a'.
	 */
	int findMax() {
	    // Find local max.
	    int max = a[startInd];
	    for (int i = startInd + 1; i < stopInd; i++) {
		if (a[i] > max) {
		    max = a[i];
		}
	    }
	    localMaxArray[id] = max;
	    synch();
	    
	    // Find global max.
	    for (int i = 0; i < localMaxArray.length; i++) {
		if (localMaxArray[i] > max) {
		    max = localMaxArray[i];
		}
	    }
	    return max;
	} // end findMax

	/**
	 * C) Accumulate the frequencies in 'allCount'.
	 * @param size: The length of the rows in 'allCount'.
	 * @return: the 'count' array to be used in step d).
	 */
	int[] stepC(int length) {
	    int acumVal = 0;
	    int numCols; // per thread
	    int startCol, stopCol;
	    int[] count = new int[length];

	    // Divide the columns among the threads.
	    numCols = length/numThreads;
	    startCol = id*numCols;
	    stopCol = (id == numThreads - 1) ? length : startCol + numCols;

	    // Accumulate the values in each column. Store the
	    // sum in the last row of the column.
	    int tmp;
	    for (int i = startCol; i < stopCol; i++) {
		int j;
		for (j = 0; j < numThreads; j++) {
		    tmp = allCount[j][i];
		    allCount[j][i] = acumVal;
		    acumVal += tmp;
		}
		allCount[numThreads][i] = acumVal; // Store sum.
		acumVal = 0;
	    }	    
	    synch();

	    // Calculate values of 'count' using 'allCount'.
	    count[0] = allCount[id][0];
	    acumVal = allCount[numThreads][0];
	    for (int i = 1; i < length; i++) {
		count[i] = acumVal + allCount[id][i];
		acumVal += allCount[numThreads][i];
	    }
	    return count;
	} // end stepC

	/**
	 * Copy the thread's part of the array from one array to
	 * another.
	 */
	void copy(int[] from, int[] to) {
	    for (int i = startInd; i < stopInd; i++) {
		to[i] = from[i];
	    }
	} // end copy

	/**
	 * Wait by a synchronization barrier.
	 */
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
	} // end synch
    } // end inner class Para
} // end class ParallelRadix
