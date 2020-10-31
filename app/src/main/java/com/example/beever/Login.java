package com.example.beever;

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
    }
}