import java.io.*;

/**
 * A class for writing the results of empirical analysis of an
 * algorithm to file.
 */
class MyFileWriter {
    public static void main(String[] args) {
	MyFileWriter writer = new MyFileWriter();
	double[] a = {2.0, 2.3, 4.5};
	writer.printToFile("../Kjoretider/test.txt", a);
    }

    /**
     * Print an array of speedups to file.
     * @param filename: A string identifying the parallel algorithm.
     * @param speedup: An array of speedup for a number of input sizes.
     */ 
    void printToFile(String filename, double[] speedup) {
	OutputStream file = null;

	try {
	    file = new FileOutputStream(filename, false);
	} catch(FileNotFoundException e) {
	    System.out.println("printToFile: " + e.toString());
	    System.exit(0);
	}
	
	PrintWriter writer = new PrintWriter(file);
	for (int i = 0; i < speedup.length; i++) {
	    writer.print(speedup[i]);
	    writer.print(' ');
	}
	writer.println();
	writer.close();
    } // end printToFile

    /**
     * Print an array of input sizes to file.
     * @param filename: A string identifying the parallel algorithm.
     * @param sizes: The input sizes.
     */
    void printToFile(String filename, int[] sizes) {
	OutputStream file = null;

	try {
	    file = new FileOutputStream(filename, false);
	} catch(FileNotFoundException e) {
	    System.out.println("printToFile: " + e.toString());
	    System.exit(0);
	}
	
	PrintWriter writer = new PrintWriter(file);
	for (int i = 0; i < sizes.length; i++) {
	    writer.print(sizes[i]);
	    writer.print(' ');
	}
	writer.println();
	writer.close();	
    } // end printToFile
} // end class FileWriter
