package com.ansoft.musetimusicplayer.Service;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.ansoft.musetimusicplayer.Data.Song;
import com.ansoft.musetimusicplayer.R;
import com.ansoft.musetimusicplayer.SongListActivity;

public class MusicService extends Service implements AudioManager.OnAudioFocusChangeListener{
	String LOG_CLASS = "MusicService";
	public static MediaPlayer mp;
	int NOTIFICATION_ID = 1111;
	public static final String NOTIFY_PREVIOUS = "com.ansoft.musetimusicplayer.previous";
	public static final String NOTIFY_DELETE = "com.ansoft.musetimusicplayer.delete";
	public static final String NOTIFY_PAUSE = "com.ansoft.musetimusicplayer.pause";
	public static final String NOTIFY_PLAY = "com.ansoft.musetimusicplayer.play";
	public static final String NOTIFY_NEXT = "com.ansoft.musetimusicplayer.next";

	private ComponentName remoteComponentName;
	private RemoteControlClient remoteControlClient;
	AudioManager audioManager;
	Bitmap mDummyAlbumArt;
	private static Timer timer;
	private static boolean currentVersionSupportBigNotification = false;
	private static boolean currentVersionSupportLockScreenControls = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		mp = new MediaPlayer();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        currentVersionSupportBigNotification = PlayerFunctions.currentVersionSupportBigNotification();
        currentVersionSupportLockScreenControls = PlayerFunctions.currentVersionSupportLockScreenControls();
        timer = new Timer();
        mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				MusicControls.nextControl(getApplicationContext());
			}
		});
		super.onCreate();
	}

	/**
	 * Send message from timer
	 * @author jonty.ankit
	 */
	private class MainTask extends TimerTask{
        public void run(){
            handler.sendEmptyMessage(0);
        }
    }

	 private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
        	if(mp != null){
        		int progress = (mp.getCurrentPosition()*100) / mp.getDuration();
        		Integer i[] = new Integer[3];
        		i[0] = mp.getCurrentPosition();
        		i[1] = mp.getDuration();
        		i[2] = progress;
        		try{
        			PC.PROGRESSBAR_HANDLER.sendMessage(PC.PROGRESSBAR_HANDLER.obtainMessage(0, i));
        		}catch(Exception e){}
        	}
    	}
    };

    @SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			if(PC.SONGS_LIST.size() <= 0){
				PC.SONGS_LIST = PlayerFunctions.listOfSongs(getApplicationContext());
			}
			Song data = PC.SONGS_LIST.get(PC.SONG_NUMBER);
			if(currentVersionSupportLockScreenControls){
				RegisterRemoteClient();
			}
			String songPath = data.getData();
			playSong(songPath, data);
			newNotification();

			PC.SONG_CHANGE_HANDLER = new Handler(new Callback() {
				@Override
				public boolean handleMessage(Message msg) {
					Song data = PC.SONGS_LIST.get(PC.SONG_NUMBER);
					String songPath = data.getData();
					newNotification();
					try{
						playSong(songPath, data);
						SongListActivity.updateUI(data, getApplicationContext(), false);
					}catch(Exception e){
						e.printStackTrace();
					}
					return false;
				}
			});

			PC.PLAY_PAUSE_HANDLER = new Handler(new Callback() {
				@Override
				public boolean handleMessage(Message msg) {
					String message = (String)msg.obj;
					boolean pause=false;
					if(mp == null)
						return false;
					if(message.equalsIgnoreCase(getResources().getString(R.string.play))){
						PC.SONG_PAUSED = false;
						if(currentVersionSupportLockScreenControls){
							remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
						}
						pause=false;
						mp.start();
					}else if(message.equalsIgnoreCase(getResources().getString(R.string.pause))){
						PC.SONG_PAUSED = true;
						if(currentVersionSupportLockScreenControls){
							remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
						}
						mp.pause();
						pause=true;
					}
					newNotification();
					try{
						SongListActivity.updateUI(PC.SONGS_LIST.get(PC.SONG_NUMBER), getApplicationContext(), pause);
					}catch(Exception e){}
					Log.d("TAG", "TAG Pressed: " + message);
					return false;
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
		return START_STICKY;
	}

	/**
	 * Notification
	 * Custom Bignotification is available from API 16
	 */
	@SuppressLint("NewApi")
	private void newNotification() {
		String songName = PC.SONGS_LIST.get(PC.SONG_NUMBER).getName();
		String albumName = PC.SONGS_LIST.get(PC.SONG_NUMBER).getAlbumName();
		RemoteViews simpleContentView = new RemoteViews(getApplicationContext().getPackageName(),R.layout.custom_notification);
		RemoteViews expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.noty_large);

		Notification notification = new NotificationCompat.Builder(getApplicationContext())
        .setSmallIcon(R.drawable.ic_music)
        .setContentTitle(songName).build();

		setListeners(simpleContentView);
		setListeners(expandedView);

		notification.contentView = simpleContentView;
		if(currentVersionSupportBigNotification){
			notification.bigContentView = expandedView;
		}

		try{
			long albumId = PC.SONGS_LIST.get(PC.SONG_NUMBER).getAlbumId();
			Bitmap albumArt = PlayerFunctions.getAlbumart(getApplicationContext(), albumId);
			if(albumArt != null){
				notification.contentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
				if(currentVersionSupportBigNotification){
					notification.bigContentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
				}
			}else{
				notification.contentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.default_thumbanil);
				if(currentVersionSupportBigNotification){
					notification.bigContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.default_thumbanil);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		if(PC.SONG_PAUSED){
			notification.contentView.setViewVisibility(R.id.btnPause, View.GONE);
			notification.contentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);

			if(currentVersionSupportBigNotification){
				notification.bigContentView.setViewVisibility(R.id.btnPause, View.GONE);
				notification.bigContentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
			}
		}else{
			notification.contentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
			notification.contentView.setViewVisibility(R.id.btnPlay, View.GONE);

			if(currentVersionSupportBigNotification){
				notification.bigContentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
				notification.bigContentView.setViewVisibility(R.id.btnPlay, View.GONE);
			}
		}

		notification.contentView.setTextViewText(R.id.textSongName, songName);
		notification.contentView.setTextViewText(R.id.textAlbumName, albumName);
		if(currentVersionSupportBigNotification){
			notification.bigContentView.setTextViewText(R.id.textSongName, songName);
			notification.bigContentView.setTextViewText(R.id.textAlbumName, albumName);
		}
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		startForeground(NOTIFICATION_ID, notification);
	}



	public void setListeners(RemoteViews view) {
		Intent previous = new Intent(NOTIFY_PREVIOUS);
		Intent delete = new Intent(NOTIFY_DELETE);
		Intent pause = new Intent(NOTIFY_PAUSE);
		Intent next = new Intent(NOTIFY_NEXT);
		Intent play = new Intent(NOTIFY_PLAY);

		PendingIntent pPrevious = PendingIntent.getBroadcast(getApplicationContext(), 0, previous, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPrevious, pPrevious);

		PendingIntent pDelete = PendingIntent.getBroadcast(getApplicationContext(), 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnDelete, pDelete);

		PendingIntent pPause = PendingIntent.getBroadcast(getApplicationContext(), 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPause, pPause);

		PendingIntent pNext = PendingIntent.getBroadcast(getApplicationContext(), 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnNext, pNext);

		PendingIntent pPlay = PendingIntent.getBroadcast(getApplicationContext(), 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPlay, pPlay);

	}

	@Override
	public void onDestroy() {
		if(mp != null){
			mp.stop();
			mp = null;
		}
		super.onDestroy();
	}

	/**
	 * Play song, Update Lockscreen fields
	 * @param songPath
	 * @param data
	 */
	@SuppressLint("NewApi")
	private void playSong(String songPath, Song data) {
		try {
			if(currentVersionSupportLockScreenControls){
				UpdateMetadata(data);
				remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
			}
			mp.reset();
			mp.setDataSource(songPath);
			mp.prepare();
			mp.start();
			timer.scheduleAtFixedRate(new MainTask(), 0, 100);
			SongListActivity.updateUI(data, getApplicationContext(), false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@SuppressLint("NewApi")
	private void RegisterRemoteClient(){
		remoteComponentName = new ComponentName(getApplicationContext(), new NotificationBroadcast().ComponentName());
		 try {
		   if(remoteControlClient == null) {
			   audioManager.registerMediaButtonEventReceiver(remoteComponentName);
			   Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			   mediaButtonIntent.setComponent(remoteComponentName);
			   PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
			   remoteControlClient = new RemoteControlClient(mediaPendingIntent);
			   audioManager.registerRemoteControlClient(remoteControlClient);
		   }
		   remoteControlClient.setTransportControlFlags(
				   RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
				   RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
				   RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE |
				   RemoteControlClient.FLAG_KEY_MEDIA_STOP |
				   RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
				   RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
	  }catch(Exception ex) {
	  }
	}

	@SuppressLint("NewApi")
	private void UpdateMetadata(Song data){
		if (remoteControlClient == null)
			return;
		MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, data.getAlbumName());
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, data.getArtistName());
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, data.getName());
		mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.player_bg);
		if(mDummyAlbumArt == null){
			mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.player_bg);
		}
		metadataEditor.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, mDummyAlbumArt);
		metadataEditor.apply();
		audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
	}

	@Override
	public void onAudioFocusChange(int focusChange) {}
}