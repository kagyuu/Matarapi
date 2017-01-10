/*
 * Copyright 2017 HONDOH Atsushi.
 */
package jp.hondoh.matarapi.test;

import jp.hondoh.matarapi.IMatrix;
import jp.hondoh.matarapi.MatKernel;
import jp.hondoh.matarapi.Matrix;
import jp.hondoh.matarapi.MockMatrix;
import static jp.hondoh.matarapi.test.MatAssert.assertMatrix;
import org.junit.Test;
import static jp.hondoh.matarapi.test.MatAssert.assertMatrixInvert;

/**
 * Simple calculation test.
 *
 * @author atsushi
 */
public class MatrixTest {

    @Test
    public void testLU() throws Exception {
        Matrix m0 = new Matrix(new float[][]{
            {2.0f, 5.0f, 7.0f, 8.0f},
            {4.0f, 13.0f, 20.0f, 25.0f},
            {8.0f, 29.0f, 50.0f, 71.0f},
            {10.0f, 34.0f, 78.0f, 98.0f},
        });
        // m0 will be over-writed to lu-matrix
        Matrix original = m0.copyAll();
        
        // m0 => LU
        // oreder is row order for LU dividing
        final int[] order = m0.toLU();
                        
        // L U => mul
        IMatrix work = new MockMatrix(4, 4);
        MatKernel kernel = new MatKernel(m0, work) {

            @Override
            public void run() {
                int pass = getPassId();
                if (0 == pass) {
                    matMulLU(0, 1);
                } else if (1 == pass) {
                    // revert pivoting
                    matSortRow(1, 0, order);
                }
            }
        };
        kernel.execute(m0.getSize(),2);
        Matrix mul = kernel.getMat(0);
                
//        System.out.println(mul.toString());
        
        // LU expects m0
        assertMatrix(original, mul, 1.0E-6f);        
    }
    
    @Test
    public void testInvert() throws Exception {
        Matrix m0 = new Matrix(new float[][]{
            {2.0f, 5.0f, 7.0f, 8.0f},
            {4.0f, 13.0f, 20.0f, 25.0f},
            {8.0f, 29.0f, 50.0f, 71.0f},
            {10.0f, 34.0f, 78.0f, 98.0f},
        });
        // m0 will be over-writed to lu-matrix
        Matrix original = m0.copyAll();
        
        // m0 => LU with pivoting
        // oreder is row order for LU dividing
        final int[] order = m0.toLU();
                                
        // L U => mul
        IMatrix work = new MockMatrix(4, 4);
        MatKernel kernel = new MatKernel(m0, work) {

            @Override
            public void run() {
                int pass = getPassId();
                // Can't use switch statement on Aparapi.
                if (0 == pass) {
                    // U => U(-1)
                    matInvU(0,1);
                    // L => L(-1)
                    matInvL(0,1);
                } else if (1 == pass) {
                    // U(-1) L(-1) => A(-1)
                    matMulUL(1,0);
                } else if (2 == pass) {
                    // revert pivoting                    
                    matSortCol(0,1,order);
                }
            }
        };
        kernel.execute(m0.getSize(), 3);
        Matrix invert = kernel.getMat(1);
        
//        System.out.println(expect);
//        System.out.println(invert);
        
        // precision is not well, but its ok.
        // It's enough for the machine learning.
        // We don't use GPGPU for Rocket Science.
        assertMatrixInvert(original, invert, 1.0E-5f);
    }
}
