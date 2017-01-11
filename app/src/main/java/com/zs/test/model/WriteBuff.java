package com.zs.test.model;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.zs.test.constant.Constant;
import com.zs.test.util.MD5;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by zhoushuang on 2016/7/28.
 */
public class WriteBuff implements Runnable {
    private final String TAG = "Consumer";

    private CachePool cachePool = null;
    private OutputStream out = null;
    private Handler handler = null;

    public WriteBuff(CachePool cachePool,OutputStream out, Handler handler) {
        super();
        this.cachePool = cachePool;
        this.out = out;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            int len = 0;
            long pending = cachePool.getPending();

            while (len < pending) {
                CacheModel cache = cachePool.getBuff();
                byte[] buff = cache.getBuff();
                int readLen = cache.getLength();
                if (buff.length != readLen) {
                    Log.e(TAG, "buff len : " + buff.length + " , readLen: " + readLen);
                }
                out.write(buff, 0, readLen);
//                out.flush();

                len += cache.getLength();

                // Delete memory
                buff = null;
                cache.setBuff(null);
            }

            cachePool.setPending(0);

        } catch (IOException e) {
            Message message = handler.obtainMessage();
            message.what = 400;
            message.obj = "writeBuff error: IOException: " + e.getMessage();
            handler.sendMessage(message);
        }
    }
}
