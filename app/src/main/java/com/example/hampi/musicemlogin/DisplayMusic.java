package com.example.hampi.musicemlogin;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

public class DisplayMusic extends AppCompatActivity {
    private ArrayList<Songs> arrayTempList;
    MusicDatabaseHelper musicDatabaseHelper;
    private int oneTimeOnly = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_music);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        TextView textView = findViewById(R.id.editText);
        String value = getIntent().getExtras().getString("emotion");
        textView.setText("You look "+value+"!");
        if (getString(R.string.subscription_key).startsWith("Please")) {
            new AlertDialog.Builder(this)
                    .setTitle("add_subscription_key_tip_title")
                    .setMessage("Please put your two subscription keys for Emotion API/Face API to source file \\\"app/res/values/strings.xml\\\" to enable Emotion API and Face API calls.")
                    .setCancelable(false)
                    .show();
        }

        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final Cursor songCursor = contentResolver.query(songUri, null, null, null, null);
        arrayTempList = new ArrayList<Songs>();

        if (songCursor != null && songCursor.moveToFirst()) {
            int songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songPath = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            do {
                long currentId = songCursor.getLong(songId);
                String currentTitle = songCursor.getString(songTitle);
                String currentPath = songCursor.getString(songPath);
                int currentEmotionIndex = 0;
                arrayTempList.add(new Songs(currentId, currentTitle, currentPath, currentEmotionIndex));
            } while (songCursor.moveToNext());
        }
        // Add array list to shared preferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayTempList);
        editor.putString("songTempList", json);
        editor.commit();

        musicDatabaseHelper = new MusicDatabaseHelper(this);
        if(musicDatabaseHelper.getFlag(this)== 0){
            oneTimeOnly = 0;
            musicDatabaseHelper.setFlag(this);
        } else{
            oneTimeOnly = 1;
        }

    }


    public void activitySelectMode(View v) {
        Intent intent;
      /*  if(oneTimeOnly == 0){
            intent = new Intent(this, MusicTag.class);

        } else {
            intent = new Intent(this, MusicScanOption.class);
        }*/
      intent = new Intent(this, MusicTag.class);
      startActivity(intent);
    }


    public void musicplayer(View view) {
        Intent intent = new Intent(this, MusicPlayer.class);
        startActivity(intent);
    }
}
