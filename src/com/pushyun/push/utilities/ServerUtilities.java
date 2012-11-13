package com.pushyun.push.utilities;

<<<<<<< HEAD
import static com.pushyun.push.utilities.CommonUtilities.CMS_SERVER_REGISTER_URL;
=======
import static com.pushyun.push.utilities.CommonUtilities.SERVER_URL;
>>>>>>> 1220c312c5c9d3868b100b6d6a0d9f152da03a85
import static com.pushyun.push.utilities.CommonUtilities.TAG;
import static com.pushyun.push.utilities.CommonUtilities.displayMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.util.Log;

import com.pushyun.push.R;
import com.pushyun.push.gcm.GCMRegistrar;

/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities
{

    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();

    /**
     * Register this account/device pair within the server.
     * 
     * @return whether the registration succeeded or not.
     */
<<<<<<< HEAD
    public static boolean registerCMSServer(final Context context, final String regId)
    {
        Log.i(TAG, "registering device (regId = " + regId + ")");
        
=======
    public static boolean registerA2DMServer(final Context context, final String regId)
    {
        Log.i(TAG, "registering device (regId = " + regId + ")");
        
        //TODO Test
        String serverUrl = SERVER_URL;
        
>>>>>>> 1220c312c5c9d3868b100b6d6a0d9f152da03a85
        List<NameValuePair> nameValuePairs=new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("appkey", "com.push.test"));
        nameValuePairs.add(new BasicNameValuePair("token", "ffffffff-8b25-33fb-e812-480b62ffe7ff"));
        nameValuePairs.add(new BasicNameValuePair("name", "Test"));
        nameValuePairs.add(new BasicNameValuePair("version", "1"));
        
        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        for (int i = 1; i <= MAX_ATTEMPTS; i++)
        {
            Log.d(TAG, "Attempt #" + i + " to register");
            try
            {
                displayMessage(context, context.getString(R.string.server_registering, i, MAX_ATTEMPTS));
                
<<<<<<< HEAD
                postToCMSServer(CMS_SERVER_REGISTER_URL, nameValuePairs);
=======
                postToA2DMServer(serverUrl, nameValuePairs);
>>>>>>> 1220c312c5c9d3868b100b6d6a0d9f152da03a85
                
                GCMRegistrar.setRegisteredOnServer(context, true);
                String message = context.getString(R.string.server_registered);
                CommonUtilities.displayMessage(context, message);
                return true;
            }
            catch (Exception e)
            {
                // Here we are simplifying and retrying on any error; in a real
                // application, it should retry only on unrecoverable errors
                // (like HTTP error code 503).
                Log.e(TAG, "Failed to register on attempt " + i, e);
                if (i == MAX_ATTEMPTS)
                {
                    break;
                }
                try
                {
                    Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);
                }
                catch (InterruptedException e1)
                {
                    // Activity finished before we complete - exit.
                    Log.d(TAG, "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return false;
                }
                // increase backoff exponentially
                backoff *= 2;
            }
        }
        String message = context.getString(R.string.server_register_error, MAX_ATTEMPTS);
        CommonUtilities.displayMessage(context, message);
        return false;
    }
    
    

    /**
     * Unregister this account/device pair within the server.
     */
<<<<<<< HEAD
    public static void unregisterCMSServer(final Context context, final String regId)
=======
    public static void unregisterA2DMServer(final Context context, final String regId)
>>>>>>> 1220c312c5c9d3868b100b6d6a0d9f152da03a85
    {
        Log.i(TAG, "unregistering device (regId = " + regId + ")");
        
        //TODO need to confirm the unregister api
        List<NameValuePair> nameValuePairs=new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("appkey", "com.push.test"));
        nameValuePairs.add(new BasicNameValuePair("regId", regId));
        
        try
        {
<<<<<<< HEAD
            postToCMSServer(CMS_SERVER_REGISTER_URL, nameValuePairs);
=======
            postToA2DMServer(SERVER_URL, nameValuePairs);
>>>>>>> 1220c312c5c9d3868b100b6d6a0d9f152da03a85
            GCMRegistrar.setRegisteredOnServer(context, false);
            String message = context.getString(R.string.server_unregistered);
            CommonUtilities.displayMessage(context, message);
        }
        catch (Exception e)
        {
            // At this point the device is unregistered from GCM, but still
            // registered in the server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.
            String message = context.getString(R.string.server_unregister_error, e.getMessage());
            CommonUtilities.displayMessage(context, message);
        }
    }

    /**
     * Issue a POST request to the server.
     * 
     * @param endpoint POST address.
     * @param params request parameters.
     * 
     * @throws IOException propagated from POST.
     */
<<<<<<< HEAD
    private static void postToCMSServer(String endpoint, List<NameValuePair> nameValuePairs)
=======
    private static void postToA2DMServer(String endpoint, List<NameValuePair> nameValuePairs)
>>>>>>> 1220c312c5c9d3868b100b6d6a0d9f152da03a85
    {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;

        try
        {
            HttpPost httppost = new HttpPost(endpoint);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();
            
            if (resEntity != null)
            {
                Log.v(TAG, "Response content length: " + resEntity.getContentLength());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
    }

}
