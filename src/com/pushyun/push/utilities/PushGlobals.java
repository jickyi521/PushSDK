package com.pushyun.push.utilities;

/**
 * Global resources for Ignition.
*/
public class PushGlobals
{
    private static PushGlobals mPushGlobals = null;
    
    private boolean mA2DMServiceStarted = false;
    private boolean mGCMChecked = false;
    private boolean mGCMAvailabe = false;
    
    private PushGlobals()
    {
        
    }
    
    public synchronized static PushGlobals getInstance()
    {
        if(mPushGlobals == null)
        {
            mPushGlobals = new PushGlobals();
        }
        return mPushGlobals;
    }
    
    public boolean isA2DMServiceStarted()
    {
        return mA2DMServiceStarted;
    }

    public void setA2DMServiceStarted(boolean a2dmServiceStarted)
    {
        mA2DMServiceStarted = a2dmServiceStarted;
    }
    
    public boolean isGCMChecked()
    {
        return mGCMChecked;
    }

    public void setGCMChecked(boolean gcmChecked)
    {
        mGCMChecked = gcmChecked;
    }
    
    public boolean isGCMAvailabe()
    {
        return mGCMAvailabe;
    }

    public void setGCMAvailabe(boolean gcmAvailabe)
    {
        setGCMChecked(true);
        mGCMAvailabe = gcmAvailabe;
    }

}
