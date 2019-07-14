package com.android.rr.potholes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.rr.potholes.pothoelsinterfaces.ISplashPresenter;
import com.android.rr.potholes.presenters.SplashPresenter;

public class SplashScreen extends AppCompatActivity implements ISplashPresenter {
    private SplashPresenter mSplashPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mSplashPresenter = new SplashPresenter(SplashScreen.this);
        mSplashPresenter.postDelay();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mSplashPresenter.postDelay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSplashPresenter.stopDelay();
    }

    @Override
    public void startMain() {
        startActivity(new Intent(SplashScreen.this, MainActivity.class));
    }
}