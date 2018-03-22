/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.major.project.musicem.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.major.project.musicem.MainActivity;
import com.major.project.musicem.R;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceRectangle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class PlaceholderActivity extends BaseActivity {

    private String emotion;
    private TextView textView;
    private ProgressBar progressBar;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);
        textView = findViewById(R.id.placeholder);
        emotion = String.valueOf(getIntent().getExtras().get("emotion"));
        textView.setText("You look " + emotion);
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(PlaceholderActivity.this.openFileInput("myImage"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // imageView.setImageBitmap(bitmap);
        detectAndFrame(bitmap);
        initializeToolbar();
    }

    public void analyzeEmotions(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void cool(View view) {
        Toast.makeText(this,"Enjoy the Music!",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), MusicPlayerActivity.class);
        startActivity(intent);
    }

    private static Bitmap drawFaceRectanglesOnBitmap(Bitmap originalBitmap, Face[] faces) {
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        int stokeWidth = 10 ;
        paint.setStrokeWidth(stokeWidth);
        if (faces != null) {
            for (Face face : faces) {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect(
                        faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left + faceRectangle.width,
                        faceRectangle.top + faceRectangle.height,
                        paint);
            }
        }
        return bitmap;
    }

    private void detectAndFrame(final Bitmap imageBitmap) {

         final FaceServiceClient faceServiceClient =
                new FaceServiceRestClient("https://eastasia.api.cognitive.microsoft.com/face/v1.0", "e9d22b0c67284676be2b1a4c4068e1ad");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());
        @SuppressLint("StaticFieldLeak") AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressBar = findViewById(R.id.progress_bar);
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            Log.d("PlaceHolder...........","Detecting.........");
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    false,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                    null           // returnFaceAttributes: a string like "age, gender"
                            );
                            if (result == null) {
                                Log.d("PlaceHolder..........", "Detection Finished. Nothing detected");
                                return null;
                            }
                            Log.d("PlaceHolder..........", String.valueOf(result.length));
                            return result;
                        } catch (Exception e) {
                            publishProgress("Detection failed");
                            return null;
                        }
                    }
                    @Override
                    protected void onPostExecute(Face[] result) {
                        if (result == null) return;
                        ImageView imageView = findViewById(R.id.Face_rect);
                        imageView.setImageBitmap(drawFaceRectanglesOnBitmap(imageBitmap, result));
                        progressBar.setVisibility(View.GONE);
                        imageBitmap.recycle();
                    }
                };
        detectTask.execute(inputStream);
    }
}
