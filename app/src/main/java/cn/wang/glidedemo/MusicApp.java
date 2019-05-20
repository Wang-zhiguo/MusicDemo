package cn.wang.glidedemo;

import android.app.Application;

/**
 * Time: 2019-05-16
 * Author: wangzhiguo
 * Description: 功能描述
 */
public class MusicApp extends Application {
    NotificationContentWrapper wrapper;


    public NotificationContentWrapper getWrapper() {
        return wrapper;
    }

    public void setWrapper(NotificationContentWrapper wrapper) {
        this.wrapper = wrapper;
    }
}
