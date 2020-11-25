package com.example.beever.database;

import android.os.Handler;
import android.os.Looper;

public abstract class AsyncGetter extends Thread {

    protected Handler handler = new Handler();

    public void run(){
        runMainBody();
        handler.post(new Runnable(){
            public void run(){
                onPostExecute();
            }
        });
        return;
    }

    public abstract void runMainBody();

    public abstract void onPostExecute();

    public abstract boolean isSuccessful();
}
