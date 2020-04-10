package com.ansoft.musetimusicplayer.Service;

import java.util.ArrayList;
import android.os.Handler;

import com.ansoft.musetimusicplayer.Data.Song;

public class PC {
	//List of Songs
	public static ArrayList<Song> SONGS_LIST = new ArrayList<Song>();
	//song number which is playing right now from SONGS_LIST
	public static int SONG_NUMBER = 0;
	//song is playing or paused
	public static boolean SONG_PAUSED = true;
	//song changed (next, previous)
	public static boolean SONG_CHANGED = false;
	//handler for song changed(next, previous) defined in service(MusicService)
	public static Handler SONG_CHANGE_HANDLER;
	//handler for song play/pause defined in service(MusicService)
	public static Handler PLAY_PAUSE_HANDLER;
	//handler for showing song progress defined in Activities(MainActivity, AudioPlayerActivity)
	public static Handler PROGRESSBAR_HANDLER;
}
