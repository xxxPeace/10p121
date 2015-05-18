package com.mrcornman.otp.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mrcornman.otp.R;
import com.mrcornman.otp.adapters.CardAdapter;
import com.mrcornman.otp.models.MatchItem;
import com.mrcornman.otp.utils.DatabaseHelper;
import com.mrcornman.otp.views.CardStackLayout;
import com.mrcornman.otp.views.CardView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class GameFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    private CardStackLayout mCardStackLayoutFirst;
    private CardStackLayout mCardStackLayoutSecond;
    private CardAdapter mCardAdapterFirst;
    private CardAdapter mCardAdapterSecond;

    private SharedPreferences sharedPreferences;

    private List<ParseUser> mCardUsersFirst;
    private List<ParseUser> mCardUsersSecond;
    private String potentialFirstId = "";
    private String potentialSecondId = "";

    public static GameFragment newInstance() {
        GameFragment fragment = new GameFragment();
        return fragment;
    }

    public GameFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        // init views
        mCardStackLayoutFirst = (CardStackLayout) view.findViewById(R.id.cardstack_first);
        mCardStackLayoutSecond = (CardStackLayout) view.findViewById(R.id.cardstack_second);

        Button refreshButtonFirst = (Button) view.findViewById(R.id.btn_refresh_first);
        refreshButtonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshFirst();
            }
        });

        Button refreshButtonSecond = (Button) view.findViewById(R.id.btn_refresh_second);
        refreshButtonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshSecond();
            }
        });

        mCardStackLayoutFirst.setCardStackListener(new CardStackLayout.CardStackListener() {
            @Override
            public void onBeginProgress(View view) {
                buildPotentialMatch(getCurrentFirstId(), getCurrentSecondId());
            }

            @Override
            public void onUpdateProgress(boolean positif, float percent, View view) {
                CardView item = (CardView) view;
                item.onUpdateProgress(positif, percent, view);
            }

            @Override
            public void onCancelled(View beingDragged) {
                CardView item = (CardView) beingDragged;
                item.onCancelled(beingDragged);
                clearPotentialMatch();
            }

            @Override
            public void onChoiceMade(boolean choice, View beingDragged) {
                /*
                SingleUserView item = (SingleUserView) beingDragged;
                item.onChoiceMade(choice, beingDragged);
                //todo: handle what to do after the choice is made.
                if (choice) {
                    db.updateNumLikes(item.userItem, db.VALUE_LIKED);
                } else {
                    db.updateNumLikes(item.userItem, db.VALUE_DISLIKED);
                }
                Log.d("game fragment", "updated the choice made " + String.valueOf(choice) + " " + item.userItem.getName());
                */
                onCreateMatch();
            }
        });

        mCardStackLayoutSecond.setCardStackListener(new CardStackLayout.CardStackListener() {
            @Override
            public void onBeginProgress(View view) {
                buildPotentialMatch(getCurrentFirstId(), getCurrentSecondId());
            }

            @Override
            public void onUpdateProgress(boolean positif, float percent, View view) {
                CardView item = (CardView) view;
                item.onUpdateProgress(positif, percent, view);
            }

            @Override
            public void onCancelled(View beingDragged) {
                CardView item = (CardView) beingDragged;
                item.onCancelled(beingDragged);
                clearPotentialMatch();
            }

            @Override
            public void onChoiceMade(boolean choice, View beingDragged) {
                onCreateMatch();
            }
        });

        // init data
        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        // resetStoredValues();

        mCardUsersFirst = new ArrayList<>();
        mCardAdapterFirst = new CardAdapter(getActivity().getApplicationContext(), mCardUsersFirst);
        mCardStackLayoutFirst.setAdapter(mCardAdapterFirst);

        mCardUsersSecond = new ArrayList<>();
        mCardAdapterSecond = new CardAdapter(getActivity().getApplicationContext(), mCardUsersSecond);
        mCardStackLayoutSecond.setAdapter(mCardAdapterSecond);

        refreshFirst();
        refreshSecond();

        return view;
    }


    private void buildPotentialMatch(String firstId, String secondId) {
        potentialFirstId = firstId != null ? firstId : "";
        potentialSecondId = secondId != null ? secondId : "";
    }

    private void clearPotentialMatch() {
        potentialFirstId = "";
        potentialSecondId = "";
    }

    public void onCreateMatch() {
        if(potentialFirstId.equals("") || potentialSecondId.equals("")) return;

        final String cachedFirstId = potentialFirstId;
        final String cachedSecondId = potentialSecondId;

        clearPotentialMatch();

        if(cachedFirstId == cachedSecondId) {
            Log.e("Game Fragment", "User tried to match up with self.");
            return;
        }

        // TODO: Possibly make it update on insert match instead of doing a costly check beforehand
        DatabaseHelper.getMatchByPair(cachedFirstId, cachedSecondId, new GetCallback<MatchItem>() {
            @Override
            public void done(MatchItem matchItem, ParseException e) {
                if (matchItem == null) {
                    DatabaseHelper.insertNewMatchByPair(cachedFirstId, cachedSecondId);
                } else {
                    DatabaseHelper.updateMatchNumLikes(matchItem.getObjectId(), matchItem.getNumLikes() + 1);
                }
            }
        });
    }

    //fixme: figure out how to eliminate the ui lag when the database gets new products from the internet..
    // already tried initializing the product stack inside the overridden method onViewCreated, and it didn't improve anything..

    private void refreshFirst() {
        /*
        if (startFromFirst > Integer.parseInt(maxCardsFirst)){
            startFromFirst = 0;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            //editor.putInt(getString(R.string.men_shoes_start_from_key), 0);
            editor.commit();
        }
        */

        ParseQuery<ParseUser> query = ParseUser.getQuery();

        // exclude self
        //query.whereNotEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        query.setLimit(20);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < list.size(); i++) {
                        mCardUsersFirst.add(list.get(i));
                    }
                    mCardAdapterFirst.notifyDataSetChanged();
                    mCardStackLayoutFirst.refreshStack();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Sorry, there was a problem loading users",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void refreshSecond() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();

        // exclude self
        query.whereNotEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        query.setLimit(20);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < list.size(); i++) {
                        mCardUsersSecond.add(list.get(i));
                    }
                    mCardAdapterSecond.notifyDataSetChanged();
                    mCardStackLayoutSecond.refreshStack();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Sorry, there was a problem loading users",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public String getCurrentFirstId() {
        CardView view = mCardStackLayoutFirst.getmBeingDragged() != null ? (CardView)mCardStackLayoutFirst.getmBeingDragged() : (CardView)mCardStackLayoutFirst.getTopCard();
        return view != null ? view.mUserId : null;
    }

    public String getCurrentSecondId() {
        CardView view = mCardStackLayoutSecond.getmBeingDragged() != null ? (CardView)mCardStackLayoutSecond.getmBeingDragged() : (CardView)mCardStackLayoutSecond.getTopCard();
        return view != null ? view.mUserId : null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}
