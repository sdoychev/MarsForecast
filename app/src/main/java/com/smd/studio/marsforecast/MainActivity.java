package com.smd.studio.marsforecast;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Random;


public class MainActivity extends Activity {

    final static String RECENT_API_ENDPOINT = "http://marsweather.ingenology.com/v1/latest/";
    final static String FLICKR_API_KEY = "26d69afddc218b64b43facc7b856358f";
    final static String IMAGES_API_ENDPOINT = "https://api.flickr.com/services/rest/?format=json&nojsoncallback=1&sort=random&method=flickr.photos.search&tags=mars,planet,rover&tag_mode=all&api_key=";
    final static String SHARED_PREFS_IMG_KEY = "img";
    final static String SHARED_PREFS_DAY_KEY = "day";
    final static int mainColor = Color.parseColor("#FF5722");
    final static int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    TextView mTxtDegrees, mTxtWeather, mTxtError;
    ImageView mImageView;
    MarsWeather helper = MarsWeather.getInstance();
    SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTxtDegrees = (TextView) findViewById(R.id.degrees);
        mTxtWeather = (TextView) findViewById(R.id.weather);
        mTxtError = (TextView) findViewById(R.id.error);
        mImageView = (ImageView) findViewById(R.id.main_bg);
        mTxtDegrees.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-Light.ttf"));
        mTxtWeather.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-Light.ttf"));

        mSharedPref = getPreferences(Context.MODE_PRIVATE);
        if (mSharedPref.getInt(SHARED_PREFS_DAY_KEY, 0) != today) {
            // Search and load a random mars picture
            try {
                searchRandomImage();
            } catch (Exception e) {
                // Please remember to set your own Flickr API! Otherwise I won't be able to show a random Mars picture
                imageError(e);
            }
        } else {
            // We already have a pict of the day, let's load it
            loadImage(mSharedPref.getString(SHARED_PREFS_IMG_KEY, ""));
        }
        loadWeatherData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadWeatherData() {
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.GET, RECENT_API_ENDPOINT, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String minTemp, maxTemp, atmo;
                    int avgTemp;
                    response = response.getJSONObject("report");
                    minTemp = response.getString("min_temp");
                    minTemp = minTemp.substring(0, minTemp.indexOf("."));
                    maxTemp = response.getString("max_temp");
                    maxTemp = maxTemp.substring(0, maxTemp.indexOf("."));
                    avgTemp = (Integer.parseInt(minTemp) + Integer.parseInt(maxTemp)) / 2;
                    atmo = response.getString("atmo_opacity");
                    mTxtDegrees.setText(avgTemp + "°");
                    mTxtWeather.setText(atmo);
                } catch (Exception e) {
                    txtError(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                txtError(error);
            }
        });
        request.setPriority(Request.Priority.HIGH);
        helper.add(request);
    }

    private void txtError(Exception e) {
        mTxtError.setVisibility(View.VISIBLE);
        e.printStackTrace();
    }

    private void searchRandomImage() throws Exception {
        if (FLICKR_API_KEY.equals("")) {
            throw new Exception("You didn't provide a working Flickr API!");
        }
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.GET, IMAGES_API_ENDPOINT + FLICKR_API_KEY, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray images = response.getJSONObject("photos").getJSONArray("photo");
                    int index = new Random().nextInt(images.length());
                    JSONObject imageItem = images.getJSONObject(index);
                    String imageUrl = "http://farm" + imageItem.getString("farm") + ".static.flickr.com/" + imageItem.getString("server") + "/" + imageItem.getString("id") + "_" + imageItem.getString("secret") + "_" + "c.jpg";
                    SharedPreferences.Editor editor = mSharedPref.edit();
                    editor.putInt(SHARED_PREFS_DAY_KEY, today);
                    editor.putString(SHARED_PREFS_IMG_KEY, imageUrl);
                    editor.commit();
                    loadImage(imageUrl);
                } catch (Exception e) {
                    imageError(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                imageError(error);
            }
        });
        request.setPriority(Request.Priority.LOW);
        helper.add(request);
    }

    private void loadImage(String imageUrl) {
        // Retrieves an image specified by the URL, and displays it in the UI
        ImageRequest request = new ImageRequest(imageUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                mImageView.setImageBitmap(bitmap);
            }
        }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                imageError(error);
            }
        });
        helper.add(request);
    }

    private void imageError(Exception e) {
        mImageView.setBackgroundColor(mainColor);
        e.printStackTrace();
    }

}
