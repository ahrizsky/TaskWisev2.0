package com.example.taskwiserebirth;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        SystemUIHelper.setSystemUIVisibility(this);

        replaceFragment(new Live2DFragment());

        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);

            if (itemId == R.id.bottom_home && !(currentFragment instanceof Live2DFragment)) {
                replaceFragment(new Live2DFragment());
            } else if (itemId == R.id.bottom_clip && !(currentFragment instanceof AddTaskFragment)) {
                replaceFragment(new AddTaskFragment());
            } else if (itemId == R.id.bottom_sms && !(currentFragment instanceof SMSFragment)) {
                replaceFragment(new SMSFragment());
            } else if (itemId == R.id.bottom_settings && !(currentFragment instanceof SettingsFragment)) {
                replaceFragment(new SettingsFragment());
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Check if there's already a fragment attached, and remove it if necessary
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);
        if (currentFragment != null) {
            fragmentTransaction.remove(currentFragment);
        }
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public void toggleNavBarVisibility(boolean visible, boolean isScrollToggle) {
        long duration = 100;
        if (isScrollToggle) {
            if (visible) {
                bottomNavigationView.animate().translationY(0).setDuration(duration).setInterpolator(new DecelerateInterpolator());
            } else {
                bottomNavigationView.animate().translationY(bottomNavigationView.getHeight()).setDuration(duration).setInterpolator(new AccelerateInterpolator());
            }
        } else {
            if (bottomNavigationView.getVisibility() == View.VISIBLE) {
                bottomNavigationView.animate().translationY(bottomNavigationView.getHeight()).setDuration(duration).setInterpolator(new AccelerateInterpolator()).withEndAction(() -> bottomNavigationView.setVisibility(View.GONE));
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE);
                bottomNavigationView.animate().translationY(0).setDuration(duration).setInterpolator(new DecelerateInterpolator());
            }
        }
    }

}
