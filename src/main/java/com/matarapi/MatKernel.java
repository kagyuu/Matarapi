/*
 * Copyright 2017 HONDOH Atsushi.
 */
package com.matarapi;

import com.aparapi.Kernel;

/**
 * Matrix calculate Kernel.
 * @author a.ho
 */
public abstract class MatKernel extends Kernel {
    /**
     * data array.
     */
    protected float ary[];
    /**
     * data offset.
     */
    protected int[] offset;
    /**
     * matrix size.
     */
    protected int[] matSize;
    /**
     * col size.
     */
    protected int[] colSize;

    /**
     * Constructor.
     * @param data matrixes
     */
    public MatKernel(IMatrix ...data) {
        int arySize = 0;
        for (IMatrix m : data) {
            arySize += m.getSize();
        }
        ary = new float[arySize];
        offset = new int[data.length];
        matSize = new int[data.length];
        colSize = new int[data.length];

        int p = 0;
        int n = 0;
        for (IMatrix m : data) {

            offset[n] = p;
            matSize[n] = m.getRowSize() * m.getColSize();
            colSize[n] = m.getColSize();

            p = m.accept(ary, p);
            n += 1;
        }
    }

    /**
     * get Matrix.
     * @param no matrix number
     * @return Matrix
     */
    public Matrix getMat(final int no) {
        return new Matrix(ary, offset[no], matSize[no] / colSize[no], colSize[no]);
    }

    /**
     * add.
     * <pre>
     * in1       in2        out
     * |a b c|   |A B C|    |a+A b+B c+C|
     * |d e f| + |D E F| -> |d+D e+E f+F|
     * |g h i|   |G H I|    |g+G h+H i+I|
     * 
     * paralles size : matrix size (= row * col)
     * </pre>
     * @param in1 input1
     * @param in2 input2
     * @param out output
     */
    protected void matAdd(int in1, int in2, int out) {

        int i = getGlobalId();
        if (i > matSize[out]) return;

        int p0 = offset[out] + i;
        int p1 = offset[in1] + i;
        int p2 = offset[in2] + i;
        ary[p0] = ary[p1] + ary[p2];
    }

    /**
     * sub.
     * <pre>
     * in1       in2        out
     * |a b c|   |A B C|    |a-A b-B c-C|
     * |d e f| - |D E F| -> |d-D e-E f-F|
     * |g h i|   |G H I|    |g-G h-H i-I|
     * 
     * paralles size : matrix size (= row * col)
     * </pre>
     * @param in1 input1
     * @param in2 input2
     * @param out output
     */
    protected void matSub(int in1, int in2, int out) {

        int i = getGlobalId();
        if (i > matSize[out]) return;

        int p0 = offset[out] + i;
        int p1 = offset[in1] + i;
        int p2 = offset[in2] + i;
        ary[p0] = ary[p1] - ary[p2];
    }

    /**
     * ReLU (Rectified linear unit).
     * <pre>
     * An simple activation function for neural network.
     * This is enough complicity for machine learning.
     * That's differentiate is so simple!
     *
     * ReLu(x) = max(0,x)
     *         = 0 (x <  0)
     *           x (x >= 0)
     *
     * cf.
     * ReLu-1(x) = 0 (x <  0)
     *             1 (x >= 0)
     * 
     * paralles size : matrix size (= row * col)
     * </pre>
     * @param in input
     * @param out output
     */
    protected void matReLu(int in,int out) {

        int i = getGlobalId();
        if (i > matSize[out]) return;

        int p0 = offset[out] + i;
        int p1 = offset[in] + i;
        ary[p0] = max(ary[p1],0.0f);
    }

    /**
     * differentiate of ReLU (Rectified linear unit).
     * <pre>
     * an simple activation function for neural network.
     *
     * ReLu-1(x) = 0 (x <  0)
     *             1 (x >= 0)
     * 
     * cf.
     * ReLu(x) = max(0,x)
     *         = 0 (x <  0)
     *           x (x >= 0)
     * 
     * paralles size : matrix size (= row * col)
     * </pre>
     * @param in input
     * @param out output
     */
    protected void matdReLu(int in,int out) {
        
        int i = getGlobalId();
        if (i > matSize[out]) return;

        int p0 = offset[out] + i;
        int p1 = offset[in] + i;
        ary[p0] = ary[p1] < 0.0f ? 0.0f : 1.0f;
    }

    /**
     * multiple.
     * <pre>
     * in1       in2        out
     * |a b c|   |A B C|    |aA+bD+cG dA+eD+fG gA+hD+iG|
     * |d e f| X |D E F| -> |aB+bE+cH dB+eE+fH gB+hE+iH|
     * |g h i|   |G H I|    |aC+aF+aI dC+eF+fI gC+hF+iI|
     * 
     * paralles size : matrix size (= row * col)
     * </pre>
     * @param in1 input1
     * @param in2 input2
     * @param out output
     */
    protected void matMul(int in1, int in2, int out) {
        
        int i = getGlobalId();
        if (i > matSize[out]) return;

        int c0 = colSize[out];
        int c1 = colSize[in1];
        int c2 = colSize[in2];

        int col = i % c0;
        int row = (i - col) / c0;

        int p0 = offset[out] + i;
        int p1 = offset[in1] + c1 * row;
        int p2 = offset[in2] + col;

        float mul = 0.0f;
        for (int cnt=0; cnt < c1; cnt++) {
            mul += ary[p1] * ary[p2];
            p1 += 1;
            p2 += c2;
        }
        ary[p0] = mul;
    }
    
    /**
     * multiple LU.
     * <pre>
     * in1       in2        out
     * |a 0 0|   |1 B C|    |a aB   aC      |
     * |d e 0| X |0 1 F| -> |d dB+e dC+eF   |
     * |g h i|   |0 0 1|    |g gB+h gC+hF+i |
     * 
     * in1 and in2 is combined represnted in arg 'in'
     * |a B C|
     * |d e F|
     * |g h i|
     * 
     * paralles size : matrix size (= row * col)
     * </pre>
     * @param in  input
     * @param out output
     */
    protected void matMulLU(int in, int out) {
        
        int i = getGlobalId();
        if (i > matSize[out]) return;
        
        int size = colSize[in];

        int col = i % size;
        int row = (i - col) / size;
        
        int p0 = offset[out] + i;
        int pL = offset[in] + size * row;
        int pU = offset[in] + col;

        float mul = 0.0f;
        int step = min(col, row);
        for (int cnt=0; cnt <= step; cnt++) {
            if (col == cnt) {
                mul += ary[pL];
            } else {
                mul += ary[pL] * ary[pU];
            }
            pL += 1;
            pU += size;
        }
        ary[p0] = mul;
    }

    /**
     * multiple UL.
     * <pre>
     * in1       in2        out
     * |1 B C|   |a 0 0|    |a+Bd+Cg Be+Ch Ci |
     * |0 1 F| X |d e 0| -> |d+Fg    e+FH  Fi |
     * |0 0 1|   |g h i|    |g       h     i  |
     * 
     * in1 and in2 is combined represnted in arg 'in'
     * |a B C|
     * |d e F|
     * |g h i|
     * 
     * paralles size : matrix size (= row * col)
     * </pre>
     * @param in  input
     * @param out output
     */
    protected void matMulUL(int in, int out) {
        
        int i = getGlobalId();
        if (i > matSize[out]) return;
        
        int size = colSize[in];

        int col = i % size;
        int row = (i - col) / size;
        int skip = max(row, col);
        
        int p0 = offset[out] + i;
        int pU = offset[in] + size * row + skip;
        int pL = offset[in] + col + size * skip;

        float mul = 0.0f;
        for (int cnt=skip; cnt < size; cnt++) {
            if (row == cnt) {
                mul += ary[pL];
            } else {
                mul += ary[pU] * ary[pL];
            }
            pU += 1;
            pL += size;
        }
        ary[p0] = mul;
    }
    
    /**
     * ivert Upper triangle matrix.
     * <pre>
     * in        out        E
     * |1 b c|   |1 B C|    |1 0 0|
     * |0 1 f| X |0 1 F| -> |0 1 0|
     * |0 0 1|   |0 0 1|    |0 0 1|
     * 
     * The Lower triangle of input matrix, includes diagonal components '1'
     * , is ignored.
     * 
     * paralles size : col size
     * </pre>
     * @param in  input
     * @param out output
     */
    protected void matInvU(int in, int out) {
        
        int k = getGlobalId();
        
        int size = colSize[in];
        
        if (k > size) { return; }
        
        int p0 = offset[out] + size * k + k;        
        ary[p0] = 1.0f;
        
        float inv;
        int pU, pUi;
        for (int i = k - 1; i >= 0; i--) {
            inv = 0.0f;
            
            int j = i + 1;
            pU = offset[in] + size * i + j; // U[i][j]
            pUi = offset[out] + size * j + k; // U-1[j][k]
            for (; j <= k; j++) {
                inv -= ary[pU] * ary[pUi];
                
                pU += 1;     // move right
                pUi += size; // move down
            }
            p0 -= size; // move up
            ary[p0] = inv;
        }
    }
    
    /**
     * ivert Lower triangle matrix.
     * <pre>
     * in        out        E
     * |a 0 0|   |A 0 0|    |1 0 0|
     * |d e 0| X |D E 0| -> |0 1 0|
     * |g h i|   |G H I|    |0 0 1|
     * 
     * The Upper triangle of input matrix is ignored.
     * 
     * paralles size : col size
     * </pre>
     * @param in  input
     * @param out output
     */
    protected void matInvL(int in, int out) {
        
        int k = getGlobalId();
        
        int size = colSize[in];
        
        if (k > size) { return; }
        
        int p0 = offset[out] + size * k + k;
        int p1 = offset[in] + size * k + k;
        ary[p0] = 1.0f / ary[p1];
        
        float inv;
        int pL, pLi;
        for (int i = k + 1; i < size; i++) {
            inv = 0.0f;
            
            int j = k;
            pL = offset[in] + size * i + j; // L[i][j]
            pLi = offset[out] + size * j + j; // L-1[j][k]
            for (; j <= i-1; j++) {
                inv -= ary[pL] * ary[pLi];
                
                pL += 1;     // move right
                pLi += size; // move down
            }
            p0 += size; // move down
            // L-1[i][k] = - (Σ(L[i][j]*L-1[j][k]) / L[i][i]
            ary[p0] = inv / ary[offset[in] + size * i + i]; 
        }
    }
    
    /**
     * calculate hadamard product.
     * <pre>
     * in1       in2        out
     * |a b c|   |A B C|    |aA bB cC|
     * |d e f| O |D E F| -> |dD eE fF|
     * |g h i|   |G H I|    |gG hH iI|
     * </pre>
     * @param in1 input1
     * @param in2 input2
     * @param out output
     */
    protected void matHmul(int in1, int in2, int out) {

        int i = getGlobalId();
        if (i > matSize[out]) return;

        int p0 = offset[out] + i;
        int p1 = offset[in1] + i;
        int p2 = offset[in2] + i;
        ary[p0] = ary[p1] * ary[p2];
    }

    /**
     * calculate transpose matrix.
     * <pre>
     * in        out
     * |a b c| = |a d|
     * |d e f|   |b e|
     *           |c f|
     * 
     * paralles size : matrix size (= row * col)
     * </pre>
     * @param in input
     * @param out output
     */
    protected void matTranspose(int in, int out) {

        int i = getGlobalId();
        if (i > matSize[out]) return;

        int c0 = colSize[out];
        int c1 = colSize[in];

        int col = i % c1;
        int row = (i - col) / c1;

        int p1 = offset[in] + i;
        int p0 = offset[out] + row + col * c0; // col <-> row

        ary[p0] = ary[p1];
    }

    /**
     * sort columns.
     * <pre>
     * in        out
     * |a b c| = |b a c|
     * |d e f|   |e d f|
     * |g h i|   |h g i|
     * 
     * order = [1,0,2] means:
     * Col 0 -> Col 1
     * Col 1 -> Col 0
     * Col 2 -> Col 2
     * 
     * paralles size : matrix size (= row * col)
     * </pre>
     * @param in input
     * @param out output
     * @param order sort order
     */
    protected void matSortCol(int in, int out, int[] order) {

        int i = getGlobalId();
        if (i > matSize[out]) return;

        int c0 = colSize[out];
        int c1 = colSize[in];

        int col = i % c1;
        int row = (i - col) / c1;
        
        int p0 = offset[out] + order[col] + c0 * row;
        int p1 = offset[in] + i;

        ary[p0] = ary[p1];
    }
    
    /**
     * sort rows.
     * <pre>
     * in        out
     * |a b c| = |d e f|
     * |d e f|   |a b c|
     * |g h i|   |g h i|
     * 
     * order = [1,0,2] means:
     * Row 0 -> Row 1
     * Row 1 -> Row 0
     * Row 2 -> Row 2
     * 
     * paralles size : matrix size (= row * col)
     * </pre>
     * @param in input
     * @param out output
     * @param order sort order
     */
    protected void matSortRow(int in, int out, int[] order) {

        int i = getGlobalId();
        if (i > matSize[out]) return;

        int c0 = colSize[out];
        int c1 = colSize[in];

        int col = i % c1;
        int row = (i - col) / c1;

        int p0 = offset[out] + col + c0 * order[row];
        int p1 = offset[in] + i;

        ary[p0] = ary[p1];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("ary:\n");
        for (float val : ary) {
            sb.append(String.format("%.2f ", val));
        }
        sb.append("\noffset:\n");
        for (int val : offset) {
            sb.append(String.format("%d ", val));
        }
        sb.append("\nrow size:\n");
        int cnt = 0;
        for (int val : matSize) {
            sb.append(String.format("%d ", val / colSize[cnt++]));
        }
        sb.append("\ncol size:\n");
        for (int val : colSize) {
            sb.append(String.format("%d ", val));
        }

        return sb.toString();
    }
}
