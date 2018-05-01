import java.util.concurrent.*;

/**
 * Implements the find max algorithm.
 */
class Algo2 implements FindMax {
    int nThreads;
    CyclicBarrier barrier;
    int[] localMaxArray;
    int globalMax;
    int[] a;
    
    Algo2() {
	// Set nr of threads = nr of cores on the machine.
	nThreads = Runtime.getRuntime().availableProcessors();
	if (nThreads == 64) {
	    // NB: Hardcode the nr of physical cores (this particular machine
	    // is using hyperthreading with 2 logical cores per physical core).
	    nThreads = 32;
	}
	System.out.println("Algo 2: nThreads = " + nThreads);

	localMaxArray = new int[nThreads];
	barrier = new CyclicBarrier(nThreads);
    }

    /**
     * Find the maximum element in an array.
     * @param a: The array.
     * @return: Max
     */
    public int getMax(int[] a) {
	globalMax = -1; // Reset max from last call.
	// Delimit each thread's part of the array.
	int startInd = 0, stopInd;
	int n = a.length;
	int len = n/nThreads;
	Thread[] myThreads = new Thread[nThreads];
	Thread t;
	int i;
	for (i = 0; i < nThreads - 1; i++) {
	    stopInd = startInd + len;
	    t = new Thread(new Para(i, a, startInd, stopInd));
	    t.start();
	    myThreads[i] = t;
	    startInd = stopInd;
	}
	
	// Start the last thread.
	t = new Thread(new Para(i, a, startInd, n));
	t.start();
	myThreads[i] = t;

	// Wait for threads to finish.
	for (i = 0; i < nThreads; i++) {
	    try {
		myThreads[i].join();
	    } catch (InterruptedException e) {
		System.out.println(e.toString());
		System.exit(1);
	    }
	}
	return globalMax;
    } // end getMax	
	
    /**
     * The calling thread waits by a synchronization barrier.
     * @param b: The barrier.
     */
    void waitByBarrier(CyclicBarrier b) {
	try {
	    b.await();
	} catch (InterruptedException e) {
	    System.out.println("InterruptedException: " +
			       e.getMessage());
	    System.exit(1);
	} catch (BrokenBarrierException e) {
	    System.out.println("BrokenBarrierException: " +
			       e.getMessage());
	    System.exit(1);
	}	
    } // end waitByBarrier   

    /* Thread class */
    class Para implements Runnable {
	int id, startInd, stopInd;
	int[] a;
	
	Para(int id, int[] a, int start, int stop) {
	    this.id = id;
	    this.a = a;
	    startInd = start; stopInd = stop;
	}

	public void run() {
	    // Find local max.
	    int localMax = a[startInd];
	    for (int i = startInd + 1; i < stopInd; i++) {
		if (a[i] > localMax) {
		    localMax = a[i];
		}
	    }
	    localMaxArray[id] = localMax;
	    waitByBarrier(barrier);

	    // Find global max.
	    if (id == 0) {
		for (int i = 0; i < localMaxArray.length; i++) {
		    if (localMaxArray[i] > localMax) {
			localMax = localMaxArray[i];
		    }
		}
		globalMax = localMax;
	    }
	} // end run
    } // end class Para

    /**
     * Return a string identifying the algorithm.
     */
    public String toString() {
	return "A more realistic \"Find max\" algorithm";
    } 
} // end Algo2
