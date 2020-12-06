/*
Copyright (c) 2020 Beever

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.example.beever.database;

import android.os.Handler;
import android.os.Looper;

/**
 * Subclass extending the Thread class, meant for retrieving objects from Firestore.
 * This class features the creation of a handler which allows for execution of post-retrieval
 * operations through the class itself (without having to continuously query for thread state).
 * In addition, the use of Thread over AsyncTask allows for initiation of threads
 * through non-UI threads.
 */
public abstract class AsyncGetter extends Thread {

    /**
     * Handler to execute operations on creating thread.
     * This requires, for threads created from non-UI thread, for a looper to be prepared
     * and started on the creating thread prior to creating an object of this class.
     */
    protected Handler handler = new Handler();

    /**
     * Thread execution body.
     * Note that here we rely on 2 functions runMainBody(), which is the main execution
     * body in this thread, and onPostExecute(), which is the post-retrieval execution
     * body in the calling thread.
     */
    public void run(){
        runMainBody();
        handler.post(new Runnable(){
            public void run(){
                onPostExecute();
            }
        });
        return;
    }

    /**
     * Main execution body, to be run in this thread.
     */
    public abstract void runMainBody();

    /**
     * Post-retrieval execution body, to be run in calling thread. Usually this is left abstract
     * in non-anonymous subclasses, and left to be defined in anonymous subclasses.
     */
    public abstract void onPostExecute();

    /**
     * Check whether query/retrieval was successful.
     * @return boolean for whether query/retrieval was successful
     */
    public abstract boolean isSuccessful();
}
