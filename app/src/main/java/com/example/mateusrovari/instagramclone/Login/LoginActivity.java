package com.example.mateusrovari.instagramclone.Login;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.example.mateusrovari.instagramclone.Home.HomeActivity;
import com.example.mateusrovari.instagramclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {


    private Context mContext;
    private ProgressBar mProgressBar;
    private EditText mEmail, mPassword;
    private TextView mPleaseWait;

    private static final String TAG = "LoginActivity";

    //    firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: started");

        initialize();
        setupFirebase();
    }

    private void initialize(){
        mProgressBar = findViewById(R.id.loginProgressBar);
        mProgressBar.setVisibility(View.GONE);
        mPleaseWait = findViewById(R.id.pleaseWait);
        mPleaseWait.setVisibility(View.GONE);
        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mContext = LoginActivity.this;
        
        Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to log in");

                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if (isStringNull(email) && isStringNull(password)){
                    Toast.makeText(mContext, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mPleaseWait.setVisibility(View.VISIBLE);
                    
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Log.d(TAG, "onComplete: onComplete" + task.isSuccessful());
                                    if (!task.isSuccessful()) {
                                        Log.w(TAG, "onComplete: failed", task.getException());
                                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                                        mProgressBar.setVisibility(View.GONE);
                                        mPleaseWait.setVisibility(View.GONE);
                                    }
                                    else {
                                        Log.d(TAG, "onComplete: successful login");
                                        Toast.makeText(LoginActivity.this, getString(R.string.auth_success), Toast.LENGTH_SHORT).show();
                                        mProgressBar.setVisibility(View.GONE);
                                        mPleaseWait.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }
        });

        TextView linkSignUp = findViewById(R.id.link_signup);
        linkSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to register screen");
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });

        /**
         If the user is Logged in then navigate to HomeActivity
         */
        if (mAuth.getCurrentUser() != null) {
            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(i);
            finish();
        }

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
