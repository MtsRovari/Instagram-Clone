package com.example.mateusrovari.instagramclone.Utils;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mateusrovari.instagramclone.R;
import com.example.mateusrovari.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.mateusrovari.instagramclone.Utils.FirebaseMethods;
import com.example.mateusrovari.instagramclone.Utils.GridImageAdapter;
import com.example.mateusrovari.instagramclone.Utils.SquareImageView;
import com.example.mateusrovari.instagramclone.Utils.UniversalImageLoader;
import com.example.mateusrovari.instagramclone.models.Like;
import com.example.mateusrovari.instagramclone.models.Photo;
import com.example.mateusrovari.instagramclone.models.User;
import com.example.mateusrovari.instagramclone.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ViewPostFragment extends Fragment {

    private static final String TAG = "ViewPostFragment";

    public ViewPostFragment() {
        super();
        setArguments(new Bundle());
    }

    //vars
    private Photo mPhoto;
    private int mActivityNumber = 0;
    private String photoUsername;
    private String photoUrl;
    private UserAccountSettings mUserAccountSettings;
    private GestureDetector mGestureDetector;
    private Heart mHeart;
    private Boolean mLikedByCurrentUser;
    private StringBuilder mUsers;
    private String mLikesString = "";

    //    firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    //widgets
    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationView;
    private TextView mBlackLabel, mCaption, mUsername, mTimestamp, mLikes;
    private ImageView mBackArrow, mMore, mHeartRed, mHeartWhite, mProfileImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);

        mPostImage = view.findViewById(R.id.post_image);
        bottomNavigationView = view.findViewById(R.id.bottomNavViewBar);
        mBackArrow = view.findViewById(R.id.backArrow);
        mBlackLabel = view.findViewById(R.id.tvBackLabel);
        mCaption = view.findViewById(R.id.image_caption);
        mUsername = view.findViewById(R.id.username);
        mTimestamp = view.findViewById(R.id.image_time_post);
        mLikes = view.findViewById(R.id.image_likes);
        mMore = view.findViewById(R.id.more_posts);
        mHeartRed = view.findViewById(R.id.image_heat_red);
        mHeartWhite = view.findViewById(R.id.image_heat_outline);
        mProfileImage = view.findViewById(R.id.profile_photo);
        mHeart = new Heart(mHeartWhite, mHeartRed);
        mGestureDetector = new GestureDetector(getActivity(), new GestureListner());

        try {
            mPhoto = getPhotoFromBundle();
            UniversalImageLoader.setImage(mPhoto.getImage_path(), mPostImage, null, "");
            mActivityNumber = getActivityNumFromBundle();
            getPhotoDetails();
            getLikesString();
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException:" + e.getMessage());
        }

        setupFirebaseAuth();
        setupBottomNavigationView();

        return view;
    }

    private void getLikesString() {
        Log.d(TAG, "getLikesString: getting likes string");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUsers = new StringBuilder();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference
                            .child(getString(R.string.dbname_users))
                            .orderByChild(getString(R.string.field_user_id))
                            .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    Log.d(TAG, "onDataChange: found like: " +
                                        singleSnapshot.getValue(User.class).getUsername());

                                    mUsers.append(singleSnapshot.getValue(User.class).getUsername());
                                    mUsers.append(",");
                            }

                            String[] splitUsers = mUsers.toString().split(",");
                            if (mUsers.toString().contains(mUserAccountSettings.getUsername())) {
                                mLikedByCurrentUser = true;
                            } else {
                                mLikedByCurrentUser = false;
                            }

                            int length = splitUsers.length;
                            if (length == 1) {
                                mLikesString = "Liked by " + splitUsers[0];
                            }
                            else if (length == 2) {
                                mLikesString = "Liked by " + splitUsers[0] + " and " + splitUsers[1];
                            }
                            else if (length == 3) {
                                mLikesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] + " and " + splitUsers[2];
                            }
                            else if (length == 4) {
                                mLikesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] + ", " + splitUsers[2] + " and " + splitUsers[3];
                            }
                            else if (length > 4) {
                                mLikesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] + ", " + splitUsers[2] + " and " + (splitUsers.length - 3) + " others";
                            }
                            setupWidgets();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                if (!dataSnapshot.exists()) {
                    mLikesString = "";
                    mLikedByCurrentUser = false;
                    setupWidgets();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class GestureListner extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: double tap detected");
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(getString(R.string.dbname_photos))
                    .child(mPhoto.getPhoto_id())
                    .child(getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                        String keyId = singleSnapshot.getKey();
                        //case1:  The user already liked the photo
                        if (mLikedByCurrentUser && singleSnapshot.getValue(Like.class).getUser_id()
                                .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                            myRef.child(getString(R.string.dbname_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyId)
                                    .removeValue();

                            myRef.child(getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid() )
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyId)
                                    .removeValue();

                            mHeart.toggleLike();
                            getLikesString();
                        }
                        //case2: The user has not liked the photo
                        else if (!mLikedByCurrentUser){
                            //add new like
                            addNewLike();
                            break;
                        }
                    }
                    if (!dataSnapshot.exists()) {
                        //add new like
                        addNewLike();
                    }
                }

                private void addNewLike() {
                    Log.d(TAG, "addNewLike: adding new like");

                    String newLikeId = myRef.push().getKey();
                    Like like = new Like();
                    like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

                    myRef.child(getString(R.string.dbname_photos))
                            .child(mPhoto.getPhoto_id())
                            .child(getString(R.string.field_likes))
                            .child(newLikeId)
                            .setValue(like);

                    myRef.child(getString(R.string.dbname_user_photos))
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(mPhoto.getPhoto_id())
                            .child(getString(R.string.field_likes))
                            .child(newLikeId)
                            .setValue(like);

                    mHeart.toggleLike();
                    getLikesString();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return true;
        }
    }

    private void getPhotoDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mPhoto.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        mUserAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                }
                //setupWidgets();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: querry canceled");
            }
        });
    }

    private void setupWidgets() {
        String timestampDiff = getTimestampDifference();
        if (!timestampDiff.equals("0")) {
            mTimestamp.setText(timestampDiff + " DAYS AGO");
        } else {
            mTimestamp.setText("TODAY");
        }
        UniversalImageLoader.setImage(mUserAccountSettings.getProfile_photo(), mProfileImage, null, "");
        mUsername.setText(mUserAccountSettings.getUsername());
        mLikes.setText(mLikesString);

        if (mLikedByCurrentUser) {
            mHeartWhite.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);
            mHeartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: red heart touch detected");
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        } else {
            mHeartWhite.setVisibility(View.VISIBLE);
            mHeartRed.setVisibility(View.GONE);
            mHeartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: white heart touch detected");
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        }

    }

    /**
     * returns a string representing the number of days ago the post was made
     * @return
     */
    private String getTimestampDifference() {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = mPhoto.getData_created();
        try {
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24)));
        }catch (ParseException e) {
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage());
            difference = "0";
        }

        return difference;
    }

    /**
     * retrieve the activity number from the incoming bundle from profileActivity interface
     * @return
     */
    private int getActivityNumFromBundle() {
        Log.d(TAG, "getActivityNumFromBundle: arguments : " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getInt(getString(R.string.photo));
        } else {
            return 0;
        }
    }

    /**
     * retrieve the photo from the incoming bundle from profileActivity interface
     * @return
     */
    private Photo getPhotoFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments : " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.photo));
        } else {
            return null;
        }
    }

    //    BottomNavigationView setup
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setupBottomNavigationView((BottomNavigationViewEx) bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(getActivity(), getActivity(), bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
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
    public void onStop() {
        super.onStop();
        if (mAuthListner != null) {
            mAuth.removeAuthStateListener(mAuthListner);
        }
    }
}
