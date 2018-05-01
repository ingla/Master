/**
 * Implement the sequential solution.
 */
class SeqAlgo implements FindMax {

    /**
     * Find the maximum element in an array.
     * @param a: The array.
     * @return: Max
     */
    public int getMax(int[] a) {
	int maximum = a[0];
	int n = a.length;

	for (int i = 1; i < n; i++) {
	    if (a[i] > maximum) {
		maximum = a[i];
	    }
	}
	return maximum;
    } // end getMax

    /**
     * Return a string identifying the algorithm.
     */
    public String toString() {
	return "Sequential solution";
    }
} // end SeqSolution
