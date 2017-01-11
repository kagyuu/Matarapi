/*
 * Copyright 2017 HONDOH Atsushi.
 */
package com.matarapi.test;

import com.matarapi.IMatrix;
import com.matarapi.MatKernel;
import com.matarapi.Matrix;
import com.matarapi.MockMatrix;
import static com.matarapi.test.MatAssert.assertMatrix;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Simple calculation test.
 *
 * @author atsushi
 */
public class MatKernelTest {

    Matrix m0 = new Matrix(new float[][]{
        {1.0f, 2.0f, 3.0f},
        {4.0f, 5.0f, 6.0f},
        {7.0f, 8.0f, 9.0f}
    });
    Matrix m1 = new Matrix(new float[][]{
        {10.0f, 11.0f, 12.0f},
        {13.0f, 14.0f, 15.0f},
        {16.0f, 17.0f, 18.0f}
    });
    IMatrix m2 = new MockMatrix(3, 3);

    @Test
    public void addTest() {

        MatKernel kernel = new MatKernel(m0, m1, m2) {

            @Override
            public void run() {
                matAdd(0, 1, 2);
            }
        };

        kernel.execute(m2.getSize());

        // running on OpenCL?
        assertTrue(kernel.isRunningCL());

        float[][] expected = new float[][]{
            {11.0f, 13.0f, 15.0f},
            {17.0f, 19.0f, 21.0f},
            {23.0f, 25.0f, 27.0f}
        };

        assertMatrix(expected, kernel.getMat(2).getData(), 1.0E-6f);
    }

    @Test
    public void subTest() {

        MatKernel kernel = new MatKernel(m0, m1, m2) {

            @Override
            public void run() {
                matSub(0, 1, 2);
            }
        };

        kernel.execute(m2.getSize());

        // running on OpenCL?
        assertTrue(kernel.isRunningCL());

        float[][] expected = new float[][]{
            {-9.0f, -9.0f, -9.0f},
            {-9.0f, -9.0f, -9.0f},
            {-9.0f, -9.0f, -9.0f}
        };

        assertMatrix(expected, kernel.getMat(2).getData(), 1.0E-6f);
    }

    @Test
    public void mulTest() {

        MatKernel kernel = new MatKernel(m0, m1, m2) {

            @Override
            public void run() {
                matMul(0, 1, 2);
            }
        };

        kernel.execute(m2.getSize());

        // running on OpenCL?
        assertTrue(kernel.isRunningCL());

        float[][] expected = new float[][]{
            {84.0f, 90.0f, 96.0f},
            {201.0f, 216.0f, 231f},
            {318.0f, 342.0f, 366.0f}
        };

        assertMatrix(expected, kernel.getMat(2).getData(), 1.0E-6f);
    }
    
    @Test
    public void mulTest2() {
        Matrix mA = new Matrix(new float[][]{
            {1.0f, 2.0f, 3.0f},
            {4.0f, 5.0f, 6.0f}
        });
        Matrix mB = new Matrix(new float[][]{
            {10.0f, 11.0f},
            {13.0f, 14.0f},
            {16.0f, 17.0f}
        });
        IMatrix mC = new MockMatrix(2,2);

        MatKernel kernel = new MatKernel(mA, mB, mC) {

            @Override
            public void run() {
                matMul(0, 1, 2);
            }
        };

        kernel.execute(mC.getSize());
        mC = kernel.getMat(2);

        // running on OpenCL?
        assertTrue(kernel.isRunningCL());

        System.out.println(mC.toString());
        
        float[][] expected = new float[][]{
            {84.0f, 90.0f},
            {201.0f, 216.0f}
        };

        assertMatrix(expected, mC, 1.0E-6f);
    }
    
    @Test
    public void hmulTest() {

        MatKernel kernel = new MatKernel(m0, m1, m2) {

            @Override
            public void run() {
                matHmul(0, 1, 2);
            }
        };

        kernel.execute(m2.getSize());

        // running on OpenCL?
        assertTrue(kernel.isRunningCL());

        float[][] expected = new float[][]{
            {10.0f, 22.0f, 36.0f},
            {52.0f, 70.0f, 90.0f},
            {112.0f, 136.0f, 162.0f}
        };

        assertMatrix(expected, kernel.getMat(2).getData(), 1.0E-6f);
    }

    @Test
    public void transposeTest() {

        MatKernel kernel = new MatKernel(m0, m2) {

            @Override
            public void run() {
                matTranspose(0, 1);
            }
        };

        kernel.execute(m2.getSize());

        // running on OpenCL?
        assertTrue(kernel.isRunningCL());

        float[][] expected = new float[][]{
            {1.0f, 4.0f, 7.0f},
            {2.0f, 5.0f, 8.0f},
            {3.0f, 6.0f, 9.0f}
        };

        assertMatrix(expected, kernel.getMat(1).getData(), 1.0E-6f);
    }

    @Test
    public void sortColTest() {

        final int[] order = new int[]{2,0,1};
        MatKernel kernel = new MatKernel(m0, m1, m2) {

            @Override
            public void run() {
                matSortCol(0, 2, order);
            }
        };

        kernel.execute(m2.getSize());

        // running on OpenCL?
        assertTrue(kernel.isRunningCL());

        float[][] expected = new float[][]{
            {2.0f, 3.0f, 1.0f},
            {5.0f, 6.0f, 4.0f},
            {8.0f, 9.0f, 7.0f}
        };
        
        assertMatrix(expected, kernel.getMat(2).getData(), 1.0E-6f);
    }


    @Test
    public void sortRowTest() {

        final int[] order = new int[]{2,0,1};
        MatKernel kernel = new MatKernel(m0, m1, m2) {

            @Override
            public void run() {
                matSortRow(0, 2, order);
            }
        };

        kernel.execute(m2.getSize());

        // running on OpenCL?
        assertTrue(kernel.isRunningCL());

        float[][] expected = new float[][]{
            {4.0f, 5.0f, 6.0f},
            {7.0f, 8.0f, 9.0f},
            {1.0f, 2.0f, 3.0f}
        };
        
        assertMatrix(expected, kernel.getMat(2).getData(), 1.0E-6f);
    }
}
