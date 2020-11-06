package com.example.beever.navigation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.beever.Login;
import com.example.beever.MainActivity;
import com.example.beever.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyProfileFragment extends Fragment {

    //Create variables for each element
    private TextInputLayout name, email, password;
    private TextView usernameLabel, nameLabel;
    private SharedPreferences mSharedPref;

    //Global variables to hold user data inside this activity
    private String _USERNAME,_NAME,_EMAIL,_PASSWORD;
    private DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.my_profile_fragment, container, false);

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("Profile");

        //Create new reference to Firebase database
        reference = FirebaseDatabase.getInstance().getReference("Users");
        mSharedPref = this.getActivity().getSharedPreferences("SharedPref", Context.MODE_PRIVATE);

        //Hooks
        usernameLabel = root.findViewById(R.id.username_label);
        nameLabel = root.findViewById(R.id.name_label);
        name = root.findViewById(R.id.name_field);
        email = root.findViewById(R.id.email_field);
        password = root.findViewById(R.id.password_field);
        MaterialButton update = root.findViewById(R.id.update_button);
        FloatingActionButton toOnBoarding = root.findViewById(R.id.to_onboarding);

        showUserData();

        toOnBoarding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putBoolean("firstTime", true);
                editor.apply();

                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Call each method to check if user inputs are different from existing values and if not, set them to new values
                if (isNameChanged() || isPasswordChanged() || isEmailChanged()) {
                    Toast.makeText(getActivity(), "Updated Successfully", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getActivity(), "Please enter a different value", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return root;
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

    private boolean isEmailChanged() {

        if (!_EMAIL.equals(email.getEditText().getText().toString())) {
            String newEmail = email.getEditText().getText().toString();
            String validEmail = "[a-zA-z0-9._-]+@[a-z]+\\.+[a-z]+";
            if (newEmail.isEmpty()) {
                email.setError("Field cannot be empty");
                return false;
            } else if (!newEmail.matches(validEmail)) {
                email.setError("Invalid email address");
                return false;

            } else {
                email.setError(null);
                email.setErrorEnabled(false);
                reference.child(_USERNAME).child("email").setValue(newEmail);
                _EMAIL = newEmail;
                return true;
            }

        } else {
            return false;
        }
    }

    private boolean isPasswordChanged() {

        if (!_PASSWORD.equals(password.getEditText().getText().toString())) {
            String newPassword = password.getEditText().getText().toString();
            String validPassword = "^" +
//                "(?=.*[0-9])" +          //at least 1 digit
//                "(?=.*[a-z])" +          //at least 1 lower case letter
//                "(?=.*[A-Z])" +          //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +         //any letter
                    "(?=.*[@#$%^&+=])" +       //at least 1 special character
                    ".{4,}" +                  //at least 4 characters
                    "$";
            if (newPassword.isEmpty()) {
                password.setError("Field cannot be empty");
                return false;

            } else if (newPassword.contains(" ")) {
                password.setError("Spaces not allowed in password");
                return false;

            } else if (!newPassword.matches(validPassword)) {
                password.setError("Password is too weak");
                return false;

            } else {
                password.setError(null);
                password.setErrorEnabled(false);
                reference.child(_USERNAME).child("password").setValue(newPassword);
                _PASSWORD = newPassword;
                return true;
            }

        } else {
            return false;
        }
    }

    private boolean isNameChanged() {

        if (!_NAME.equals(name.getEditText().getText().toString())) {
            String newName = name.getEditText().getText().toString();
            if (newName.isEmpty()) {
                name.setError("Field cannot be empty");
                return false;
            } else {
                name.setError(null);
                name.setErrorEnabled(false);
                reference.child(_USERNAME).child("name").setValue(newName);
                _NAME = newName;
                return true;
            }

        } else {
            return false;
        }
    }
}
