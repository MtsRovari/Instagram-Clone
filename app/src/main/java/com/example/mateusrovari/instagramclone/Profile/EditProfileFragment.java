package com.example.mateusrovari.instagramclone.Profile;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mateusrovari.instagramclone.R;
import com.example.mateusrovari.instagramclone.Share.ShareActivity;
import com.example.mateusrovari.instagramclone.Utils.FirebaseMethods;
import com.example.mateusrovari.instagramclone.Utils.UniversalImageLoader;
import com.example.mateusrovari.instagramclone.dialogs.ConfirmPasswordDialog;
import com.example.mateusrovari.instagramclone.models.User;
import com.example.mateusrovari.instagramclone.models.UserAccountSettings;
import com.example.mateusrovari.instagramclone.models.UserSettings;
//import com.google.android.gms.flags.IFlagProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.OnConfirmPasswordListner{


    private ImageView backArrow;

    //    firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private String userId;

    private static final String TAG = "EditProfileFragment";

//    EditProfileFragment widgets
    private EditText mDisplayName, mUsername, mWebsite, mDescription, mEmail, mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;
    
    //varr
    private UserSettings mUserSettings;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);
        mProfilePhoto = view.findViewById(R.id.profile_photo);
        mDisplayName = view.findViewById(R.id.editProfile_display_name);
        mUsername = view.findViewById(R.id.editProfile_username);
        mWebsite = view.findViewById(R.id.editProfile_website);
        mDescription = view.findViewById(R.id.editProfile_description);
        mEmail = view.findViewById(R.id.editProfile_email);
        mPhoneNumber = view.findViewById(R.id.editProfile_phoneNumber);
        mChangeProfilePhoto = view.findViewById(R.id.changeProfilePhoto);

        mFirebaseMethods = new FirebaseMethods(getActivity());

//        setProfileImage();
        setupFirebaseAuth();

//      back arrow for navigating back to "ProfileActivity"
        backArrow = view.findViewById(R.id.backToProfile);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        ImageView checkmark = view.findViewById(R.id.saveChanges);
        checkmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attemping to save changes");
                saveProfileSettings();
            }
        });
        return view;
    }

    /**
     * Retrieves the data contained in the widgets and submits it to the database
     * Before doing so it checks to make sure the username chosen is unique
     */
    private void saveProfileSettings() {
        final String displayName = mDisplayName.getText().toString();
        final String username = mUsername.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        final long phoneNumber = Long.parseLong(mPhoneNumber.getText().toString());


        //case1: if the user node a change to their username
        if (!mUserSettings.getUser().getUsername().equals(username)) {
            checkIfUsernameExists(username);
        }
        //case2: if the user node a change their email
        if (!mUserSettings.getUser().getEmail().equals(email)) {

            //Reauthenticate
            //      confirm the password and email
            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
            dialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));
            dialog.setTargetFragment(EditProfileFragment.this, 1);
            //check if the email already is registered
            //      fetchProvidersForEmail(String email)
            //change the email
            //      submit the new email to the database and authentication
        }

        /**
         * check the rest of the settings that do not require uniqueness
         */
        if (!mUserSettings.getSettings().getDisplay_name().equals(displayName)) {
            mFirebaseMethods.updateUserAccountSettings(displayName, null, null, 0);
        }
        if (!mUserSettings.getSettings().getWebsite().equals(website)) {
            mFirebaseMethods.updateUserAccountSettings(null, website, null, 0);
        }
        if (!mUserSettings.getSettings().getDescription().equals(description)) {
            mFirebaseMethods.updateUserAccountSettings(null, null, description, 0);
        }
        if (mUserSettings.getSettings().getPhone_number() != phoneNumber) {
            mFirebaseMethods.updateUserAccountSettings(null, null, null, phoneNumber);
        }

    }

    /**
     * check is @param username already exists in the database
     * @param username
     */
    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: checking if " + username + " already exists");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    //add the username
                    mFirebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(), "Saved username..", Toast.LENGTH_SHORT).show();
                }
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    if (singleSnapshot.exists()) {
                        Log.d(TAG, "onDataChange: Found a match " + singleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(getActivity(), "That username already exists!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings) {

        mUserSettings = userSettings;

        //User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();
        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(userSettings.getUser().getEmail());
        mPhoneNumber.setText(String.valueOf(userSettings.getUser().getPhone_number()));

        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: changing profile photo");
                Intent i = new Intent(getActivity(), ShareActivity.class);
                i.addFlags(i.FLAG_ACTIVITY_NEW_TASK); //1964342
                getActivity().startActivity(i);
                getActivity().finish();
            }
        });
    }

    /**
     ---------------------------------- Firebase -------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebase: starting");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userId = mAuth.getCurrentUser().getUid();

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

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //retrieve user information from the database
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));

                //retrieve images for the user in question

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListner);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListner != null) {
            mAuth.removeAuthStateListener(mAuthListner);
        }
    }

    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: got the password: " + password);

        AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(), password);

        ///////////////////// Prompt the user to re-provide their sign-in credentials
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: re-authentication success");

                            //////////////////////check to see if the email is not already present in the database
                            mAuth.fetchProvidersForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                    if (task.isSuccessful()) {
                                        try {

                                            if (task.getResult().getProviders().size() == 1) {
                                                Log.d(TAG, "onComplete: that email is already in use");
                                                Toast.makeText(getActivity(), "That email is already in use", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Log.d(TAG, "onComplete: that email is avaliable");

                                                mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "onComplete: User email adress updated");
                                                                    Toast.makeText(getActivity(), "User email adress updated", Toast.LENGTH_SHORT).show();
                                                                    mFirebaseMethods.updateEmail(mEmail.getText().toString());
                                                                }
                                                            }
                                                        });
                                            }

                                        } catch (NullPointerException e) {
                                            Log.d(TAG, "onComplete: NullPointerException " + e.getMessage());
                                        }
                                    }
                                }
                            });



                        } else {
                            Log.d(TAG, "onComplete: re-authentication failed");
                            Toast.makeText(getActivity(), "Re-authentication failed/Wrong Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
