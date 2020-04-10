package com.ansoft.musetimusicplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ansoft.musetimusicplayer.Adapter.SongListAdapter;
import com.ansoft.musetimusicplayer.Data.Song;
import com.ansoft.musetimusicplayer.Service.MusicControls;
import com.ansoft.musetimusicplayer.Service.MusicService;
import com.ansoft.musetimusicplayer.Service.PlayerFunctions;
import com.ansoft.musetimusicplayer.Util.widget.IndexableListView;
import com.ansoft.musetimusicplayer.Util.widget.PlayerPanel.PlayerPanelView;
import com.ansoft.musetimusicplayer.Util.widget.PlayerPanel.playerview.MusicPlayerView;

public class SongListActivity extends Activity {


    static int currentTrack = 0;
    static int pausedProgress = 0;
    RelativeLayout playerPanel;
    public static SeekBar trackseekbar;
    public static SeekBar volumeseekbar;
    public static TextView panelMusicTitle, insideMusicTitle, insideMusicArtist;
    public static ImageView panelPlayPauseBtn, nextBtn, prevBtn;
    public static PlayerPanelView playerPanelView;
    public static RelativeLayout playPauseLayout;
    public static MusicPlayerView mpv;
    static Bitmap bb;
    IndexableListView listView;
    SongListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);
        trackseekbar = (SeekBar) findViewById(R.id.seekBar);
        bb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_music_icon);
        volumeseekbar = (SeekBar) findViewById(R.id.seekBar2);
        playerPanel = (RelativeLayout) findViewById(R.id.playerPanel);
        playerPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        listView = (IndexableListView) findViewById(R.id.list);
        listView.setFastScrollEnabled(true);
        adapter = new SongListAdapter( PlayerFunctions.listOfSongs(getApplicationContext()), SongListActivity.this);
        listView.setAdapter(adapter);
        panelMusicTitle = (TextView) findViewById(R.id.textView);
        insideMusicTitle = (TextView) findViewById(R.id.textViewSong);
        insideMusicArtist = (TextView) findViewById(R.id.textViewSinger);
        panelPlayPauseBtn = (ImageView) findViewById(R.id.imageView3);
        nextBtn = (ImageView) findViewById(R.id.next);
        prevBtn = (ImageView) findViewById(R.id.previous);
        mpv = (MusicPlayerView) findViewById(R.id.mpv);

        panelPlayPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicService.mp.isPlaying()) {
                    panelPlayPauseBtn.setImageResource(R.drawable.ic_play);
                    MusicControls.pauseControl(getApplicationContext());
                } else {
                    panelPlayPauseBtn.setImageResource(R.drawable.ic_pause);
                    MusicControls.playControl(getApplicationContext());
                }
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicControls.nextControl(getApplicationContext());
                currentTrack += 1;
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MusicControls.previousControl(getApplicationContext());
                currentTrack -= 1;
            }
        });
        playerPanelView = (PlayerPanelView) findViewById(R.id.sliding_layout);
        playPauseLayout = (RelativeLayout) findViewById(R.id.playPauseLayout);
        if (MusicService.mp != null) {
            if (!MusicService.mp.isPlaying()) {
                playerPanelView.setPanelHeight(0);
            } else {
                playerPanelView.setPanelHeight(playPauseLayout.getHeight());
            }
        } else {

            playerPanelView.setPanelHeight(0);
        }
        playerPanelView.addPanelSlideListener(new PlayerPanelView.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, PlayerPanelView.PanelState previousState, PlayerPanelView.PanelState newState) {
                if (newState == PlayerPanelView.PanelState.EXPANDED) {
                    playPauseLayout.setVisibility(View.GONE);
                } else {
                    playPauseLayout.setVisibility(View.VISIBLE);
                }
            }
        });

    }



    public static void updateUI(Song song, final Context activity, boolean pause) {
        playerPanelView.setPanelHeight(playPauseLayout.getHeight());
        panelMusicTitle.setText(song.getName());
        mpv.setProgress(pausedProgress);
        mpv.setBitmapCover(BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_thumbanil));
        mpv.setMax((int) (MusicService.mp.getDuration() / 1000));
        insideMusicTitle.setText(song.getName());
        insideMusicArtist.setText(song.getArtistName());
        trackseekbar.setMax(MusicService.mp.getDuration() / 1000);
        trackseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MusicService.mp.seekTo(progress * 1000);
                mpv.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        volumeseekbar.setMax(100);
        volumeseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                final int MAX_VOLUME = 100;
                float volume = (float) (1 - (Math.log(MAX_VOLUME - progress) / Math.log(MAX_VOLUME)));
                MusicService.mp.setVolume(volume, volume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mpv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicService.mp.isPlaying()) {
                    pausedProgress = MusicService.mp.getCurrentPosition();
                    MusicControls.pauseControl(activity);
                } else {
                    MusicControls.playControl(activity);

                }
            }
        });
        if (pause) {
            mpv.stop();
            panelPlayPauseBtn.setImageResource(R.drawable.ic_play);
        } else {
            mpv.start();
            mpv.setProgress(pausedProgress);
            panelPlayPauseBtn.setImageResource(R.drawable.ic_pause);
        }
    }

    public static int getCurrentTrack() {
        return currentTrack;
    }

    public static void setCurrentTrack(int currentTrack) {
        SongListActivity.currentTrack = currentTrack;
    }
}
