package com.example.beever.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.beever.R;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import de.hdodenhof.circleimageview.CircleImageView;

public class Registration extends AppCompatActivity {

    //Create variables for each element
    private TextInputLayout regName, regUsername, regEmail, regPassword;
    private CircularProgressButton regButton;
    private SharedPreferences mSharedPref;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;

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
        regButton = findViewById(R.id.register);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        mSharedPref = getSharedPreferences("SharedPref",MODE_PRIVATE);

        //Validate if user entered information is formatted properly by calling registerUser()
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regButton.startAnimation();
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
            regButton.revertAnimation();
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
            regButton.revertAnimation();
            regUsername.setError("Field cannot be empty");
            return false;

        } else if (s.matches(noWhiteSpace)) {
            regButton.revertAnimation();
            regUsername.setError("Spaces are not allowed in username");
            return false;

        } else if (s.length() >= 12) {
            regButton.revertAnimation();
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
            regButton.revertAnimation();
            regEmail.setError("Field cannot be empty");
            return false;
        } else if (!s.matches(validEmail)) {
            regButton.revertAnimation();
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
            regButton.revertAnimation();
            regPassword.setError("Field cannot be empty");
            return false;

        } else if (s.contains(" ")) {
            regButton.revertAnimation();
            regPassword.setError("Spaces not allowed in password");
            return false;

        } else if (!s.matches(validPassword)) {
            regButton.revertAnimation();
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


        //Get values from user inputs
        String name = regName.getEditText().getText().toString();
        String userName = regUsername.getEditText().getText().toString();
        String email = regEmail.getEditText().getText().toString();
        String password = regPassword.getEditText().getText().toString();

        fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Registration.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    userID = fAuth.getCurrentUser().getUid();
                    SharedPreferences.Editor editor = mSharedPref.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("registeredName", name);
                    editor.putString("registeredUsername", userName);
                    editor.putString("registeredEmail", email);
                    editor.apply();

                    DocumentReference documentReference = fStore.collection("users").document(userID);
                    Map<String, Object> user = new HashMap<>();
                    user.put("name", name);
                    user.put("email", email);
                    user.put("username", userName);
                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Log", "onSuccess: User profile is created for " + userID);
                        }
                    });

                    FirebaseUser fUser = fAuth.getCurrentUser();
                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();
                    fUser.updateProfile(request);

                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getApplicationContext(), NavigationDrawer.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finishAffinity();
                        }
                    }, 2000);


                } else {
                    Toast.makeText(Registration.this, "Error! " + task.getException(), Toast.LENGTH_SHORT).show();
                    regButton.revertAnimation();
                }
            }
        });

    }
}