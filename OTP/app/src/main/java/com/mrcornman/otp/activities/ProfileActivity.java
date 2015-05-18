package com.mrcornman.otp.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mrcornman.otp.R;
import com.mrcornman.otp.fragments.ClientListFragment;
import com.mrcornman.otp.fragments.NavigationDrawerFragment;
import com.mrcornman.otp.models.PhotoFile;
import com.mrcornman.otp.models.PhotoItem;
import com.mrcornman.otp.utils.PrettyTime;
import com.mrcornman.otp.utils.ProfileBuilder;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import com.mrcornman.otp.fragments.GameFragment;
import com.mrcornman.otp.fragments.MakerListFragment;
import com.mrcornman.otp.fragments.SettingsFragment;

import java.util.List;

public class ProfileActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks, ClientListFragment.ClientListInteractionListener {

    /**
     * Navigation Identifiers
     */
    public static final int NAV_GAME = 0;
    public static final int NAV_SETTINGS = 1;
    public static final int NAV_PROFILE = 2;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = null;

        switch (position) {
            case NAV_GAME:
                fragment = GameFragment.newInstance();
                break;
            case NAV_SETTINGS:
                fragment = SettingsFragment.newInstance();
                break;

        }
        if (position == NAV_PROFILE){
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                            //.addToBackStack(null)
                    .commit();
            onSectionAttached(position + 1);
            restoreActionBar();
        }
    }
    @Override
    public void onMenuItemClientList() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, ClientListFragment.newInstance(1))
                        //.addToBackStack(null)
                .commit();
        restoreActionBar();
    }
    @Override
    public void onMenuItemMatchmakerList() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, MakerListFragment.newInstance(1))
                        //.addToBackStack(null)
                .commit();
        restoreActionBar();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = "Game";
                break;
            case 2:
                mTitle = "Settings";
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }
    @Override
    public void onRequestOpenConversation(String recipientId) {
        openConversation(recipientId);
    }
    // helpers
    public void openConversation(String recipientId) {
        // TODO: Make sure the user exists when populating the list view in client list fragment so that there isn't the potential problem of the user not existing here
        Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
        intent.putExtra("recipient_id", recipientId);
        startActivity(intent);
        Log.i("MainActivity", "Beginning conversation with " + recipientId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        //RelativeLayout rootView = (RelativeLayout)findViewById(R.layout.activity_profile);


        TextView nameText = (TextView) findViewById(R.id.name_text);
        TextView ageText = (TextView) findViewById(R.id.age_text);

        final FrameLayout pictureContainer = (FrameLayout) findViewById(R.id.picture_container);
        final ImageView pictureImage = (ImageView) findViewById(R.id.picture_image);

        // need this to get the finalized width of the framelayout after the match_parent width is calculated
        pictureContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int pictureWidth = pictureContainer.getWidth();
                pictureContainer.setLayoutParams(new RelativeLayout.LayoutParams(pictureWidth, pictureWidth));
            }
        });

        // NOTE: This is how you get the current user once they've logged in from Facebook
        ParseUser user = ParseUser.getCurrentUser();

        // and this is how you can get data from the user profile
        nameText.setText(user.getString(ProfileBuilder.PROFILE_KEY_NAME));
        ageText.setText(PrettyTime.getAgeFromBirthDate(user.getDate(ProfileBuilder.PROFILE_KEY_BIRTHDATE)) + "");

        // and this is how you grab an image from the user profile and put it into image view
        List<PhotoItem> photoItems = user.getList(ProfileBuilder.PROFILE_KEY_PHOTOS);
        PhotoItem mainPhoto = photoItems.get(0);
        mainPhoto.fetchIfNeededInBackground(new GetCallback<PhotoItem>() {
            @Override
            public void done(PhotoItem photoItem, ParseException e) {
                PhotoFile mainFile = photoItem.getPhotoFiles().get(0);
                Picasso.with(ProfileActivity.this.getApplicationContext()).load(mainFile.url).resize(pictureImage.getMeasuredWidth(), pictureImage.getMeasuredHeight()).centerCrop().into(pictureImage);
            }
        });


    }


}