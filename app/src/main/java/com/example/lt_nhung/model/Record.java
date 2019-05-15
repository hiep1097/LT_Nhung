package com.example.lt_nhung.model;

public class Record {
    private Byte[] a;
    private String name;


    public Record() {
    }

    public Record(Byte[] a, String name) {
        this.a = a;
        this.name = name;
    }

    public Byte[] getA() {
        return a;
    }

    public void setA(Byte[] a) {
        this.a = a;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
