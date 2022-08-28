package com.jovistar.caltxt.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.images.ImageLoader;
import com.jovistar.caltxt.images.ImageViewZoom;

/**
 * Created by jovika on 5/17/2017.
 */

public class PhotoFullscreen extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_fullscreen);

        String pic_url = getIntent().getStringExtra("URL");
        final ImageViewZoom imageView = findViewById(R.id.imageView);

        ImageLoader.getInstance(this).DisplayImage(
                pic_url, imageView,
                0/*means full size*/,
                R.drawable.ic_person_white_24dp_web, false);

//        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//        imageView.setAdjustViewBounds(true);

//        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
//        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
    }
}
