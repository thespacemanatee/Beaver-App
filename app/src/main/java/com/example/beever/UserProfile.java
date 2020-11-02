package com.example.beever;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserProfile extends AppCompatActivity {

    //Create variables for each element
    private TextInputLayout name, email, password;
    private TextView usernameLabel, nameLabel;

    //Global variables to hold user data inside this activity
    private String _USERNAME,_NAME,_EMAIL,_PASSWORD;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //Create new reference to Firebase database
        reference = FirebaseDatabase.getInstance().getReference("Users");

        //Hooks
        usernameLabel = findViewById(R.id.username_label);
        nameLabel = findViewById(R.id.name_label);
        name = findViewById(R.id.name_field);
        email = findViewById(R.id.email_field);
        password = findViewById(R.id.password_field);

        showUserData();
    }

    private void showUserData() {

        //Get current intent and get respective Extras that were passed in from previous activity
        Intent intent = getIntent();
        _USERNAME = intent.getStringExtra("username");
        _NAME = intent.getStringExtra("name");
        _EMAIL = intent.getStringExtra("email");
        _PASSWORD = intent.getStringExtra("password");

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