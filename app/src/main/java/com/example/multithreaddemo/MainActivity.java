package com.example.multithreaddemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.WeakHashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //进度条常量：Message.what类型
    private MyHandler myHandler = new MyHandler(this);
    private static final int START_NUM = 1;
    private static final int ADDING_NUM = 2;
    private static final int ENDING_NUM = 3;
    private static final int CANCEL_NUM = 4;
    private ProgressBar progressBar;
    private TextView textView;
    private Button etThread,etTask,etDown,etImg,etOther;
    private ImageView imageView;


    private static final String DOWNLOAD_URL="https://desk-fd.zol-img.com.cn/t_s1920x1080c5/g5/M00/07/07/ChMkJlXw8QmIO6kEABYKy-RYbJ4AACddwM0pT0AFgrj303.jpg\n";
    private static final int MSG_SHOW_PROGRESS = 11;

    private ProgressDialog progressDialog;
    private final String IMGURL = "https://up.enterdesk.com/edpic/41/f3/04/41f304cd41e8824cdf8700336f094431.jpg";


    private static final int MSG_SHOW_IMAGE = 12;
    static class MyUIHandler extends Handler{
        //定义弱引用对象
        private WeakReference<Activity> ref;
        //在构造方法中创建此对象
        public MyUIHandler(Activity activity){
            this.ref = new WeakReference<>(activity);
        }
        //重写handler方法

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //1.获取弱引用指向的activity对象
            MainActivity activity = (MainActivity) ref.get();
            if(activity == null){
                return;
            }
            switch (msg.what){
                case MSG_SHOW_PROGRESS:
                    activity.progressBar.setVisibility(View.VISIBLE);
                    break;
                case MSG_SHOW_IMAGE:
                    activity.progressBar.setVisibility(View.GONE);
                    activity.imageView.setImageBitmap((Bitmap)msg.obj);
                    break;
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.push_progressBar);
        textView = findViewById(R.id.text);
        etThread = findViewById(R.id.btn_compute);
        etTask = findViewById(R.id.btn_task);
        etDown = findViewById(R.id.btn_handler);
        etImg = findViewById(R.id.btn_asynctask);
        etOther = findViewById(R.id.btn_else);
        imageView = findViewById(R.id.picture);

        etThread.setOnClickListener(this);
        etTask.setOnClickListener(this);
        etDown.setOnClickListener(this);
        etImg.setOnClickListener(this);
        etOther.setOnClickListener(this);

        myTask = new MyTask();
        progressDialog = new ProgressDialog(this);
        progressDialog.setIcon(R.drawable.ic_launcher_foreground);
        progressDialog.setTitle("提示信息");
        progressDialog.setMessage("正在下载中，请稍后……");
//        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }
private CalculateThread calculateThread;
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_compute:
                calculateThread = new CalculateThread();
                calculateThread.start();
                break;
            case R.id.btn_task:
                myTask = new MyTask();
                myTask.execute();
//                myTask.cancel(true);
                break;
            case R.id.btn_handler:
                new Thread(new DownloadImageFetcher(DOWNLOAD_URL)).start();
                break;
            case R.id.btn_asynctask:
                new MAsyncTask().execute(IMGURL);
                break;
            case R.id.btn_else:
                break;
        }
    }
    //自定义Handler静态类
    static class MyHandler extends Handler{
        //定义弱引用对象
        private WeakReference<Activity> ref;
        //在构造方法中创建此对象
        public MyHandler(Activity activity){
            this.ref = new WeakReference<>(activity);


        }
        //重写handler方法
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //1.获取弱引用指向的Activity对象
            MainActivity activity = (MainActivity) ref.get();
            if(activity == null){
                return;
            }
            //2.根据Message的what属性值处理消息
            switch (msg.what){
                case START_NUM:
                    activity.progressBar.setVisibility(View.VISIBLE);
                    break;
                case ADDING_NUM:
                    activity.progressBar.setProgress(msg.arg1);
                    activity.textView.setText("计算已完成"+msg.arg1+"%");
                    break;
                case ENDING_NUM:
                    activity.progressBar.setVisibility(View.GONE);
                    activity.textView.setText("计算已完成,结果为:"+msg.arg1);
                    activity.myHandler.removeCallbacks(activity.calculateThread);
                    break;
                case CANCEL_NUM:
                    activity.progressBar.setProgress(0);
                    activity.progressBar.setVisibility(View.GONE);
                    activity.textView.setText("计算已取消");
                    break;
            }

        }


    }
    //计算的子线程，实现1+2+…+100的功能
    class CalculateThread extends Thread{
        @Override
        public void run() {
            int result = 0;//存放结果的变量
            boolean isCancel = false;
            //1.刚开始发送一个空消息
            myHandler.sendEmptyMessage(START_NUM);
            //2.计算过程，要求：每隔100ms计算一次
            for (int i = 0;i<=100;i++){
                try {
                    Thread.sleep(100);
                    result += i;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    isCancel = true;
                    break;
                }
                //2.发送进度条更新的消息
                if (i%5 == 0){
                    //获取进度条更新的消息
                    Message msg = Message.obtain();
                    msg.what = ADDING_NUM;
                    msg.arg1 = i;
                    //
                    myHandler.sendMessage(msg);
                }
            }
            if (!isCancel){
                Message msg = myHandler.obtainMessage();
                msg.what = ENDING_NUM;
                msg.arg1 = result;
                myHandler.sendMessage(msg);
            }

        }
    }
    private MyUIHandler uiHandler = new MyUIHandler(this);
    //下载图片的线程
    private class DownloadImageFetcher implements Runnable{
        private String imgUrl;
        public DownloadImageFetcher(String strUrl){
            this.imgUrl = strUrl;
        }
        @Override
        public void run() {
            InputStream in =null;
            myHandler.obtainMessage(MSG_SHOW_PROGRESS).sendToTarget();
            try {
                URL url = new URL(imgUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                in = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
             //   uiHandler.obtainMessage(MSG_SHOW_IMAGE,bitmap).sendToTarget();
                Message msg = uiHandler.obtainMessage();
                msg.what = MSG_SHOW_IMAGE;
                msg.obj = bitmap;
                uiHandler.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (in != null){
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    MyTask myTask;
        private class MyTask extends AsyncTask<String,Integer,String>{
        @Override
        //执行 线性任务前的操作
        protected void onPreExecute() {
            textView.setText("加载中");
        }
//接收输入参数、执行任务中的耗时操作、返回 线性任务执行的结果
        @Override
        protected String doInBackground(String... strings) {

                try {
                    int count = 0;
                    int length = 1;
                    while (count < 99) {
                        count += length;
                        //可调用publishProgress()显示进度，之后将执行onProgressUpdate（）
                        publishProgress(count);
                        //模拟耗时任务
                        Thread.sleep(50);
                    }
                    } catch(InterruptedException e){
                        e.printStackTrace();
                    }

            return null;


        }
        //在主线程 显示线程任务执行的进度

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
            textView.setText("loading…"+values[0]+"%");
        }
        //接收线程任务执行结果、将执行结果显示到UI组件

        @Override
        protected void onPostExecute(String result) {
            textView.setText("加载完毕");
        }
        //将异步任务设置为：取消状态

        @Override
        protected void onCancelled() {
            textView.setText("已取消");
            progressBar.setProgress(0);
        }
    }

    public class MAsyncTask extends AsyncTask<String,Integer,Bitmap>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (imageView != null){
                imageView.setVisibility(View.GONE);
            }
            progressDialog.show();
            progressDialog.setProgress(0);
        }


        @Override
        protected Bitmap doInBackground(String... strings) {
            String url = strings[0];
            Bitmap bitmap = null;
            URLConnection connection;
            InputStream is;
            ByteArrayOutputStream bos;
            int len;
            float count=0,total;
            try {
                connection=(URLConnection) new java.net.URL(url).openConnection();
                total=(int)connection.getContentLength();
                is=connection.getInputStream();
                bos = new ByteArrayOutputStream();
                byte []data=new byte[1024];
                while ((len=is.read(data))!=-1){
                    count+=len;
                    bos.write(data,0,len);
                    publishProgress((int)(count/total*100));

                }
                Thread.sleep(500);
                bitmap=BitmapFactory.decodeByteArray(bos.toByteArray(),0,bos.toByteArray().length);
                is.close();
                bos.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            progressDialog.cancel();
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }
    }
}
