package ca.mcgill.ecse420.a3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MatrixVectorMultiplication {
    private static int matrixSize = 3;
    private static int numberThreads = 30;

    public static void main(String[] args) {
        double[][] a = generateRandomMatrix(matrixSize, matrixSize);
        System.out.println("\nGenerated matrix: " + Arrays.deepToString(a));
        double[] v = generateRandomVector(matrixSize);
        System.out.println("\n Generated Vector: " + Arrays.toString(v));
        System.out.println("Starting matrix multiplication with " + numberThreads + " threads and matrix size of " + matrixSize + "x" + matrixSize);

        System.out.println("\nSequential matrix vector multiplication started.");
        double[] result = sequentialMatrixVectorMultiply(a, v);
        System.out.println("\n Sequential matrix vector multiplication result: " + Arrays.toString(result));


        System.out.println("\nTest sequential matrix vector");
        verifySequentialExecutionResults();
    }

    public static double[] sequentialMatrixVectorMultiply(double[][] a, double[] v){
        double[] result = new double[matrixSize];
        for (int row = 0; row < matrixSize; row++){
            System.out.println("row: "+row);
            double sum = 0.0;
            for (int col = 0; col < matrixSize; col++) {
                sum += a[row][col] * v[col];
            }
            result[row] = sum;
        }
        return result;
    }

    /**
     * Validates te result of sequential matrix vector execution against known correct result
     * */
    public static void verifySequentialExecutionResults() {
        int originalMatrixSize = matrixSize;

        // test 1
        matrixSize = 2;
        double[][] a1 = { {1.0, 2.0}, {3.0, 4.0} };
        double[] v1 = {5.0, 3.0};
        double[] expected1 = {11.0, 27.0};
        double[] actual1 = sequentialMatrixVectorMultiply(a1, v1);
        boolean passed1 = assertArrayEquals(expected1, actual1);

        // test 2
        matrixSize = 3;
        double[][] identity = { {1.0, 0.0, 0.0}, {0.0, 1.0, 0.0}, {0.0, 0.0, 1.0} };
        double[] v2 = {3.0, 5.0, 8.0};
        double[] expected2 = {3.0, 5.0, 8.0};
        double[] actual2 = sequentialMatrixVectorMultiply(identity, v2);
        boolean passed2 = assertArrayEquals(expected2, actual2);

        // test 3
        matrixSize = 2;
        double[][] a3 = { {1, 2}, {3, 4} };
        double[] zero = {0, 0};
        double[] expectedZero = {0, 0};
        double[] actual3 = sequentialMatrixVectorMultiply(a3, zero);
        boolean passed3 = assertArrayEquals(expectedZero, actual3);

        // restore matrix size to original value
        matrixSize = originalMatrixSize;
        System.out.println("Sequential execution validation test results: " + (passed1 && passed2 && passed3 ? "All tests passed." : "Some tests failed."));
    }

    private static boolean assertArrayEquals(double[] expected, double[] actual){
        if (expected == null || actual == null || expected.length != actual.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Populates a matrix of given size with randomly generated integers between 0-10.
     * @param numRows number of rows
     * @param numCols number of cols
     * @return matrix
     */
    private static double[][] generateRandomMatrix (int numRows, int numCols) {
        double[][] matrix = new double[numRows][numCols];
        for (int row = 0 ; row < numRows ; row++ ) {
            for (int col = 0 ; col < numCols ; col++ ) {
                matrix[row][col] = (double) ((int) (Math.random() * 10.0));
            }
        }
        return matrix;
    }

    /**
     * Populates a matrix of given size with randomly generated integers between 0-10.
     * @param size number of rows
     * @return vector
     */
    private static double[] generateRandomVector (int size) {
        double[] vector = new double[size];
        for (int i = 0 ; i < size ; i++ ) {
            vector[i] = (double) ((int) (Math.random() * 10.0));
        }
        return vector;
    }
}


