package com.heckbot.screenondemo;

import java.util.Calendar;
import java.util.Random;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

public class SchedulerReciever extends BroadcastReceiver {
    public final static String PROCESS_SHARED_PREFS = SchedulerReciever.class.getPackage().toString() + ".PROCESS_SHARED_PREFS";
    public final static String ENABLED = SchedulerReciever.class.getPackage().toString() + ".ENABLED";

    public static LiveCardService mService;
    public static boolean mBound = false;
    private static ServiceConnection mConnection;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
//            Log.d("onRecieve", "Alarm fired");
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (!pm.isScreenOn()){
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
                wl.acquire();

                // Do whatever you need right here
                // Dummy code creates random int, and reacts only if int in preset range
                Random rand = new Random();
                int  n = rand.nextInt(10) + 1;

                if (n <= 5) {
                    UpdateLiveCard("Meters from goal: " + n);
                }

                wl.release();
            }
            // schedules next alarm
            startStopSelf(context, true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startStopSelf(Context context, boolean start, boolean overrideSchedule) {
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(context, SchedulerReciever.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        mgr.cancel(pi);

        if (start) {
            mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 5000, pi);
        }

    }

    public static boolean isEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PROCESS_SHARED_PREFS, 0);
        return prefs.getBoolean(ENABLED, false);
    }


    public static void setEnabled(Context context, boolean running) {
        SharedPreferences prefs = context.getSharedPreferences(PROCESS_SHARED_PREFS, 0);
        Editor edit = prefs.edit();
        edit.putBoolean(ENABLED, running);
        edit.apply();

        // creates live card service with connection to service for updates
        Intent intentLiveCard = new Intent(context, LiveCardService.class);
        if(running) {
            Log.d("SchedulerReciever", "starting LiveCardService");
            context.startService(intentLiveCard);

            /** Defines callbacks for service binding, passed to bindService() */
            mConnection = new ServiceConnection() {

                @Override
                public void onServiceConnected(ComponentName className, IBinder service) {
                    // We've bound to LocalService, cast the IBinder and get LocalService instance
                    LiveCardService.MyLocalBinder binder = (LiveCardService.MyLocalBinder) service;
                    mService = binder.getService();
                    mBound = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName arg0) {
                    mBound = false;
                }
            };


            // Bind to LocalService
            Intent intent = new Intent(context, LiveCardService.class);
            context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        }
        else{
            // Unbind from the service
            if (mBound) {
                context.unbindService(mConnection);
                mBound = false;
            }
            Log.d("SchedulerReciever", "stopping LiveCardService");
            context.stopService(intentLiveCard);
        }
    }

    public static void actionEnable(Context context) {
        setEnabled(context, true);
        startStopSelf(context, true, true);
    }

    public static void actionDisable(Context context) {
        setEnabled(context, false);
        startStopSelf(context, false, false);
    }

    // Calls live card service to update the text
    public static void UpdateLiveCard(String string){

        if (mBound) {
            // Call a method from the LocalService.
            // However, if this call were something that might hang, then this request should
            // occur in a separate thread to avoid slowing down the activity performance.
            mService.updateResults(string);
        }
    }
}
