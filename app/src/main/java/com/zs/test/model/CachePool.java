package com.zs.test.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoushuang on 2016/7/28.
 */
public class CachePool {
    private final int CACHE_POOL_MAX = 16;
    private List<CacheModel> list = new ArrayList<CacheModel>();
    private long pending = 0;

    /**
     * 读取buff，放入缓存池中
     * @param cache
     */
    public synchronized void setBuff(CacheModel cache) {
        while (list.size() >= CACHE_POOL_MAX) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        list.add(cache);
        this.notify();
    }

    /**
     * 从缓存池中取buff
     * @return
     */
    public synchronized CacheModel getBuff() {
        CacheModel cache;
        while (list.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        cache = list.get(0);
        list.remove(0);

        this.notify();
        return cache;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    public long getPending() {
        return pending;
    }
}
