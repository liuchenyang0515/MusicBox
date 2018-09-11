package com.example.musicbox;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MusicSevice extends Service {
    private static final String TAG = "MusicSevice";
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private TimerTask task;

    public MusicSevice() {
        Log.d(TAG, "构造: ");
        initMediaPlayer();
    }

    // 把定义的中间人返回
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    // 服务已开启就执行这个方法
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // 播放音乐的方法
    public void playMusic() {
        // 准备播放
        // mediaPlayer.prepare();
        if (mediaPlayer == null) {
            Log.d(TAG, "mediaplayer确实为null");
            initMediaPlayer();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { // 准备完成的监听器
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // 开始播放
                    mediaPlayer.start();
                    // 更新进度条
                    updateSeekBar();
                }
            });
        }
        if (!mediaPlayer.isPlaying()) {
            Log.d(TAG, "没有正在播放，开始操作");
            mediaPlayer.start();
            // 更新进度条
            updateSeekBar();
            Log.d(TAG, "音乐播放了");
        }
    }

    private void initMediaPlayer() {
        // 初始化mediaplayer
        try {
            mediaPlayer = new MediaPlayer();
            // 设置要播放的资源位置path，可以是网络路径，也可以是本地路径
            // /mnt/sdcard/yinyue.mp3
            mediaPlayer.setDataSource("http://192.168.164.1:8080/yinyue.mp3");
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateSeekBar() {
        // 获取当前歌曲总长度
        final int duration = mediaPlayer.getDuration();
        // 使用Timer 定时器去获取当前进度
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                // 一秒钟获取一次当前进度
                int currentPosition = mediaPlayer.getCurrentPosition();
                // 拿到我们在MainActivity创建的handler发消息，消息可以携带数据
                Message message = Message.obtain();
                //obj只能携带一个数据
                Bundle bundle = new Bundle(); // map
                bundle.putInt("duration", duration);
                bundle.putInt("currentPosition", currentPosition);
                message.setData(bundle);
                // 发送一条消息，MainActivity里面的handlemessage就会执行
                MainActivity.handler.handleMessage(message);
            }
        };
        // 100ms后每间隔1s、执行一次run方法
        timer.schedule(task, 100, 1000);
        // 设置播放完成的监听
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG, "歌曲播放完成");
                timer.cancel();
                task.cancel();
            }
        });
    }

    // 实现指定播放的位置
    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    // 暂停音乐的方法
    public void pauseMusic() {
        if (mediaPlayer.isPlaying()) {
            // 暂停音乐
            mediaPlayer.pause();
            timer.cancel();
            Log.d(TAG, "音乐暂停了");
        }
    }

    public void stopMusic() {
        mediaPlayer.stop();
        // 如果不取消定时任务，mediaplayer停止就崩了
        timer.cancel();
        task.cancel();
        mediaPlayer = null;
    }

    // 在服务的内部定义一个中间人对象(IBinder)
    private class MyBinder extends Binder implements Iservice {

        @Override
        public void callPlayMusic() {
            playMusic();
        }

        @Override
        public void callPauseMusic() {
            pauseMusic();
        }

        @Override
        public void callStopMusic() {
            stopMusic();
        }

        @Override
        public void callSeekTo(int position) {
            seekTo(position);
        }
    }
}
