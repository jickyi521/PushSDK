package com.pushyun.push.utilities;

import android.content.Context;
import android.content.Intent;

/**
 * Helper class providing methods and constants common to other classes in the app.
 */
public final class CommonUtilities
{

    /**
     * Base URL of the Demo Server (such as http://10.0.2.2:8080/gcm-demo)
     */
    public static final String SERVER_URL = "http://192.168.196.58/api.php?op=push_mobile&a=register";

    /**
     * Google API project id registered to use GCM.
     */
    public static final String SENDER_ID = "487820657755";

    /**
     * Tag used on log messages.
     */
    public static final String TAG = "GCM Mode";

    /**
     * Intent used to display a message in the screen.
     */
    public static final String DISPLAY_MESSAGE_ACTION = "com.google.android.gcm.demo.app.DISPLAY_MESSAGE";

    /**
     * Intent's extra that contains the message to be displayed.
     */
    public static final String EXTRA_MESSAGE = "message";

    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by the UI and the
     * background service.
     * 
     * @param context application's context.
     * @param message message to be displayed.
     */
    public static void displayMessage(Context context, String message)
    {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
}
