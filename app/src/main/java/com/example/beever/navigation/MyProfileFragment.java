package com.example.beever.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.beever.R;
import com.example.beever.admin.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyProfileFragment extends Fragment {

    //Create variables for each element
    private TextInputLayout name, email, password;
    private TextView usernameLabel, nameLabel;
    private CircularProgressButton update;
    private CircleImageView profilePic;
    private SharedPreferences mSharedPref;

    //Global variables to hold user data inside this activity
    private static String _USERNAME,_NAME,_EMAIL,_PASSWORD;
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private final String TAG = "Logcat";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.my_profile_fragment, container, false);

        ((NavigationDrawer)getActivity()).getSupportActionBar().setTitle("Profile");

        mSharedPref = getActivity().getSharedPreferences("SharedPref", Context.MODE_PRIVATE);

        //Get user information from SharedPreferences
        _USERNAME = mSharedPref.getString("registeredUsername","");
        _NAME = mSharedPref.getString("registeredName","");
        _EMAIL = mSharedPref.getString("registeredEmail","");

        //Hooks
        usernameLabel = root.findViewById(R.id.username_label);
        nameLabel = root.findViewById(R.id.name_label);
        name = root.findViewById(R.id.name_field);
        email = root.findViewById(R.id.email_field);
        password = root.findViewById(R.id.password_field);
        update = root.findViewById(R.id.update_button);
        profilePic = root.findViewById(R.id.profile_image);
        FloatingActionButton toOnBoarding = root.findViewById(R.id.to_onboarding);

        FirebaseUser fUser = fAuth.getCurrentUser();
        Glide.with(getActivity()).load(fUser.getPhotoUrl()).into(profilePic);

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

                update.startAnimation();

                //Call each method to check if user inputs are different from existing values and if not, set them to new values
                if (isNameChanged() || isPasswordChanged() || isEmailChanged()) {

                    update.revertAnimation();
                    Toast.makeText(getActivity(), "Updated Successfully", Toast.LENGTH_SHORT).show();

                } else {

                    update.revertAnimation();
                    Toast.makeText(getActivity(), "Please enter a different value", Toast.LENGTH_SHORT).show();
                }
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1000);
            }
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri = data.getData();

                update.startAnimation();
                uploadImageFirebase(imageUri);
            }
        }
    }

    private void uploadImageFirebase(Uri imageUri) {

        FirebaseUser fUser = fAuth.getCurrentUser();
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(imageUri)
                .build();
        fUser.updateProfile(request)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Glide.with(getActivity()).load(imageUri).into(profilePic);
                            Glide.with(getActivity()).load(imageUri).into((CircleImageView) getActivity().findViewById(R.id.profile_nav));
                            NavigationDrawer.profile_uri = imageUri;
                            update.revertAnimation();
                            Log.d(TAG, "User profile updated.");
                            Toast.makeText(getActivity(), "Updated profile image", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Failed to update profile image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void showUserData() {

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
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putString("registeredEmail", newEmail);
                editor.apply();
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
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putString("registeredPassword", newPassword);
                editor.apply();
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
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putString("registeredName", newName);
                editor.apply();
                return true;
            }

        } else {
            return false;
        }
    }
}
