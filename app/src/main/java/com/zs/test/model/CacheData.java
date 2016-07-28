package com.zs.test.model;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by zhoushuang on 2016/7/27.
 */
public class CacheData {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");

    private long cacheMaxSize = 10;// 容器最大值

    private static volatile LinkedList<byte[]> list = new LinkedList<byte[]>();

    private LinkedList<byte[]> temp = new LinkedList<byte[]>();

    private long pending;

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    public void relese(){
        Collections.synchronizedList(list);
        list.clear();
    }

    public synchronized void setByteBuff(byte[] data) {
        Collections.synchronizedList(list);
//        synchronized (list) {
            try {
                if (list.size() >= cacheMaxSize) {
                    this.wait();
                }

                list.add(data);

                this.notifyAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//        }
    }

    public synchronized byte[] getByteBuff() {
        Collections.synchronizedList(list);
//        synchronized (list) {
            try {
                if (list.size() <= 0) {
                    this.wait();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            byte[] bs = list.getFirst();
            list.removeFirst();

            for(byte[] bt : list){
                temp.add(bt);
            }

            list.clear();
            list.addAll(temp);
            temp.clear();

            this.notifyAll();

            return bs;
//        }
    }
}
