package com.udacity.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.facebook.stetho.Stetho;
import com.udacity.popularmovies.fragments.GridFragment;

public class MainActivity extends AppCompatActivity {

    boolean twoPaneUI = false;
    Bundle uiBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Stetho.initializeWithDefaults(this);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = new GridFragment();

        uiBundle = new Bundle();

        if (savedInstanceState == null) {

            if (findViewById(R.id.fragment_detail_container) != null) {
                twoPaneUI = true;
            } else {
                twoPaneUI = false;
            }

            uiBundle.putBoolean("twoPaneExists",twoPaneUI);
            fragment.setArguments(uiBundle);
            fragmentTransaction.add(R.id.grid_fragment_container, fragment).commit();
        }

    }

}
