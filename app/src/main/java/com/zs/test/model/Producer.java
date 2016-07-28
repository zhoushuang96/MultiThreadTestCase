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
    private InputStream input = null;
    private Handler handler = null;

    public Producer(SyncStack ss, InputStream input, Handler handler) {
        this.ss = ss;
        this.input = input;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            int id = 0;
            int left = 0;
            int once = 0;

            SteamBread stb;
            ss.setPending(input.available());


            byte[] buff = new byte[32*1024];
            int len = 0;
            while((len = input.read(buff)) != -1){
                stb = new SteamBread(id, len, buff);
                id++;

                ss.push(stb);

                stb = null;

                buff = null;
                buff = new byte[32*1024];
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
