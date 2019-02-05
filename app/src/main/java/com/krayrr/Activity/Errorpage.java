package com.krayrr.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.krayrr.R;

public class Errorpage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_errorpage);
        getSupportActionBar().hide();
    }
}
