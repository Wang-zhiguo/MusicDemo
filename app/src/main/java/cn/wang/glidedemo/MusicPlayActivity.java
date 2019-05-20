package cn.wang.glidedemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;

import cn.wang.glidedemo.lrc.LrcView;

public class MusicPlayActivity extends AppCompatActivity {
    private LrcView lrcView;
    private SeekBar seekBar;
    private Button btnPlayPause;

    private MusicLrcReceiver receiver;
    private MusicApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);

        lrcView = (LrcView) findViewById(R.id.lrc_view);
        seekBar = (SeekBar) findViewById(R.id.progress_bar);
        btnPlayPause = (Button) findViewById(R.id.btn_play_pause);
        app = (MusicApp) getApplication();
        System.out.println("---"+app.wrapper.lrcUrl);
        lrcView.loadLrcByUrl(app.wrapper.lrcUrl);

        receiver = new MusicLrcReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("Change_Music");
        filter.addAction("Change_Position");
        registerReceiver(receiver,filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    class MusicLrcReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if("Change_Music".equals(intent.getAction())){
                System.out.println("---"+app.wrapper.lrcUrl);
                lrcView.loadLrcByUrl(app.wrapper.lrcUrl);
            }else if("Change_Position".equals(intent.getAction())){
                int pos = intent.getIntExtra("pos",0);
                int duration = intent.getIntExtra("duration",0);
                seekBar.setMax(duration);
                seekBar.setProgress(pos/1000);
                lrcView.updateTime(pos);
            }
        }
    }
}
