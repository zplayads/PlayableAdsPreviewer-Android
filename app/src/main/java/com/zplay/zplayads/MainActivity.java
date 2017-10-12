package com.zplay.zplayads;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.playableads.PlayPreloadingListener;
import com.playableads.PlayableAds;
import com.playableads.SimplePlayLoadingListener;
import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import static com.zplay.zplayads.GalleryActivity.EXTRA_PATH;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "ccc";
    private static final int REQUEST_IMAGE = 1;

    private TextView info;
    private ScrollView mScrollView;

    CaptureFragment captureFragment;

    PlayableAds mAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAds = PlayableAds.init(this, "androidDemoApp", "androidDemoAdUnit");

        info = findViewById(R.id.text);
        mScrollView = findViewById(R.id.scrollView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                setInfo(getString(R.string.open_camera_permission));
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);
                return;
            }
        }


        initCaptureFragment();
    }

    private void initCaptureFragment() {
        captureFragment = new CaptureFragment();
        // 为二维码扫描界面设置定制化界面
        CodeUtils.setFragmentArgs(captureFragment, R.layout.my_camera);

        captureFragment.setAnalyzeCallback(analyzeCallback);

        getSupportFragmentManager().beginTransaction().replace(R.id.fl_my_container, captureFragment).commit();
    }

    private void setInfo(final String msg) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (info != null) {
                    info.append(msg + "\n\n");
                }
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            mAds.requestPlayableAds(result, mPreloadingListener);
            setInfo(getString(R.string.start_request));
        }

        @Override
        public void onAnalyzeFailed() {
            setInfo("未识别");
        }
    };

    private PlayPreloadingListener mPreloadingListener = new PlayPreloadingListener() {

        @Override
        public void onLoadFinished() {
            setInfo(getString(R.string.pre_cache_finished));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "展示广告", Toast.LENGTH_SHORT).show();
                    mAds.presentPlayableAD(MainActivity.this, new SimplePlayLoadingListener() {

                        @Override
                        public void playableAdsIncentive() {
                            setInfo(getString(R.string.ads_incentive));
                        }

                        @Override
                        public void onAdsError(int code, String msg) {
                            setInfo(getString(R.string.ads_error, code, msg));
                        }
                    });
                }
            }, 1000);

        }

        @Override
        public void onLoadFailed(int errorCode, String msg) {
            setInfo(String.format(getString(R.string.load_failed), errorCode, msg));
        }
    };

    public void openGallery(View view) {
        GalleryActivity.launch(this, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_IMAGE) {
            if (data != null) {
                try {
                    String path = data.getStringExtra(EXTRA_PATH);
                    CodeUtils.analyzeBitmap(path, new CodeUtils.AnalyzeCallback() {
                        @Override
                        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                            mAds.requestPlayableAds(result, mPreloadingListener);
                            setInfo(getString(R.string.start_request));
                        }

                        @Override
                        public void onAnalyzeFailed() {
                            Toast.makeText(MainActivity.this, "未识别", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
