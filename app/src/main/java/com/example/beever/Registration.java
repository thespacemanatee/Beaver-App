package com.example.beever;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Registration extends AppCompatActivity {

    //Create variables for each element
    private TextInputLayout regName, regUsername, regEmail, regPassword;
    private SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //Hooks
        Button callLogin = findViewById(R.id.sign_in);
        regName = findViewById(R.id.reg_name);
        regUsername = findViewById(R.id.reg_username);
        regEmail = findViewById(R.id.reg_email);
        regPassword = findViewById(R.id.reg_password);
        Button regButton = findViewById(R.id.register);

        //Validate if user entered information is formatted properly by calling registerUser()
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser(v);
            }
        });

        //Return to sign in page
        callLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Registration.super.onBackPressed();
            }
        });
    }

    private Boolean validateName() {
        String s = regName.getEditText().getText().toString();
        if (s.isEmpty()) {
            regName.setError("Field cannot be empty");
            return false;
        } else {
            regName.setError(null);
            regName.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateUserName() {
        String s = regUsername.getEditText().getText().toString();
        String noWhiteSpace = ".*\\s+.*";

        if (s.isEmpty()) {
            regUsername.setError("Field cannot be empty");
            return false;

        } else if (s.matches(noWhiteSpace)) {
            regUsername.setError("Spaces are not allowed in username");
            return false;

        } else if (s.length() >= 12) {
            regUsername.setError("Username cannot be more than 12 characters");
            return false;

        } else {
            regUsername.setError(null);
            regUsername.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateEmail() {
        String s = regEmail.getEditText().getText().toString();
        String validEmail = "[a-zA-z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (s.isEmpty()) {
            regEmail.setError("Field cannot be empty");
            return false;
        } else if (!s.matches(validEmail)) {
            regEmail.setError("Invalid email address");
            return false;

        } else {
            regEmail.setError(null);
            regEmail.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword() {
        String s = regPassword.getEditText().getText().toString();
        String validPassword = "^" +
//                "(?=.*[0-9])" +          //at least 1 digit
//                "(?=.*[a-z])" +          //at least 1 lower case letter
//                "(?=.*[A-Z])" +          //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +         //any letter
                "(?=.*[@#$%^&+=])" +       //at least 1 special character
                ".{4,}" +                  //at least 4 characters
                "$";
        if (s.isEmpty()) {
            regPassword.setError("Field cannot be empty");
            return false;

        } else if (s.contains(" ")) {
            regPassword.setError("Spaces not allowed in password");
            return false;

        } else if (!s.matches(validPassword)) {
            regPassword.setError("Password is too weak");
            return false;

        } else {
            regPassword.setError(null);
            regPassword.setErrorEnabled(false);
            return true;
        }
    }

    //Save user data to Firebase on register click
    public void registerUser(View view) {

        //Check if each field is formatting correctly
        if (!validateName() || !validateUserName() || !validateEmail() || !validatePassword()) {
            return;
        }

        FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
        DatabaseReference reference = rootNode.getReference("Users");

        //Get values from user inputs
        String name = regName.getEditText().getText().toString();
        String userName = regUsername.getEditText().getText().toString();
        String email = regEmail.getEditText().getText().toString();
        String password = regPassword.getEditText().getText().toString();

        //Pass user inputs into helper class constructor and set database to those values
        UserHelperClass userHelperClass = new UserHelperClass(name,userName,email,password);

        reference.child(userName).setValue(userHelperClass);

        Query validateUser = reference.orderByChild("username").equalTo(userName);

        validateUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    //Get password from entered username from database
                    String passwordFromDB = dataSnapshot.child(userName).child("password").getValue(String.class);

                    //Check if password is valid
                    if (passwordFromDB.equals(password)) {

                        //Retrieve relevant data from database and pass them into new intent as Extras, and start new activity
                        String nameFromDB = dataSnapshot.child(userName).child("name").getValue(String.class);
                        String usernameFromDB = dataSnapshot.child(userName).child("username").getValue(String.class);
                        String emailFromDB = dataSnapshot.child(userName).child("email").getValue(String.class);

                        Intent intent = new Intent(getApplicationContext(),UserProfile.class);
                        intent.putExtra("name",nameFromDB);
                        intent.putExtra("username",usernameFromDB);
                        intent.putExtra("email",emailFromDB);
                        intent.putExtra("password",passwordFromDB);


                        mSharedPref = getSharedPreferences("SharedPref",MODE_PRIVATE);
                        SharedPreferences.Editor editor = mSharedPref.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("registeredName", nameFromDB);
                        editor.putString("registeredUsername", usernameFromDB);
                        editor.putString("registeredEmail", emailFromDB);
                        editor.putString("registeredPassword", passwordFromDB);
                        editor.commit();

                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

    }
}