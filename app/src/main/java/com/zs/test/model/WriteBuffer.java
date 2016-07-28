package com.zs.test.model;

import android.os.Environment;
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
 * Created by zhoushuang on 2016/7/27.
 */
public class WriteBuffer implements Runnable {
    private final String TAG = "WriteCache";
    private CacheData cache;
    private Handler handler;

    public WriteBuffer(CacheData cache, Handler handler) {
        this.cache = cache;
        this.handler = handler;
    }

    @Override
    public void run() {
        OutputStream out = null;
        try {
//            Log.e(TAG, "PATH: " + Constant.TARGET_PATH);
            File target = new File(Constant.TARGET_PATH);
            if (target.exists()) {
                target.delete();
            } else {
                File parent = target.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
            }

            boolean success = target.createNewFile();

            out = new FileOutputStream(target);
            while (cache.getPending() > 0) {
                int len = 0;
                byte[] buff = cache.getByteBuff();
                if (buff != null) {
                    len = buff.length;
                    if (len < 32768) {
                        Log.e(TAG, "len: " + len);
                    }
                    out.write(buff, 0, len);
                }

                buff = null;
                cache.setPending(cache.getPending() - len);
            }

            out.flush();

            String md5 = MD5.getMd5ByFile(target);
            if (Constant.MD5_VALUE.equals(md5)) {
//                Log.e(TAG, "验证成功");
                handler.sendEmptyMessage(200);
            } else {
                Log.e(TAG, "失败，MD5值不一致 MD5 VALUE: " + md5);
                cache.relese();
                Message message = handler.obtainMessage();
                message.what = 400;
                message.obj = "error: Fail, the MD5 value is not consistent with VALUE MD5";
                handler.sendMessage(message);
            }

        } catch (FileNotFoundException e) {
            cache.relese();
            Log.e(TAG, "error: " + e.getMessage());
            Message message = handler.obtainMessage();
            message.what = 400;
            message.obj = "error: " + e.getMessage();
            handler.sendMessage(message);
        } catch (IOException e) {
            cache.relese();
            Log.e(TAG, "error: " + e.getMessage());
            Message message = handler.obtainMessage();
            message.what = 400;
            message.obj = "error: " + e.getMessage();
            handler.sendMessage(message);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "error: " + e.getMessage());
                    Message message = handler.obtainMessage();
                    message.what = 400;
                    message.obj = "error: " + e.getMessage();
                    handler.sendMessage(message);
                }
            }
        }
    }
}
