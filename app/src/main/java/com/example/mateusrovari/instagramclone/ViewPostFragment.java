package com.example.mateusrovari.instagramclone;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mateusrovari.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.mateusrovari.instagramclone.Utils.SquareImageView;
import com.example.mateusrovari.instagramclone.Utils.UniversalImageLoader;
import com.example.mateusrovari.instagramclone.models.Photo;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class ViewPostFragment extends Fragment {

    private static final String TAG = "ViewPostFragment";

    public ViewPostFragment() {
        super();
        setArguments(new Bundle());
    }

    //vars
    private Photo mPhoto;
    private int mActivityNumber = 0;

    //widgets
    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationView;
    private TextView mBlackLabel, mCaption, mUsername, mTimestamp;
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
        mMore = view.findViewById(R.id.more_posts);
        mHeartRed = view.findViewById(R.id.image_heat_red);
        mHeartWhite = view.findViewById(R.id.image_heat_outline);
        mProfileImage = view.findViewById(R.id.profile_photo);

        try {
            mPhoto = getPhotoFromBundle();
            UniversalImageLoader.setImage(mPhoto.getImage_path(), mPostImage, null, "");
            mActivityNumber = getActivityNumFromBundle();
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException:" + e.getMessage());
        }

        setupBottomNavigationView();

        return view;
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
        BottomNavigationViewHelper.enableNavigation(getActivity(), (BottomNavigationViewEx) bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
    }
}
