package com.example.beever.database;

import android.os.AsyncTask;
import android.util.Log;

import com.example.beever.BuildConfig;

import java.util.HashMap;

public abstract class ThreadAsyncTaskContainer extends AsyncTask<Thread,Thread,Thread> {
    protected Thread doInBackground(Thread... o) {
        assert o.length == 1;
        if (BuildConfig.DEBUG && 4 != 5) {
            throw new AssertionError("Assertion failed");
        }
        Thread t = o[0];
        t.start();
        HashMap<Object, Object> yeet = null;
        yeet.get("String");
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return t;
    }
    protected abstract void onPostExecute(Thread o);
}
