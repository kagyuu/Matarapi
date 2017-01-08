/*
 * Copyright 2017 HONDOH Atsushi.
 */
package jp.hondoh.matarapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.Data;

/**
 * Matrix data structure for Matarapi.
 *
 * @author a.ho
 */
@Data
public class Matrix implements IMatrix {

    private float[][] data;
    
    public Matrix(final int rowcol) {
        this(rowcol, rowcol);        
    }

    public Matrix(final int row, final int col) {
        data = new float[row][col];
    }

    public Matrix(final float[][] initval) {
        data = initval;
    }

    public Matrix(final float[] ary, final int offset, final int row, final int col) {
        this(row, col);

        int p = offset;
        for (int r = 0; r < row; r++) {
            for (int c = 0; c < col; c++) {
                setVal(r, c, ary[p]);
                p += 1;
            }
        }
    }

    @Override
    public final int getSize() {
        return getRowSize() * getColSize();
    }

    @Override
    public final int getRowSize() {
        return data.length;
    }

    @Override
    public final int getColSize() {
        return data[0].length;
    }

    public final void setVal(final int row, final int col, final float val) {
        data[row][col] = val;
    }

    public final float getVal(final int row, final int col) {
        return data[row][col];
    }

    /**
     * copy all (deep copy).
     *
     * @return whole copy of this matrix
     */
    public Matrix copyAll() {
        List<float[]> cpy = new ArrayList<>();

        for (float[] row : data) {
            cpy.add(Arrays.copyOf(row, row.length));
        }
        return new Matrix(cpy.toArray(new float[0][]));
    }

    /**
     * copy lower triangle (deep copy).
     * <pre>
     * data       lower trianger
     * |a b c| => |a 0 0|
     * |d e f|    |d e 0|
     * |g h i|    |g h i|
     * </pre>
     *
     * @return copy of the lower triangle in this matrix
     */
    public Matrix copyL() {
        float[][] cpy = new float[getRowSize()][getColSize()];

        for (int x = 0; x < getColSize(); x++) {
            for (int y = x; y < getRowSize(); y++) {
                cpy[y][x] = data[y][x];
            }
        }

        return new Matrix(cpy);
    }

    /**
     * copy upper triangle (deep copy).
     * <pre>
     * data       upper trianger
     * |a b c| => |1 b c|
     * |d e f|    |0 1 f|
     * |g h i|    |0 0 1|
     * </pre>
     *
     * @return copy of the lower triangle in this matrix
     */
    public Matrix copyU() {
        float[][] cpy = new float[getRowSize()][getColSize()];

        for (int x = 0; x < getColSize(); x++) {
            for (int y = 0; y < x; y++) {
                cpy[y][x] = data[y][x];
            }
            cpy[x][x] = 1.0f;
        }

        return new Matrix(cpy);
    }

    /**
     * LU decomposition.
     * <pre>
     * 1. this method find following LU.
     * data       L                   U
     * |a b c| = |l(11) 0     0    ||1     u(12) u(13)|
     * |d e f|   |l(21) l(22) 0    ||0     1     u(23)|
     * |g h i|   |l(31) l(32) l(33)||0     0     1    |
     *
     * 2. LU is stored in the 'data' variable as following structure
     * data
     * |l(11) u(12) u(13)|
     * |l(21) l(22) u(23)|
     * |l(31) l(32) l(33)|
     *
     * # The diagonal component of the upper triangle is ommited in this structure,
     * # because all these are always 1.
     * </pre>
     *
     * @return sort order by pivotting
     * @throws ExecutionException
     */
    public int[] toLU() throws ExecutionException {
        final int colSize = getColSize();
        final int rowSize = getRowSize();

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
                            data[o[yy]][ss] -= data[o[yy]][k] * data[o[k]][ss];
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
                    tmp = Math.abs(data[o[y]][s]);
                    if (maxL < tmp) {
                        maxLidx = y;
                        maxL = tmp;
                    }
                }

                if (maxLidx < 0) {
                    // ie. l[i][i] == 0, in other words, Matrix m has no l and u strictly.
                    // But this method calculate an approximation on the assumption that
                    // l[i][i] would be Float.MIN_VALUE(1.401298464324817E-45f).
                    data[o[s]][s] = Float.MIN_VALUE;
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
                            data[o[ss]][xx] -= data[o[ss]][k] * data[o[k]][xx];
                        }
                        data[o[ss]][xx] /= data[o[ss]][ss];
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
            float[][] cpy = Arrays.copyOf(data, data.length);
            for (int cnt = 0; cnt < data.length; cnt++) {
                data[cnt] = cpy[o[cnt]];
            }

            return o;
        } catch (InterruptedException interruptedEx) {
            throw new ExecutionException(interruptedEx);
        } finally {
            ex.shutdown();
        }
    }

    /**
     * overwrite unit (identical) matrix.
     * <pre>
     * data       data
     * |a b c| => |1 0 0|
     * |d e f|    |0 1 0|
     * |g h i|    |0 0 1|
     * 
     * E is an initial letter of Einheitsmatrix, Identical Matrix in Germany.
     * This method same as toI();
     * </pre>
     * @return this object
     */
    public Matrix toE() {
        
        int rowNo = 0;
        for (float[] row : data) {
            for (int colNo = 0; colNo < row.length; colNo++) {
                row[colNo] = (rowNo == colNo ? 1.0f : 0.0f);
            }
            rowNo += 1;
        }
        
        return this;
    }
    
    /**
     * overwrite zero (null) matrix .
     * <pre>
     * data       data
     * |a b c| => |0 0 0|
     * |d e f|    |0 0 0|
     * |g h i|    |0 0 0|
     * </pre>
     * @return this object
     */
    public Matrix toZ() {
        
        int rowNo = 0;
        for (float[] row : data) {
            for (int colNo = 0; colNo < row.length; colNo++) {
                row[colNo] = 0.0f;
            }
            rowNo += 1;
        }
        
        return this;
    }
    
    /**
     * overwrite unit (identical) matrix.
     * <pre>
     * data       data
     * |a b c| => |1 0 0|
     * |d e f|    |0 1 0|
     * |g h i|    |0 0 1|
     * 
     * I is an initial letter of Identical Matrix.
     * This method same as toE();
     * </pre>
     * @return this object
     */
    public Matrix toI() {
        return toE();
    }
    
    /**
     * copy containts data to ary.
     * @param ary data array
     * @param p write pointer
     * @return next pointer
     */    
    @Override
    public int accept(float[] ary, int p) {
        for (float[] vec : data) {
            for (float val : vec) {
                ary[p] = val;
                p += 1;
            }
        }
        return p;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (float[] vec : data) {
            for (float val : vec) {
                sb.append(String.format("%.2f ", val));
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
