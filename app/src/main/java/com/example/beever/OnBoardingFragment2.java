package com.example.beever;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class OnBoardingFragment2 extends Fragment {

    private SharedPreferences mSharedPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_onboarding2,container,false);

        mSharedPref = this.getActivity().getSharedPreferences("SharedPref", Context.MODE_PRIVATE);

        MaterialButton skip = root.findViewById(R.id.skip_button);

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isLoggedIn = mSharedPref.getBoolean("isLoggedIn", false);

                if (isLoggedIn) {

                    SharedPreferences.Editor editor = mSharedPref.edit();
                    editor.putBoolean("firstTime", false);
                    editor.commit();

                    Intent intent = new Intent(getActivity(), UserProfile.class);

                    intent.putExtra("name",mSharedPref.getString("registeredName", ""));
                    intent.putExtra("username",mSharedPref.getString("registeredUsername", ""));
                    intent.putExtra("email",mSharedPref.getString("registeredEmail", ""));
                    intent.putExtra("password",mSharedPref.getString("registeredPassword", ""));

                    startActivity(intent);

                } else {

                    Intent intent = new Intent(getActivity(),Login.class);
                    startActivity(intent);
                }
                getActivity().finish();
            }
        });

        return root;
    }
}
