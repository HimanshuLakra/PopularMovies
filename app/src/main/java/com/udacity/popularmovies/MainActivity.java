package com.udacity.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.facebook.stetho.Stetho;
import com.udacity.popularmovies.fragments.GridFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    Bundle uiBundle;
    @Bind(R.id.toolbar_activity)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Stetho.initializeWithDefaults(this);
        ButterKnife.bind(this);

        toolbar.setTitle("Movie Hall");
        setSupportActionBar(toolbar);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = new GridFragment();

        uiBundle = new Bundle();

        if (savedInstanceState == null) {

            uiBundle.putBoolean("twoPaneExists",getTwoPaneUI());
            fragment.setArguments(uiBundle);
            fragmentTransaction.add(R.id.grid_fragment_container, fragment).commit();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    public boolean getTwoPaneUI(){

        if (findViewById(R.id.fragment_detail_container) != null) {
            return true;
        } else {
           return false;
        }
    }

}
