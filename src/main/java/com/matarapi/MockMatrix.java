/*
 * Copyright 2017 HONDOH Atsushi.
 */
package com.matarapi;

/**
 * MockMatrix.
 * <pre>
 * Mock matrix is used to obtain work area for Matarapi library.
 * This class only maintain size information of matrix.
 * </pre>
 * @author atsushi
 */
public class MockMatrix implements IMatrix {

    /**
     * row size.
     */
    private final int row;
    /**
     * col size.
     */
    private final int col;
    
    public MockMatrix(final int rowcol) {
        this(rowcol, rowcol);        
    }
    
    public MockMatrix(final int row, final int col) {
        this.row = row;
        this.col = col;
    }
    
    /**
     * copy containts data to ary.
     * <pre>
     * This method simply skip data size in ary.
     * </pre>
     * @param ary data array
     * @param p write pointer
     * @return next pointer
     */
    @Override
    public int accept(float[] ary, int p) {
        return p + getSize();
    }

    @Override
    public int getColSize() {
        return col;
    }

    @Override
    public int getRowSize() {
        return row;
    }

    @Override
    public int getSize() {
        return col * row;
    }

    @Override
    public float[][] getData() {
        throw new UnsupportedOperationException("Not supported.");
    }
    
}
