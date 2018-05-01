/**
 * Implement the sequential algorithm for radix sort.
 */
class SequentialRadix implements RadixAlgo {
    public void radix(int[] a, int numDigits) {
	int max = a[0], numBits = 1, n = a.length;

	// a1) Find max value in a.
	for (int i = 1; i < n; i++) {
	    if (a[i] > max) max = a[i];
	}

	// a2) Find number of bits in max.
	while (max >> numBits != 0) {
	    numBits++;
	}
	int[] b = new int[n]; // Stores intermediary results.
	
	if (numDigits == 1) {
	    radixSort(a, b, numBits, 0);
	    copy(b, a);

	} else if (numDigits == 2) {
	    int bit1 = numBits/2;
	    int bit2 = numBits - bit1;
	    radixSort(a, b, bit1, 0);    // Sort on 1st digit.
	    radixSort(b, a, bit2, bit1); // Sort on 2nd digit.

	} else if (numDigits == 3) {
	    int bit1 = numBits/3;
	    int bit2 = bit1;
	    int bit3 = numBits - (bit1 + bit2);
	    radixSort(a, b, bit1, 0);    // Sort on 1st digit.
	    radixSort(b, a, bit2, bit1); // Sort on 2nd digit.	    
	    radixSort(a, b, bit3, (bit1 + bit2)); // Sort on 3rd digit.
	    copy(b, a);

	} else if (numDigits == 4) {
	    int bit1 = numBits/4;
	    int bit4 = numBits - (3*bit1);
	    radixSort(a, b, bit1, 0);    // Sort on 1st digit.
	    radixSort(b, a, bit1, bit1); // Sort on 2nd digit.	    
	    radixSort(a, b, bit1, (2*bit1)); // Sort on 3rd digit.
	    radixSort(b, a, bit4, (3*bit1)); // Sort on 4th digit.
	}
    } // end radix

    /**
     * Sort the array with respect to a specified digit.
     * @param a: The array to sort.
     * @param b: The result array.
     * @param maskLen: Number of bits in the digit to sort on.
     * @param shift: Number of shifts in order to obtain the digit
     * to sort on.
     */
    void radixSort(int[] a, int[] b, int maskLen, int shift) {
	int n = a.length, acumVal = 0;
	int mask = (1 << maskLen) - 1;   // The last 'maskLen' bits set to 1.
	int[] count = new int[mask + 1];

	// b) Count = the frequency of each radix value in a.
	for (int i = 0; i < n; i++) {
	    count[(a[i] >> shift) & mask]++;
	}

	// c) Accumulate the frequencies in 'count'.
	int tmp;
	for (int i = 0; i <= mask; i++) {
	    tmp = count[i];
	    count[i] = acumVal;
	    acumVal += tmp;
	}

	// d) Move numbers in sorted order from a to b.
	for (int i = 0; i < n; i++) {
	    b[count[(a[i]>>shift) & mask]++] = a[i];
	}
    } // end radixSort

    /**
     * Copy the thread's part of the array from one array to
     * another.
     */
    void copy(int[] from, int[] to) {
	int n = from.length;
	for (int i = 0; i < n; i++) {
	    to[i] = from[i];
	}
    } // end copy
} // end class SequentialRadix
