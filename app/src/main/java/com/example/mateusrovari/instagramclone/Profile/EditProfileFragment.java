package com.example.mateusrovari.instagramclone.Profile;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.mateusrovari.instagramclone.R;
import com.example.mateusrovari.instagramclone.Utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

public class EditProfileFragment extends Fragment {

    private ImageView mProfilePhoto;

    private static final String TAG = "EditProfileFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);
        mProfilePhoto = view.findViewById(R.id.profile_photo);

        initImageLoader();
        setProfileImage();
        return view;
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(getActivity());
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private  void setProfileImage(){
        Log.d(TAG, "setProfileImage: setting profile image");
        String imgURL = "media.licdn.com/dms/image/C4D03AQES5n0xQz-xtQ/profile-displayphoto-shrink_200_200/0?e=1539216000&v=beta&t=WhJKFmuQoQ3MMzSp9SzQT_uVeTfrAsYhKBuhBqQa8hA";
        UniversalImageLoader.setImage(imgURL, mProfilePhoto, null, "https://");
    }
}
