package com.hackathon.igfeels;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.hackathon.igfeels.api.ApiFactory;
import com.hackathon.igfeels.instagramApi.ApplicationData;
import com.hackathon.igfeels.instagramApi.InstagramApp;
import com.hackathon.igfeels.instagramApi.InstagramSession;
import com.hackathon.igfeels.instagramApi.MediaResult;
import com.hackathon.igfeels.instagramApi.UserQueryResult;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
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
    PhotoDetector detection;
    int currentIndex;
    String token;

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
            fetchUserIdFromName(username.getText().toString());
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

    public void fetchMediaFromUserId(int userId){
        ApiFactory.getApi().getUserMedia(String.valueOf(userId), token).enqueue(new Callback<MediaResult>() {
            @Override
            public void onResponse(Response<MediaResult> response, Retrofit retrofit) {
                MediaResult mr = response.body();
                if(mr != null && mr.getData().length > 0){
                    displayImages(mr.getData());
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
        analyze.setEnabled(true);

        for(MediaResult.MediaElement me : data){
            if(!me.getType().equals(MediaResult.MediaElement.TYPE_IMAGE)) continue;

            View v = LayoutInflater.from(this).inflate(R.layout.image_list_item, container, false);

            String url = me.getImages().getLowResolution().getUrl();
            Log.d(TAG, "Image URL: " + url);
            long time = Long.valueOf(me.getCreatedTime()) * 1000L;
            Log.d(TAG, "Time: " + time);

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

    public Bitmap getBitmapFromAsset(Context context, String filePath) throws IOException {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap;
        istr = assetManager.open(filePath);
        bitmap = BitmapFactory.decodeStream(istr);

        return bitmap;
    }

    public Bitmap getBitmapFromUri(Uri uri) throws FileNotFoundException {
        InputStream istr;
        Bitmap bitmap;
        istr = getContentResolver().openInputStream(uri);
        bitmap = BitmapFactory.decodeStream(istr);

        return bitmap;
    }

    @Override
    public void onImageResults(List<Face> faces, Frame image, float timestamp) {

        Log.d(TAG, "Found " + faces.size() + " faces for index " + currentIndex);

        currentIndex++;
        ad.setMessage(currentIndex + "/" + imageData.length + " processed");
        new ImageDownloadTask().execute();
    }
}
