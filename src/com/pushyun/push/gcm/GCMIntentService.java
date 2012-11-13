package com.pushyun.push.gcm;

import static com.pushyun.push.utilities.CommonUtilities.SENDER_ID;
import static com.pushyun.push.utilities.CommonUtilities.displayMessage;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pushyun.push.R;
import com.pushyun.push.utilities.ServerUtilities;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService
{

    @SuppressWarnings("hiding")
    private static final String TAG = "GCMIntentService";

    public GCMIntentService()
    {
        super(SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId)
    {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        displayMessage(context, getString(R.string.gcm_registered));
<<<<<<< HEAD
        ServerUtilities.registerCMSServer(context, registrationId);
=======
        ServerUtilities.registerA2DMServer(context, registrationId);
>>>>>>> 1220c312c5c9d3868b100b6d6a0d9f152da03a85
    }

    @Override
    protected void onUnregistered(Context context, String registrationId)
    {
        Log.i(TAG, "Device unregistered");
        displayMessage(context, getString(R.string.gcm_unregistered));
<<<<<<< HEAD
        if (GCMRegistrar.isRegisteredOnCMSServer(context))
        {
            ServerUtilities.unregisterCMSServer(context, registrationId);
=======
        if (GCMRegistrar.isRegisteredOnServer(context))
        {
            ServerUtilities.unregisterA2DMServer(context, registrationId);
>>>>>>> 1220c312c5c9d3868b100b6d6a0d9f152da03a85
        }
        else
        {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            Log.i(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent)
    {
        Log.i(TAG, "Received message");
        String message = getString(R.string.gcm_message);
        displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }

    @Override
    protected void onDeletedMessages(Context context, int total)
    {
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
        displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }

    @Override
    public void onError(Context context, String errorId)
    {
        Log.i(TAG, "Received error: " + errorId);
        displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId)
    {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        displayMessage(context, getString(R.string.gcm_recoverable_error, errorId));
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message)
    {
        // int icon = R.drawable.ic_stat_gcm;
        // long when = System.currentTimeMillis();
        // NotificationManager notificationManager = (NotificationManager)
        // context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Notification notification = new Notification(icon, message, when);
        // String title = context.getString(R.string.app_name);
        // Intent notificationIntent = new Intent(context, GCMTestActivity.class);
        // // set intent so it does not start a new activity
        // notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
        // Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // PendingIntent intent =
        // PendingIntent.getActivity(context, 0, notificationIntent, 0);
        // notification.setLatestEventInfo(context, title, message, intent);
        // notification.flags |= Notification.FLAG_AUTO_CANCEL;
        // notificationManager.notify(0, notification);
    }

}