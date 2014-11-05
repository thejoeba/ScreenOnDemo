package com.heckbot.screenondemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ConfigureActivity extends Activity {

	TextView enableText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        // keeps the screen from going to sleeep
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_configure);
		enableText = (TextView)findViewById(R.id.enabledisable);

		updateText();
	}
	
	void updateText() {
		final boolean enabled = SchedulerReciever.isEnabled(this);
		
		if (enabled) {
			enableText.setText(R.string.config_on);
			enableText.setTextColor(Color.GREEN);
		} else {
			enableText.setText(R.string.config_off);
			enableText.setTextColor(Color.RED);
		}
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        final boolean enabled = SchedulerReciever.isEnabled(this);
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (enabled) {
                // Starts the 5 second alarm
                SchedulerReciever.actionDisable(this);
            } else {
                // Stops the alarm
                SchedulerReciever.actionEnable(this);
            }
            updateText();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

	@Override
	protected void onStart() {
		super.onStart();
		updateText();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateText();
	}
}
