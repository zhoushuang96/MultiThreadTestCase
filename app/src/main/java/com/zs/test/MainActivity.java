package com.zs.test;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.zs.test.constant.Constant;
import com.zs.test.model.WriteBuff;
import com.zs.test.model.ReadBuff;
import com.zs.test.model.CachePool;
import com.zs.test.util.MD5;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/***
 * Created by zhoushuang on 2016/7/28.
 */
public class MainActivity extends Activity implements View.OnClickListener{
    private final String TAG = "MainActivity";

    private TextView file_path;

    private TextView resourceMD5;
    private TextView resultTV;
    private ScrollView runLogSV;
    private TextView runLog;
    private EditText executeNum;
    private Button btn;

    private int excu_num = 0;//当前执行次数
    private int count = 10;//总次数  默认10次
    private int success_num = 0;
    private int fail_num = 0;
    private boolean isRunning = false;//是否正在执行
    private long BASE_SIZE = 3 * 1024 * 1024;//参照3M文件大小 睡5s
    private long SLEEP_MILLISECOND = 5000;//默认5秒
    private StringBuilder run_log = new StringBuilder();

    private MD5FileValueAsyncTask asyncTask = null;
    private RunningAsyncTask runningTask = null;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            boolean flag = false;
            switch (what){
                case 200:
                    flag = true;
                    success_num += 1;
                    run_log.append("验证成功。已成功（" + success_num + "）次  \n\t");
                    break;
                case 201:
                    flag = false;
                    String md5_Value = (String) msg.obj;
                    Constant.MD5_VALUE = md5_Value;
                    resourceMD5.setText("源文件MD5值：" + md5_Value);
                    run_log.append("源文件MD5值：\n\t" + md5_Value + " \n\t");
                    break;
                case 202:
                    Constant.MD5_VALUE = null;
                    run_log.append("------------- 分隔符 ------------- \n\t");
                    run_log.append("源文件：\n\t" + Constant.RESOURCE_PATH + " \n\t");
                    String name = "";
                    String type = "";
                    if (Constant.RESOURCE_PATH.contains(".")){
                        name = Constant.RESOURCE_PATH.substring(Constant.RESOURCE_PATH.lastIndexOf("/") + 1, Constant.RESOURCE_PATH.lastIndexOf(".")) + "_copy";
                        type = Constant.RESOURCE_PATH.substring(Constant.RESOURCE_PATH.lastIndexOf("."), Constant.RESOURCE_PATH.length());
                    }  else {
                        name = Constant.RESOURCE_PATH.substring(Constant.RESOURCE_PATH.lastIndexOf("/") + 1, Constant.RESOURCE_PATH.length()) + "_copy";
                    }

                    Constant.TARGET_PATH = Constant.TARGET_ROOT_PATH + name + type;
                    run_log.append("目标文件：\n\t" + Constant.TARGET_PATH + " \n\t");

                    resourceMD5.setText("正在计算源文件MD5值，请等待....");

                    //异步计算文件MD5值
                    asyncTask = null;
                    asyncTask = new MD5FileValueAsyncTask();
                    asyncTask.execute();
                    break;
                case 400:
                    flag = true;
                    fail_num += 1;
                    run_log.append("验证失败，MD5值不一致。已失败（" + fail_num + "）次 \n\t");
                    run_log.append("失败原因： " + msg.obj + "\n\t");
                    break;
                case 401:
                    Toast.makeText(MainActivity.this, "正在测试中，请稍后再试！", Toast.LENGTH_SHORT).show();
                    break;
                case 402:
                    Toast.makeText(MainActivity.this, "正在计算源文件MD5值或者没有选择文件，请确认！", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

            runLog.setText(run_log.toString());
            runLogSV.scrollTo(0, runLog.getHeight());

            if (flag) {
                excu_num++;

                String result = String.format(getResources().getString(R.string.result), excu_num, success_num, fail_num);

                resultTV.setText(Html.fromHtml(result));

                if (excu_num < count) {
                    runningTask = null;
                    runningTask = new RunningAsyncTask();
                    runningTask.execute();
                } else {
                    isRunning = false;
                    btn.setBackgroundColor(Color.GRAY);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        file_path = (TextView) findViewById(R.id.file_path);
        findViewById(R.id.select_file).setOnClickListener(this);

        resultTV = (TextView) findViewById(R.id.result);
        resourceMD5 = (TextView) findViewById(R.id.resourceMD5);
        runLogSV = (ScrollView) findViewById(R.id.runLogSV);
        runLog = (TextView) findViewById(R.id.runLog);
        executeNum = (EditText) findViewById(R.id.executeNum);
        btn = (Button) findViewById(R.id.start);

        btn.setOnClickListener(this);

        //默认文件路径
        file_path.setText(Constant.RESOURCE_PATH);

        String result = String.format(getResources().getString(R.string.result),excu_num, success_num, fail_num);

        resultTV.setText(Html.fromHtml(result));


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(Constant.RESOURCE_PATH);
                    if (!file.exists()) return;
                    String md5_value = MD5.getMd5ByFile(file);

                    Message message = handler.obtainMessage();
                    message.what = 201;
                    message.obj = md5_value;
                    handler.sendMessage(message);
                } catch (FileNotFoundException e){
                    e.printStackTrace();
                }

            }
        }).start();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start:
                if (Constant.MD5_VALUE == null || "".equals(Constant.MD5_VALUE)) {
                    handler.sendEmptyMessage(402);
                    return;
                }
                if (!isRunning){
                    if (excu_num > 0) {
                        run_log.append("----------------- 重新执行 ----------------- \n\t");
                        runLog.setText(run_log.toString());
                    }
                    excu_num = 0;//当前执行次数
                    count = 10;//总次数  默认10次
                    success_num = 0;
                    fail_num = 0;


                    isRunning = true;
                    String num = executeNum.getText().toString();
                    if (!TextUtils.isEmpty(num)) {
                        count = Integer.parseInt(num);
                    }

                    btn.setBackgroundColor(Color.GREEN);

                    runningTask = null;
                    runningTask = new RunningAsyncTask();
                    runningTask.execute();
                } else {
                    handler.sendEmptyMessage(401);
                }
                break;
            case R.id.select_file:
                if (!isRunning){
                    startActivityForResult(new Intent(this, FileManagerActivity.class).putExtra("type", "file"), 200);
                } else {
                    handler.sendEmptyMessage(401);
                }
                break;
            default:
                break;
        }
    }

    protected void start(){
        InputStream input = null;
        OutputStream out = null;
        long pending = 0;
        try {
            input = new FileInputStream(new File(Constant.RESOURCE_PATH));
            pending = input.available();

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


            CachePool cachePool = new CachePool();
            cachePool.setPending(pending);
            ReadBuff p = new ReadBuff(cachePool, input, handler);
            WriteBuff c = new WriteBuff(cachePool,out, handler);

            Thread tp = new Thread(p);
            Thread tc = new Thread(c);

            tp.start();
            tc.start();

            while(cachePool.getPending() > 0){
                //do nothing
            }

            if (cachePool.getPending() <= 0) {
                out.flush();
            }

            String md5 = "";
            if (pending > 300*1024*1024){
                md5 = MD5.getFileMD5(target);
            } else {
                md5 = MD5.getMd5ByFile(target);
            }
            if (Constant.MD5_VALUE.equals(md5)) {
                handler.sendEmptyMessage(200);
            } else {
                Log.e(TAG, "失败，MD5值不一致 MD5 VALUE: " + md5);
                Message message = handler.obtainMessage();
                message.what = 400;
                message.obj = "writeBuff error: Fail, the MD5 value is not consistent with VALUE MD5";
                handler.sendMessage(message);
            }
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
        } finally {
            try {
                if (input != null) {
                    input.close();
                }

                if (out != null) {
                    out.close();
                }
            }catch (IOException e) {
                //do nothing
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 200 && requestCode == 200) {
            Constant.RESOURCE_PATH = data.getStringExtra("file_path");
            file_path.setText(Constant.RESOURCE_PATH);

            handler.sendEmptyMessage(202);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        }
    }


    class MD5FileValueAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String md5_value = null;
            try {
                File file = new File(Constant.RESOURCE_PATH);
                if (!file.exists()) return md5_value;
                if (file.length() > (300 * 1024 * 1024)) {
                    md5_value = MD5.getFileMD5(file);
                } else {
                    md5_value = MD5.getMd5ByFile(file);
                }

            } catch (FileNotFoundException e) {
                md5_value = null;
            }
            return md5_value;
        }

        @Override
        protected void onPostExecute(String md5Value) {
            super.onPostExecute(md5Value);
            Message message = handler.obtainMessage();
            message.what = 201;
            message.obj = md5Value;
            handler.sendMessage(message);
        }
    }

    class RunningAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            start();
            return null;
        }
    }
}
