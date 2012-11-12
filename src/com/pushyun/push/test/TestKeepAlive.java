package com.pushyun.push.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.pushyun.push.R;

public class TestKeepAlive extends Activity
{
    public static final String LOG_TAG = "TestKeepAlive";

    private final OnClickListener mClicked = new OnClickListener()
    {
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.start:
                    PushService.actionStart(TestKeepAlive.this);
                    break;
                case R.id.stop:
                    PushService.actionStop(TestKeepAlive.this);
                    break;
                case R.id.ping:
                    PushService.actionPing(TestKeepAlive.this);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.keep_alvie_main);

        findViewById(R.id.start).setOnClickListener(mClicked);
        findViewById(R.id.stop).setOnClickListener(mClicked);
        findViewById(R.id.ping).setOnClickListener(mClicked);
    }
}