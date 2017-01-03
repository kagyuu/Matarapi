/*
 * Copyright 2017 HONDOH Atsushi.
 */
package jp.hondoh.matarapi;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Matrix calculate Utility.
 *
 * @author a.ho
 */
public final class MatUtil {

    public static int[] lu(Matrix m) throws InterruptedException, ExecutionException {
        final float[][] a = m.getData();
        final int colSize = m.getColSize();
        final int rowSize = m.getRowSize();

        int[] o = new int[colSize];
        for (int cnt = 0; cnt < colSize; cnt++) {
            o[cnt] = cnt;
        }

        ExecutorService ex = Executors.newCachedThreadPool();
        Queue<Future> fQueue = new LinkedList<>();
        try {
            for (int s = 0; s < colSize; s++) {
                final int ss = s;
                
                // === l[y][s] ===
                for (int y = ss; y < rowSize; y++) {
                    final int yy = y;
                    fQueue.add(ex.submit(() -> {
                        for (int k = 0; k < ss; k++) {
                            a[o[yy]][ss] -= a[o[yy]][k] * a[o[k]][ss];
                        }
                    }));                    
                }
                
                // === pivot select ===
                while (!fQueue.isEmpty()) {
                    (fQueue.poll()).get();
                }
                
                int maxLidx = -1;
                float maxL = 0.0f, tmp;
                for (int y = ss; y < rowSize; y++) {
                    tmp = Math.abs(a[o[y]][s]);
                    if (maxL < tmp) {
                        maxLidx = y;
                        maxL = tmp;
                    }
                }
                
                if (maxLidx < 0) {
                    // ie. l[i][i] == 0, in other words, Matrix m has no l and u strictly.
                    // But this method calculate an approximation on the assumption that
                    // l[i][i] would be Float.MIN_VALUE(1.401298464324817E-45f).
                    a[o[s]][s] = Float.MIN_VALUE;
                } else {
                    // found pivot row, pivot row is o[maxLidx].
                    // So, swap row o[s] and o[maxLidx].
                    // o[s] (former o[maxLidx]) row will be processed following
                    // "=== u[s][x] ===" section.
                    int t = o[s];
                    o[s] = o[maxLidx];
                    o[maxLidx] = t;
                }

                // === u[s][x] ===
                // to calculate u[s][x]
                // , it is needed to finish calculating u[s-1][x]
                // (x = s+1,s+2,...,n)
                for (int x = ss + 1; x < colSize; x++) {
                    final int xx = x;
                    fQueue.add(ex.submit(() -> {
                        for (int k = 0; k < ss; k++) {
                            a[o[ss]][xx] -= a[o[ss]][k] * a[o[k]][xx];
                        }
                        a[o[ss]][xx] /= a[o[ss]][ss];
                    }));
                }
                // to calculate l[y][s+1] (y=s+1,s+2,...,n)
                // , it is needed to finish calculating u[s][s+1]
                if (!fQueue.isEmpty()) {
                    // wait until the first submitted task is finished.
                    (fQueue.poll()).get();
                }
            }

            // === sort ===
            m.sortRow(o);

            return o;
        } finally {
            ex.shutdown();
        }
    }

    public static Matrix l(Matrix m) {
        float[][] src = m.getData();
        float[][] l = new float[m.getRowSize()][m.getColSize()];

        for (int x = 0; x < m.getColSize(); x++) {
            for (int y = x; y < m.getRowSize(); y++) {
                l[y][x] = src[y][x];
            }
        }

        return new Matrix(l);
    }

    public static Matrix u(Matrix m) {
        float[][] src = m.getData();
        float[][] u = new float[m.getRowSize()][m.getColSize()];

        for (int x = 0; x < m.getColSize(); x++) {
            for (int y = 0; y < x; y++) {
                u[y][x] = src[y][x];
            }
            u[x][x] = 1.0f;
        }

        return new Matrix(u);
    }

    public static Matrix mul(Matrix m0, Matrix m1) {
        Matrix ans = new Matrix(m0.getRowSize(), m1.getColSize());

        MatKernel kernel = new MatKernel(m0, m1, ans) {
            @Override
            public void run() {
                matMul(0, 1, 2);
            }
        };

        kernel.execute(ans.getSize());

        return kernel.getMat(2);
    }
}
