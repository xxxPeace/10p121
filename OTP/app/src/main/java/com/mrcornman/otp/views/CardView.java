package com.mrcornman.otp.views;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mrcornman.otp.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Anil on 7/18/2014.
 */
@EViewGroup(R.layout.card_item)
public class CardView extends RelativeLayout implements CardStackLayout.CardStackListener {

    @ViewById
    ImageView picture;

    @ViewById
    TextView nameText;

    @ViewById
    TextView ageText;

    @ViewById
    TextView yes;

    @ViewById
    TextView no;

    @ViewById
    ImageView yesicon;

    @ViewById
    ImageView noicon;

    public String mUserId;

    public CardView(Context context) {
        super(context);
    }

    public void bind(String userId){
        mUserId = userId;
        return;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        yesicon.setAlpha((float) 0);
        noicon.setAlpha((float) 0);
        // todo: you can download the picture here or in the getView function of the ProductCardAdapter, for now things work so, i'm happy
    }

    @Override
    public void onBeginProgress(View view) {
        yes.setAlpha(0);
        no.setAlpha(0);
        yesicon.setAlpha(0f);
        noicon.setAlpha(0f);
        nameText.setAlpha(1);
        ageText.setAlpha(1);
    }

    @Override
    public void onUpdateProgress(boolean positif, float percent, View view) {
        if (positif) {
            yes.setAlpha(percent);
            yesicon.setAlpha(percent);
        } else {
            no.setAlpha(percent);
            noicon.setAlpha(percent);
        }
        // nameText.setAlpha(0);
        // actualPrice.setAlpha(0);
        // ageText.setAlpha(0);
    }

    @Override
    public void onCancelled(View beingDragged) {
        yes.setAlpha(0);
        no.setAlpha(0);
        yesicon.setAlpha(0f);
        noicon.setAlpha(0f);
        nameText.setAlpha(1);
        ageText.setAlpha(1);
    }

    @Override
    public void onChoiceMade(boolean choice, View beingDragged) {
        yes.setAlpha(0);
        no.setAlpha(0);
        yesicon.setAlpha(0f);
        noicon.setAlpha(0f);
        nameText.setAlpha(1);
        ageText.setAlpha(1);
        // fixme: here you have to do what happens after the choice is made,
        // todo: can we make Product public in the main class..?
    }
}
