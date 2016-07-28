package com.zs.test.model;

import android.os.Handler;

import com.zs.test.constant.Constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2016/7/28.
 */
public class Producer implements Runnable {
    private SyncStack ss = null;
    private Handler handler = null;

    public Producer(SyncStack ss, Handler handler) {
        this.ss = ss;
        this.handler = handler;
    }

    @Override
    public void run() {
        // 开始生产馒头
        InputStream input = null;
        try {
            int id = 0;
            int left = 0;
            int once = 0;

            SteamBread stb;
            input = new FileInputStream(new File(Constant.RESOURCE_PATH));

            left = input.available();
            while(left > 0){
                byte[] buff = new byte[32*1024];

                once = input.read(buff);
                stb = new SteamBread(id, once, buff);
                left -= once;
                id++;

                ss.push(stb);
                buff = null;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
