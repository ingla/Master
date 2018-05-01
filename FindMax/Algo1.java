import java.util.concurrent.*;

/**
 * Wilkinson's parallel algorithm for finding the largest element
 * in an array.
 */
class Algo1 implements FindMax {
    CyclicBarrier barrier;
    int max;

    /**
     * Find the maximum element in an array.
     * @param a: The array.
     * @return: The maximum element.
     */
    public int getMax(int[] a) {
	max = -1; // Reset max from last call.
	int n = a.length;
        boolean m[] = new boolean[n];
	
	barrier = new CyclicBarrier(n);
	// Start worker threads.
	Thread[] workerThreads = new Thread[n];
	Thread tmp;
	for (int i = 0; i < n; i++) {
	    tmp = new Thread(new P_i(i, a, m));
	    tmp.start();
	    workerThreads[i] = tmp;
	}

	// Wait for worker threads to finish.
	for (int i = 0; i < workerThreads.length; i++) {
	    try {
		workerThreads[i].join();
	    } catch (InterruptedException e) {
		System.out.println("getMax: " + e.toString());
		System.exit(0);
	    }
	}
	return max;
    } // end getMax

    /**
     * Return a string identifying the algorithm.
     */
    public String toString() {
	return "Wilkinson's algorithm";	
    }

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

    /**
     * Thread class for P_i where 0 <= i < n.
     */
    class P_i implements Runnable {
	int i;
	int[] a;
	int n;
	boolean[] m;

	P_i(int i, int[] a, boolean[] m) {
	    this.i = i;
	    this.a = a;
	    n = a.length;
	    this.m = m;
	}

	public void run() {
	    m[i] = true;
	    waitByBarrier(barrier);  // Synch

	    Thread[] myThreads = new Thread[n-1];
	    Thread tmp;
	    int ind = 0;
	    for (int j = 0; j < n; j++) {
		if (j != i) {
		    tmp = new Thread(new P_j(j));
		    tmp.start();
		    myThreads[ind++] = tmp;
		}
	    }

	    // Wait for threads P_j to finish.
	    for (int i = 0; i < myThreads.length; i++) {  // Synch
		try {
		    myThreads[i].join();
		} catch (InterruptedException e) {
		    System.out.println(e.toString());
		    System.exit(0);
		}
	    }

	    if (m[i]) {
		max = a[i];
	    }
	} // end run

	/**
	 * Thread class for P_j where 0 <= i < n.
	 */ 
	class P_j implements Runnable {
	    int j;
	    
	    P_j(int j) {
		this.j = j;
	    }

	    public void run() {
		if (a[i] < a[j]) {
		    m[i] = false;
		}
	    }
	} // end inner class P_j
    } // end inner class P_i
} // end class Algo1
