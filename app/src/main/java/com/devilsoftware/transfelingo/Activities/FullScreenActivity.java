package com.devilsoftware.transfelingo.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.devilsoftware.transfelingo.R;

public class FullScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);
        TextView textView = findViewById(R.id.textout);
        textView.setText(getIntent().getStringExtra("text"));
    }
}
