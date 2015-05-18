package com.mrcornman.otp.utils;

import com.mrcornman.otp.models.MatchItem;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.Arrays;

/**
 * Common Database Functionality
 */
public class DatabaseHelper {

    private DatabaseHelper() {}

    public static void insertNewMatchByPair(String firstId, String secondId) {
        MatchItem match = new MatchItem();
        match.setFirstId(firstId);
        match.setSecondId(secondId);
        match.setNumLikes(1);

        ParseRelation<ParseObject> relation = match.getRelation("followers");
        relation.add(ParseUser.getCurrentUser());
        match.saveInBackground();
    }

    public static void getUserById(String id, GetCallback<ParseUser> getCallback){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.getInBackground(id, getCallback);
    }

    public static void getMatchById(String id, GetCallback<MatchItem> getCallback){
        ParseQuery<MatchItem> query = ParseQuery.getQuery(MatchItem.class);
        query.getInBackground(id, getCallback);
    }

    public static void getMatchByPair(String firstId, String secondId, GetCallback<MatchItem> getCallback){
        ParseQuery<MatchItem> query = ParseQuery.getQuery(MatchItem.class);

        String[] userIds = { firstId, secondId };
        query.whereContainedIn(MatchItem.MATCH_KEY_FIRST_ID, Arrays.asList(userIds));
        query.whereContainedIn(MatchItem.MATCH_KEY_SECOND_ID, Arrays.asList(userIds));
        query.getFirstInBackground(getCallback);
    }

    public static void findTopMatches(int limit, FindCallback<MatchItem> findCallback) {
        ParseQuery<MatchItem> query = ParseQuery.getQuery(MatchItem.class);
        query.orderByDescending(MatchItem.MATCH_KEY_NUM_LIKES);
        query.setLimit(limit);
        query.findInBackground(findCallback);
    }

    public static void updateMatchNumLikes(String matchId, final int numLikes){
        ParseQuery<MatchItem> query = ParseQuery.getQuery(MatchItem.class);
        query.getInBackground(matchId, new GetCallback<MatchItem>() {
            @Override
            public void done(MatchItem matchItem, ParseException e) {
                matchItem.setNumLikes(numLikes);
                matchItem.saveInBackground();
            }
        });
    }
}