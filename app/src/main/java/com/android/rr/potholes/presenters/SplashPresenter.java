package com.android.rr.potholes.presenters;

import android.os.Handler;

import com.android.rr.potholes.potholesconstants.PotHolesConstants;
import com.android.rr.potholes.SplashScreen;

public class SplashPresenter {
    private Handler mHandler;
    private Runnable mRunnable;
    private SplashScreen mSplashScreen;

    public SplashPresenter (SplashScreen splashScreen) {
        mSplashScreen = splashScreen;
    }

    public void postDelay () {
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
               mSplashScreen.startMain();
            }
        };
        mHandler.postDelayed(mRunnable, PotHolesConstants.SPLASH_SCREEN_DELAY);
    }

    public void stopDelay()
    {
        if (null != mHandler && null != mRunnable)
            mHandler.removeCallbacks(mRunnable);
    }
}