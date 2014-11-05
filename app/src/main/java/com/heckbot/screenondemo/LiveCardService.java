package com.heckbot.screenondemo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

public class LiveCardService extends Service {

	private static final String LIVE_CARD_TAG = "LiveCardDemo";

	private LiveCard mLiveCard;
	private RemoteViews mLiveCardView;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {

            // Get an instance of a live card
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);

            // Inflate a layout into a remote view
            mLiveCardView = new RemoteViews(getPackageName(), R.layout.live_card_layout);

            mLiveCardView.setTextViewText(R.id.tvCardText, "Start");

            // Publish the live card
//			mLiveCard.publish(PublishMode.REVEAL);
            mLiveCard.publish(PublishMode.SILENT);
            Log.d("LiveCardService","card published");

            // Queue the update text runnable
            mLiveCard.setViews(mLiveCardView);

        }
        return START_STICKY;
    }

	@Override
	public void onDestroy() {
		if (mLiveCard != null && mLiveCard.isPublished()) {
			// Stop the handler from queuing more Runnable jobs

			mLiveCard.unpublish();
			mLiveCard = null;
		}
		super.onDestroy();
	}

    private final IBinder myBinder = new MyLocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return myBinder;
	}

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class MyLocalBinder extends Binder {
        LiveCardService getService() {
            return LiveCardService.this;
        }
    }

    public void updateResults(String string){
        mLiveCardView.setTextViewText(R.id.tvCardText, string);

        // Always call setViews() to update the live card's RemoteViews.
        mLiveCard.setViews(mLiveCardView);

        // Move to the live card before turning on the screen
        mLiveCard.navigate();

        // Turn on the screen
        Log.d("LiveCardService", "Turning on screen");
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "My Tag");
        wl.acquire();
        wl.release();
    }
}
