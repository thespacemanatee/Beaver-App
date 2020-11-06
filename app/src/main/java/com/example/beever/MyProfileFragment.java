package com.example.beever;

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

        //Create new reference to Firebase database
        reference = FirebaseDatabase.getInstance().getReference("Users");
        mSharedPref = this.getActivity().getSharedPreferences("SharedPref", Context.MODE_PRIVATE);

        //Hooks
        usernameLabel = root.findViewById(R.id.username_label);
        nameLabel = root.findViewById(R.id.name_label);
        name = root.findViewById(R.id.name_field);
        email = root.findViewById(R.id.email_field);
        password = root.findViewById(R.id.password_field);
        MaterialButton signOut = root.findViewById(R.id.signout_button);
        FloatingActionButton toOnBoarding = root.findViewById(R.id.to_onboarding);

        showUserData();

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.remove("registeredName");
                editor.remove("registeredUsername");
                editor.remove("registeredEmail");
                editor.remove("registeredPassword");
                editor.commit();

                Intent intent = new Intent(getActivity(), Login.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        toOnBoarding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putBoolean("firstTime", true);
                editor.commit();

                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();
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

    public void update(View v) {

        //Call each method to check if user inputs are different from existing values and if not, set them to new values
        if (isNameChanged() || isPasswordChanged() || isEmailChanged()) {
            Toast.makeText(getActivity(), "Updated Successfully", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(getActivity(), "Please enter a different value", Toast.LENGTH_LONG).show();
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
