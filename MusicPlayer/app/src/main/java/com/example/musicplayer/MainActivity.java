package com.example.musicplayer;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import com.zhy.m.permission.MPermissions;



import java.io.File;
/**
 * Describe:
 * <p>主页信息</p>
 *
 * @author zhouhuan
 * @Date 2020/5/8
 */
public class MainActivity extends AppCompatActivity implements Runnable {

    int flag = 1;//设置一个标志，供点击“开始/暂停”按钮使用
    private TextView txtInfo;
    private SeekBar seekBar;
    private MusicService musicService;
    private Handler handler;// 处理改变进度条事件
    int UPDATE = 0x101;
    private EditText song;
    private boolean autoChange, manulChange;// 判断是进度条是自动改变还是手动改变
    private boolean isPause;// 判断是从暂停中恢复还是重新播放


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //请求权限
        getPermission();
        musicService = new MusicService();
        try {
            setListViewAdapter();
        } catch (Exception e) {
            Log.i("TAG", "读取信息失败");
        }

        Button btnStart = findViewById(R.id.btn_star);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    /*
                     * 引入flag作为标志，当flag为1 的时候，此时player内没有东西，所以执行musicService.play()函数
                     * 进行第一次播放，然后flag自增二不再进行第一次播放
                     * 当再次点击“开始/暂停”按钮次数即大于1 将执行暂停或继续播放goplay()函数
                     */
                    if (flag == 1) {
                        musicService.play();
                        flag++;
                    } else if (!musicService.player.isPlaying()) {
                        musicService.goPlay();
                    } else if (musicService.player.isPlaying()) {
                        musicService.pause();
                    }
                } catch (Exception e) {
                    Log.i("LAT", "开始异常！");
                }

            }
        });

        Button btnSearch = findViewById(R.id.search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pathOld = song.getText().toString();
                System.out.println(pathOld);
                String pathNew=null;
                int index = pathOld.indexOf(" ");
                if(index == -1){
                    pathNew = pathOld;
                }else{
                    String[] x = pathOld.split(" ");
                    pathNew = x[0];
                    for(int i=1; i<x.length;i++) {
                        pathNew = pathNew + "-" + x[i];
                    }
                }
                System.out.println(pathNew);
                String pathLast = "https://freemusicarchive.org/track/" + pathNew +"/download";
                System.out.println(pathLast);
                Intent intent = new Intent(MainActivity.this, DownMusicService.class);
                intent.putExtra("path",pathLast);
                startService(intent);
            }
        });
        song = findViewById(R.id.song);
        Button btnContinue = findViewById(R.id.btn_continue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    musicService.goPlay();
                    txtInfo.setText("继续播放");
                } catch (Exception e) {
                    Log.i("LAT", "停止异常！");
                }

            }
        });

        Button btnPause = findViewById(R.id.btn_stop);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    musicService.pause();
                    flag = 1;//当点击停止按钮时，flag置为1
                    txtInfo.setText("停止播放");
                } catch (Exception e) {
                    Log.i("LAT", "停止异常！");
                }

            }
        });

        Button btnLast =  findViewById(R.id.btn_last);
        btnLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    musicService.last();
                } catch (Exception e) {
                    Log.i("LAT", "上一曲异常！");
                }

            }
        });

        Button btnNext =  findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    musicService.next();
                } catch (Exception e) {
                    Log.i("LAT", "下一曲异常！");
                }

            }
        });
        Button rand = findViewById(R.id.suiji);
        rand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    musicService.randPlay();
                }catch (Exception e){
                    Log.i("LAT","随机播放异常！");
                }
            }
        });

        Button refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent1 = getIntent();
                    finish();
                    startActivity(intent1);
                }catch (Exception e){
                    Log.i("TAG","刷新失败!");
                }
            }
        });
        Button shunxu = findViewById(R.id.shunxu);
        shunxu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    musicService.shunxu();
                }catch (Exception e){
                    Log.i("LAT","顺序播放异常！");
                }
            }
        });

        seekBar =  findViewById(R.id.sb);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {//用于监听SeekBar进度值的改变
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {//用于监听SeekBar开始拖动
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {//用于监听SeekBar停止拖动  SeekBar停止拖动后的事件
                int progress = seekBar.getProgress();
                Log.i("TAG:", "" + progress + "");
                int musicMax = musicService.player.getDuration(); //得到该首歌曲最长秒数
                int seekBarMax = seekBar.getMax();
                musicService.player.seekTo(musicMax * progress / seekBarMax);//跳到该曲该秒
                autoChange = true;
                manulChange = false;
            }
        });

        txtInfo =  findViewById(R.id.tv1);
        Thread t = new Thread(this);// 自动改变进度条的线程
        //实例化一个handler对象
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //更新UI
                int mMax = musicService.player.getDuration();//最大秒数
                if (msg.what == UPDATE) {
                    try {
                        seekBar.setProgress(msg.arg1);
                        txtInfo.setText(setPlayInfo(msg.arg2 / 1000, mMax / 1000));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    seekBar.setProgress(0);
                    txtInfo.setText("播放已经停止");
                }
            }
        };
        t.start();

    }

    private static final int ACCESS_FINE_LOCATION = 3;

    @TargetApi(Build.VERSION_CODES.M)
    private void getPermission() {
        MPermissions.requestPermissions(MainActivity.this, ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION
        );
    }

    //向列表添加MP3名字
    private void setListViewAdapter() {
        String[] str = new String[musicService.musicList.size()];
        int i = 0;
        for (String path : musicService.musicList) {
            File file = new File(path);
            str[i++] = file.getName();
        }
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, str);
        ListView listView = findViewById(R.id.lv1);
        listView.setAdapter(adapter);
    }

    @Override
    public void run() {
        int position, mMax, sMax;
        while (!Thread.currentThread().isInterrupted()) {
            if (musicService.player != null && musicService.player.isPlaying()) {
                position = musicService.getCurrentProgress();//得到当前歌曲播放进度(秒)
                mMax = musicService.player.getDuration();//最大秒数
                sMax = seekBar.getMax();//seekBar最大值，算百分比
                Message m = handler.obtainMessage();//获取一个Message
                m.arg1 = position * sMax / mMax;//seekBar进度条的百分比
                m.arg2 = position;
                m.what = UPDATE;
                handler.sendMessage(m);
                //  handler.sendEmptyMessage(UPDATE);
                try {
                    Thread.sleep(1000);// 每间隔1秒发送一次更新消息
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    //设置当前播放的信息
    private String setPlayInfo(int position, int max) {
        String info = "正在播放:  " + musicService.songName + "\t\t";
        int pMinutes = 0;
        while (position >= 60) {
            pMinutes++;
            position -= 60;
        }
        String now = (pMinutes < 10 ? "0" + pMinutes : pMinutes) + ":"
                + (position < 10 ? "0" + position : position);

        int mMinutes = 0;
        while (max >= 60) {
            mMinutes++;
            max -= 60;
        }
        String all = (mMinutes < 10 ? "0" + mMinutes : mMinutes) + ":"
                + (max < 10 ? "0" + max : max);

        return info + now + " / " + all;
    }

}