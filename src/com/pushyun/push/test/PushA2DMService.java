package com.pushyun.push.test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Date;

import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import com.pushyun.push.R;
import com.pushyun.push.utilities.CommonUtilities;

public class PushA2DMService extends Service
{

    public static final String LOG_TAG = "PushA2DMService";

    private static final String HOST = "http://192.168.196.58";
    private static final int PORT = 80;

    private static final String ACTION_START = "org.devtcg.demo.keepalive.START";
    private static final String ACTION_STOP = "org.devtcg.demo.keepalive.STOP";
    private static final String ACTION_KEEPALIVE = "org.devtcg.demo.keepalive.KEEP_ALIVE";
    private static final String ACTION_RECONNECT = "org.devtcg.demo.keepalive.RECONNECT";

    private ConnectionLog mLog;

    private ConnectivityManager mConnMan;
    private NotificationManager mNotifMan;

    private boolean mStarted;
    private A2DMConnectionThread mConnection;

    private static final long KEEP_ALIVE_INTERVAL = 1000 * 60 * 28;

    private static final long INITIAL_RETRY_INTERVAL = 1000 * 10;
    private static final long MAXIMUM_RETRY_INTERVAL = 1000 * 60 * 30;

    private SharedPreferences mPrefs;

    private static final int NOTIF_CONNECTED = 0;

    private static final String PREF_STARTED = "isStarted";

    public static void actionStart(Context ctx)
    {
        Intent i = new Intent(ctx, PushA2DMService.class);
        i.setAction(ACTION_START);
        ctx.startService(i);
    }

    public static void actionStop(Context ctx)
    {
        Intent i = new Intent(ctx, PushA2DMService.class);
        i.setAction(ACTION_STOP);
        ctx.startService(i);
    }

    public static void actionPing(Context ctx)
    {
        Intent i = new Intent(ctx, PushA2DMService.class);
        i.setAction(ACTION_KEEPALIVE);
        ctx.startService(i);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        try
        {
            mLog = new ConnectionLog();
            Log.i(LOG_TAG, "Opened log at " + mLog.getPath());
        }
        catch (IOException e)
        {
        }

        mPrefs = getSharedPreferences(LOG_TAG, MODE_PRIVATE);

        mConnMan = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

        mNotifMan = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        /*
         * If our process was reaped by the system for any reason we need to restore our state with
         * merely a call to onCreate. We record the last "started" value and restore it here if
         * necessary.
         */
        handleCrashedService();
    }

    private void handleCrashedService()
    {
        if (wasStarted() == true)
        {
            /*
             * We probably didn't get a chance to clean up gracefully, so do it now.
             */
            hideNotification();
            stopKeepAlives();

            /* Formally start and attempt connection. */
            start();
        }
    }

    @Override
    public void onDestroy()
    {
        log("Service destroyed (started=" + mStarted + ")");

        if (mStarted == true) stop();

        try
        {
            if (mLog != null) mLog.close();
        }
        catch (IOException e)
        {
        }
    }

    private void log(String message)
    {
        Log.i(LOG_TAG, message);

        if (mLog != null)
        {
            try
            {
                mLog.println(message);
            }
            catch (IOException e)
            {
            }
        }
    }

    private boolean wasStarted()
    {
        return mPrefs.getBoolean(PREF_STARTED, false);
    }

    private void setStarted(boolean started)
    {
        mPrefs.edit().putBoolean(PREF_STARTED, started).commit();
        mStarted = started;
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        log("Service started with intent=" + intent);

        super.onStart(intent, startId);

        testStartCommand(intent);

        if (intent.getAction().equals(ACTION_STOP) == true)
        {
            stop();
            stopSelf();
        }
        else if (intent.getAction().equals(ACTION_START) == true)
        {
            start();

        }
        else if (intent.getAction().equals(ACTION_KEEPALIVE) == true) keepAlive();
        else if (intent.getAction().equals(ACTION_RECONNECT) == true) reconnectIfNecessary();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private synchronized void start()
    {
        if (mStarted == true)
        {
            Log.w(LOG_TAG, "Attempt to start connection that is already active");
            return;
        }

        setStarted(true);

        registerReceiver(mConnectivityChanged, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        log("Connecting...");

        mConnection = new A2DMConnectionThread(HOST, PORT);
        mConnection.start();
    }

    private synchronized void stop()
    {
        if (mStarted == false)
        {
            Log.w(LOG_TAG, "Attempt to stop connection not active.");
            return;
        }

        setStarted(false);

        unregisterReceiver(mConnectivityChanged);
        cancelReconnect();

        if (mConnection != null)
        {
            mConnection.abort();
            mConnection = null;
        }
    }

    private synchronized void keepAlive()
    {
        try
        {
            if (mStarted == true && mConnection != null) mConnection.sendKeepAlive();
        }
        catch (IOException e)
        {
        }
    }

    private void startKeepAlives()
    {
        Intent i = new Intent();
        i.setClass(this, PushA2DMService.class);
        i.setAction(ACTION_KEEPALIVE);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + KEEP_ALIVE_INTERVAL, KEEP_ALIVE_INTERVAL, pi);
    }

    private void stopKeepAlives()
    {
        Intent i = new Intent();
        i.setClass(this, PushA2DMService.class);
        i.setAction(ACTION_KEEPALIVE);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmMgr.cancel(pi);
    }

    public void scheduleReconnect(long startTime)
    {
        long interval = mPrefs.getLong("retryInterval", INITIAL_RETRY_INTERVAL);

        long now = System.currentTimeMillis();
        long elapsed = now - startTime;

        if (elapsed < interval) interval = Math.min(interval * 4, MAXIMUM_RETRY_INTERVAL);
        else interval = INITIAL_RETRY_INTERVAL;

        log("Rescheduling connection in " + interval + "ms.");

        mPrefs.edit().putLong("retryInterval", interval).commit();

        Intent i = new Intent();
        i.setClass(this, PushA2DMService.class);
        i.setAction(ACTION_RECONNECT);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, now + interval, pi);
    }

    public void cancelReconnect()
    {
        Intent i = new Intent();
        i.setClass(this, PushA2DMService.class);
        i.setAction(ACTION_RECONNECT);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmMgr.cancel(pi);
    }

    private synchronized void reconnectIfNecessary()
    {
        if (mStarted == true && mConnection == null)
        {
            log("Reconnecting...");

            mConnection = new A2DMConnectionThread(HOST, PORT);
            mConnection.start();
        }
    }

    private BroadcastReceiver mConnectivityChanged = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            NetworkInfo info = mConnMan.getActiveNetworkInfo();
            // NetworkInfo info =
            // (NetworkInfo)intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

            boolean hasConnectivity = (info != null && info.isConnected()) ? true : false;

            log("Connecting changed: connected=" + hasConnectivity);

            if (hasConnectivity) reconnectIfNecessary();
        }
    };

    private void showNotification()
    {
        Notification n = new Notification();

        n.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        n.icon = R.drawable.connected_notify;
        n.when = System.currentTimeMillis();

        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, TestKeepAlive.class), 0);

        n.setLatestEventInfo(this, "KeepAlive connected", "Connected to " + HOST + ":" + PORT, pi);

        mNotifMan.notify(NOTIF_CONNECTED, n);
    }

    private void hideNotification()
    {
        mNotifMan.cancel(NOTIF_CONNECTED);
    }

    private class A2DMConnectionThread extends Thread
    {
        private final Socket mSocket;
        private final String mHost;
        private final int mPort;

        private volatile boolean mAbort = false;

        public A2DMConnectionThread(String host, int port)
        {
            mHost = host;
            mPort = port;
            mSocket = new Socket();
        }

        public boolean isConnected()
        {
            return mSocket.isConnected();
        }

        private boolean isNetworkAvailable()
        {
            NetworkInfo info = mConnMan.getActiveNetworkInfo();
            if (info == null) return false;

            return info.isConnected();
        }

        public void run()
        {
            URL url = null;
            HttpURLConnection conn = null;
            StringBuilder builder = new StringBuilder();
            String line = "";
            try
            {
                url = new URL(CommonUtilities.A2DM_SERVER_REGISTER_URL + "udid=test&app=app1");
                conn = (HttpURLConnection)url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("coonection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                while ((line = reader.readLine()) != null)
                {
                    builder.append(line);
                }

                JSONObject jSONObject = new JSONObject(builder.toString());
                Log.i(LOG_TAG, jSONObject.toString());

                // handle the response
                int status = conn.getResponseCode();
                if (status != 200) { throw new IOException("Post failed with error code " + status); }
            }
            catch (Exception e)
            {
                Log.e(LOG_TAG, e.toString());
            }
            finally
            {
                if (conn != null)
                {
                    conn.disconnect();
                }
            }

            Socket s = mSocket;

            long startTime = System.currentTimeMillis();

            try
            {
                s.connect(new InetSocketAddress(mHost, mPort), 20000);

                s.setSoTimeout(0);

                s.setKeepAlive(true);

                /*
                 * This is a special case for our demonstration. The keep-alive is sent from the
                 * client side but since I'm testing it with just netcat, no response is sent from
                 * the server. This means that we might never actually read any data even though our
                 * connection is still alive. Most instances of a persistent TCP connection would
                 * have some sort of application-layer acknowledgement from the server and so should
                 * set a read timeout of KEEP_ALIVE_INTERVAL plus an arbitrary timeout such as 2
                 * minutes.
                 */
                // s.setSoTimeout((int)KEEP_ALIVE_INTERVAL + 120000);

                if (isConnected())
                {
                    startKeepAlives();
                    showNotification();

                    String json = "Hello";
                    InputStream input = s.getInputStream();
                    OutputStream output = s.getOutputStream();
                    DataInputStream inputstream = new DataInputStream(input);
                    DataOutputStream outputstream = new DataOutputStream(output);

                    outputstream.writeInt(json.getBytes().length);
                    outputstream.write(json.getBytes());

                    int arrLength = inputstream.readInt();
                    if (arrLength > 0)
                    {
                        byte[] fromServer = new byte[arrLength];
                        inputstream.read(fromServer, 0, fromServer.length);
                        String content = new String(fromServer);
                        // String content = new String(Base64.decode(new String(fromServer), 0));
                        Log.i(LOG_TAG, "[SOCKET] Reading " + content);
                    }
                }

                log("Connection established to " + s.getInetAddress() + ":" + mPort);
                //
                // startKeepAlives();
                // showNotification();

                // InputStream in = s.getInputStream();
                // OutputStream out = s.getOutputStream();
                //
                // /*
                // * Note that T-Mobile appears to implement an opportunistic connect algorithm
                // where
                // * the connect call may succeed even when the remote peer would reject the
                // * connection. Shortly after an attempt to send data an exception will occur
                // * indicating the connection was reset.
                // */
                // out.write("Hello, world.\n".getBytes());
                //
                // byte[] b = new byte[1024];
                // int n;
                //
                // while ((n = in.read(b)) >= 0)
                // out.write(b, 0, n);

                if (mAbort == false) log("Server closed connection unexpectedly.");
            }
            catch (IOException e)
            {
                log("Unexpected I/O error: " + e.toString());
            }
            finally
            {
                stopKeepAlives();
                hideNotification();

                if (mAbort == true) log("Connection aborted, shutting down.");
                else
                {
                    try
                    {
                        s.close();
                    }
                    catch (IOException e)
                    {
                    }

                    synchronized (PushA2DMService.this)
                    {
                        mConnection = null;
                    }

                    /*
                     * If our local interface is still up then the connection failure must have been
                     * something intermittent. Try our connection again later (the wait grows with
                     * each successive failure). Otherwise we will try to reconnect when the local
                     * interface comes back.
                     */
                    if (isNetworkAvailable() == true) scheduleReconnect(startTime);
                }
            }
        }

        public void sendKeepAlive() throws IOException
        {
            Socket s = mSocket;
            Date d = new Date();
            s.getOutputStream().write((d.toString() + "\n").getBytes());

            log("Keep-alive sent.");
        }

        public void abort()
        {
            log("Connection aborting.");

            mAbort = true;

            try
            {
                mSocket.shutdownOutput();
            }
            catch (IOException e)
            {
            }

            try
            {
                mSocket.shutdownInput();
            }
            catch (IOException e)
            {
            }

            try
            {
                mSocket.close();
            }
            catch (IOException e)
            {
            }

            while (true)
            {
                try
                {
                    join();
                    break;
                }
                catch (InterruptedException e)
                {
                }
            }
        }
    }

    private void testStartCommand(Intent intent)
    {
        String token = intent.getStringExtra("app");
        Log.v(LOG_TAG,
                "*******action******" + intent.getAction() + "******token*****" + token + "********package******" + intent.getPackage());

        Intent appIntent = new Intent("com.google.android.a2dm.intent.RECEIVER");
        appIntent.setPackage("com.tokudu.demo");
        appIntent.putExtra("msg", "receive msg from push");
        sendBroadcast(appIntent);
    }
}
