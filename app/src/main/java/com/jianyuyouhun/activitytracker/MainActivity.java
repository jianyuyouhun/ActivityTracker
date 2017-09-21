package com.jianyuyouhun.activitytracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jianyuyouhun.inject.annotation.FindViewById;
import com.jianyuyouhun.jmvplib.app.BaseActivity;

public class MainActivity extends BaseActivity {
    private static final int REQUEST_CODE = 1;

    @FindViewById(R.id.open_finder)
    private Button mOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkOverlayPermission();
        registerListener();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    private void registerListener() {
        mOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AccessibilityUtil.checkAccessibility(MainActivity.this)) {
                    startService(
                            new Intent(MainActivity.this, TrackerService.class)
                                    .putExtra(TrackerService.COMMAND, TrackerService.COMMAND_OPEN)
                    );
                    finish();
                }
            }
        });
    }

    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                startActivityForResult(
                        new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        REQUEST_CODE
                );
                Toast.makeText(this, "请先授予 \"Activity 栈\" 悬浮窗权限", Toast.LENGTH_LONG).show();
            }
        }
    }
}
