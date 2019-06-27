package ru.javaops.masterjava.matrix;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    // parallel mechanism using streams
    //based  2nd approach is to combine transposition and its usage
    public static int[][] concurrentMultiply3(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        List<Callable<Void>> tasks = IntStream.range(0, matrixSize)
                .parallel()
                .mapToObj(i -> new Callable<Void>() {
                    final int[] thatColumn = new int[matrixSize];

                    @Override
                    public Void call() throws Exception {
                        //get column as a row
                        for (int k = 0; k < matrixSize; k++) {
                            thatColumn[k] = matrixB[k][i];
                        }
                        //calculate
                        for (int j = 0; j < matrixSize; j++) {
                            int thisRow[] = matrixA[j];
                            int sum = 0;
                            for (int k = 0; k < matrixSize; k++) {
                                sum += thisRow[k] * thatColumn[k];
                            }
                            matrixC[j][i] = sum;
                        }
                        return null;
                    }
                }).collect(Collectors.toList());
        executor.invokeAll(tasks);
        return matrixC;
    }

    
    //Multithreaded version with CompletionService
    //of multiplying matrices using 2nd approach where we take explicitly row and column
    public static int[][] concurrentMultiply2(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;

        //internal class
         class ColumnMultipleResult {
             final int col;
             final int[] columnC;

             public ColumnMultipleResult(int col, int[] columnC) {
                 this.col = col;
                 this.columnC = columnC;
             }
         }

        CompletionService<ColumnMultipleResult> completionService = new ExecutorCompletionService<>(executor);

        for (int j = 0; j < matrixSize; j++) {
            final int col = j;
            final int[] columnB = new int[matrixSize];
            for (int k = 0; k < matrixSize; k++) {
                columnB[k] = matrixB[k][col];
            }
            //getting calculated columns
            completionService.submit(() -> {
                final int[] columnC = new int[matrixSize];
                for (int row = 0; row < matrixSize; row++) {
                    final int[] rowA = matrixA[row];
                    int sum = 0;
                    for (int k = 0; k < matrixSize; k++) {
                        sum += rowA[k] * columnB[k];
                    }
                    columnC[row] = sum;
                }
                return new ColumnMultipleResult(col, columnC);
            });
        }

        final int[][] matrixC = new int[matrixSize][matrixSize];
        //need to fill result matrix
        for (int i = 0; i < matrixSize; i++) {
            ColumnMultipleResult res = completionService.take().get();
            for (int k = 0; k < matrixSize; k++) {
                matrixC[k][res.col] = res.columnC[k];
            }
        }

        return matrixC;
    }


    //Bad!
    //Parallel implementation basid using only last loop
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        ExecutorCompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        int[][] transpMatrixB = new int[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                transpMatrixB[j][i] = matrixB[i][j];
            }
        }
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                final int ii = i;
                final int jj = j;
                int sum = 0;
                List<Future<Integer>> futures = IntStream.range(0, matrixSize)
                        .mapToObj(k -> completionService.submit(() -> matrixA[ii][k] * transpMatrixB[jj][k]))
                        .collect(Collectors.toList());
                while (!futures.isEmpty()) {
                    Future<Integer> complFuture = completionService.poll(5, TimeUnit.SECONDS);
                    if (complFuture != null) {
                        sum += complFuture.get();
                        futures.remove(complFuture);
                    }
                }
                matrixC[i][j] = sum;
            }
        }

        return matrixC;
    }

    
    //basic approach O(n^3)
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += matrixA[i][k] * matrixB[k][j];
                }
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    //1st approach - transposition matrix
    //it allows us to use L1 cache - matrix row is cached (very fast memory call)
    public static int[][] singleThreadMultiply2(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        //1st approach - transposition matrix
        //it allows us to use L1 cache - matrix row is cached (very fast memory call)
        int[][] transpMatrixB = new int[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                transpMatrixB[j][i] = matrixB[i][j];
            }
        }

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += matrixA[i][k] * transpMatrixB[j][k];
                }
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    //2nd approach is to combine
    public static int[][] singleThreadMultiply3(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        int thatColumn[] = new int[matrixSize];

        for (int j = 0; j < matrixSize; j++) {
            for (int k = 0; k < matrixSize; k++) {
                thatColumn[k] = matrixB[k][j];
            }

            for (int i = 0; i < matrixSize; i++) {
                int thisRow[] = matrixA[i];
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += thisRow[k] * thatColumn[k];
                }
                matrixC[i][j] = sum;
            }
        }

        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}


