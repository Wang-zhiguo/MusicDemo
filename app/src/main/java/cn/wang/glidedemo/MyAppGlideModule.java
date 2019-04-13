package cn.wang.glidedemo;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class MyAppGlideModule extends AppGlideModule {

    // 用来更改Glide配置时使用
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {

    }

    // 用于替换Glide组件时使用
    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {

    }


}