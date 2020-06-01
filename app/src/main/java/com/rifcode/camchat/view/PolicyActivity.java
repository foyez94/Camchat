package com.rifcode.camchat.view;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.rifcode.camchat.R;

public class PolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy);

        getSupportActionBar().setTitle(getString(R.string.laws_tv));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
