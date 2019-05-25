package cn.wang.glidedemo.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import cn.wang.glidedemo.view.MainActivity;
import cn.wang.glidedemo.MusicApp;
import cn.wang.glidedemo.bean.MusicBean;
import cn.wang.glidedemo.view.MusicPlayActivity;
import cn.wang.glidedemo.bean.NotificationContentWrapper;
import cn.wang.glidedemo.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Time: 2019-05-15
 * Author: wangzhiguo
 * Description: 功能描述
 */
public class PlayService extends Service {
    private static final int NOTIFICATION_CUSTOM = 10;
    private static final String ACTION_LOVE = "cn.wang.music.love";
    private static final String ACTION_PRE = "cn.wang.music.pre";
    private static final String ACTION_PLAY_OR_PAUSE = "cn.wang.music.play";
    private static final String ACTION_LYRICS = "cn.wang.music.lyrics";
    private static final String ACTION_CANCEL = "cn.wang.music.cancel";
    private static final String ACTION_NEXT = "cn.wang.music.next";

    private MediaPlayer mPlayer;
    private int currentPosition;
    //当前歌曲时长
    private int mDuration;
    //当前正在播放的歌曲的位置
    ArrayList<MusicBean.SongListBean> mp3Infos;

    private NotificationManager notificationManager;
    NotificationCompat.Builder nb;
    private Timer timer;
    private boolean timerStart = false;
    private TimerTask task;
    private Intent intentPos;

    public PlayService() {
    }

    //内部类PlayBinder实现Binder,
    public class PlayBinder extends Binder {
        public PlayService getPlayService() {
            System.out.println("PlayService #1 " + PlayService.this);
            return PlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
        //通过PlayBinder拿到PlayService,给Activity调用
    }

    public void setMp3Infos(ArrayList<MusicBean.SongListBean> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new MediaPlayer();

        mPlayer.setOnCompletionListener(mp -> next());

        notificationManager = getSystemService(NotificationManager.class);

        nb = new NotificationCompat.Builder(this, "media");
        //判断是否是8.0Android.O
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan1 = new NotificationChannel(
                    "media",
                    "会话类型", NotificationManager.IMPORTANCE_DEFAULT);
            //chan1.setLightColor(Color.GREEN);
            //chan1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(chan1);
            nb.setChannelId("media");
        }

        timer = new Timer();
        intentPos = new Intent("Change_Position");
        task = new TimerTask() {
            @Override
            public void run() {
                intentPos.putExtra("pos", mPlayer.getCurrentPosition());
                intentPos.putExtra("duration", mDuration);
                sendBroadcast(intentPos);

            }
        };
        //mp3Infos = MediaUtils.getMp3Infos(this);
        //获取Mp3列表
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getStringExtra("action");
        if (action != null) {
            if ("next".equals(action)) {
                next();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    //播放
    public void play(int position) {
        if (position >= 0 && position < mp3Infos.size()) {
            MusicBean.SongListBean mp3Info = mp3Infos.get(position);
            //获取mp3Info对象
            //进行播放,播放前判断
            try {
                mPlayer.reset();//复位
                mPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
                //资源解析,Mp3地址
                mPlayer.prepare();//准备
                mPlayer.setOnPreparedListener(MediaPlayer::start);
                //mPlayer.start();//开始(播放)
                //保存当前位置到currentPosition,比如第一首,currentPosition = 1
                currentPosition = position;
                mDuration = mp3Info.getFile_duration();

                getPicByUrl(mp3Info);
                if (!timerStart) {
                    timer.schedule(task, 0, 1000);
                    timerStart = true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //暂停
    public void pause() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    //下一首
    public void next() {
        if (currentPosition >= mp3Infos.size() - 1) {
            //如果超出最大值,(因为第一首是0),说明已经是最后一首
            currentPosition = 0;
            //回到第一首
        } else {
            currentPosition++;//下一首
        }
        play(currentPosition);
    }

    //上一首 previous
    public void prev() {
        if (currentPosition - 1 < 0) {
            //如果上一首小于0,说明已经是第一首
            currentPosition = mp3Infos.size() - 1;
            //回到最后一首
        } else {
            currentPosition--;//上一首
        }
        play(currentPosition);
    }

    //
    public void start() {
        if (mPlayer != null && !mPlayer.isPlaying()) {
            //判断当前歌曲不等于空,并且没有在播放的状态
            mPlayer.start();
        }
    }


    public void sendCustomViewNotification(Context context, NotificationManager nm, NotificationContentWrapper content, Boolean isLoved, Boolean isPlaying) {
        //创建点击通知时发送的广播
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
        //创建各个按钮的点击响应广播
        Intent intentLove = new Intent(context, PlayService.class);
        intentLove.setAction(ACTION_LOVE);
        PendingIntent piLove = PendingIntent.getService(context, 0, intentLove, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPre = new Intent(context, PlayService.class);
        intentPre.setAction(ACTION_PRE);
        PendingIntent piPre = PendingIntent.getService(context, 0, intentPre, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPlayOrPause = new Intent(context, PlayService.class);
        intentPlayOrPause.setAction(ACTION_PLAY_OR_PAUSE);
        PendingIntent piPlayOrPause = PendingIntent.getService(context, 0, intentPlayOrPause, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentNext = new Intent(context, PlayService.class);
        //intentNext.setAction(ACTION_NEXT);
        intentNext.putExtra("action", "next");
        PendingIntent piNext = PendingIntent.getService(context, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentLyrics = new Intent(context, MusicPlayActivity.class);
        PendingIntent piLyrics = PendingIntent.getActivity(context, 0, intentLyrics, 0);

        Intent intentCancel = new Intent(context, PlayService.class);
        intentCancel.setAction(ACTION_CANCEL);
        PendingIntent piCancel = PendingIntent.getService(context, 0, intentCancel, PendingIntent.FLAG_UPDATE_CURRENT);
        //创建自定义小视图
        RemoteViews customView = new RemoteViews(context.getPackageName(), R.layout.custom_view_layout);
        customView.setImageViewBitmap(R.id.iv_content, content.bitmap);
        customView.setTextViewText(R.id.tv_title, content.title);
        customView.setTextViewText(R.id.tv_summery, content.summery);
        customView.setImageViewBitmap(R.id.iv_play_or_pause, BitmapFactory.decodeResource(context.getResources(),
                isPlaying ? R.mipmap.ic_pause : R.mipmap.ic_play));
        customView.setOnClickPendingIntent(R.id.iv_play_or_pause, piPlayOrPause);
        customView.setOnClickPendingIntent(R.id.iv_next, piNext);
        customView.setOnClickPendingIntent(R.id.iv_lyrics, piLyrics);
        customView.setOnClickPendingIntent(R.id.iv_cancel, piCancel);
        //创建自定义大视图
        RemoteViews customBigView = new RemoteViews(context.getPackageName(), R.layout.custom_big_view_layout);
        customBigView.setImageViewBitmap(R.id.iv_content_big, content.bitmap);
        customBigView.setTextViewText(R.id.tv_title_big, content.title);
        customBigView.setTextViewText(R.id.tv_summery_big, content.summery);
        customBigView.setImageViewBitmap(R.id.iv_love_big, BitmapFactory.decodeResource(context.getResources(),
                isLoved ? R.mipmap.ic_loved : R.mipmap.ic_love));
        customBigView.setImageViewBitmap(R.id.iv_play_or_pause_big, BitmapFactory.decodeResource(context.getResources(),
                isPlaying ? R.mipmap.ic_pause : R.mipmap.ic_play));
        customBigView.setOnClickPendingIntent(R.id.iv_love_big, piLove);
        customBigView.setOnClickPendingIntent(R.id.iv_pre_big, piPre);
        customBigView.setOnClickPendingIntent(R.id.iv_play_or_pause_big, piPlayOrPause);
        customBigView.setOnClickPendingIntent(R.id.iv_next_big, piNext);
        customBigView.setOnClickPendingIntent(R.id.iv_lyrics_big, piLyrics);
        customBigView.setOnClickPendingIntent(R.id.iv_cancel_big, piCancel);
        //创建通知
        //设置通知左侧的小图标
        nb.setSmallIcon(R.mipmap.ic_notification)
                //设置通知标题
                .setContentTitle("Custom notification")
                //设置通知内容
                .setContentText("Demo for custom notification !")
                //设置通知不可删除
                .setOngoing(true)
                //设置显示通知时间
                .setShowWhen(true)
                //设置点击通知时的响应事件
                .setContentIntent(pi)
                //设置自定义小视图
                .setCustomContentView(customView)
                //设置自定义大视图
                .setCustomBigContentView(customBigView);
        //发送通知
        nm.notify(NOTIFICATION_CUSTOM, nb.build());
//        startForeground(1,nb.build());
    }

    private void getPicByUrl(MusicBean.SongListBean bean) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(bean.getPic_small())
                .addHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:0.9.4)")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                byte[] bytes = response.body().bytes();
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                NotificationContentWrapper wrapper = new NotificationContentWrapper(
                        bmp, bean.getTitle(),
                        bean.getArtist_name(),
                        bean.getLrclink());
                sendCustomViewNotification(getApplicationContext(), notificationManager, wrapper
                        , true, true);

                Intent intent = new Intent("Change_Music");
                ((MusicApp) getApplication()).setWrapper(wrapper);
                sendBroadcast(intent);
            }
        });
    }
}
