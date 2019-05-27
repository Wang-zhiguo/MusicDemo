package cn.wang.glidedemo.utils;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import cn.wang.glidedemo.bean.MusicBean;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Time: 2019-05-25
 * Author: wangzhiguo
 * Description: 功能描述
 */
public class DownUtils {
    //下载歌词成功
    private static final int SUCCESS_LRC = 1;
    //下载歌词失败
    private static final int FAILED_LRC = 2;
    //下载歌曲成功
    private static final int SUCCESS_MP3 = 3;
    //下载歌曲失败
    private static final int FAILED_MP3 = 4;
    //获取音乐专辑图片成功
    private static final int GET_MP3_PIC = 5;
    //获取音乐专辑图片失败
    private static final int GET_FAILED_MP3_PIC = 6;
    //下载时,音乐已存在
    private static final int MUSIC_EXISTS = 7;


    private static DownUtils sInstance;
    private OnDownloadListener mListener;

    private ThreadPoolExecutor mThreadPool;

    /**
     * 设置回调监听器对象
     *
     * @param mListener
     * @return
     */
    public DownUtils setListener(OnDownloadListener mListener) {
        this.mListener = mListener;
        return this;
    }

    //获取下载工具的实例
    public synchronized static DownUtils getsInstance() {
        if (sInstance == null) {
            try {
                sInstance = new DownUtils();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        return sInstance;
    }

    /**
     * 下载的具体业务方法
     *
     * @throws ParserConfigurationException
     */
    private DownUtils() throws ParserConfigurationException {
        //mThreadPool = Executors.newSingleThreadExecutor();
        //构造一个线程池
        mThreadPool = new ThreadPoolExecutor(
                1,1,10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(1),
                new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    public void download(MusicBean.SongListBean music) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SUCCESS_LRC:
                        if (mListener != null) mListener.onDowload("歌词下载成功");
                        break;
                    case FAILED_LRC:
                        if (mListener != null) mListener.onFailed("歌词下载失败");
                        break;
                    case GET_MP3_PIC:
                        System.out.println("GET_MP3_URL:" + msg.obj);
//                        downloadMusic((String) msg.obj, this);
                        break;
                    case GET_FAILED_MP3_PIC:
                        if (mListener != null) mListener.onFailed("下载失败,该歌曲为收费或VIP类型");
                        break;
                    case SUCCESS_MP3:
                        if (mListener != null)
                            mListener.onDowload(music.getTitle() + "已经下载");
                        String url = music.getLrclink();
                        System.out.println("download lrc:" + url);
                        downloadLRC(url, music.getTitle(), this);
                        break;
                    case FAILED_MP3:
                        if (mListener != null)
                            mListener.onFailed(music.getTitle() + "下载失败");
                        break;
                    case MUSIC_EXISTS:
                        if (mListener != null) mListener.onFailed("音乐已存在");
                        break;
                }
            }
        };
        downloadMusic(music.getUrl(),music.getTitle(),handler);
    }

    //下载歌词
    private void downloadLRC(final String url, final String musicName, final Handler handler) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    File LrcDirFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                    String target = LrcDirFile + "/" + musicName + ".lrc";
                    File fileTarget = new File(target);
                    if (fileTarget.exists()) {
                        handler.obtainMessage(MUSIC_EXISTS).sendToTarget();
                        return;
                    } else {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url(url).build();
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            PrintStream ps = new PrintStream(new File(target));
                            byte[] bytes = response.body().bytes();
                            ps.write(bytes, 0, bytes.length);
                            ps.close();
                            handler.obtainMessage(SUCCESS_LRC, target).sendToTarget();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
    }


    //下载MP3
    private void downloadMusic(final String url, final String musicName, final Handler handler) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                File musicDirFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                if (!musicDirFile.exists()) {
                    musicDirFile.mkdirs();
                }
                String mp3url = url;

                String target = musicDirFile + "/" + musicName + ".mp3";
                File fileTarget = new File(target);
                if (fileTarget.exists()) {
                    handler.obtainMessage(MUSIC_EXISTS).sendToTarget();
                    return;
                } else {
                    //使用OkHttpClient组件
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(url).build();
                    System.out.println(request);
                    try {
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            PrintStream ps = new PrintStream(fileTarget);
                            byte[] bytes = response.body().bytes();
                            ps.write(bytes, 0, bytes.length);
                            ps.close();
                            handler.obtainMessage(SUCCESS_MP3).sendToTarget();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        handler.obtainMessage(FAILED_MP3).sendToTarget();
                    }
                }
            }
        });
    }

    //自定义下载事件监听器
    public interface OnDownloadListener {
        public void onDowload(String mp3Url);

        public void onFailed(String error);
    }
}
