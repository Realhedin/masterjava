package ru.javaops.masterjava.matrix;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * gkislin
 * 03.07.2016
 */
public class MainMatrix {
    private static final int MATRIX_SIZE = 1000;
    private static final int THREAD_NUMBER = 10;

    private final static ExecutorService executor = Executors.newFixedThreadPool(MainMatrix.THREAD_NUMBER);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final int[][] matrixA = MatrixUtil.create(MATRIX_SIZE);
        final int[][] matrixB = MatrixUtil.create(MATRIX_SIZE);

        double singleThreadSum = 0.;
        double singleThreadSum2 = 0.;
        double singleThreadSum3 = 0.;
        double concurrentThreadSum = 0.;
        int count = 1;
        while (count < 6) {
            System.out.println("Pass " + count);
            long start = System.currentTimeMillis();
            final int[][] matrixC = MatrixUtil.singleThreadMultiply(matrixA, matrixB);
            double duration = (System.currentTimeMillis() - start) / 1000.;
            out("Single thread time Basic, sec: %.3f", duration);
            singleThreadSum += duration;

            start = System.currentTimeMillis();
            final int[][] matrixC2 = MatrixUtil.singleThreadMultiply2(matrixA, matrixB);
            duration = (System.currentTimeMillis() - start) / 1000.;
            out("Single thread time Transp, sec: %.3f", duration);
            singleThreadSum2 += duration;

            start = System.currentTimeMillis();
            final int[][] matrixC3 = MatrixUtil.singleThreadMultiply3(matrixA, matrixB);
            duration = (System.currentTimeMillis() - start) / 1000.;
            out("Single thread time Transp in one loop, sec: %.3f", duration);
            singleThreadSum3 += duration;

            start = System.currentTimeMillis();
            final int[][] concurrentMatrixC = MatrixUtil.concurrentMultiply2(matrixA, matrixB, executor);
            duration = (System.currentTimeMillis() - start) / 1000.;
            out("Concurrent thread time, sec: %.3f", duration);
            concurrentThreadSum += duration;

            if (!MatrixUtil.compare(matrixC, concurrentMatrixC)) {
                System.err.println("Comparison failed");
                break;
            }
            count++;
        }
        executor.shutdown();
        out("\nAverage single thread time Basic, sec: %.3f", singleThreadSum / 5.);
        out("Average single thread time Transp, sec: %.3f", singleThreadSum2 / 5.);
        out("Average single thread time Transp in one loop, sec: %.3f", singleThreadSum3 / 5.);
        out("Average concurrent thread time, sec: %.3f", concurrentThreadSum / 5.);
    }

    private static void out(String format, double ms) {
        System.out.println(String.format(format, ms));
    }
}
