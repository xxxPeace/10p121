package com.mrcornman.otp.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mrcornman.otp.R;
import com.mrcornman.otp.fragments.ClientListFragment;
import com.mrcornman.otp.fragments.GameFragment;
import com.mrcornman.otp.fragments.MakerListFragment;
import com.mrcornman.otp.fragments.SettingsFragment;
import com.mrcornman.otp.fragments.NavigationDrawerFragment;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks, ClientListFragment.ClientListInteractionListener {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setTitle("Please wait...");
        progressDialog.show();

        // listen for successful startup broadcast from MessageService
        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("MainActivity", "We got yo broadcast");
                boolean success = intent.getBooleanExtra("success", false);
                progressDialog.dismiss();

                if(!success) {
                    Toast.makeText(getApplicationContext(),
                            "There was a problem with the messaging service, please restart the app",
                            Toast.LENGTH_LONG).show();
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("com.mrcornman.otp.activities.MainActivity"));
    }

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
            case NAV_PROFILE:
                Intent profile = new Intent(this, ProfileActivity.class);
                startActivity(profile);

                break;
        }
        if (position != NAV_PROFILE){
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
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // fragment interface actions
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
}
