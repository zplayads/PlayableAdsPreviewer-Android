package com.zplay.zplayads;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.playableads.PlayPreloadingListener;
import com.playableads.PlayableAds;
import com.playableads.SimplePlayLoadingListener;
import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.zplay.zplayads.GalleryActivity.EXTRA_PATH;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "ccc";
    private static final int REQUEST_IMAGE = 1;

    @BindView(R.id.text)
    TextView info;
    @BindView(R.id.am_textView)
    TextView textView;
    @BindView(R.id.scrollView)
    ScrollView mScrollView;
    @BindView(R.id.am_loading)
    View mLoading;

    CaptureFragment captureFragment;

    PlayableAds mAds;
    private boolean canRequestNextAd = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        textView.setText(Html.fromHtml(getString(R.string.open_gallery)));

        mAds = PlayableAds.init(this, "androidDemoApp", "androidDemoAdUnit");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                    && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                setInfo(getString(R.string.open_camera_permission));
                requestPermissions(new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE}, 0);
                return;
            }

            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                setInfo(getString(R.string.open_camera_permission));
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                return;
            }

            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                setInfo(getString(R.string.open_camera_permission));
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);
                return;
            }

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

        initCaptureFragment();
    }

    private void initCaptureFragment() {
        captureFragment = new CaptureFragment();
        // 为二维码扫描界面设置定制化界面
        CodeUtils.setFragmentArgs(captureFragment, R.layout.my_camera);

        captureFragment.setAnalyzeCallback(analyzeCallback);

        getSupportFragmentManager().beginTransaction().replace(R.id.fl_my_container, captureFragment).commitAllowingStateLoss();
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
            requestAd(result);
        }

        @Override
        public void onAnalyzeFailed() {
            setInfo(getString(R.string.unknown));
        }
    };

    private void requestAd(String result) {
        if (canRequestNextAd) {
            mLoading.setVisibility(View.VISIBLE);
            mAds.requestPlayableAds(result, mPreloadingListener);
            setInfo(getString(R.string.start_request));
        }
        canRequestNextAd = false;
    }

    private PlayPreloadingListener mPreloadingListener = new PlayPreloadingListener() {

        @Override
        public void onLoadFinished() {
            mLoading.setVisibility(View.GONE);
            setInfo(getString(R.string.pre_cache_finished));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setInfo(getString(R.string.present_ad));
                    mAds.presentPlayableAD(MainActivity.this, new SimplePlayLoadingListener() {

                        @Override
                        public void playableAdsIncentive() {
                            setInfo(getString(R.string.ads_incentive));
                            canRequestNextAd = true;
                            setInfo(getString(R.string.dividing_line));
                        }

                        @Override
                        public void onAdsError(int code, String msg) {
                            canRequestNextAd = true;
                            setInfo(getString(R.string.ads_error, code, msg));
                            setInfo(getString(R.string.dividing_line));
                        }
                    });
                }
            }, 1000);

        }

        @Override
        public void onLoadFailed(int errorCode, String msg) {
            setInfo(String.format(getString(R.string.load_failed), errorCode, msg));
            canRequestNextAd = true;
            mLoading.setVisibility(View.GONE);
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
                            requestAd(result);
                        }

                        @Override
                        public void onAnalyzeFailed() {
                            Toast.makeText(MainActivity.this, R.string.unknown, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isOk = true;
        for (int i = 0; i < permissions.length; i++) {
            if (TextUtils.equals(permissions[i], Manifest.permission.CAMERA)
                    && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isOk = false;
            }

            if (TextUtils.equals(permissions[i], Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isOk = false;
            }
        }

        if (isOk) {
            initCaptureFragment();
        } else {
            setInfo(getString(R.string.permission_msg));
        }
    }
}
