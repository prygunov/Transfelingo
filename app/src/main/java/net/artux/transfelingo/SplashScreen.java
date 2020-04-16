package net.artux.transfelingo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import net.artux.transfelingo.Activities.MainActivity;


/**
 * Created by Максим on 21.10.2017.
 */

public class SplashScreen extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
