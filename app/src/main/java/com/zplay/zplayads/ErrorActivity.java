package com.zplay.zplayads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * lgd on 2017/10/16.
 */

public class ErrorActivity extends Activity {
    private static final String EXTRA_MSG = "extra.msg";
    private static long time = 0;

    @BindView(R.id.ae_loadingInfo)
    TextView mTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        ButterKnife.bind(this);
        String msg = getIntent().getStringExtra(EXTRA_MSG);
        mTextView.setText(msg);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);
        time = 0;
    }

    public static void launch(Context ctx, String msg) {
        if (time != 0) {
            return;
        }
        time = System.currentTimeMillis();
        Intent i = new Intent(ctx, ErrorActivity.class);
        i.putExtra(EXTRA_MSG, msg);
        ctx.startActivity(i);
    }
}
