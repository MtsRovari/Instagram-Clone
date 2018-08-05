package com.example.mateusrovari.instagramclone.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.mateusrovari.instagramclone.R;
import com.example.mateusrovari.instagramclone.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

public class FirebaseMethods {

    private Context mContext;


    private static final String TAG = "FirebaseMethods";

    //    firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private String userId;

    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mContext = context;

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }
    }

    public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot) {
        Log.d(TAG, "checkIfUsernameExists: checking if " + username + " already exists");

        User user = new User();

        for (DataSnapshot ds: dataSnapshot.getChildren()) {
            Log.d(TAG, "checkIfUsernameExists: datasnapshot " + ds);

            user.setUsername(ds.getValue(User.class).getUsername());
            Log.d(TAG, "checkIfUsernameExists: username " + user.getUsername());

            if (StringManipulation.expandUsername(user.getUsername()).equals(username)) {
                Log.d(TAG, "checkIfUsernameExists: found a match " + user.getUsername());
                return true;
            }
        }
        return false;
    }

    /**
     * Register a new email and password to firebase authentication
     * @param email
     * @param password
     * @param username
     */
    public void registerNewEmail(final String email, String password, final String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "onComplete: " + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Toast.makeText(mContext, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                        }
                        else if (task.isSuccessful()) {
                            userId = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "onComplete: Authstate changed" + userId);
                        }
                    }
                });
    }
}
