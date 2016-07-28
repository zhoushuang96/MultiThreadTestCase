package com.zs.test.model;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.zs.test.constant.Constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhoushuang on 2016/7/27.
 */
public class ReadBuffer implements Runnable {
    private final String TAG = "ReadBuffer";
    private CacheData cache;
    private Handler handler;

    public ReadBuffer(CacheData cache, Handler handler) {
        this.cache = cache;
        this.handler = handler;
    }

    @Override
    public void run() {
        InputStream input = null;
        try {
            input = new FileInputStream(new File(Constant.RESOURCE_PATH));
            long pending = input.available();
            cache.setPending(pending);

            byte[] buff = null;
            int len = 0;


            while (pending > 0) {
                long read_len = pending > 32768 ? 32768 : pending;
//                Log.e(TAG, "********* read_len : " + read_len);

                buff = new byte[(int)read_len];

                len = input.read(buff, 0, (int) read_len);

//                Log.e(TAG, "xxxxxxxxxxx len : " + len);

                if (buff.length > len) {
                    byte[] bt = new byte[len];
                    System.arraycopy(buff, 0, bt, 0, len);
                    cache.setByteBuff(bt);
                } else {
                    cache.setByteBuff(buff);
                }

                buff = null;

                pending -= len;
            }
        } catch (FileNotFoundException e) {
            Message message = handler.obtainMessage();
            message.what = 400;
            message.obj = "error: " + e.getMessage();
            handler.sendMessage(message);
        } catch (IOException e) {
            Message message = handler.obtainMessage();
            message.what = 400;
            message.obj = "error: " + e.getMessage();
            handler.sendMessage(message);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Message message = handler.obtainMessage();
                    message.what = 400;
                    message.obj = "error: " + e.getMessage();
                    handler.sendMessage(message);
                }
            }
        }

    }
}
