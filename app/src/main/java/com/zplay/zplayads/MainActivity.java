package com.zplay.zplayads;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.playableads.PlayPreloadingListener;
import com.playableads.PlayableAds;
import com.playableads.SimplePlayLoadingListener;
import com.playableads.constants.BusinessConstants;
import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.zplay.zplayads.GalleryActivity.EXTRA_PATH;

public class MainActivity extends FragmentActivity {
    private static final List<String> REQUIRED_DANGEROUS_PERMISSIONS = new ArrayList<>();

    static {
        REQUIRED_DANGEROUS_PERMISSIONS.add(CAMERA);
        REQUIRED_DANGEROUS_PERMISSIONS.add(WRITE_EXTERNAL_STORAGE);
    }

    private static final String TAG = "ccc";
    private static final int REQUEST_IMAGE = 1;
    private static final String APP_ID = "androidDemoApp";

    @BindView(R.id.text)
    TextView info;
    @BindView(R.id.text_container)
    View textContainer;
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
    @BindView(R.id.copyright)
    TextView mCopyrightText;

    CaptureFragment captureFragment;

    PlayableAds mAds;
    RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ZXingLibrary.initDisplayOpinion(this);

        mRequestQueue = Volley.newRequestQueue(this);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setCopyright();
        textView.setText(Html.fromHtml(getString(R.string.open_office)));

        mAds = PlayableAds.init(this, APP_ID);
        mAds.setAutoLoadAd(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<>();
            for (String p : REQUIRED_DANGEROUS_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(p);
                }
            }
            if (!permissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissions.toArray(
                        new String[permissions.size()]), 0);
                return;
            }
        }

        initCaptureFragment();
    }

    private void setCopyright() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        mCopyrightText.setText(getString(R.string.copyright, year));
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
        textContainer.setVisibility(View.VISIBLE);
    }

    private void setInfo(final String msg) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                info.append(msg + "\n\n");
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

    private void requestAd(String srcId) {
        showLoadingInfo(getString(R.string.loading), false);
        final ValueCallback<JSONObject> callback = new ValueCallback<JSONObject>() {
            @Override
            public void onReceiveValue(JSONObject value) {
                mAds.requestPlayableAds(value, new PlayPreloadingListener() {
                    @Override
                    public void onLoadFinished() {
                        final String tag = Encrypter.doMD5Encode16(String.valueOf(this.hashCode()));
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mAds.presentPlayableAD(tag, new SimplePlayLoadingListener() {

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
                        }, 500);
                    }

                    @Override
                    public void onLoadFailed(int errorCode, String msg) {
                        Log.d(TAG, "onLoadFailed: " + errorCode);
                        mLoadingContainer.post(new Runnable() {
                            @Override
                            public void run() {
                                mLoadingContainer.setVisibility(View.GONE);
                            }
                        });
                        ErrorActivity.launch(MainActivity.this, getString(R.string.ad_load_failed));
                    }
                });
            }
        };

        if (srcId.startsWith("https://") || srcId.startsWith("http://")) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt("response_target", "preview");
                jsonObject.putOpt("video_page_url", srcId);
            } catch (JSONException ignore) {
            }
            callback.onReceiveValue(jsonObject);
        } else {
            String url = BusinessConstants.HOST_SRC_ID() + "/" + srcId;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jo) {
                    try {
                        jo.putOpt("response_target", "preview");
                    } catch (JSONException ignore) {
                    }
                    callback.onReceiveValue(jo);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "onLoadFailed: " + error);
                    mLoadingContainer.post(new Runnable() {
                        @Override
                        public void run() {
                            mLoadingContainer.setVisibility(View.GONE);
                        }
                    });
                    ErrorActivity.launch(MainActivity.this, getString(R.string.ad_load_failed));
                }
            });
            mRequestQueue.add(jsonObjectRequest);
        }
    }

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
                            Log.d(TAG, "onAnalyzeSuccess: " + result);
                            showLoadingInfo(getString(R.string.parse_success), false);
                            requestAd(result);
                        }

                        @Override
                        public void onAnalyzeFailed() {
                            Log.d(TAG, "onAnalyzeFailed: ");
                            mLoadingContainer.setVisibility(View.GONE);
                            ErrorActivity.launch(MainActivity.this, getString(R.string.code_request_error));
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "onActivityResult: ", e);
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
            if (TextUtils.equals(permissions[i], CAMERA)
                    && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isOk = false;
                setInfo(getString(R.string.open_camera_permission));
            }

            if (TextUtils.equals(permissions[i], Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isOk = false;
                setInfo(getString(R.string.open_write_permission));
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

    public void openOfficeWebsite(View view) {
        try {
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.zplayads.com"));
            startActivity(myIntent);
        } catch (Exception ignore) {
        }
    }
}
