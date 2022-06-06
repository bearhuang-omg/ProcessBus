// ICat.aidl
package com.example.aidltest;

// Declare any non-default types here with import statements

interface ICat {
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
//            double aDouble, String aString);

    String getColor(int id);
    double getWeight(int id);
}