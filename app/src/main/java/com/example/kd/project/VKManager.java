package com.example.kd.project;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.util.VKUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.logging.Handler;

/**
 * Created by Alex on 14.01.2018.
 */

public class VKManager extends Application {
    public static VKAccessToken token;

    static Bitmap cutImage(Bitmap b, double x, double y, double x1, double y1) {
        return Bitmap.createBitmap(b, (int) (x), (int) (y), (int) (x1 - x), (int) (y1 - y), null, false);
    }

    static class Request extends VKRequest.VKRequestListener
    {
        ImageView w;
        Integer defW,defH, type;
        Request(ImageView w, Integer type, Integer defW, Integer defH) {
            this.w = w;
            this.defH = defH;
            this.defW = defW;
            this.type = type;
        }

        @Override
        public void onComplete(VKResponse response) {
            try {
                JSONObject obj = response.json
                        .getJSONArray("response")
                        .getJSONObject(0)
                        .getJSONObject("crop_photo");

                final VKApiPhoto p = new VKApiPhoto(obj.getJSONObject("photo"));

                JSONObject object = obj.getJSONObject("crop");
                final double x = object.getDouble("x");
                final double y = object.getDouble("y");
                final double x1 = object.getDouble("x2");
                final double y1 = object.getDouble("y2");
                ImageLoader imageLoader = ImageLoader.getInstance();
                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .cacheOnDisk(true)
                        .cacheInMemory(true)
                        .build();
                imageLoader.loadImage(getPhoto(p,type), options, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        loadedImage = VKManager.cutImage(loadedImage, loadedImage.getWidth() / 100.0 * x,
                                loadedImage.getHeight() / 100.0 * y,
                                loadedImage.getWidth() / 100.0 * x1,
                                loadedImage.getHeight() / 100.0 * y1);
                        if(defH != null && defW != null)
                            loadedImage = Bitmap.createScaledBitmap(loadedImage, defW, defH, true);
                        final Bitmap img = loadedImage;
                        w.setImageBitmap(img);
                    }
                });
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void setPhotoByUserId(Context context, String id, ImageView imgView, Integer type, Integer defW, Integer defH)
    {
        VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_IDS, id,VKApiConst.FIELDS, "crop_photo"));
        Request t = new Request(imgView, type, defW, defH);
        request.executeWithListener(t);
    }

    private static String getPhoto(VKApiPhoto photo, int type)
    {
        String ans = "";
        switch (type)
        {
            case 5: { ans = photo.photo_2560; if(!ans.equals("")) break; }
            case 4: { ans = photo.photo_1280; if(!ans.equals("")) break; }
            case 3: { ans = photo.photo_807; if(!ans.equals("")) break; }
            case 2: { ans = photo.photo_604; if(!ans.equals("")) break; }
            case 1: { ans = photo.photo_130; if(!ans.equals("")) break; }
            case 0: { ans = photo.photo_75; if(!ans.equals("")) break; }
        }
        return ans;
    }

    VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                //Intent intent = new Intent(VKManager.this, MainActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //startActivity(intent);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        ImageLoaderConfiguration config = ImageLoaderConfiguration.createDefault(this);
        ImageLoader.getInstance().init(config);

        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(this);
        //Log.d("asda", VKUtil.getCertificateFingerprint(this, this.getPackageName())[0]);
    }
}