package com.example.mateusrovari.instagramclone.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.mateusrovari.instagramclone.Profile.AccountSettingsActivity;
import com.example.mateusrovari.instagramclone.R;
import com.example.mateusrovari.instagramclone.Utils.Permissions;

import static android.app.Activity.RESULT_OK;

public class PhotoFragment extends Fragment {
    private static final String TAG = "PhotoFragment";

    private Button launchCamera;

    //constant
    private static final int PHOTO_FRAGMENT_NUM = 1;
    private static final int GALLERY_FRAGMENT_NUM = 2;
    private static final int CAMERA_REQUEST_CODE = 5;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        launchCamera = view.findViewById(R.id.launchCamera);
        launchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: launching camera");
                if (((ShareActivity)getActivity()).getCurrentTabNumber() == PHOTO_FRAGMENT_NUM) {
                    Log.d(TAG, "onClick: getCurrent TabNumber");
                    if (((ShareActivity)getActivity()).checkPermissions(Permissions.CAMERA_PERMISION[0])) {
                        Log.d(TAG, "onClick: starting camera");
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                    } else {
                        Log.d(TAG, "onClick: else camera");
                        Intent intent = new Intent(getActivity(), ShareActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }
        });

        return view;
    }

    private boolean isRootTask() {
        if (((ShareActivity)getActivity()).getTask() == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode != RESULT_OK) return;

        if (requestCode == CAMERA_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: done taking a photo");
            Log.d(TAG, "onActivityResult: attempting to navigate to final share screen");

            Bitmap bitmap;
            bitmap = (Bitmap) data.getExtras().get("data");
            
            if (isRootTask()) {
                try{
                    Log.d(TAG, "onActivityResult: received new bitmap from camera " + bitmap);
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                    startActivity(intent);
                }catch (NullPointerException e) {
                    Log.d(TAG, "onActivityResult: NullPointerException " + e.getMessage());
                }
            } else {
                try{
                    Log.d(TAG, "onActivityResult: received new bitmap from camera " + bitmap);
                    Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();
                }catch (NullPointerException e) {
                    Log.d(TAG, "onActivityResult: NullPointerException " + e.getMessage());
                }
            }
        }
    }
}
