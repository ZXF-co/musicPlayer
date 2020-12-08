package com.example.musicplayer;

import android.app.IntentService;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.musicplayer.MainActivity;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

public class DownMusicService extends IntentService {

    public static final File PATH = Environment.getExternalStoragePublicDirectory("/Music");
    private String fileName;
    private String fileNameNew;

    protected void onHandleIntent(Intent intent) {

        final String url = intent.getStringExtra("path");
        System.out.println(url);
        String str = url.substring(0,url.indexOf("/download"));
        fileName=str.substring(str.lastIndexOf("/")+1);
        int index = fileName.indexOf("-");
        if(index == -1){
            fileNameNew = fileName;
        }else{
            String[] x = fileName.split("-");
            fileNameNew = x[0];
            for(int i=1; i<x.length; i++){
                fileNameNew = fileNameNew + " " + x[i];
            }
        }
        final long startTime = System.currentTimeMillis();
        Log.i("DOWNLOAD","startTime="+startTime);
        Request request = new Request.Builder().url(url).build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败
                e.printStackTrace();
                Log.i("DOWNLOAD","download failed");
                Handler handler=new Handler(Looper.getMainLooper());
                handler.post(new Runnable(){
                    public void run(){
                        Toast.makeText(getApplicationContext(), "下载失败", Toast.LENGTH_LONG).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Sink sink = null;
                BufferedSink bufferedSink = null;
                    try {
                        File dest = new File(PATH, fileNameNew + ".mp3");
                        sink = Okio.sink(dest);
                        bufferedSink = Okio.buffer(sink);
                        bufferedSink.writeAll(response.body().source());
                        bufferedSink.close();
                        Log.i("DOWNLOAD", "download success");
                        Log.i("DOWNLOAD", "totalTime=" + (System.currentTimeMillis() - startTime));

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "下载成功!", Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("DOWNLOAD", "download failed");
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "下载失败!", Toast.LENGTH_LONG).show();
                            }
                        });
                    } finally {
                        if (bufferedSink != null) {
                            bufferedSink.close();
                        }
                    }
                }
        });
        Intent intentNew = new Intent(DownMusicService.this, MainActivity.class);
        intentNew.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity(intentNew);
    }
    public DownMusicService() {
        super("");

    }
}