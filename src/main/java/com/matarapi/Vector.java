/*
 * Copyright 2017 HONDOH Atsushi.
 */
package com.matarapi;

/**
 * Vector data structure for Matarapi.
 * @author a.ho
 */
public class Vector extends Matrix {

    public Vector(int size) {
        super(size, 1);
    }
    
    public Vector(float[] initval) {
        super(initval.length, 1);
        
        int row = 0;
        for (float val : initval) {
            setVal(row, 1, val);
        }
    }
}
