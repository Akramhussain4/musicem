package com.example.hampi.musicemlogin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.FaceRectangle;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.contract.Scores;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ///Akram Hussain

    private Uri mUriPhotoTaken;
    private static final int REQUEST_TAKE_PHOTO = 0;


    private ProgressBar progressBar;
    private FirebaseAuth auth;
    String status;


    // The button to select an image
    private Button mButtonSelectImage;

    // The URI of the image selected to detect.
    private Uri mImageUri;

    //private TextView mEditText;

    private EmotionServiceClient client;

    private Bitmap mBitmap;
    private static final int REQUEST_SELECT_IMAGE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);



        auth = FirebaseAuth.getInstance();


        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
       // setDataToView(user);

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null) {
            // Save the photo taken to a temporary file.
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                File file = File.createTempFile("IMG_", ".jpg", storageDir);
                mUriPhotoTaken = Uri.fromFile(file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriPhotoTaken);
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            } catch (IOException e) {
               // Log.e(getMessage());
            }
        }




    // this listener will be called when there is change in firebase user session
    FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // user auth state is changed - user is null
                // launch login activity
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "fail", Toast.LENGTH_SHORT).show();

            }
        }


    };




        if (client == null) {
            client = new EmotionServiceRestClient(getString(R.string.subscription_key));
        }
        //mEditText = (TextView) findViewById(R.id.editText);
    }




    //sign out method
  /**  public void signOut() {
       auth.signOut();
**/

// this listener will be called when there is change in firebase user session
        FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }












    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("RecognizeActivity", "onActivityResult");
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    // If image is selected successfully, set the image URI and bitmap.
                //   mImageUri = data.getData();

                    mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                           mUriPhotoTaken, getContentResolver());
                    if (mBitmap != null) {

                        // Show the image on screen.


                        // Add detection log.
                        Log.d("RecognizeActivity", "Image: " + mUriPhotoTaken + " resized to " + mBitmap.getWidth()
                                + "x" + mBitmap.getHeight());
                        doRecognize();

                    }

                }
                break;
            default:
                break;
        }
    }

    public void doRecognize() {
//        mButtonSelectImage.setEnabled(false);

        // Do emotion detection using auto-detected faces.
        try {
            new MainActivity.doRequest(false).execute();
        } catch (Exception e) {
            //mEditText.append("Error encountered. Exception is: " + e.toString());
        }

        String faceSubscriptionKey = getString(R.string.faceSubscription_key);
        if (faceSubscriptionKey.equalsIgnoreCase("Please_add_the_face_subscription_key_here")) {
            //mEditText.append("\n\nThere is no face subscription key in res/values/strings.xml. Skip the sample for detecting emotions using face rectangles\n");
        } else {
            // Do emotion detection using face rectangles provided by Face API.
            try {
                new MainActivity.doRequest(true).execute();
            } catch (Exception e) {
              //  mEditText.append("Error encountered. Exception is: " + e.toString());
            }
        }
    }



    private List<RecognizeResult> processWithAutoFaceDetection() throws EmotionServiceException, IOException {
        Log.d("emotion", "Start emotion detection with auto-face detection");

        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long startTime = System.currentTimeMillis();
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE STARTS HERE
        // -----------------------------------------------------------------------

        List<RecognizeResult> result = null;
        //
        // Detect emotion by auto-detecting faces in the image.
        //
        result = this.client.recognizeImage(inputStream);

        String json = gson.toJson(result);
        Log.d("result", json);

        Log.d("emotion", String.format("Detection done. Elapsed time: %d ms", (System.currentTimeMillis() - startTime)));
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE ENDS HERE
        // -----------------------------------------------------------------------
        return result;
    }

    private List<RecognizeResult> processWithFaceRectangles() throws EmotionServiceException, com.microsoft.projectoxford.face.rest.ClientException, IOException {
        Log.d("emotion", "Do emotion detection with known face rectangles");
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long timeMark = System.currentTimeMillis();
        Log.d("emotion", "Start face detection using Face API");
        FaceRectangle[] faceRectangles = null;
        String faceSubscriptionKey = getString(R.string.faceSubscription_key);
        FaceServiceRestClient faceClient = new FaceServiceRestClient(faceSubscriptionKey);
        Face faces[] = faceClient.detect(inputStream, false, false, null);
        Log.d("emotion", String.format("Face detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));

        if (faces != null) {
            faceRectangles = new FaceRectangle[faces.length];

            for (int i = 0; i < faceRectangles.length; i++) {
                // Face API and Emotion API have different FaceRectangle definition. Do the conversion.
                com.microsoft.projectoxford.face.contract.FaceRectangle rect = faces[i].faceRectangle;
                faceRectangles[i] = new com.microsoft.projectoxford.emotion.contract.FaceRectangle(rect.left, rect.top, rect.width, rect.height);
            }
        }

        List<RecognizeResult> result = null;
        if (faceRectangles != null) {
            inputStream.reset();

            timeMark = System.currentTimeMillis();
            Log.d("emotion", "Start emotion detection using Emotion API");
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE STARTS HERE
            // -----------------------------------------------------------------------
            result = this.client.recognizeImage(inputStream, faceRectangles);

            String json = gson.toJson(result);
            Log.d("result", json);
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE ENDS HERE
            // -----------------------------------------------------------------------
            Log.d("emotion", String.format("Emotion detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));
        }
        return result;
    }


    private class doRequest extends AsyncTask<String, String, List<RecognizeResult>> {
        // Store error message
        private Exception e = null;
        private boolean useFaceRectangles = false;

        public doRequest(boolean useFaceRectangles) {
            this.useFaceRectangles = useFaceRectangles;
        }

        @Override
        protected List<RecognizeResult> doInBackground(String... args) {
            if (this.useFaceRectangles == false) {
                try {
                    return processWithAutoFaceDetection();
                } catch (Exception e) {
                    this.e = e;    // Store error
                }
            } else {
                try {
                    return processWithFaceRectangles();
                } catch (Exception e) {
                    this.e = e;    // Store error
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<RecognizeResult> result) {
            super.onPostExecute(result);
            // Display based on error existence

            if (this.useFaceRectangles == false) {
               // mEditText.append("\n\nRecognizing emotions with auto-detected face rectangles...\n");
            } else {
                //mEditText.append("\n\nRecognizing emotions with existing face rectangles from Face API...\n");
            }
            if (e != null) {
              //  mEditText.setText("Error: " + e.getMessage());
                this.e = null;
            } else {
                if (result.size() == 0) {
                 //   mEditText.append("No emotion detected :(");
                } else {
                    Integer count = 0;
                    // Covert bitmap to a mutable bitmap by copying it
                    Bitmap bitmapCopy = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas faceCanvas = new Canvas(bitmapCopy);
                    faceCanvas.drawBitmap(mBitmap, 0, 0, null);
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(5);
                    paint.setColor(Color.RED);


                    for (RecognizeResult r : result) {


                         status = getEmo(r);


                    }

                    for (RecognizeResult t : result) {
                        String.format("\nFace #%1$d \n", count);
                        String.format("\t anger: %1$.5f\n", t.scores.anger);
                        String.format("\t contempt: %1$.5f\n", t.scores.contempt);
                        String.format("\t disgust: %1$.5f\n", t.scores.disgust);
                        String.format("\t fear: %1$.5f\n", t.scores.fear);
                        String.format("\t happiness: %1$.5f\n", t.scores.happiness);
                        String.format("\t neutral: %1$.5f\n", t.scores.neutral);
                        String.format("\t sadness: %1$.5f\n", t.scores.sadness);
                        String.format("\t surprise: %1$.5f\n", t.scores.surprise);

                    }


                    Log.d("response",status);




                }


            }


        }





        private String getEmo(RecognizeResult res) {
            List<Double> list = new ArrayList<>();
            Scores scores = res.scores;

            list.add(scores.anger);
            list.add(scores.happiness);
            list.add(scores.contempt);
            list.add(scores.disgust);
            list.add(scores.fear);
            list.add(scores.neutral);
            list.add(scores.sadness);
            list.add(scores.surprise);

            Collections.sort(list);



            double maxNum = list.get(list.size() - 1);
            if(maxNum == scores.anger)
                return "Anger";
            else if(maxNum == scores.happiness)
                return "Happy";
            else if(maxNum == scores.contempt)
                return "Contemp";
            else if(maxNum == scores.disgust)
                return "Disgust";
            else if(maxNum == scores.fear)
                return "Fear";
            else if(maxNum == scores.neutral)
                return "Neutral";
            else if(maxNum == scores.sadness)
                return "Sadness";
            else if(maxNum == scores.surprise)
                return "Surprise";
            else
                return "Neutral";

        }

    }
}



