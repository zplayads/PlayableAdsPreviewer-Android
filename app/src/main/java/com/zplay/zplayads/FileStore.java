package com.zplay.zplayads;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Description: 文件路径管理器，注意！android 7.0的适配！
 * Creator: lgd
 * Date: 17-9-4
 */

public final class FileStore {
    private static final String TAG = "FileStore";

    private static final String CACHE_HOME = "ZPLAYAds";

    private static FileStore instance;
    private String homeDir;

    public static FileStore getInstance(String dir) {
        if (instance == null) {
            if (TextUtils.isEmpty(dir)) {
                dir = Environment.getExternalStorageDirectory().getAbsolutePath();
            }
            instance = new FileStore(dir);
        }
        return instance;
    }

    private FileStore(String dir) {
        homeDir = dir + File.separator + CACHE_HOME;

    }

    public String getHomeDir() {
        return mkdirs(homeDir) ? homeDir : null;
    }

    public String getCacheDir() {
        String path = homeDir + File.separator + "cache";
        return mkdirs(path) ? path : null;
    }

    public String getVideoCacheDir() {
        String path = getCacheDir() + File.separator + "video";
        return mkdirs(path) ? path : null;
    }

    public void clearCacheDir() {
        String path = getCacheDir();
        if (path == null) {
            return;
        }
        File cacheDir = new File(getCacheDir());
        if (!cacheDir.exists()) {
            return;
        }

        deleteFileAsync(cacheDir.getPath());
    }

    public static String getReportStatisticsPath() {
        final String reportFilePath = Environment.getExternalStorageDirectory() + "/paStatistics.tmp";
        File file = new File(reportFilePath);
        if (file.exists()) {
            return reportFilePath;
        }
        try {
            if (file.createNewFile()) {
                return reportFilePath;
            }
        } catch (IOException ignore) {
        }
        return "";
    }

    String getLogDir() {
        String path = homeDir + File.separator + "log";
        return mkdirs(path) ? path : null;
    }

    private boolean mkdirs(String path) {
        File f = new File(path);
        return f.exists() || f.mkdirs();
    }

    private void deleteFileAsync(final String filePath) {
        AsyncTaskTool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    deleteFile(filePath);
                } catch (Exception e) {
                    Log.e(TAG, "delete file failed: ", e);
                }
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            file.delete();
            return;
        }

        File[] files = file.listFiles();
        for (File f : files) {
            deleteFile(f.getPath());
        }
        file.delete();
    }

    public static String calculateMD5(File file) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception while getting digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }
}
