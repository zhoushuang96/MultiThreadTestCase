package com.zs.test.model;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhoushaung on 2016/7/28.
 */
public class ReadBuff implements Runnable {
    private final String TAG = "ReadBuff";
    private CachePool cachePool = null;
    private InputStream input = null;
    private Handler handler = null;

    public ReadBuff(CachePool cachePool, InputStream input, Handler handler) {
        this.cachePool = cachePool;
        this.input = input;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            CacheModel stb;
            byte[] buff = new byte[32*1024];
            int len = 0;
            while((len = input.read(buff)) != -1){
                stb = new CacheModel(len, buff);
                cachePool.setBuff(stb);
                stb = null;
                buff = null;
                buff = new byte[32*1024];
            }
        } catch (FileNotFoundException e) {
            Message message = handler.obtainMessage();
            message.what = 400;
            message.obj = "readBuff error: FileNotFoundException: " + e.getMessage();
            handler.sendMessage(message);
        } catch (IOException e) {
            Message message = handler.obtainMessage();
            message.what = 400;
            message.obj = "readBuff error: IOException: " + e.getMessage();
            handler.sendMessage(message);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Message message = handler.obtainMessage();
                    message.what = 400;
                    message.obj = "readBuff error: InputStream close exception: " + e.getMessage();
                    handler.sendMessage(message);
                }
            }
        }
    }
}
