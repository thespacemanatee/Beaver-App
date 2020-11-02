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

public class MainActivity extends AppCompatActivity {

    private static final int NUM_PAGES = 3;
    private static final int SPLASH_TIMEOUT = 3000;
    SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        splashImg.animate().translationY(-3600).setDuration(1000).setStartDelay(3000);
        image.animate().translationY(3400).setDuration(1000).setStartDelay(3000);
        logo.animate().translationY(3400).setDuration(1000).setStartDelay(3000);
        slogan.animate().translationY(3400).setDuration(1000).setStartDelay(3000);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //Check if "firstTime" key value in SharedPreferences is true or false
                mSharedPref = getSharedPreferences("SharedPref",MODE_PRIVATE);
                boolean isFirstTime = mSharedPref.getBoolean("firstTime",true);

                if (!isFirstTime) {

                    //If not the first time launching app, create intent and startActivity with animations
                    Intent intent = new Intent(MainActivity.this,Login.class);

                    Pair[] pairs = new Pair[2];
                    pairs[0] = new Pair<View, String>(image,"logo_image");
                    pairs[1] = new Pair<View, String>(logo,"logo_text");

                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,pairs);
                    startActivity(intent, options.toBundle());

                }
            }
        },SPLASH_TIMEOUT);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSharedPref = getSharedPreferences("SharedPref",MODE_PRIVATE);
                boolean isFirstTime = mSharedPref.getBoolean("firstTime",true);

                //If isFirstTime then set "firstTime" to false in SharedPreferences
                if (isFirstTime) {
                    SharedPreferences.Editor editor = mSharedPref.edit();
                    editor.putBoolean("firstTime", false);
                    editor.commit();

                } else {
                    finish();
                }
            }
        }, SPLASH_TIMEOUT+1000);
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
}