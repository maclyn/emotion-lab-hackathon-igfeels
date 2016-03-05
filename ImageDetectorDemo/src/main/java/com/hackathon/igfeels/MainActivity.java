package com.hackathon.igfeels;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.affectiva.android.affdex.sdk.detector.Detector;
import com.affectiva.android.affdex.sdk.detector.Face;
import com.affectiva.android.affdex.sdk.detector.PhotoDetector;
import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.igfeels.R;
import com.hackathon.igfeels.api.ApiFactory;
import com.hackathon.igfeels.api.EmptyResponse;
import com.hackathon.igfeels.api.PavlokApiFactory;
import com.hackathon.igfeels.instagramApi.ApplicationData;
import com.hackathon.igfeels.instagramApi.InstagramApp;
import com.hackathon.igfeels.instagramApi.InstagramSession;
import com.hackathon.igfeels.instagramApi.MediaResult;
import com.hackathon.igfeels.instagramApi.UserEntry;
import com.hackathon.igfeels.instagramApi.UserProfileResult;
import com.hackathon.igfeels.instagramApi.UserQueryResult;
import com.squareup.okhttp.RequestBody;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * A sample app showing how to use ImageDetector.
 *
 * This app is not a production release and is known to have bugs. Specifically, the UI thread is blocked while the image is being processed,
 * and the app will crash if the user tries loading a very large image.
 *
 * For some images, facial tracking dots will not appear in the correct location.
 *
 * Also, the UI element that displays metrics is not aesthetic.
 *
 */
public class MainActivity extends Activity implements Detector.ImageListener {
    private static final String TAG = "MainActivity";
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("'Posted' h:mm aa 'on' MMMM d", Locale.US);

    @Bind(R.id.container) LinearLayout container;
    @Bind(R.id.analyze) Button analyze;
    @Bind(R.id.celebName) EditText username;

    InstagramApp mApp;
    AlertDialog ad;
    MediaResult.MediaElement[] imageData;
    List<EmotionResults> results;
    PhotoDetector detection;
    int currentIndex;
    String token;
    String objectId;

    private class ImageDownloadTask extends AsyncTask<Void, Void, Frame>{
        @Override
        protected Frame doInBackground(Void... params) {
            int index = currentIndex;
            if(index >= imageData.length) return null;

            try {
                Bitmap b = Picasso.with(MainActivity.this).load(imageData[index].getImages().getStandardResolution().getUrl()).get();
                return new Frame.BitmapFrame(b, Frame.COLOR_FORMAT.UNKNOWN_TYPE);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Frame result) {
            if(result == null){ //At end of list/error
                ad.dismiss();

                if(results.size() > 0){
                    Toast.makeText(MainActivity.this, "Found " + results.size() + " faces.", Toast.LENGTH_SHORT).show();

                    String out = "?count=" + results.size();
                    for(int i = 0; i < results.size(); i++){
                        out += "&image" + i + "_imageid=" + i;
                        out += "&image" + i + "_joy=" + String.format("%1.3f", results.get(i).getHappiness() / 100);
                        out += "&image" + i + "_sadness=" + String.format("%1.3f", results.get(i).getSadness() / 100);
                        out += "&image" + i + "_anger=" + String.format("%1.3f", results.get(i).getAnger() / 100);
                        out += "&image" + i + "_engagement=" + String.format("%1.3f", results.get(i).getEngagement() / 100);
                    }

                    WebView wv = new WebView(MainActivity.this);
                    wv.getSettings().setAllowContentAccess(true);
                    wv.getSettings().setAllowFileAccess(true);
                    wv.getSettings().setAllowFileAccessFromFileURLs(true);
                    wv.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                    wv.getSettings().setDisplayZoomControls(true);
                    wv.getSettings().setSupportZoom(true);
                    wv.getSettings().setBuiltInZoomControls(true);
                    wv.getSettings().setJavaScriptEnabled(true);

                    EmotionResults mostRecent = results.get(0);

                    String title = "";
                    String alert = "";
                    if(greaterThanAllOthers(mostRecent.getAnger(), mostRecent.getSadness(),
                            mostRecent.getEngagement(), mostRecent.getHappiness())){
                        title = "Anger!";
                        alert = "shock";
                    } else if (greaterThanAllOthers(mostRecent.getHappiness(), mostRecent.getSadness(),
                            mostRecent.getEngagement(), mostRecent.getAnger())) {
                        title = "Happiness!";
                        alert = "vibrate";
                    } else if (greaterThanAllOthers(mostRecent.getSadness(), mostRecent.getAnger(),
                            mostRecent.getEngagement(), mostRecent.getHappiness())) {
                        title = "Sadness :(";
                        alert = "beep";
                    } else {
                        title = "Engagement?";
                        alert = "vibrate";
                    }

                    final String finalTitle = title;
                    final String finalAlert = alert;
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Emotions Graph")
                            .setView(wv)
                            .setPositiveButton("Okay", null)
                            .setNeutralButton("Send " + title, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PavlokApiFactory.getApi().sendAlert(objectId,
                                            finalAlert, finalAlert).enqueue(new Callback<EmptyResponse>() {
                                        @Override
                                        public void onResponse(Response<EmptyResponse> response, Retrofit retrofit) {
                                        }

                                        @Override
                                        public void onFailure(Throwable t) {
                                        }
                                    });
                                }
                            })
                            .show();

                    wv.loadUrl("file:///android_asset/www/chart.html" + out);
                    Log.d(TAG, "To feed: " + out);
                } else {
                    Toast.makeText(MainActivity.this, "No faces found in the photos!", Toast.LENGTH_SHORT).show();
                }
            } else {
                try {
                    detection = new PhotoDetector(MainActivity.this);
                    detection.setDetectAllEmotions(true); //emotions
                    detection.setDetectAllExpressions(true); //expressions
                    detection.setLicensePath("Affdex.license");
                    detection.setImageListener(MainActivity.this);

                    startDetector();
                    detection.process(result);
                    stopDetector();
                } catch (Exception e){
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        }
    }

    private boolean greaterThanAllOthers(float f, float... f2){
        for(float f3 : f2){
            if(f3 > f) return false;
        }
        return true;
    }

    private class EmotionResults {
        private float happiness;
        private float sadness;
        private float anger;
        private float engagement;

        public EmotionResults(float happiness, float sadness, float anger, float engagement) {
            this.happiness = happiness;
            this.sadness = sadness;
            this.anger = anger;
            this.engagement = engagement;
        }

        public float getHappiness() {
            return happiness;
        }

        public float getSadness() {
            return sadness;
        }

        public float getAnger() {
            return anger;
        }

        public float getEngagement() {
            return engagement;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mApp = new InstagramApp(this, ApplicationData.CLIENT_ID, ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL);
        mApp.setListener(new InstagramApp.OAuthAuthenticationListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Successfully authorized.", Toast.LENGTH_SHORT).show();
                token = new InstagramSession(MainActivity.this).getAccessToken();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
        if(new InstagramSession(this).getAccessToken() == null) {
            mApp.authorize();
        } else {
            token = new InstagramSession(this).getAccessToken();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    void startDetector() {
        if (!detection.isRunning()) {
            detection.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    void stopDetector() {
        if (detection.isRunning()) {
            detection.stop();
        }
    }

    @OnClick(R.id.doTheThing)
    public void startFetch(){
        this.imageData = null;
        analyze.setEnabled(false);
        container.removeAllViews();

        if(token != null){
            try {
                Integer result = Integer.parseInt(username.getText().toString());
                fetchMediaFromUserId(result);
            } catch (NumberFormatException nfe){
                fetchUserIdFromName(username.getText().toString());
            }
        } else {
            Log.d(TAG, "Token was null!");
            mApp.authorize();
        }
    }

    public void fetchUserIdFromName(String name){
        ApiFactory.getApi().getUserId(name, 1, token).enqueue(new Callback<UserQueryResult>() {
            @Override
            public void onResponse(Response<UserQueryResult> response, Retrofit retrofit) {
                if(response.isSuccess() && response.body() != null && response.body().getData() != null && response.body().getData().length > 0){
                    fetchMediaFromUserId(response.body().getData()[0].getId());
                } else {
                    Toast.makeText(MainActivity.this, "No matching user found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(MainActivity.this, "Error finding users..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserProfile(final int userId){
        ApiFactory.getApi().getUserProfile(String.valueOf(userId), token).enqueue(new Callback<UserProfileResult>() {
            @Override
            public void onResponse(Response<UserProfileResult> response, Retrofit retrofit) {
                Log.d(TAG, response.body().toString());

                try {
                    String bio = response.body().getData().getBio();
                    int pavIdIndex = bio.indexOf("pavid=");
                    if(pavIdIndex != -1){
                        objectId = bio.substring(pavIdIndex + 6);
                        Log.d(TAG, objectId);
                    }
                } catch (Exception ignored) {}
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    public void fetchMediaFromUserId(final int userId){
        ApiFactory.getApi().getUserMedia(String.valueOf(userId), token).enqueue(new Callback<MediaResult>() {
            @Override
            public void onResponse(Response<MediaResult> response, Retrofit retrofit) {
                MediaResult mr = response.body();
                if(mr != null && mr.getData().length > 0){
                    displayImages(mr.getData());
                    fetchUserProfile(userId);
                } else {
                    Toast.makeText(MainActivity.this, "No images from the user :(", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(MainActivity.this, "Error finding user images..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayImages(MediaResult.MediaElement[] data) {
        this.currentIndex = 0;
        this.imageData = data;
        this.results = new ArrayList<>();
        analyze.setEnabled(true);

        for(MediaResult.MediaElement me : data){
            if(!me.getType().equals(MediaResult.MediaElement.TYPE_IMAGE)) continue;

            View v = LayoutInflater.from(this).inflate(R.layout.image_list_item, container, false);

            String url = me.getImages().getLowResolution().getUrl();
            long time = Long.valueOf(me.getCreatedTime()) * 1000L;

            Picasso.with(this).load(url).into(((ImageView) v.findViewById(R.id.thumbnail)));
            ((TextView) v.findViewById(R.id.date)).setText(timeFormat.format(new Date(time)));

            container.addView(v);
        }
    }

    @OnClick(R.id.analyze)
    public void analyze(){
        analyze.setEnabled(false);
        ad = new AlertDialog.Builder(this)
                .setMessage("Waiting...")
                .setCancelable(false)
                .setTitle("Analyzing with Affectiva")
                .create();
        ad.show();
        new ImageDownloadTask().execute();
    }

    @Override
    public void onImageResults(List<Face> faces, Frame image, float timestamp) {

        Log.d(TAG, "Found " + faces.size() + " faces for index " + currentIndex);

        if(faces.size() > 0) {
            float happy = 0;
            float sad = 0;
            float anger = 0;
            float engagement = 0;
            for (int i = 0; i < faces.size(); i++) {
                Face f = faces.get(i);

                happy += f.emotions.getJoy();
                sad += f.emotions.getSadness();
                anger += f.emotions.getAnger();
                engagement += f.emotions.getEngagement();
            }
            happy /= faces.size();
            sad /= faces.size();
            anger /= faces.size();
            engagement /= faces.size();

            results.add(new EmotionResults(happy, sad, anger, engagement));

            Log.d(TAG, "Results of processing: happy=" + happy + ", sad=" + sad + ", " +
                    "anger=" + anger + ", engagement=" + engagement);
        }

        currentIndex++;
        ad.setMessage(currentIndex + "/" + imageData.length + " processed");
        new ImageDownloadTask().execute();
    }
}
