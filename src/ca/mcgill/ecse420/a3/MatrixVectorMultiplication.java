package ca.mcgill.ecse420.a3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class MatrixVectorMultiplication {
    private static int matrixSize = 4000;
    private static int numberThreads = 30;

    public static void main(String[] args) {
        double[][] a = generateRandomMatrix(matrixSize, matrixSize);
        double[] v = generateRandomVector(matrixSize);
        System.out.println("Starting matrix multiplication with " + numberThreads + " threads and matrix size of " + matrixSize + "x" + matrixSize);

        System.out.println("\nSequential matrix vector multiplication started.");
        sequentialMatrixVectorMultiply(a, v);

        System.out.println("\nParallel matrix vector multiplication started.");
        parallelMultiplyMatrixVector(a, v);

        System.out.println("\nExecution time comparison started.");
        compareExecutionTime(a, v);

        System.out.println("\nTest matrix vector multiplication");
        verifyParallelExecutionResult(a, v);
        verifySequentialExecutionResults();
    }

    public static double[] sequentialMatrixVectorMultiply(double[][] a, double[] v){
        double[] result = new double[matrixSize];
        for (int row = 0; row < matrixSize; row++){
            double sum = 0.0;
            for (int col = 0; col < matrixSize; col++) {
                sum += a[row][col] * v[col];
            }
            result[row] = sum;
        }
        return result;
    }

    public static double[] parallelMultiplyMatrixVector(double[][] a, double[] v) {
        ForkJoinPool pool = new ForkJoinPool(numberThreads);    // create a ForkJoinPool with specified # of processors
        double[] result = new double[matrixSize];
        RecursiveAction mainTask = new MultiplyTask(a, v, result, 0, matrixSize);
        pool.invoke(mainTask);
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

    public static void verifyParallelExecutionResult(double[][] a, double[] v){
        double[] seq_results = sequentialMatrixVectorMultiply(a, v);
        double[] parallel_results = parallelMultiplyMatrixVector(a, v);

        boolean passed = assertArrayEquals(seq_results, parallel_results);
        System.out.println("Parallel execution validation test results: " + (passed ? "Verification passed." : "Verification failed."));
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

    private static class MultiplyTask extends RecursiveAction{
        private static final int THRESHOLD = matrixSize / 30; // threshold for splitting the task into smaller tasks

        private final double[][] a;
        private final double[] v;
        private final double[] result;
        private final int startRow;
        private final int endRow;

        MultiplyTask(double[][] a, double[] v, double[] result, int startRow, int endRow) {
            this.a = a;
            this.v = v;
            this.result = result;
            this.startRow = startRow;
            this.endRow = endRow;
        }

        @Override
        protected void compute() {
            int rowCount = endRow - startRow;
            if (rowCount < THRESHOLD) { // base case: if the number of rows is less than the threshold, perform sequential multiplication
                int cols_A = a[0].length;
                for (int row = startRow; row < endRow; row++) {
                    double sum = 0;
                        for (int k = 0; k < cols_A; k++) {
                            sum += a[row][k] * v[k];
                        }
                        result[row] = sum;
                }
            }
            else {
                int mid = startRow + rowCount / 2;
                MultiplyTask task1 = new MultiplyTask(a, v, result, startRow, mid);
                MultiplyTask task2 = new MultiplyTask(a, v, result, mid, endRow);
                invokeAll(task1, task2);
            }
        }
    }

    /**
     * Prints the execution time of sequential and parallel matrix multiplication for the given matrices.
     * @param a matrix a
     * @param v vector v
     */
    private static void compareExecutionTime(double[][] a, double[] v) {
        long startTime = System.currentTimeMillis();
        System.out.println("\n[SEQUENTIAL] Starting sequential matrix multiplication at time " + startTime);
        sequentialMatrixVectorMultiply(a, v);
        long endTime = System.currentTimeMillis();
        System.out.println("[SEQUENTIAL] Ending sequential matrix multiplication at time " + endTime);
        System.out.println("\n[SEQUENTIAL] Sequential time is " + (endTime - startTime) + " ms");

        startTime = System.currentTimeMillis();
        System.out.println("\n[PARALLEL] Starting parallel matrix multiplication at time " + startTime);
        parallelMultiplyMatrixVector(a, v);
        endTime = System.currentTimeMillis();
        System.out.println("[PARALLEL] Ending parallel matrix multiplication at time " + endTime);
        System.out.println("\n[PARALLEL] Parallel time with " + numberThreads + " processors is " + (endTime - startTime) + " ms");
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


