package com.example.zhouzixin;

import android.app.Application;

import org.xutils.x;

public class MyApplication extends Application {
    /**
     * SDK初始化也可以放到Application中
     */
    public static String APPID ="4312f2b0178b9b6145678639c372be1e";
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        //x.Ext.setDebug(BuildConfig.DEBUG); // 是否输出debug日志, 开启debug会影响性能.
    }
}