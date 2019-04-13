package cn.wang.glidedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    String path = "http://tingapi.ting.baidu.com/v1/restserver/ting?format=json&calback=&from=webapp_music&method=baidu.ting.billboard.billList&type=1&size=10&offset=";
    private RecyclerView rv_music;
    private BaseRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData(0);
    }

    private void initView() {
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
                Toast.makeText(MainActivity.this, "you click:"+position, Toast.LENGTH_SHORT).show();
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
                //System.out.println(response.body().string());
                Gson gson = new Gson();
                MusicBean musicBean = gson.fromJson(response.body().string(), MusicBean.class);
                for (MusicBean.SongListBean bean:musicBean.getSong_list()) {
                    System.out.println(bean.getTitle());
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

    String timeFormat(int minute){
        long  ms = minute * 1000 ;//毫秒数
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");//初始化Formatter的转换格式。

        String hms = formatter.format(ms);
        return hms;
    }
}
