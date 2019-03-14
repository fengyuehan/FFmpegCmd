package com.wyh.ffmpegcmd.ffmpeg;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.wyh.ffmpegcmd.App;
import com.wyh.ffmpegcmd.AppExecutor;
import com.wyh.ffmpegcmd.PermissionHelper;
import com.wyh.ffmpegcmd.ffmpeg.Jni.FFmpegJni;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wyh on 2019/3/13.
 */
public enum FFmpeg {
    instance;

    public static FFmpeg getInstance() {
        return instance;
    }

    private final ExecutorService mExecutor =
            Executors.newSingleThreadExecutor(new AppExecutor.ExecutorsThreadFactory("ffmpeg"));

    private volatile boolean mIsRunning = false;

    public boolean isRunning() {
        return mIsRunning;
    }

    public void run(@NonNull List<String> list, @Nullable final Callback callback) {
        String[] commands = new String[list.size()];
        list.toArray(commands);
        run(commands, callback);
    }

    public void run(@NonNull final String[] cmd, @Nullable final Callback callback) {
        if (!PermissionHelper.hasWriteAndReadStoragePermission(App.get())) {
            AppExecutor.executeMain(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(App.get(), "请开启读写权限！", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        Objects.requireNonNull(cmd);
        if (mIsRunning) {
            throw new IllegalStateException("FFmpeg IsRunning");
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mIsRunning = true;
                int ret = 1;
                try {
                    ret = FFmpegJni.excute(cmd);
                    done(callback, ret != 1);
                } catch (Exception e) {
                    done(callback, ret != 1);
                }
                mIsRunning = false;
            }
        });
    }

    private void done(final Callback callback, final boolean success) {
        if (callback != null) {
            AppExecutor.executeMain(new Runnable() {
                @Override
                public void run() {
                    if (success) {
                        callback.onSuccess();
                    } else {
                        callback.onFail();
                    }
                }
            });
        }
    }


}