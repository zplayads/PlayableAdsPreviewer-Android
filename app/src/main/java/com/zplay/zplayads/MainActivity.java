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
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.playableads.PlayPreloadingListener;
import com.playableads.PlayableAds;
import com.playableads.SimplePlayLoadingListener;
import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

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
    @BindView(R.id.am_loadingContainer)
    View mLoadingContainer;
    @BindView(R.id.am_loadingInfo)
    TextView mLoadingInfo;
    @BindView(R.id.fl_my_container)
    View mFragmentContainer;
    @BindView(R.id.am_topTextView)
    View mTopTextView;

    CaptureFragment captureFragment;

    PlayableAds mAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ZXingLibrary.initDisplayOpinion(this);

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
        showTextInfo();
    }

    private void showTextInfo() {
        mFragmentContainer.setVisibility(View.VISIBLE);
        mTopTextView.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
    }

    private void setInfo(final String msg) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (info != null) {
                    info.append(msg + "\n\n");
                }
            }
        });
    }

    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            showLoadingInfo(getString(R.string.parse_success), false);
            requestAd(result);
        }

        @Override
        public void onAnalyzeFailed() {
            mLoadingContainer.setVisibility(View.GONE);
            ErrorActivity.launch(MainActivity.this, getString(R.string.code_request_error));
        }
    };

    private void requestAd(String result) {
        mAds.requestPlayableAds(result, mPreloadingListener);
    }

    private PlayPreloadingListener mPreloadingListener = new PlayPreloadingListener() {

        @Override
        public void onLoadFinished() {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAds.presentPlayableAD(MainActivity.this, new SimplePlayLoadingListener() {

                        @Override
                        public void playableAdsIncentive() {
                            mLoadingContainer.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAdsError(int code, String msg) {
                            setInfo(msg);
                        }
                    });
                }
            }, 1000);

        }

        @Override
        public void onLoadFailed(int errorCode, String msg) {
            if (errorCode == -1) {
                mLoadingContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingContainer.setVisibility(View.GONE);
                    }
                });
                ErrorActivity.launch(MainActivity.this, getString(R.string.code_request_error));
            }
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
                            showLoadingInfo(getString(R.string.parse_success), false);
                            requestAd(result);
                        }

                        @Override
                        public void onAnalyzeFailed() {
                            mLoadingContainer.setVisibility(View.GONE);
                            ErrorActivity.launch(MainActivity.this, getString(R.string.code_request_error));
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void showLoadingInfo(final String msg, boolean autoDismiss) {
        mLoadingInfo.setText(msg);
        mLoadingContainer.setVisibility(View.VISIBLE);
        if (autoDismiss) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLoadingContainer.setVisibility(View.GONE);
                }
            }, 3000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        info.setText("");
        boolean isOk = true;
        for (int i = 0; i < permissions.length; i++) {
            if (TextUtils.equals(permissions[i], Manifest.permission.CAMERA)
                    && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isOk = false;
                setInfo(getString(R.string.open_camera_permission));
            }

            if (TextUtils.equals(permissions[i], Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isOk = false;
                setInfo(getString(R.string.storage_permission_msg));
            }
        }

        if (isOk) {
            initCaptureFragment();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PlayableAds.getInstance().onDestroy();
    }
}
