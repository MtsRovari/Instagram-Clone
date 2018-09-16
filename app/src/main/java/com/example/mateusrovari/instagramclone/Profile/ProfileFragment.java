package com.example.mateusrovari.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app .Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mateusrovari.instagramclone.Login.LoginActivity;
import com.example.mateusrovari.instagramclone.R;
import com.example.mateusrovari.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.mateusrovari.instagramclone.Utils.FirebaseMethods;
import com.example.mateusrovari.instagramclone.Utils.GridImageAdapter;
import com.example.mateusrovari.instagramclone.Utils.UniversalImageLoader;
import com.example.mateusrovari.instagramclone.models.Comment;
import com.example.mateusrovari.instagramclone.models.Like;
import com.example.mateusrovari.instagramclone.models.Photo;
import com.example.mateusrovari.instagramclone.models.User;
import com.example.mateusrovari.instagramclone.models.UserAccountSettings;
import com.example.mateusrovari.instagramclone.models.UserSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    public interface OnGridImageSelectedListner {
        void onGridImageSelected(Photo photo, int activityNumber);
    }
    OnGridImageSelectedListner mOnGridImageSelectedListner;

    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;

    private Context mContext;

    //widgets
    private TextView mPosts, mFollowers, mFollowing, mDisplayName, mUsername, mWebsite, mDescription;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationView bottomNavigationView;

    //    firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Log.d(TAG, "onCreateView: started");

        mDisplayName = view.findViewById(R.id.displayName);
        mUsername = view.findViewById(R.id.profileUsername);
        mWebsite = view.findViewById(R.id.website);
        mDescription = view.findViewById(R.id.description);
        mProfilePhoto = view.findViewById(R.id.profilePhoto);
        mPosts = view.findViewById(R.id.profilePosts);
        mFollowers = view.findViewById(R.id.profileFollowers);
        mFollowing = view.findViewById(R.id.profileFollowing);
        mProgressBar = view.findViewById(R.id.profileProgressBar);
        gridView = view.findViewById(R.id.gridView);
        toolbar = view.findViewById(R.id.profileToolbar);
        profileMenu = view.findViewById(R.id.profileMenu);
        bottomNavigationView = view.findViewById(R.id.bottomNavViewBar);
        mFirebaseMethods = new FirebaseMethods(getActivity());
        mContext = getActivity();

        initialize();

        TextView editProfile = view.findViewById(R.id.profileEdit);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to fragment");
                Intent i = new Intent(getActivity(), AccountSettingsActivity.class);
                i.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                startActivity(i);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try{
            mOnGridImageSelectedListner = (OnGridImageSelectedListner) getActivity();
        }catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException " + e.getMessage());
        }
        super.onAttach(context);
    }

    private void initialize() {
        setupBottomNavigationView();
        setupToolbar();
        setupFirebaseAuth();
        setupGridView();
    }

    private void setupGridView() {
        Log.d(TAG, "setupGridView: setting up image grid");

        final ArrayList<Photo> photos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                    photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                    photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                    photo.setData_created(objectMap.get(getString(R.string.field_data_created)).toString());
                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                    ArrayList<Comment> mComments = new ArrayList<Comment>();
                    for (DataSnapshot dSnapshot : singleSnapshot
                            .child(getString(R.string.field_comments)).getChildren()) {
                        Comment comment = new Comment();
                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                        mComments.add(comment);
                    }

                    photo.setComments(mComments);

                    List<Like> likesList = new ArrayList<Like>();
                    for (DataSnapshot dSnapshot : singleSnapshot
                            .child(getString(R.string.field_likes)).getChildren()) {
                        Like like = new Like();
                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                        likesList.add(like);
                    }

                    photo.setLikes(likesList);
                    photos.add(photo);
                }

                //setup our image grid
                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth/NUM_GRID_COLUMNS;
                gridView.setColumnWidth(imageWidth);

                ArrayList<String> imgUrls = new ArrayList<String>();
                for (int i = 0; i < photos.size(); i++) {
                    imgUrls.add(photos.get(i).getImage_path());
                }
                GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview,
                        "",  imgUrls);
                gridView.setAdapter(adapter);

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mOnGridImageSelectedListner.onGridImageSelected(photos.get(position), ACTIVITY_NUM);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: querry canceled");
            }
        });

    }

    private void setProfileWidgets(UserSettings userSettings) {

        User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mPosts.setText(String.valueOf(settings.getPosts()));
        mFollowing.setText(String.valueOf(settings.getFollowing()));
        mFollowers.setText(String.valueOf(settings.getFollowers()));
        mProgressBar.setVisibility(View.GONE);
    }

    private void setupToolbar() {

        ((ProfileActivity)getActivity()).setSupportActionBar(toolbar);

        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Navigating to account settings");
                Intent i = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(i);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

    }

    //    BottomNavigationView setup
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setupBottomNavigationView((BottomNavigationViewEx) bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext, getActivity(), (BottomNavigationViewEx) bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
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
}
