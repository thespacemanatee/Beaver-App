package com.example.beever.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.beever.admin.Login;
import com.example.beever.R;
import com.example.beever.database.EventEntry;
import com.example.beever.database.UserEntry;
import com.example.beever.feature.DashboardEventComparator;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class NavigationDrawer extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener{

    private static final int POS_DASHBOARD = 0;
    private static final int POS_MY_PROFILE = 1;
    private static final int POS_SETTINGS = 2;
    private static final int POS_LOGOUT = 4;
    private int pos;
    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();

    private String[] screenTitles;
    private Drawable[] screenIcons;
    private SlidingRootNav slidingRootNav;
    private SharedPreferences mSharedPref;
    private DrawerAdapter adapter;
    private Timestamp upcomingEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My title");

        mSharedPref = getSharedPreferences("SharedPref",MODE_PRIVATE);
        String userId = fAuth.getCurrentUser().getUid();

        slidingRootNav = new SlidingRootNavBuilder(this)
                .withDragDistance(140)
                .withRootViewScale(0.7f)
                .withRootViewElevation(25)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.drawer_menu)
                .inject();

        screenIcons = loadScreenIcons();
        screenTitles = loadScreenTitles();

        adapter = new DrawerAdapter(Arrays.asList(
                createItemFor(POS_DASHBOARD),
                createItemFor(POS_MY_PROFILE),
                createItemFor(POS_SETTINGS),
                new SpaceItem(200),
                createItemFor(POS_LOGOUT)
        ));

        adapter.setListener(this);
        RecyclerView list = findViewById(R.id.drawer_list);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        adapter.setSelected(POS_DASHBOARD);

        TextView countdown = findViewById(R.id.countdown);

        UserEntry.GetUserEntry getUserEntry = new UserEntry.GetUserEntry(userId, 5000) {
            @Override
            public void onPostExecute() {
                UserEntry.GetUserRelevantEvents getUserRelevantEvents = new UserEntry.GetUserRelevantEvents(getResult(), 5000, true, false) {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onPostExecute() {
                        try {
                            Date currentTime = new Date();
                            ArrayList<EventEntry> events = getResult();
                            events.sort(new DashboardEventComparator());

                            for (int i = 0; i < events.size(); i++) {
                                if (events.get(i).getStart_time().toDate().getTime() < currentTime.getTime()) {
                                    upcomingEvent = events.get(i+1).getStart_time();
                                    Log.d("UPCOMING EVENT", "onTick: " + events.get(i));
                                }
                            }

                            new CountDownTimer(upcomingEvent.toDate().getTime() - currentTime.getTime(), 1000) {
                                public void onTick(long millisUntilFinished) {
                                    countdown.setText(new SimpleDateFormat("HH:mm").format(new Date(millisUntilFinished)));
                                }

                                public void onFinish() {
                                    countdown.setText("No upcoming meetings!");
                                }
                            }.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                };
                getUserRelevantEvents.start();
            }
        };
        getUserEntry.start();
    }

    @SuppressWarnings("rawtypes")
    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(color(R.color.beever_pink))
                .withTextTint(color(R.color.black))
                .withSelectedIconTint(color(R.color.beever_pink))
                .withSelectedTextTint(color(R.color.beever_pink));
    }

    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }

    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.id_activityScreenTitles);
    }

    private Drawable[] loadScreenIcons() {
        TypedArray typedArray = getResources().obtainTypedArray(R.array.id_activityScreenIcons);
        Drawable[] icons = new Drawable[typedArray.length()];
        for (int i = 0; i < typedArray.length(); i++) {
            int id = typedArray.getResourceId(i, 0);

            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        typedArray.recycle();
        return icons;
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0 ){
            getFragmentManager().popBackStack();
        } else if (pos == POS_DASHBOARD && slidingRootNav.isMenuClosed()) {
            super.onBackPressed();
        } else if (slidingRootNav.isMenuOpened()) {
            slidingRootNav.closeMenu();
        } else {
            adapter.setSelected(POS_DASHBOARD);
            onItemSelected(POS_DASHBOARD);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

//        String fUserID = fAuth.getCurrentUser().getUid();
//        UserEntry.GetUserEntry getProfileImage = new UserEntry.GetUserEntry(fUserID, 5000) {
//            @Override
//            public void onPostExecute() {
//                if (getResult().getDisplay_picture() != null) {
//                    Glide.with(NavigationDrawer.this).load(getResult().getDisplay_picture()).into((CircleImageView) findViewById(R.id.profile_nav));
//                }
//            }
//        };
//        getProfileImage.start();

        FirebaseUser fUser = fAuth.getCurrentUser();
        if (fUser.getPhotoUrl() != null) {
            Glide.with(NavigationDrawer.this).load(fUser.getPhotoUrl()).into((CircleImageView) findViewById(R.id.profile_nav));
        }
    }

    @Override
    public void onItemSelected(int position) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (position == POS_DASHBOARD) {
            pos = POS_DASHBOARD;
            MainDashboardFragment mainDashboardFragment = new MainDashboardFragment();
            transaction.replace(R.id.container, mainDashboardFragment);

        } else if (position == POS_MY_PROFILE) {
            MyProfileFragment myProfileFragmentFragment = new MyProfileFragment();
            pos = POS_MY_PROFILE;
            transaction.replace(R.id.container, myProfileFragmentFragment);

        } else if (position == POS_SETTINGS) {
            pos = POS_SETTINGS;
            SettingsFragment settingsFragment = new SettingsFragment();
            transaction.replace(R.id.container, settingsFragment);

        } else if (position == POS_LOGOUT) {
            pos = POS_LOGOUT;
            FirebaseAuth.getInstance().signOut();
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.remove("registeredName");
            editor.remove("registeredUsername");
            editor.remove("registeredEmail");
            editor.apply();

            Intent intent = new Intent(NavigationDrawer.this, Login.class);
            startActivity(intent);
            finish();
        }

        slidingRootNav.closeMenu();
        transaction.commit();
    }
}