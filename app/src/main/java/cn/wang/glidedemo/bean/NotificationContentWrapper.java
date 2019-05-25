package cn.wang.glidedemo.bean;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 *
 * @author peter
 * @date 2018/7/4
 */

public class NotificationContentWrapper implements Serializable {
    public Bitmap bitmap;
    public String title;
    public String summery;
    public String lrcUrl;

    public NotificationContentWrapper(Bitmap bitmap, String title, String summery,String lrcUrl) {
        this.bitmap = bitmap;
        this.title = title;
        this.summery = summery;
        this.lrcUrl = lrcUrl;
    }
}
