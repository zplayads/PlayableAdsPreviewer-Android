package com.zplay.zplayads;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * lgd on 2017/10/12.
 */

public class GalleryActivity extends Activity {
    static final String EXTRA_PATH = "extra.path";

    @BindView(R.id.ag_textView)
    TextView mProgressMsg;
    @BindView(R.id.ag_recyclerView)
    RecyclerView mRecyclerView;

    ArrayList<String> mFilePathArray;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                setInfo(getString(R.string.open_write_permission));
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                return;
            }
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
                setInfo(getString(R.string.open_phone_permission));
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
            }
        }

        loadImages();

    }

    private void loadImages() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                mProgressMsg.setVisibility(View.VISIBLE);
                mProgressMsg.setText(R.string.loading);
                mFilePathArray = new ArrayList<>();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = GalleryActivity.this
                        .getContentResolver();

                // 只查询jpeg和png的图片
                Cursor mCursor = mContentResolver.query(mImageUri, null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png"},
                        MediaStore.Images.Media.DATE_MODIFIED);
                if (mCursor == null) {
                    return false;
                }
                while (mCursor.moveToNext()) {
                    // 获取图片的路径
                    String path = mCursor.getString(mCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));


                    File f = new File(path);
                    if (f.length() > 4096) {
                        mFilePathArray.add(path);
                    }

                }
                mCursor.close();

                Collections.reverse(mFilePathArray);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean ok) {
                if (ok) {
                    mProgressMsg.setVisibility(View.GONE);
                    mRecyclerView.setAdapter(new GalleryAdapter(mFilePathArray));
                } else {
                    mProgressMsg.setText(R.string.image_load_failed);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            if (TextUtils.equals(permissions[i], Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.storage_permission_msg, Toast.LENGTH_LONG).show();
                } else {
                    loadImages();
                }
            }
        }
    }

    private void setInfo(final String msg) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mProgressMsg.append(msg + "\n\n");
            }
        });
    }

    public static void launch(Activity activity, int reqCode) {
        Intent i = new Intent(activity, GalleryActivity.class);
        activity.startActivityForResult(i, reqCode);
    }

    public void onBackClicked(View view) {
        finish();
    }
}
