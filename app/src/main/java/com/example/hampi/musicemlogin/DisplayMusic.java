package com.example.hampi.musicemlogin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DisplayMusic extends AppCompatActivity {

    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_music);
        textView = (TextView)findViewById(R.id.editText);
        String value = getIntent().getExtras().getString("emotion");
        textView.setText(value);
    }
}
