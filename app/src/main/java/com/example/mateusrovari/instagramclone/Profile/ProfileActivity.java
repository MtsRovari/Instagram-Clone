package com.example.mateusrovari.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.mateusrovari.instagramclone.R;
import com.example.mateusrovari.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.mateusrovari.instagramclone.Utils.GridImageAdapter;
import com.example.mateusrovari.instagramclone.Utils.UniversalImageLoader;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity{

    private ProgressBar mProgress;
    private ImageView profilePhoto;
    private static final int NUM_GRID_COLLUMNS = 3;

    private static final String TAG = "ProfileActivity";
    private static final int ACTIVITY_NUM = 4;

    private Context mContext = ProfileActivity.this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: started.");

        init();

//        initialize();
//        setupBottomNavigationView();
//        setupToolbar();
//        setProfileImage();
//
//        tempGridSetup();
    }
    
    private void init() {
        Log.d(TAG, "init: inflating " + getString(R.string.profile_fragment));

        ProfileFragment fragment = new ProfileFragment();
        FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.profile_fragment));
        transaction.commit();
    }

//    private void initialize() {
//        mProgress = findViewById(R.id.profileProgressBar);
//        mProgress.setVisibility(View.GONE);
//        profilePhoto = findViewById(R.id.profilePhoto);
//    }
//
//    private void tempGridSetup(){
//        ArrayList<String> imgURLs = new ArrayList<>();
//        imgURLs.add("http://www.loucoporviagens.com.br/wp-content/uploads/2011/11/rochadopulpito.jpg");
//        imgURLs.add("http://www.loucoporviagens.com.br/wp-content/uploads/2011/11/cavernaazul.jpg");
//        imgURLs.add("http://www.loucoporviagens.com.br/wp-content/uploads/2011/11/jiuzhaigou.jpg");
//        imgURLs.add("http://www.loucoporviagens.com.br/wp-content/uploads/2011/11/Plitvice.jpg");
//        imgURLs.add("http://www.loucoporviagens.com.br/wp-content/uploads/2011/11/skaftafeli.jpg");
//        imgURLs.add("http://www.loucoporviagens.com.br/wp-content/uploads/2011/11/paterswildsemeer.jpg");
//        imgURLs.add("http://www.loucoporviagens.com.br/wp-content/uploads/2011/11/cavernasdemarmore.jpg");
//        imgURLs.add("http://www.loucoporviagens.com.br/wp-content/uploads/2011/11/borabora.jpg");
//        imgURLs.add("http://www.loucoporviagens.com.br/wp-content/uploads/2011/11/capilano.jpg");
//
//        setupImageGrid(imgURLs);
//    }

//    private void setupImageGrid(ArrayList<String> imgURLs) {
//        GridView gridView = findViewById(R.id.gridView);
//
//        int gridWidth = getResources().getDisplayMetrics().widthPixels;
//        int imageWidth = gridWidth/NUM_GRID_COLLUMNS;
//        gridView.setColumnWidth(imageWidth);
//
//        GridImageAdapter mAdapter = new GridImageAdapter(mContext, R.layout.layout_grid_imageview, "", imgURLs);
//        gridView.setAdapter(mAdapter);
//    }
//
//    private void setProfileImage() {
//        Log.d(TAG, "setProfileImage: setting profile photo");
//        String imgURL = "media.licdn.com/dms/image/C4D03AQES5n0xQz-xtQ/profile-displayphoto-shrink_200_200/0?e=1539216000&v=beta&t=WhJKFmuQoQ3MMzSp9SzQT_uVeTfrAsYhKBuhBqQa8hA";
//        UniversalImageLoader.setImage(imgURL, profilePhoto, mProgress, "https://");
//    }
//
//    private void setupToolbar() {
//        Toolbar toolbar = findViewById(R.id.profileToolbar);
//        setSupportActionBar(toolbar);
//
//        ImageView profileMenu = findViewById(R.id.profileMenu);
//        profileMenu.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: Navigating to account settings");
//                Intent i = new Intent(ProfileActivity.this, AccountSettingsActivity.class);
//                startActivity(i);
//            }
//        });
//
//    }
//
//    //    BottomNavigationView setup
//    private void setupBottomNavigationView(){
//        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
//        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
//        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
//        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
//        Menu menu = bottomNavigationViewEx.getMenu();
//        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
//        menuItem.setChecked(true);
//    }
}
