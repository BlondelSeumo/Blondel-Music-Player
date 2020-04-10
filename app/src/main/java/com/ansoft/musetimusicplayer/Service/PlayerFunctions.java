package com.ansoft.musetimusicplayer.Service;

import java.io.FileDescriptor;
import java.util.ArrayList;

import com.ansoft.musetimusicplayer.Data.Song;
import com.ansoft.musetimusicplayer.R;


import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

public class PlayerFunctions {
	static String LOG_CLASS = "PlayerFunctions";

	/**
	 * Check if service is running or not
	 * @param serviceName
	 * @param context
	 * @return
	 */
	public static boolean isServiceRunning(String serviceName, Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for(RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if(serviceName.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Read the songs present in external storage
	 * @param context
	 * @return
	 */
	public static ArrayList<Song> listOfSongs(Context context){
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor c = context.getContentResolver().query(uri, null, MediaStore.Audio.Media.IS_MUSIC + " != 0", null, MediaStore.Audio.Media.TITLE);
		ArrayList<Song> listOfSongs = new ArrayList<Song>();
		c.moveToFirst();
		while(c.moveToNext()){
			Song songData = new Song();
			
			String title = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE));
			String artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
			String album = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM));
			long duration = c.getLong(c.getColumnIndex(MediaStore.Audio.Media.DURATION));
			String data = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
			long albumId = c.getLong(c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
			String composer = c.getString(c.getColumnIndex(MediaStore.Audio.Media.COMPOSER));
			
			songData.setName(title);
			songData.setAlbumName(album);
			songData.setArtistName(artist);
			songData.setDuration(duration);
			songData.setData(data);
			songData.setAlbumId(albumId);
			songData.setComposer(composer);
			listOfSongs.add(songData);
		}
		c.close();
		Log.d("SIZE", "SIZE: " + listOfSongs.size());
		return listOfSongs;
	}

	public static Bitmap getAlbumart(Context context,Long album_id){
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
	    try{
	        final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
	        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
	        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
	        if (pfd != null){
	            FileDescriptor fd = pfd.getFileDescriptor();
	            bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
	            pfd = null;
	            fd = null;
	        }
	    } catch(Error ee){}
	    catch (Exception e) {}
	    return bm;
	}

	/**
	 * @param context
	 * @return
	 */
	public static Bitmap getDefaultAlbumArt(Context context){
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
	    try{
	    	bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_thumbanil, options);
	    } catch(Error ee){}
	    catch (Exception e) {}
	    return bm;
	}
	/**
	 * Convert milliseconds into time hh:mm:ss
	 * @param milliseconds
	 * @return time in String
	 */
	public static String getDuration(long milliseconds) {
		long sec = (milliseconds / 1000) % 60;
		long min = (milliseconds / (60 * 1000))%60;
		long hour = milliseconds / (60 * 60 * 1000);

		String s = (sec < 10) ? "0" + sec : "" + sec;
		String m = (min < 10) ? "0" + min : "" + min;
		String h = "" + hour;
		
		String time = "";
		if(hour > 0) {
			time = h + ":" + m + ":" + s;
		} else {
			time = m + ":" + s;
		}
		return time;
	}
	
	public static boolean currentVersionSupportBigNotification() {
		int sdkVersion = android.os.Build.VERSION.SDK_INT;
		if(sdkVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN){
			return true;
		}
		return false;
	}
	
	public static boolean currentVersionSupportLockScreenControls() {
		int sdkVersion = android.os.Build.VERSION.SDK_INT;
		if(sdkVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
			return true;
		}
		return false;
	}
}
