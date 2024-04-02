package com.example.taskwiserebirth;

import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.appcompat.app.AppCompatActivity;

public class SystemUIHelper {

    public static void setSystemUIVisibility(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT
                            ? View.SYSTEM_UI_FLAG_LOW_PROFILE
                            : View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            );
        } else {
            activity.getWindow().getInsetsController().hide(WindowInsets.Type.navigationBars() | WindowInsets.Type.statusBars());
            activity.getWindow().getInsetsController().setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }
}
