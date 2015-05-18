package com.mrcornman.otp.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jonathan on 5/14/2015.
 */
public class PhotoFile {

    @SerializedName("width")
    public int width;

    @SerializedName("height")
    public int height;

    @SerializedName("url")
    public String url;
}
