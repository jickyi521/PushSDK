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
<<<<<<< HEAD
                    PushA2DMService.actionStart(TestKeepAlive.this);
                    break;
                case R.id.stop:
                    PushA2DMService.actionStop(TestKeepAlive.this);
                    break;
                case R.id.ping:
                    PushA2DMService.actionPing(TestKeepAlive.this);
=======
                    PushService.actionStart(TestKeepAlive.this);
                    break;
                case R.id.stop:
                    PushService.actionStop(TestKeepAlive.this);
                    break;
                case R.id.ping:
                    PushService.actionPing(TestKeepAlive.this);
>>>>>>> 1220c312c5c9d3868b100b6d6a0d9f152da03a85
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