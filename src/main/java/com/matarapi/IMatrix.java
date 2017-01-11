/*
 * Copyright 2017 HONDOH Atsushi.
 */
package com.matarapi;

/**
 * Matrix interface.
 * @author a.ho
 */
public interface IMatrix {

    /**
     * copy containts data to ary.
     * @param ary data array
     * @param p write pointer
     * @return next pointer
     */
    int accept(float[] ary, int p);

    int getColSize();

    int getRowSize();

    int getSize();
    
    float[][] getData();
    
}
