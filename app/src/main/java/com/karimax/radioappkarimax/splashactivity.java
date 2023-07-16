package com.karimax.radioappkarimax;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.karimax.radioappkarimax.R;

public class splashactivity extends AppCompatActivity {
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashactivity);

        imageView = findViewById(R.id.imageview);

        // Adding the gif here using glide library


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i=new Intent(splashactivity.this,MainActivity.class);
               i.putExtra("Origin","splash");
                startActivity(i);
                finish();
            }
        }, 2000);






    }
}