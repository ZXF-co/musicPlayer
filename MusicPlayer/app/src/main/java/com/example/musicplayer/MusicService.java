package com.example.musicplayer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicService {

    public static final File PATH = Environment.getExternalStoragePublicDirectory("/Music");// 获取SD卡总目录。
    public List<String> musicList;// 存放找到的所有mp3的绝对路径
    public MediaPlayer player; // 定义多媒体对象
    public int songNum; // 当前播放的歌曲在List中的下标,flag为标致
    public String songName; // 当前播放的歌曲名
    public String fileName;

    static class MusicFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp3"));//返回当前目录所有以.mp3结尾的文件
        }
    }

    //扫描获取文件
    public MusicService() {
        super();
        player = new MediaPlayer();
        musicList = new ArrayList<String>();  //构造动态数组
        try {
            File MUSIC_PATH = new File(String.valueOf(PATH));//获取Music文件的二级目录
            if (MUSIC_PATH.exists()){
                File[] files = MUSIC_PATH.listFiles(new MusicFilter()); //将获取到的所有文件存入文件数组中
                if (files == null || files.length == 0) {
                    Log.e("TAG", String.format("数据为空"));
                    return;
                }
                int length = files.length;
                if (length > 0) {                   //当数组不空时，遍历数组文件
                    for (File file : MUSIC_PATH.listFiles(new MusicFilter())) {
                        musicList.add(file.getAbsolutePath());  //将数组文件的绝对路径存入数组
                    }
                }
            }
        } catch (Exception e) {
            Log.i("TAG", String.format("读取文件异常%s", e.getMessage()));
        }
    }

    //截取音乐文件名
    public void setPlayName(String dataSource) {
        File file = new File(dataSource);//假设为D:\\mm.mp3
        String name = file.getName();//name=mm.mp3
        int index = name.lastIndexOf(".");//找到最后一个.
        songName = name.substring(0, index);//截取为mm
    }
    //播放音乐
    public void play() {
        try {
            player.reset(); //重置多媒体
            String dataSource = musicList.get(songNum);//得到当前播放音乐的路径
            setPlayName(dataSource);//截取歌名
            // 指定参数为音频文件
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(dataSource);//为多媒体对象设置播放路径
            player.prepare();//准备播放
            player.start();//开始播放
            //setOnCompletionListener 当当前多媒体对象播放完成时发生的事件
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer arg0) {
                    next();//如果当前歌曲播放完毕,自动播放下一首.
                }
            });

        } catch (Exception e) {
            Log.v("MusicService", e.getMessage());
        }
    }

    //继续播放
    public void goPlay() {
        int position = getCurrentProgress(); //获取当前播放进度
        player.seekTo(position);//设置当前MediaPlayer的播放位置，单位是毫秒。
        try {
            player.prepare();//  同步的方式装载流媒体文件。
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.start();
    }

    //随机播放
    public void randPlay(){
        songNum = new Random().nextInt(musicList.size()-1);
        play();
    }

    //顺序播放
    public void shunxu(){
        play();
    }

    // 获取当前进度
    public int getCurrentProgress() {
        if (player != null & player.isPlaying()) {
            return player.getCurrentPosition();
        } else if (player != null & (!player.isPlaying())) {
            return player.getCurrentPosition();
        }
        return 0;
    }

    //下一曲
    public void next() {
        songNum = songNum == musicList.size() - 1 ? 0 : songNum + 1;
        play();
    }

    //上一曲
    public void last() {
        songNum = songNum == 0 ? musicList.size() - 1 : songNum - 1;
        play();
    }

    // 暂停播放
    public void pause() {
        if (player != null && player.isPlaying()) {
            int position = player.getCurrentPosition(); //获取当前播放进度
            player.seekTo(position);//设置当前MediaPlayer的播放位置，单位是毫秒。
            player.pause();
        }
    }
}


