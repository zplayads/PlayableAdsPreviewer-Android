package com.zplay.zplayads;

import android.os.Handler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Creator: lgd
 * Date: 17-9-7
 * Description:
 */

public class AsyncTaskTool {

    private static AsyncTaskTool instance = new AsyncTaskTool();
    private Executor mExecutor;
    private final Handler mHandler;

    public static AsyncTaskTool getInstance() {
        return instance;
    }

    public void postDelay(DelayTask task, long ms) {
        mHandler.postDelayed(task, ms);
    }

    private AsyncTaskTool() {
        mExecutor = Executors.newFixedThreadPool(4);
        mHandler = new Handler();
    }

    public void execute(Runnable task) {
        mExecutor.execute(task);
    }

    public static class DelayTask implements Runnable {

        private final Runnable targetTask;

        public DelayTask(Runnable targetTask) {
            this.targetTask = targetTask;
        }

        @Override
        public void run() {
            AsyncTaskTool.getInstance().execute(targetTask);
        }
    }
}
