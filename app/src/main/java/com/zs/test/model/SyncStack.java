package com.zs.test.model;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/7/28.
 */
public class SyncStack {
    private final int SyncStackLengthMax = 16;
    private ArrayList<SteamBread> stb = new ArrayList<SteamBread>();
    private long pending = 0;

    // 放入框中，相当于入栈
    public synchronized void push(SteamBread sb) {
        while (stb.size() >= SyncStackLengthMax) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        stb.add(sb);
        this.notify();
    }

    // 从框中拿出，相当于出栈
    public synchronized SteamBread pop() {
        SteamBread firstStb;
        while (stb.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        firstStb = stb.get(0);
//		firstStb.buff = null;
        stb.remove(0);

        this.notify();
        return firstStb;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    public long getPending() {
        return pending;
    }
}
