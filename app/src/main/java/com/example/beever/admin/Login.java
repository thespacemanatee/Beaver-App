package com.example.beever.admin;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.beever.R;
import com.example.beever.navigation.NavigationDrawer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class Login extends AppCompatActivity {

    //Create variables for each element
    private Button callRegistration;
    private ImageView image;
    private TextView logoText, signUpText;
    private TextInputLayout username, password;
    private CircularProgressButton loginButton;
    private SharedPreferences mSharedPref;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private final String TAG = "Logcat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Set firstTime to false in SharedPreferences
        mSharedPref = getSharedPreferences("SharedPref",MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean("firstTime", false);
        editor.apply();

        //Hooks
        callRegistration = findViewById(R.id.sign_up);
        image = findViewById(R.id.logo_image);
        logoText = findViewById(R.id.logo_name);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.log_in);
        signUpText = findViewById(R.id.sign_up_text);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        //Create OnClickListener for sign up button
        callRegistration.setOnClickListener(v -> {

            //Starts new registration activity when clicked on with smooth animations
            Intent intent = new Intent(Login.this,Registration.class);

            @SuppressWarnings("rawtypes")
            Pair[] pairs = new Pair[7];
            pairs[0] = new Pair<View, String>(image,"logo_image");
            pairs[1] = new Pair<View, String>(logoText,"logo_text");
            pairs[2] = new Pair<View, String>(username,"user_tran");
            pairs[3] = new Pair<View, String>(password,"password_tran");
            pairs[4] = new Pair<View, String>(loginButton,"button_tran");
            pairs[5] = new Pair<View, String>(signUpText,"sign_up_text_tran");
            pairs[6] = new Pair<View, String>(callRegistration,"sign_up_tran");

            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Login.this, pairs);
            startActivity(intent,options.toBundle());

        });

        //Create OnClickListener for login button
        loginButton.setOnClickListener(v -> {

            //Call loginUser() method on click login button
            loginButton.startAnimation();
            loginUser(v);
        });
    }

    private Boolean validateUserName() {
        String s = username.getEditText().getText().toString();

        if (s.isEmpty()) {
            username.setError("Please enter your username!");
            loginButton.revertAnimation();
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
            password.setError("Please enter your password!");
            loginButton.revertAnimation();
            return false;

        } else {
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }

    public void loginUser(View v) {

        //Validate if anything is entered in login fields
        if (validateUserName() && validatePassword()) {
            //Call isUser() to authenticate credentials
            isUser();
        }
    }

    private void isUser() {

        //Get user entered credentials and parse them into strings that can be fed into auth functions
        String usernameProvided = username.getEditText().getText().toString().trim();
        String passwordProvided = password.getEditText().getText().toString().trim();

        fAuth.signInWithEmailAndPassword(usernameProvided,passwordProvided).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();

                userID = fAuth.getCurrentUser().getUid();

                DocumentReference documentReference = fStore.collection("users").document(userID);
                documentReference.get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        DocumentSnapshot document = task1.getResult();
                        if (document.exists()) {
                            SharedPreferences.Editor editor = mSharedPref.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.putString("registeredName", document.getString("name"));
                            editor.putString("registeredUsername", document.getString("username"));
                            editor.putString("registeredEmail", document.getString("email"));
                            editor.apply();
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task1.getException());
                    }
                });

                Intent intent = new Intent(getApplicationContext(), NavigationDrawer.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
                finish();

            } else {
                Toast.makeText(Login.this, "Error! " + task.getException(), Toast.LENGTH_SHORT).show();
                loginButton.revertAnimation();
            }
        });

    }
}