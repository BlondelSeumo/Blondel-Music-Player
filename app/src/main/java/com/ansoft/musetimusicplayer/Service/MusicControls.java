package com.ansoft.musetimusicplayer.Service;

import android.content.Context;

import com.ansoft.musetimusicplayer.R;

public class MusicControls {
	public static void playControl(Context context) {
		sendMessage(context.getResources().getString(R.string.play));
	}

	public static void pauseControl(Context context) {
		sendMessage(context.getResources().getString(R.string.pause));
	}

	public static void nextControl(Context context) {
		boolean isServiceRunning = PlayerFunctions.isServiceRunning(MusicService.class.getName(), context);
		if (!isServiceRunning)
			return;
		if(PC.SONGS_LIST.size() > 0 ){
			if(PC.SONG_NUMBER < (PC.SONGS_LIST.size()-1)){
				PC.SONG_NUMBER++;
				PC.SONG_CHANGE_HANDLER.sendMessage(PC.SONG_CHANGE_HANDLER.obtainMessage());
			}else{
				PC.SONG_NUMBER = 0;
				PC.SONG_CHANGE_HANDLER.sendMessage(PC.SONG_CHANGE_HANDLER.obtainMessage());
			}
		}
		PC.SONG_PAUSED = false;
	}

	public static void previousControl(Context context) {
		boolean isServiceRunning = PlayerFunctions.isServiceRunning(MusicService.class.getName(), context);
		if (!isServiceRunning)
			return;
		if(PC.SONGS_LIST.size() > 0 ){
			if(PC.SONG_NUMBER > 0){
				PC.SONG_NUMBER--;
				PC.SONG_CHANGE_HANDLER.sendMessage(PC.SONG_CHANGE_HANDLER.obtainMessage());
			}else{
				PC.SONG_NUMBER = PC.SONGS_LIST.size() - 1;
				PC.SONG_CHANGE_HANDLER.sendMessage(PC.SONG_CHANGE_HANDLER.obtainMessage());
			}
		}
		PC.SONG_PAUSED = false;
	}
	
	private static void sendMessage(String message) {
		try{
			PC.PLAY_PAUSE_HANDLER.sendMessage(PC.PLAY_PAUSE_HANDLER.obtainMessage(0, message));
		}catch(Exception e){}
	}
}
