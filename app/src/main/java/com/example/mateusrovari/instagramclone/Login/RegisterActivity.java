package com.example.mateusrovari.instagramclone.Login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mateusrovari.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private Context mContext;
    private String email, username, password;
    private EditText mEmail, mUsername, mPassword;
    private TextView loagingPleaseWait;
    private Button btnRegister;
    private ProgressBar mProgressBar;

    private static final String TAG = "RegisterActivity";

    //    firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Log.d(TAG, "onCreate: started");

        initalize();
    }

    /**
     * Initialize the activity widgets
     */
    private void initalize() {
        Log.d(TAG, "initalize: Initializing widgets");
        mProgressBar = findViewById(R.id.registerProgressBar);
        mProgressBar.setVisibility(View.GONE);
        loagingPleaseWait = findViewById(R.id.loadingPleaseWait);
        loagingPleaseWait.setVisibility(View.GONE);
        mUsername = findViewById(R.id.register_input_username);
        mEmail = findViewById(R.id.register_input_email);
        mPassword = findViewById(R.id.register_input_password);
        mContext = RegisterActivity .this;
    }

    private boolean isStringNull(String string) {
        Log.d(TAG, "isStringNull: checking string is null");
        if (string.equals("")) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     ---------------------------------- Firebase -------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebase() {
        Log.d(TAG, "setupFirebase: starting");
        mAuth = FirebaseAuth.getInstance();
        mAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    //user is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_in" + user.getUid());
                } else {
                    //user is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListner);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListner != null) {
            mAuth.removeAuthStateListener(mAuthListner);
        }
    }
}