/*
 * Copyright 2017 HONDOH Atsushi.
 */
package jp.hondoh.matarapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;

/**
 * Matrix data structure for Matarapi.
 * @author a.ho
 */
@Data
public class Matrix {

    private float[][] data;

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

    public final int getSize() {
        return getRowSize() * getColSize();
    }

    public final int getRowSize() {
        return data.length;
    }

    public final int getColSize() {
        return data[0].length;
    }

    public final void setVal(final int row, final int col, final float val) {
        data[row][col] = val;
    }

    public final float getVal(final int row, final int col) {
        return data[row][col];
    }
    
    public final void sortRow(final int[] order) {
        float[][] cpy = Arrays.copyOf(data, data.length);
        for (int cnt=0; cnt < data.length; cnt++) {
            data[cnt] = cpy[order[cnt]];
        }
    }
    
    public final void revertRow(final int[] order) {
        float[][] cpy = Arrays.copyOf(data, data.length);
        for (int cnt=0; cnt < data.length; cnt++) {
            data[order[cnt]] = cpy[cnt];
        }
    }

    public Matrix deepcopy() {
        List<float[]> cpy = new ArrayList<>();
        
        for(float[] row : data) {
            cpy.add(Arrays.copyOf(row, row.length));
        }
        return new Matrix(cpy.toArray(new float[0][]));
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
