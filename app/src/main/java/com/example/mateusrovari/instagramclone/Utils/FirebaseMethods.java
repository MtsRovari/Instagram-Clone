package com.example.mateusrovari.instagramclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.mateusrovari.instagramclone.Home.HomeActivity;
import com.example.mateusrovari.instagramclone.Profile.AccountSettingsActivity;
import com.example.mateusrovari.instagramclone.R;
import com.example.mateusrovari.instagramclone.models.Photo;
import com.example.mateusrovari.instagramclone.models.User;
import com.example.mateusrovari.instagramclone.models.UserAccountSettings;
import com.example.mateusrovari.instagramclone.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    //    firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;
    private String userId;

    //vars
    private Context mContext;
    private double mPhotoUploadProgress = 0;

    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mContext = context;

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }
    }

    public void uploadNewPhoto(String photoType, final String caption, int count, String imgUrl,
                               Bitmap bitmap) {
        Log.d(TAG, "uploadNewPhoto: attempting to upload new photo");

        FilePaths filePaths = new FilePaths();
        
        //case1) new photo
        if (photoType.equals(mContext.getString(R.string.new_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading NEW photo");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (count + 1));

            //convert image url to bitmap
            if (bitmap == null) {
                bitmap = ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bitmap, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri firebaseUrl = taskSnapshot.getDownloadUrl();
                    Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show();

                    //add the new photo to 'photos' node and 'user_photos' node
                    addPhotoToDatabase(caption, firebaseUrl.toString());


                    //navigate to the main feed so the user can see their photo
                    Intent i = new Intent(mContext, HomeActivity.class);
                    mContext.startActivity(i);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: photo upload failed");
                    Toast.makeText(mContext, "Photo upload failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progress - 15 > mPhotoUploadProgress) {
                        Toast.makeText(mContext, "photo upload progress: " +  progress, Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });
        }
        //case1) new profile photo
        else if (photoType.equals(mContext.getString(R.string.profile_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading NEW profile photo");


            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

            //convert image url to bitmap
            if (bitmap == null) {
                bitmap = ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bitmap, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri firebaseUrl = taskSnapshot.getDownloadUrl();
                    Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show();

                    //insert into 'user_Account_settings' node
                    setProfilePhoto(firebaseUrl.toString());

                    ((AccountSettingsActivity)mContext).setViewPager(
                            ((AccountSettingsActivity)mContext).mPagerAdapter
                                    .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment))
                    );

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: photo upload failed");
                    Toast.makeText(mContext, "Photo upload failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progress - 15 > mPhotoUploadProgress) {
                        Toast.makeText(mContext, "photo upload progress: " +  progress, Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });
        }
    }

    private void setProfilePhoto(String url) {
        Log.d(TAG, "setProfilePhoto: setting a new profile image " + url);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);
    }

    private String getTimeStamp() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        return simpleDateFormat.format(new Date());
    }

    private void addPhotoToDatabase(String caption, String url) {
        Log.d(TAG, "addPhotoToDatabase: adding photo to database");

        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();

        String tags = StringManipulation.getTags(caption);

        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setData_created(getTimeStamp());
        photo.setImage_path(url);
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);

        //insert into database
        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser()
                        .getUid()).child(newPhotoKey).setValue(photo);
        myRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);
    }

    public int getImageCount(DataSnapshot dataSnapshot) {
        int count = 0;
        for (DataSnapshot ds: dataSnapshot
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()) {
            count++;
        }
        return count;
    }

    /**
     * update user_Account_settings node for the current user
     * @param displayName
     * @param website
     * @param description
     * @param phoneNumber
     */
    public void updateUserAccountSettings(String displayName, String website, String description, long phoneNumber) {
        Log.d(TAG, "updateUserAccountSettings: updating user account settings");

        if (displayName != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userId)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName);
        }

        if (website != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userId)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website);
        }

        if (description != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userId)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }

        if (phoneNumber != 0) {
            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(userId)
                    .child(mContext.getString(R.string.field_phone_number))
                    .setValue(phoneNumber);

            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userId)
                    .child(mContext.getString(R.string.field_phone_number))
                    .setValue(phoneNumber);
        }
    }

    /**
     * update username in the user's node and users_account_settings
     * @param username
     */
    public void updateUsername(String username) {
        Log.d(TAG, "updateUsername: updating username to" + username);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userId)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userId)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

    /**
     * update the email in the user's node
     * @param email
     */
    public void updateEmail(String email) {
        Log.d(TAG, "updateEmail: updating email to" + email);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userId)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);
    }

//    public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot) {
//        Log.d(TAG, "checkIfUsernameExists: checking if " + username + " already exists");
//
//        User user = new User();
//
//        for (DataSnapshot ds: dataSnapshot.child(userId).getChildren()) {
//            Log.d(TAG, "checkIfUsernameExists: datasnapshot " + ds);
//
//            user.setUsername(ds.getValue(User.class).getUsername());
//            Log.d(TAG, "checkIfUsernameExists: username " + user.getUsername());
//
//            if (StringManipulation.expandUsername(user.getUsername()).equals(username)) {
//                Log.d(TAG, "checkIfUsernameExists: found a match " + user.getUsername());
//                return true;
//            }
//        }
//        return false;
//    }

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
                            //send verification email
                            sendVerificationEmail();

                            userId = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "onComplete: Authstate changed" + userId);
                        }
                    }
                });
    }

    public  void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            } else {
                                Toast.makeText(mContext, "couldn't send verification email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
     * add information to the users nodes
     * add information to the user_account_setings
     * @param email
     * @param username
     * @param description
     * @param website
     * @param profile_photo
     */
    public void addNewUser(String email, String username, String description, String website, String profile_photo) {

        User user = new User(userId, 1, email, StringManipulation.condenseUsername(username));

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userId)
                .setValue(user);

        UserAccountSettings settings = new UserAccountSettings(
                description,
                username,
                0,
                0,
                0,
                0,
                profile_photo,
                StringManipulation.condenseUsername(username),
                website
        );

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userId)
                .setValue(settings);
    }

    /**
     * retrieve the account settings for the user currently logged in
     * database user_account_settings node
     * @param dataSnapshot
     * @return
     */
    public UserSettings getUserSettings(DataSnapshot dataSnapshot) {
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from firebase");

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();

        for (DataSnapshot ds: dataSnapshot.getChildren()) {

            //user account settings node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))) {
                Log.d(TAG, "getUserAccountSettings: datasnapshot " + ds);

                try {

                    settings.setDisplay_name(
                            ds.child(userId)
                                    .getValue(UserAccountSettings.class)
                                    .getDisplay_name()
                    );
                    settings.setUsername(
                            ds.child(userId)
                                    .getValue(UserAccountSettings.class)
                                    .getUsername()
                    );
                    settings.setWebsite(
                            ds.child(userId)
                                    .getValue(UserAccountSettings.class)
                                    .getWebsite()
                    );
                    settings.setDescription(
                            ds.child(userId)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription()
                    );
                    settings.setProfile_photo(
                            ds.child(userId)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_photo()
                    );
                    settings.setPhone_number(
                            ds.child(userId)
                                    .getValue(UserAccountSettings.class)
                                    .getPhone_number()
                    );
                    settings.setPosts(
                            ds.child(userId)
                                    .getValue(UserAccountSettings.class)
                                    .getPosts()
                    );
                    settings.setFollowers(
                            ds.child(userId)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowers()
                    );
                    settings.setFollowing(
                            ds.child(userId)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowing()
                    );

                    Log.d(TAG, "getUserAccountSettings: retrieve user_Account_Settings information " + settings.toString());

                } catch (NullPointerException e) {
                    Log.d(TAG, "getUserAccountSettings: NullPointerException " + e.getMessage());
                }
            }

            //users node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_users))) {
                Log.d(TAG, "getUserAccountSettings: datasnapshot " + ds);

                user.setUsername(
                        ds.child(userId)
                                .getValue(User.class)
                                .getUsername()
                );
                user.setEmail(
                        ds.child(userId)
                        .getValue(User.class)
                        .getEmail()
                );
                user.setPhone_number(
                        ds.child(userId)
                        .getValue(User.class)
                        .getPhone_number()
                );
                user.setUser_id(
                        ds.child(userId)
                        .getValue(User.class)
                        .getUser_id()
                );

                Log.d(TAG, "getUserAccountSettings: retrieve user information " + user.toString());

            }
        }

        return new UserSettings(user, settings);
    }
}
