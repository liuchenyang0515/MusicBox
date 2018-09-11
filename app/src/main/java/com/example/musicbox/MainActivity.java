package com.example.musicbox;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Iservice iservice;
    private MyConn myConn;
    public static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // 获取携带的数据
            Bundle data = msg.getData();
            // 获取歌曲的总时长和当前进度
            int duration = data.getInt("duration");
            int currentPosition = data.getInt("currentPosition");
            // 设置seekbar进度
            sbar.setMax(duration);
            sbar.setProgress(currentPosition);
        }
    };
    private static SeekBar sbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 混合方式开启服务
        Intent intent = new Intent(this, MusicSevice.class);
        startService(intent);
        // 调用bindService，为了回去定义的中间人对象
        myConn = new MyConn();
        bindService(intent, myConn, BIND_AUTO_CREATE);
        // 找到seekbar，设置进度
        sbar = findViewById(R.id.seekBar);
        // 4.给seekBar设置监听事件
        sbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // 当进度改变的时候调用
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }
            // 当开始拖动的时候调用
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            // 当拖动停止的时候调用
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                iservice.callSeekTo(seekBar.getProgress());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 当activity销毁的时候调用，为了不报异常
        unbindService(myConn);
        Log.d(TAG, "onDestroy: ");
    }

    // 播放
    public void click(View view) {
        // 这里播放tomcat服务器的音乐不需要权限，如果是播放/mnt/sdcard/...需要权限，因为测试的时候在这个目录，所以这里写了权限
        applyPermissions(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private void applyPermissions(String readExternalStorage) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{readExternalStorage}, 1);
        } else {
            iservice.callPlayMusic();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    iservice.callPlayMusic();
                } else {
                    Toast.makeText(this, "权限被拒绝，请在设置手动打开", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    // 暂停
    public void click2(View view) {
        iservice.callPauseMusic();
    }

    public void click3(View view) {
        iservice.callStopMusic();
    }


    // 监听服务的状态
    private class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iservice = (Iservice) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}
