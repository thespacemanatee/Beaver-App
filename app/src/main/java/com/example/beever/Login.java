package com.example.beever;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    //Create variables for each element
    private Button callRegistration, loginButton;
    private ImageView image;
    private TextView logoText, signUpText;
    private TextInputLayout username, password;
    private SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Set firstTime to false in SharedPreferences
        mSharedPref = getSharedPreferences("SharedPref",MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean("firstTime", false);
        editor.commit();

        //Hooks
        callRegistration = findViewById(R.id.sign_up);
        image = findViewById(R.id.logo_image);
        logoText = findViewById(R.id.logo_name);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.log_in);
        signUpText = findViewById(R.id.sign_up_text);

        //Create OnClickListener for sign up button
        callRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Starts new registration activity when clicked on with smooth animations
                Intent intent = new Intent(Login.this,Registration.class);


                Pair[] pairs = new Pair[7];
                pairs[0] = new Pair<View, String>(image,"logo_image");
                pairs[1] = new Pair<View, String>(logoText,"logo_text");
                pairs[2] = new Pair<View, String>(username,"user_tran");
                pairs[3] = new Pair<View, String>(password,"password_tran");
                pairs[4] = new Pair<View, String>(loginButton,"button_tran");
                pairs[5] = new Pair<View, String>(signUpText,"sign_up_text_tran");
                pairs[6] = new Pair<View, String>(callRegistration,"sign_up_tran");

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Login.this,pairs);
                startActivity(intent,options.toBundle());

            }
        });

        //Create OnClickListener for login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Call loginUser() method on click login button
                loginUser(v);
            }
        });
    }

    private Boolean validateUserName() {
        String s = username.getEditText().getText().toString();

        if (s.isEmpty()) {
            username.setError("Field cannot be empty");
            return false;

        } else {
            username.setError(null);
            username.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword() {
        String s = password.getEditText().getText().toString();

        if (s.isEmpty()) {
            password.setError("Field cannot be empty");
            return false;

        } else {
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }

    public void loginUser(View v) {

        //Validate if anything is entered in login fields
        if (!validateUserName() || !validatePassword()) {
            return;

        } else {

            //Call isUser() to authenticate credentials
            isUser();
        }

    }

    private void isUser() {

        //Get user entered credentials and parse them into strings that can be fed into auth functions
        String usernameProvided = username.getEditText().getText().toString().trim();
        String passwordProvided = password.getEditText().getText().toString().trim();

        //Create new reference from Firebase Database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        //Check if username provided is equal to any username in database
        Query validateUser = reference.orderByChild("username").equalTo(usernameProvided);

        validateUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    username.setError(null);
                    username.setErrorEnabled(false);

                    //Get password from entered username from database
                    String passwordFromDB = dataSnapshot.child(usernameProvided).child("password").getValue(String.class);

                    //Check if password is valid
                    if (passwordFromDB.equals(passwordProvided)) {

                        password.setError(null);
                        password.setErrorEnabled(false);

                        //Retrieve relevant data from database
                        String nameFromDB = dataSnapshot.child(usernameProvided).child("name").getValue(String.class);
                        String usernameFromDB = dataSnapshot.child(usernameProvided).child("username").getValue(String.class);
                        String emailFromDB = dataSnapshot.child(usernameProvided).child("email").getValue(String.class);

                        //Store user data to SharedPreferences
                        SharedPreferences.Editor editor = mSharedPref.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("registeredName", nameFromDB);
                        editor.putString("registeredUsername", usernameFromDB);
                        editor.putString("registeredEmail", emailFromDB);
                        editor.putString("registeredPassword", passwordFromDB);
                        editor.commit();

                        //Pass user data into new intent as Extras, and start new activity
                        Intent intent = new Intent(getApplicationContext(),UserProfile.class);
                        intent.putExtra("name",nameFromDB);
                        intent.putExtra("username",usernameFromDB);
                        intent.putExtra("email",emailFromDB);
                        intent.putExtra("password",passwordFromDB);

                        startActivity(intent);
                        finish();

                    } else {
                        password.setError("Wrong password");
                        password.requestFocus();
                    }
                } else {
                    username.setError("Username does not exist");
                    username.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

    }

}