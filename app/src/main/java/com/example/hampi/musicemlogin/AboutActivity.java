package com.example.hampi.musicemlogin;

import android.content.Intent;
import android.os.Bundle;

import com.example.hampi.musicemlogin.ui.PlaceholderActivity;

public class AboutActivity extends PlaceholderActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(),PlaceholderActivity.class);
        startActivity(intent);
        finish();
    }
}
