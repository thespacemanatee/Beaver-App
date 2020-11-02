package com.example.beever;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserProfile extends AppCompatActivity {

    //Create variables for each element
    private TextInputLayout name, email, password;
    private TextView usernameLabel, nameLabel;
    private SharedPreferences mSharedPref;

    //Global variables to hold user data inside this activity
    private String _USERNAME,_NAME,_EMAIL,_PASSWORD;
    private DatabaseReference reference;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //Create new reference to Firebase database
        reference = FirebaseDatabase.getInstance().getReference("Users");
        mSharedPref = getSharedPreferences("SharedPref",MODE_PRIVATE);

        //Hooks
        usernameLabel = findViewById(R.id.username_label);
        nameLabel = findViewById(R.id.name_label);
        name = findViewById(R.id.name_field);
        email = findViewById(R.id.email_field);
        password = findViewById(R.id.password_field);
        MaterialButton signOut = findViewById(R.id.signout_button);
        FloatingActionButton toOnBoarding = findViewById(R.id.to_onboarding);

        showUserData();

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSharedPref = getSharedPreferences("SharedPref",MODE_PRIVATE);
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.remove("registeredName");
                editor.remove("registeredUsername");
                editor.remove("registeredEmail");
                editor.remove("registeredPassword");
                editor.commit();

                Intent intent = new Intent(UserProfile.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        toOnBoarding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putBoolean("firstTime", true);
                editor.commit();

                Intent intent = new Intent(UserProfile.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }

    private void showUserData() {

        //Get user information from SharedPreferences
        _USERNAME = mSharedPref.getString("registeredUsername","");
        _NAME = mSharedPref.getString("registeredName","");
        _EMAIL = mSharedPref.getString("registeredEmail","");
        _PASSWORD = mSharedPref.getString("registeredPassword","");

        usernameLabel.setText(_USERNAME);
        nameLabel.setText(_NAME);
        name.getEditText().setText(_NAME);
        email.getEditText().setText(_EMAIL);
        password.getEditText().setText(_PASSWORD);

    }

    public void update(View v) {

        //Call each method to check if user inputs are different from existing values and if not, set them to new values
        if (isNameChanged() || isPasswordChanged() || isEmailChanged()) {
            Toast.makeText(this, "Updated Successfully", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(this, "Please enter a different value", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isEmailChanged() {

        if (!_EMAIL.equals(email.getEditText().getText().toString())) {
            String newEmail = email.getEditText().getText().toString();
            reference.child(_USERNAME).child("email").setValue(newEmail);
            _EMAIL = newEmail;
            return true;

        } else {
            return false;
        }
    }

    private boolean isPasswordChanged() {

        if (!_PASSWORD.equals(password.getEditText().getText().toString())) {
            String newPassword = password.getEditText().getText().toString();
            reference.child(_USERNAME).child("password").setValue(newPassword);
            _PASSWORD = newPassword;
            return true;

        } else {
            return false;
        }
    }

    private boolean isNameChanged() {

        if (!_NAME.equals(name.getEditText().getText().toString())) {
            String newName = name.getEditText().getText().toString();
            reference.child(_USERNAME).child("name").setValue(newName);
            _NAME = newName;
            return true;

        } else {
            return false;
        }
    }

}