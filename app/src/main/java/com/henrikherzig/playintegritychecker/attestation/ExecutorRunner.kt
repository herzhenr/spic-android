package com.henrikherzig.playintegritychecker.attestation

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors
class ExecutorRunner {
    // create a new instance of Executor using any factory methods
    private val executor: Executor = Executors.newSingleThreadExecutor()
    // handler of UI thread
    private val handler = Handler(Looper.getMainLooper())
    // callable to communicate the result back to UI
    interface Callback<R> {
        fun onComplete(result: R)
        fun onError(e: Exception?)
    }
    fun <R> execute(callable: Callable<R>, callback: Callback<R>) {
        executor.execute {
            val result: R
            try {
                // execute the callable or any tasks asynchronously
                result = callable.call()
                handler.post {
                    // update the result back to UI
                    callback.onComplete(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                handler.post { // communicate error or handle
                    callback.onError(e)
                }
            }
        }
    }
}