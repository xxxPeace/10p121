package com.mrcornman.otp.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Match")
public class MatchItem extends ParseObject {

    // key identifiers
    public static final String MATCH_KEY_FIRST_ID = "first_id";
    public static final String MATCH_KEY_SECOND_ID = "second_id";
    public static final String MATCH_KEY_MATCHMAKER_ID = "matchmaker_id";
    public static final String MATCH_KEY_NUM_LIKES = "num_likes";

    public String getFirstId() {
        return getString(MATCH_KEY_FIRST_ID);
    }
    public void setFirstId(String firstId) {
        put(MATCH_KEY_FIRST_ID, firstId);
    }

    public String getSecondId() {
        return getString(MATCH_KEY_SECOND_ID);
    }
    public void setSecondId(String secondId) {
        put(MATCH_KEY_SECOND_ID, secondId);
    }

    public String getMatchmakerId() {
        return getString(MATCH_KEY_MATCHMAKER_ID);
    }
    public void setMatchmakerId(String matchmakerId) {
        put(MATCH_KEY_MATCHMAKER_ID, matchmakerId);
    }

    public int getNumLikes() {
        return getInt(MATCH_KEY_NUM_LIKES);
    }
    public void setNumLikes(int numLikes) {
        put(MATCH_KEY_NUM_LIKES, numLikes);
    }

    @Override
    public String toString() {
        return "TODO: First + Second + Matchmaker";
    }
}