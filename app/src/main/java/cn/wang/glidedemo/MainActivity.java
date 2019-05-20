package cn.wang.glidedemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    String path = "http://tingapi.ting.baidu.com/v1/restserver/ting?format=json&calback=&from=webapp_music&method=baidu.ting.billboard.billList&type=1&size=10&offset=";
    String playpath = "http://tingapi.ting.baidu.com/v1/restserver/ting?format=json&calback=&from=webapp_music&method=baidu.ting.song.play&songid=";
    private RecyclerView rv_music;
    private BaseRecyclerAdapter mAdapter;
    private ImageView iv_content;
    private ImageView iv_play_or_pause;
    private ImageView iv_next;
    private ImageView iv_lyrics;
    private TextView tv_title;
    private TextView tv_artist;




    protected PlayService playService;
    private boolean playing = false;

    private Intent service;
    //是否已经绑定
    private boolean isBound = false;
    //绑定Service
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //转换
            PlayService.PlayBinder playBinder = (PlayService.PlayBinder) service;
            playService = playBinder.getPlayService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playService = null;
            isBound = false;
        }
    };
    private MusicReceiver receiver;

    //绑定服务
    public void bindPlayService(){
        if(!isBound) {
            bindService(service, conn, Context.BIND_AUTO_CREATE);
            isBound = true;
        }
    }
    //解除绑定服务
    public void unbindPlayService(){
        if(isBound) {
            unbindService(conn);
            isBound = false;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData(0);
        service = new Intent(this,PlayService.class);
        startService(service);
        bindPlayService();

        receiver = new MusicReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("Change_Music");
        //filter.addAction("Change_Position");
        registerReceiver(receiver,filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindPlayService();
        unregisterReceiver(receiver);
    }

    private void initView() {
        iv_content = findViewById(R.id.iv_content);
        iv_play_or_pause = findViewById(R.id.iv_play_or_pause);
        iv_play_or_pause.setOnClickListener(v -> {
            if(playing){
                playService.pause();
                iv_play_or_pause.setImageResource(R.mipmap.ic_play);
            }else {
                playService.start();
                iv_play_or_pause.setImageResource(R.mipmap.ic_pause);
            }
            playing = !playing;
        });

        iv_next = findViewById(R.id.iv_next);
        iv_next.setOnClickListener(v -> playService.next());
        iv_lyrics = findViewById(R.id.iv_lyrics);
        iv_lyrics.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,MusicPlayActivity.class);
            startActivity(intent);
        });
        tv_title = findViewById(R.id.tv_title);
        tv_artist = findViewById(R.id.tv_artist);

        rv_music = findViewById(R.id.rv_music);
        mAdapter = new BaseRecyclerAdapter<MusicBean.SongListBean>(this, R.layout.item_music_layout, null) {
            @Override
            public void convert(BaseViewHolder holder, MusicBean.SongListBean songListBean) {
                holder.setImageUrl(R.id.iv_icon,songListBean.getPic_small());
                holder.setText(R.id.tv_title, songListBean.getTitle());
                holder.setText(R.id.tv_singer, songListBean.getArtist_name());
                holder.setText(R.id.tv_time, timeFormat(songListBean.getFile_duration()));
            }
        };
        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                playService.setMp3Infos((ArrayList<MusicBean.SongListBean>) mAdapter.getmData());
                playService.play(position);
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rv_music.setLayoutManager(manager);
        rv_music.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        rv_music.setAdapter(mAdapter);
        rv_music.addOnScrollListener(new EndLessOnScrollListener(manager) {
            @Override
            public void onLoadMore(int currentPage) {
                initData(currentPage);
            }
        });
    }

    private void initData(int page) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(path+page*10)
                .addHeader("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:0.9.4)")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
                MusicBean musicBean = gson.fromJson(response.body().string(), MusicBean.class);
                for (MusicBean.SongListBean bean:musicBean.getSong_list()) {
                    setMusicUrl(bean);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.add2mData(musicBean.getSong_list());
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
    private void setMusicUrl(MusicBean.SongListBean bean) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(playpath+bean.getSong_id())
                .addHeader("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:0.9.4)")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject object = new JSONObject(response.body().string());
                    bean.setUrl(object.optJSONObject("bitrate").optString("file_link"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    String timeFormat(int minute){
        long  ms = minute * 1000 ;
        //毫秒数
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss",Locale.CHINA);
        //初始化Formatter的转换格式。

        String hms = formatter.format(ms);
        return hms;
    }

    class MusicReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if("Change_Music".equals(intent.getAction())){
                NotificationContentWrapper wrapper = ((MusicApp)getApplication()).getWrapper();
                tv_artist.setText(wrapper.summery);
                tv_title.setText(wrapper.title);
                iv_content.setImageBitmap(wrapper.bitmap);
                iv_play_or_pause.setImageResource(R.mipmap.ic_pause);
                playing = true;
            }else if("Change_Position".equals(intent.getAction())){
                NotificationContentWrapper wrapper = ((MusicApp)getApplication()).getWrapper();
                tv_artist.setText(wrapper.summery);
                tv_title.setText(wrapper.title);
                iv_content.setImageBitmap(wrapper.bitmap);
                iv_play_or_pause.setImageResource(R.mipmap.ic_pause);
                playing = true;
            }
        }
    }
}
