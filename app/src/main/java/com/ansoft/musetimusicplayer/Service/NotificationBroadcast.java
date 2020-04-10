package com.ansoft.musetimusicplayer.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.ansoft.musetimusicplayer.SongListActivity;

public class NotificationBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;

            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                	if(!PC.SONG_PAUSED){
    					MusicControls.pauseControl(context);
                	}else{
    					MusicControls.playControl(context);
                	}
                	break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                	break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                	break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                	break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                	Log.d("TAG", "TAG: KEYCODE_MEDIA_NEXT");
                	MusicControls.nextControl(context);
                	break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                	Log.d("TAG", "TAG: KEYCODE_MEDIA_PREVIOUS");
                	MusicControls.previousControl(context);
                	break;
            }
		}  else{
            	if (intent.getAction().equals(MusicService.NOTIFY_PLAY)) {
    				MusicControls.playControl(context);
        		} else if (intent.getAction().equals(MusicService.NOTIFY_PAUSE)) {
    				MusicControls.pauseControl(context);
        		} else if (intent.getAction().equals(MusicService.NOTIFY_NEXT)) {
        			MusicControls.nextControl(context);
        		} else if (intent.getAction().equals(MusicService.NOTIFY_DELETE)) {
					Intent i = new Intent(context, MusicService.class);
					context.stopService(i);
					Intent in = new Intent(context, SongListActivity.class);
			        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			        context.startActivity(in);
        		}else if (intent.getAction().equals(MusicService.NOTIFY_PREVIOUS)) {
    				MusicControls.previousControl(context);
        		}
		}
	}
	
	public String ComponentName() {
		return this.getClass().getName(); 
	}
}
