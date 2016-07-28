package com.zs.test.model;

/**
 * Created by Administrator on 2016/7/28.
 */
public class SteamBread {
    int id;

    int length;
    byte buff[];

    SteamBread(int id, int len, byte buff[]) {
        this.id = id;
        this.buff = buff;
        this.length = len;
    }

    public String toString() {
        return "steamBread: id: " + id + " length: " + length;
    }
}
