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
//        OutputStream out = null;
        try {
//            File target = new File(Constant.TARGET_PATH);
//            if (target.exists()) {
//                target.delete();
//            } else {
//                File parent = target.getParentFile();
//                if (!parent.exists()) {
//                    parent.mkdirs();
//                }
//            }
//
//            target.createNewFile();
//
//            out = new FileOutputStream(target);

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

//            String md5 = "";
//            if (pending > 300*1024*1024){
//                md5 = MD5.getFileMD5(target);
//            } else {
//                md5 = MD5.getMd5ByFile(target);
//            }
//            if (Constant.MD5_VALUE.equals(md5)) {
//                handler.sendEmptyMessage(200);
//            } else {
//                Log.e(TAG, "失败，MD5值不一致 MD5 VALUE: " + md5);
//                Message message = handler.obtainMessage();
//                message.what = 400;
//                message.obj = "writeBuff error: Fail, the MD5 value is not consistent with VALUE MD5";
//                handler.sendMessage(message);
//            }
        } catch (FileNotFoundException e) {
            Message message = handler.obtainMessage();
            message.what = 400;
            message.obj = "writeBuff error: FileNotFoundException: " + e.getMessage();
            handler.sendMessage(message);
        } catch (IOException e) {
            Message message = handler.obtainMessage();
            message.what = 400;
            message.obj = "writeBuff error: IOException: " + e.getMessage();
            handler.sendMessage(message);
        }finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Message message = handler.obtainMessage();
                message.what = 400;
                message.obj = "writeBuff error: OutputStream close exception: " + e.getMessage();
                handler.sendMessage(message);
            }
        }
    }
}
