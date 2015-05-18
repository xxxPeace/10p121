package com.mrcornman.otp.models;

import android.util.Log;

import com.google.gson.Gson;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("Photo")
public class PhotoItem extends ParseObject {

    // key identifiers
    public static final String PHOTO_KEY_FILES = "files";

    public List<PhotoFile> getPhotoFiles() {
        List<PhotoFile> photoFiles = new ArrayList<>();
        try {
            JSONArray photoFilesData = getJSONArray(PHOTO_KEY_FILES);
            JSONObject photoFileObj = null;
            Gson gson = new Gson();
            for (int i = 0; i < photoFilesData.length(); i++) {
                photoFileObj = photoFilesData.getJSONObject(i);
                PhotoFile photoFile = gson.fromJson(photoFileObj.toString(), PhotoFile.class);
                photoFiles.add(photoFile);
            }
        } catch(JSONException e) {
            Log.e("PhotoItem", "Error getting files from a photo item.");
            return null;
        }

        return photoFiles;
    }

    public void setPhotoFiles(List<PhotoFile> photoFiles) {

        JSONArray photoFilesData = new JSONArray();
        Gson gson = new Gson();
        String photoFileObjStr = null;
        for(int i = 0; i < photoFiles.size(); i++) {
            photoFileObjStr = gson.toJson(photoFiles.get(i));

            try {
                photoFilesData.put(new JSONObject(photoFileObjStr));
            } catch (JSONException e) {
                Log.e("PhotoItem", "There was a problem adding a new photo file to a photo item.");
            }
        }

        put(PHOTO_KEY_FILES, photoFilesData);
    }
}