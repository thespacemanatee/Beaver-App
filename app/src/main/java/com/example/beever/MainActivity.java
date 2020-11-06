package com.example.beever;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.beever.navigation.NavigationDrawer;

public class MainActivity extends AppCompatActivity {

    private static final int NUM_PAGES = 3;
    public static final int SPLASH_TIMEOUT = 2000;
    private SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPref = getSharedPreferences("SharedPref",MODE_PRIVATE);

        //Set fullscreen mode to allow drawing over cutout
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


        //Animations for logo
        Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        //Initialise onBoarding
        ViewPager viewPager = findViewById(R.id.pager);
        ScreenSlidePagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        //Hooks
        ImageView splashImg = findViewById(R.id.img);
        ImageView image = findViewById(R.id.imageView);
        TextView logo = findViewById(R.id.textView);
        TextView slogan = findViewById(R.id.textView2);

        //Set logo animations to hooks
        image.setAnimation(topAnim);
        logo.setAnimation(bottomAnim);
        slogan.setAnimation(bottomAnim);


        //Not sure why it lags... sian
//        Animation anim = AnimationUtils.loadAnimation(this,R.anim.onboarding_animation);
//        viewPager.startAnimation(anim);

        //Splash screen exit animation
        splashImg.animate().translationY(-4200).setDuration(1000).setStartDelay(2000);
        image.animate().translationY(3400).setDuration(1000).setStartDelay(2000);
        logo.animate().translationY(3400).setDuration(1000).setStartDelay(2000);
        slogan.animate().translationY(3400).setDuration(1000).setStartDelay(2000);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //Check if "firstTime" key value in SharedPreferences is true or false
                boolean isFirstTime = mSharedPref.getBoolean("firstTime",true);
                boolean isRegistered = mSharedPref.getBoolean("isLoggedIn", false);


                if (!isFirstTime) {

                    //Check if user was registered before and still logged in from previous session
                    if (isRegistered) {

                        //Pass stored user data into new userprofile activity as Extras
                        Intent intent = new Intent(MainActivity.this, NavigationDrawer.class);

                        startActivity(intent);
                        finish();

                    } else {

                        //If not the first time launching app, create intent and startActivity with animations
                        Intent intent = new Intent(MainActivity.this,Login.class);

                        Pair[] pairs = new Pair[2];
                        pairs[0] = new Pair<View, String>(image,"logo_image");
                        pairs[1] = new Pair<View, String>(logo,"logo_text");

                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,pairs);
                        startActivity(intent, options.toBundle());
                    }
                }
            }
        },SPLASH_TIMEOUT);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean isFirstTime = mSharedPref.getBoolean("firstTime",true);

                //If not the first time launching app then finish() after slight delay
                if (!isFirstTime) {
                    finish();

                }
            }
        }, SPLASH_TIMEOUT+500);
    }

    //Create LiquidPagers
    private static class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new OnBoardingFragment1();

                case 1:
                    return new OnBoardingFragment2();

                case 2:
                    return new OnBoardingFragment3();
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    @Override
    public void onBackPressed() {

        boolean isLoggedIn = mSharedPref.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putBoolean("firstTime", false);
            editor.commit();
        }
        super.onBackPressed();
    }
}