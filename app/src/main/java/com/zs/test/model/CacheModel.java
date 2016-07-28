package com.zs.test.model;

/**
 * Created by zhoushuang on 2016/7/28.
 */
public class CacheModel {
    private int length;
    private byte buff[];

    public CacheModel(int len, byte buff[]) {
        this.buff = buff;
        this.length = len;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public void setBuff(byte[] buff) {
        this.buff = buff;
    }

    public byte[] getBuff() {
        return buff;
    }

    public String toString() {
        return " length: " + length;
    }
}
