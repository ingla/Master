import java.util.concurrent.*;

class Algo1_nThreads implements FindMax {
    CyclicBarrier barrier;
    int max = -1;

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
    } // end findMax

    /**
     * Return a string identifying the algorithm.
     */
    public String toString() {
	return "Wilkinson's algorithm: n threads";
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

	    for (int j = 0; j < n; j++) { 
		if (a[i] < a[j]) {
		    m[i] = false;
		}
	    }

	    if (m[i]) {
		max = a[i];
	    }
	} // end run
    } // end class P_i
} // end class Algo1_nThreads
