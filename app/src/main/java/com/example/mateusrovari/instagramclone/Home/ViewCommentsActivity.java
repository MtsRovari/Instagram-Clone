package com.example.mateusrovari.instagramclone.Home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mateusrovari.instagramclone.R;
import com.example.mateusrovari.instagramclone.Utils.CommentListAdapter;
import com.example.mateusrovari.instagramclone.models.Comment;
import com.example.mateusrovari.instagramclone.models.Photo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ViewCommentsActivity extends AppCompatActivity {

    private static final String TAG = "ViewCommentsActivity";

//    public ViewCommentsActivity() {
//        super();
//        setArguments(new Bundle());
//    }

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    //widgets
    private ImageView mBackArrow, mCheckMark;
    private EditText mComment;
    private ListView mListView;

    //vars
    private Photo mPhoto;
    private ArrayList<Comment> mComments;
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_view_comments);
        mBackArrow = findViewById(R.id.backArrow);
        mCheckMark = findViewById(R.id.ivPostComment);
        mComment = findViewById(R.id.comment);
        mListView = findViewById(R.id.listView);
        mComments = new ArrayList<>();
        mContext = ViewCommentsActivity.this;

//        try {
            mPhoto = getFromBundle();
//            getFromBundle();
            setupFirebaseAuth();
//        } catch (NullPointerException e) {
//            Log.e(TAG, "onCreateView: NullPointerException:" + e.getMessage());
//        }
    }

//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);
//        mBackArrow = view.findViewById(R.id.backArrow);
//        mCheckMark = view.findViewById(R.id.ivPostComment);
//        mComment = view.findViewById(R.id.comment);
//        mListView = view.findViewById(R.id.listView);
//        mComments = new ArrayList<>();
//        mContext = getActivity();
//
//        try {
//            mPhoto = getPhotoFromBundle();
//            setupFirebaseAuth();
//        } catch (NullPointerException e) {
//            Log.e(TAG, "onCreateView: NullPointerException:" + e.getMessage());
//        }
//
//        return view;
//    }

    private Photo getFromBundle() {
        Intent i = getIntent();
        Photo photo = i.getParcelableExtra(getString(R.string.bundle_photo));
        Log.e(TAG, "getFromBundle: " + photo);
        return photo;
    }

    private void setupWidgets() {
        CommentListAdapter adapter = new CommentListAdapter(mContext,
                R.layout.layout_comment, mComments);
        mListView.setAdapter(adapter);

        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mComment.getText().toString().equals("")) {
                    Log.d(TAG, "onClick: attempting to submit new comment");
                    addNewComment(mComment.getText().toString());
                    mComment.setText("");
                    closeKeyBoard();
                } else {
                    Toast.makeText(ViewCommentsActivity.this, "You can't post a blank comment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewCommentsActivity.this.getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void closeKeyBoard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addNewComment(String newComment) {
        Log.d(TAG, "addNewComment: adding new comment " + newComment);

        String commentId = myRef.push().getKey();

        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setDate_created(getTimeStamp());
        comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        //insert into photos node
        myRef.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_comments))
                .child(commentId)
                .setValue(comment);

        //insert into user_photos node
        myRef.child(getString(R.string.dbname_user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_comments))
                .child(commentId)
                .setValue(comment);
    }

    private String getTimeStamp() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        return simpleDateFormat.format(new Date());
    }

    /**
     * retrieve the photo from the incoming bundle from profileActivity interface
     * @return
     */
    private Photo getPhotoFromBundle() {
//        Log.d(TAG, "getPhotoFromBundle: arguments : " + getArguments());

        Bundle bundle = new Bundle();

        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.photo));
        } else {
            return null;
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
//    private int getActivityNumFromBundle() {
//        Log.d(TAG, "getActivityNumFromBundle: arguments : " + getArguments());
//
//        Bundle bundle = this.getArguments();
//        if (bundle != null) {
//            return bundle.getInt(getString(R.string.photo));
//        } else {
//            return 0;
//        }
//    }

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

        Log.e(TAG, "setupFirebaseAuth: " + mPhoto.getComments());
        if (mPhoto.getComments() == null) {
//            mComments.clear();
            Comment firstComment = new Comment();
            firstComment.setComment(mPhoto.getCaption());
            firstComment.setUser_id(mPhoto.getUser_id());
            firstComment.setDate_created(mPhoto.getData_created());
            mComments.add(firstComment);
            mPhoto.setComments(mComments);
            setupWidgets();
        }

        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Query query = myRef
                                .child(mContext.getString(R.string.dbname_photos))
                                .orderByChild(mContext.getString(R.string.field_photo_id))
                                .equalTo(mPhoto.getPhoto_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for ( DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                                    Photo photo = new Photo();
                                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                                    photo.setCaption(objectMap.get(mContext.getString(R.string.field_caption)).toString());
                                    photo.setTags(objectMap.get(mContext.getString(R.string.field_tags)).toString());
                                    photo.setPhoto_id(objectMap.get(mContext.getString(R.string.field_photo_id)).toString());
                                    photo.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
                                    photo.setData_created(objectMap.get(mContext.getString(R.string.field_data_created)).toString());
                                    photo.setImage_path(objectMap.get(mContext.getString(R.string.field_image_path)).toString());

                                    mComments.clear();
                                    Comment firstComment = new Comment();
                                    firstComment.setComment(mPhoto.getCaption());
                                    firstComment.setUser_id(mPhoto.getUser_id());
                                    firstComment.setDate_created(mPhoto.getData_created());

                                    mComments.add(firstComment);

                                    for (DataSnapshot dSnapshot : singleSnapshot
                                            .child(mContext.getString(R.string.field_comments)).getChildren()) {
                                        Comment comment = new Comment();
                                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                                        mComments.add(comment);
                                    }

                                    photo.setComments(mComments);

                                    mPhoto = photo;

                                    setupWidgets();

//                                    List<Like> likesList = new ArrayList<Like>();
//                                    for (DataSnapshot dSnapshot : singleSnapshot
//                                            .child(getString(R.string.field_likes)).getChildren()) {
//                                        Like like = new Like();
//                                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
//                                        likesList.add(like);
//                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled: querry canceled");
                            }
                        });
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        mAuth.addAuthStateListener(mAuthListner);
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        if (mAuthListner != null) {
//            mAuth.removeAuthStateListener(mAuthListner);
//        }
//    }
}
