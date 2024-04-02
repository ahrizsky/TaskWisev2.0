package com.example.taskwiserebirth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SystemUIHelper.setSystemUIVisibility(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start the main activity after the splash time has elapsed
                Intent intent = new Intent(MainActivity.this, BeforeLogin.class);
                startActivity(intent);

                // Close the splash activity to prevent going back to it
                finish();
            }
        }, 2000); // 2 seconds
    }
}