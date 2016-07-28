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
 * Created by Administrator on 2016/7/28.
 */
public class Consumer implements Runnable {
    private final String TAG = "Consumer";

    private SyncStack ss = null;
    private Handler handler = null;

    public Consumer(SyncStack ss, Handler handler) {
        super();
        this.ss = ss;
        this.handler = handler;
    }

    @Override
    public void run() {
        OutputStream out = null;
        try {
            File target = new File(Constant.TARGET_PATH);
            if (target.exists()) {
                target.delete();
            } else {
                File parent = target.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
            }

            target.createNewFile();

            out = new FileOutputStream(target);

            int len = 0;


            long pending = ss.getPending();

            while (len < pending) {
                SteamBread stb = ss.pop();
                out.write(stb.buff, 0, stb.length);
                out.flush();

                len += stb.length;

                // Delete memory
                stb.buff = null;
            }

            String md5 = MD5.getMd5ByFile(target);
            if (Constant.MD5_VALUE.equals(md5)) {
                handler.sendEmptyMessage(200);
            } else {
                Log.e(TAG, "失败，MD5值不一致 MD5 VALUE: " + md5);
                Message message = handler.obtainMessage();
                message.what = 400;
                message.obj = "error: Fail, the MD5 value is not consistent with VALUE MD5";
                handler.sendMessage(message);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
