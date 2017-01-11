/*
 * Copyright 2017 HONDOH Atsushi.
 */
package com.matarapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import net.arnx.jsonic.JSON;

/**
 * Serialize and Deserialize Matrix
 * @author a.ho
 */
public class MatrixJSON {
    
    /**
     * Serialize Matrixes
     * @param m Matrixes
     * @return JSON
     */
    public static String encode(Matrix ...m) {
        return JSON.encode(matrix2ary(m));
    }
    
    public static void encode(OutputStream out, Matrix ...m) throws IOException {
        JSON.encode(matrix2ary(m), out);
    }
    
    public static void encode(Appendable appendable, Matrix ...m) throws IOException {
        JSON.encode(matrix2ary(m), appendable);
    }
    
    public static List<Matrix> decode(String json) {
        return ary2matrix(JSON.decode(json, float[][][].class));
    }
    
    public static List<Matrix> decode(InputStream in) throws IOException {
        return ary2matrix(JSON.decode(in, float[][][].class));
    }
    
    public static List<Matrix> decode(Reader reader) throws IOException {
        return ary2matrix(JSON.decode(reader, float[][][].class));
    }

    private static float[][][] matrix2ary(Matrix ...mary) {
        float[][][] fary = new float[mary.length][][];
        
        int cnt = 0;
        for(Matrix m : mary) {
            fary[cnt] = m.getData();
            cnt += 1;
        }
        
        return fary;
    }
    
    private static List<Matrix> ary2matrix(float[][][] fary) {
        List<Matrix> mary = new ArrayList<>();
        
        for (float[][] fmtrix : fary) {
            mary.add(new Matrix(fmtrix));
        }
        
        return mary;
    }    
}
