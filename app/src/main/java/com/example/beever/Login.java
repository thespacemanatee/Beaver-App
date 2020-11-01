package com.example.beever;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
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

    Button callRegistration, loginButton;
    ImageView image;
    TextView logoText, signUpText;
    TextInputLayout username, password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Hooks
        callRegistration = findViewById(R.id.sign_up);
        image = findViewById(R.id.logo_image);
        logoText = findViewById(R.id.logo_name);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.log_in);
        signUpText = findViewById(R.id.sign_up_text);

        callRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        //validate login credentials
        if (!validateUserName() || !validatePassword()) {
            return;
        } else {
            isUser();
        }

    }

    private void isUser() {

        String usernameProvided = username.getEditText().getText().toString().trim();
        String passwordProvided = password.getEditText().getText().toString().trim();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        Query validateUser = ref.orderByChild("username").equalTo(usernameProvided);

        validateUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    username.setError(null);
                    username.setErrorEnabled(false);

                    String passwordFromDB = dataSnapshot.child(usernameProvided).child("password").getValue(String.class);

                    if (passwordFromDB.equals(passwordProvided)) {

                        password.setError(null);
                        password.setErrorEnabled(false);

                        String nameFromDB = dataSnapshot.child(usernameProvided).child("name").getValue(String.class);
                        String usernameFromDB = dataSnapshot.child(usernameProvided).child("username").getValue(String.class);
                        String emailFromDB = dataSnapshot.child(usernameProvided).child("email").getValue(String.class);

                        Intent intent = new Intent(getApplicationContext(),UserProfile.class);
                        intent.putExtra("name",nameFromDB);
                        intent.putExtra("username",usernameFromDB);
                        intent.putExtra("email",emailFromDB);
                        intent.putExtra("password",passwordFromDB);

                        startActivity(intent);
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