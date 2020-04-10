package com.ansoft.musetimusicplayer.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.ansoft.musetimusicplayer.Data.Song;
import com.ansoft.musetimusicplayer.R;
import com.ansoft.musetimusicplayer.Service.MusicService;
import com.ansoft.musetimusicplayer.Service.PC;
import com.ansoft.musetimusicplayer.Service.PlayerFunctions;
import com.ansoft.musetimusicplayer.SongListActivity;
import com.ansoft.musetimusicplayer.Util.widget.StringMatcher;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class SongListAdapter extends BaseAdapter implements SectionIndexer {
    ArrayList<Song> data;
    Activity activity;
    private String mSections = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#";


    public SongListAdapter(ArrayList<Song> data, Activity activity) {
        this.data = data;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View cv, ViewGroup parent) {
        if (isNetworkConnected() && (position % 10 == 0)) {
            cv = activity.getLayoutInflater().inflate(R.layout.item_ad_banner, parent, false);
            AdView mAdView = (AdView) cv.findViewById(R.id.adView);
            AdRequest.Builder builder=new AdRequest.Builder().addTestDevice("");
            AdRequest adRequest = builder.build();
            mAdView.loadAd(adRequest);

        } else {
            cv = activity.getLayoutInflater().inflate(R.layout.list_item_songs, parent, false);
            TextView songName = (TextView) cv.findViewById(R.id.songName);
            TextView artalbname = (TextView) cv.findViewById(R.id.artalbname);
            View divider = cv.findViewById(R.id.dividerView);
            if (position == data.size() - 1) {
                divider.setVisibility(View.GONE);
            }
            final Song item = data.get(position);
            songName.setText(item.getName());
            artalbname.setText(item.getAlbumName() + " - " + item.getArtistName());
            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SongListActivity.mpv.start();
                    PC.SONG_PAUSED = false;
                    PC.SONG_NUMBER = position;
                    boolean isServiceRunning = PlayerFunctions.isServiceRunning(MusicService.class.getName(), activity.getApplicationContext());
                    if (!isServiceRunning) {
                        Intent i = new Intent(activity.getApplicationContext(), MusicService.class);
                        activity.startService(i);
                    } else {
                        PC.SONG_CHANGE_HANDLER.sendMessage(PC.SONG_CHANGE_HANDLER.obtainMessage());
                    }

                }
            });
        }
        return cv;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public Object[] getSections() {
        String[] sections = new String[mSections.length()];
        for (int i = 0; i < mSections.length(); i++)
            sections[i] = String.valueOf(mSections.charAt(i));
        return sections;
    }

    @Override
    public int getPositionForSection(int section) {
        for (int i = section; i >= 0; i--) {
            for (int j = 0; j < getCount(); j++) {
                if (i == 0) {
                    // For numeric section
                    for (int k = 0; k <= 9; k++) {
                        if (StringMatcher.match(String.valueOf(data.get(j).getName().charAt(0)), String.valueOf(k)))
                            return j;
                    }
                } else {
                    if (StringMatcher.match(String.valueOf(data.get(j).getName().charAt(0)), String.valueOf(mSections.charAt(i))))
                        return j;
                }
            }
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }
}
