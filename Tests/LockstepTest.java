import java.util.concurrent.*;

/**
 * The program tests whether processors do run in lockstep when performing
 * exactly the same instructions.
 */
class LockstepTest {
    
    int n = 5;
    int[] a = new int[n];
    int[] b = new int[n];
    CyclicBarrier bar = new CyclicBarrier(n);

    public static void main(String[] args) {
	LockstepTest t = new LockstepTest();
	t.runTest();
    }

    void runTest() {
	Thread[] myThreads;

	// Print initial values in array a.
	System.out.println("Initial values in input array 'a':");
	for (int i = 0; i < n; i++) {
	    System.out.print(a[i] + " ");
	}
	System.out.println();

	// Create worker threads.
	myThreads = new Thread[n];
	Thread t = null;
	for (int i = 0; i < n; i++) {
	    t = new Thread(new WorkerThread(i));
	    myThreads[i] = t;
	}

	// Start up worker threads.
	for (int i = 0; i < n; i++) {
	    myThreads[i].start();
	}
	
	// Wait for worker threads to finish.
	for (int i = 0; i < n; i++) {
	    try {
		myThreads[i].join();
	    } catch (InterruptedException e) {
		System.out.println("runTest: " + e.toString());
		System.exit(0);
	    }
	}
	// Print result array b.
	System.out.println("Result array 'b':");
	for (int i = 0; i < n; i++) {
	    System.out.print(b[i] + " ");
	}
	System.out.println();
    } // end runTest

    /* Thread class. */
    class WorkerThread implements Runnable {
	int id;

	WorkerThread(int id) {
	    this.id = id;
	}

	public void run() {
	    // Wait for the other threads to start up.
	    try {
		bar.await(); 
	    } catch (Exception e) {
		System.out.println("run(): " + e.toString());
		System.exit(0);
	    }

	    int tmp;
	    tmp = a[id];
	    b[id] = tmp;
	    a[(id+1)%n]++;
	    
	} // end run
    } // end class WorkerThreads
} // end class LockstepTest
