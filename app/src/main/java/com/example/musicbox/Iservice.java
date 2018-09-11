package com.example.musicbox;

public interface Iservice {
    // 把想暴露的方法都定义在接口中
    public void callPlayMusic();
    public void callPauseMusic();
    public void callStopMusic();
    public void callSeekTo(int position);
}
