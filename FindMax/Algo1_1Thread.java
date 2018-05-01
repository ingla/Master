import java.util.concurrent.*;

class Algo1_1Thread implements FindMax {
    CyclicBarrier barrier;
    int max;

    /**
     * Find the maximum element in an array.
     * @param a: The array.
     * @return: Max
     */
    public int getMax(int[] a) {
	max = -1; // Reset max from last call.
	int n = a.length;
	boolean m[] = new boolean[n];
	
	barrier = new CyclicBarrier(n);
	// Start worker threads.
	Thread workerThread = new Thread(new WorkerThread(a, m));
	workerThread.start();
	try {
	    workerThread.join();
	} catch (InterruptedException e) {
	    System.out.println("getMax: " + e.toString());
	    System.exit(0);
	}
	return max;
    } // end getMax

    /**
     * Return a string identifying the algorithm.
     */
    public String toString() {
	return "Wilkinson's algorithm: 1 thread";
    }
    
    /* Thread class */
    class WorkerThread implements Runnable {
	int[] a;
	int n;
	boolean[] m;

	WorkerThread(int[] a, boolean[] m) {
	    this.a = a;
	    n = a.length;
	    this.m = m;
	}

	public void run() {
	    for (int i = 0; i < n; i++) {
		m[i] = true;
	    }

	    for (int i = 0; i < n; i++) {
		for (int j = 0; j < n; j++) {
		    if (a[i] < a[j]) {
			m[i] = false;
		    }
		}
	    }

	    for (int i = 0; i < n; i++) {
		if (m[i]) {
		    max = a[i];
		}
	    }
	} // end run
    } // end inner class WorkerThread
} // end Algo1_1Thread
