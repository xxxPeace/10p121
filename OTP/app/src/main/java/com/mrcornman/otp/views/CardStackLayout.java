package com.mrcornman.otp.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.mrcornman.otp.adapters.CardAdapter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Anil on 7/18/2014.
 */
public class CardStackLayout extends RelativeLayout {

    public interface CardStackListener {
        void onBeginProgress(View view);
        void onUpdateProgress(boolean positif, float percent, View view);
        void onCancelled(View beingDragged);
        void onChoiceMade(boolean choice, View beingDragged);
    }

    private static int STACK_SIZE = 4;
    private static int MAX_ANGLE_DEGREE = 20;
    private CardAdapter mAdapter;
    private int mCurrentPosition;
    private int mMinDragDistance;
    private int mMinAcceptDistance;

    private int mXDelta;
    private int mYDelta;

    protected LinkedList<CardView> mCards = new LinkedList<CardView>();
    protected LinkedList<CardView> mRecycledCards = new LinkedList<CardView>();

    private CardStackListener mCardStackListener;

    protected LinkedList<Object> mCardStack = new LinkedList<Object>();
    private int mXStart;
    private int mYStart;

    private View mBeingDragged;
    private OnCardTouchListener mOnCardTouchListener;

    public CardStackLayout(Context context) {
        super(context);
        setup();
    }

    public CardStackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public CardStackLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    public View getmBeingDragged() {
        return mBeingDragged;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mBeingDragged != null){
            mXDelta = (int) mBeingDragged.getTranslationX();
            mYDelta = (int) mBeingDragged.getTranslationY();
        }

        int index = 0;
        Iterator<CardView> it = mCards.descendingIterator();
        while (it.hasNext()){
            CardView card = it.next();
            if (card == null)
                break;

            if (isTopCard(card)){
                card.setOnTouchListener(mOnCardTouchListener);
            } else {
                card.setOnTouchListener(null);
            }

            if (index == 0 && adapterHasMoreItems()) {
                if (mBeingDragged != null){
                    index++;
                    continue;
                }

                scaleAndTranslate(1, card);
            } else {
                scaleAndTranslate(index, card);
            }

            index++;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setCardStackListener(CardStackListener mCardStackListener) {
        this.mCardStackListener = mCardStackListener;
    }

    private void scaleAndTranslate(int cardIndex, View view) {
        LinearInterpolator interpolator = new LinearInterpolator();

        if (view == mBeingDragged){
            int sign = 1;
            if (mXDelta > 0){
                sign = -1;
            }
            float progress = Math.min(Math.abs(mXDelta)/((float)mMinAcceptDistance*5), 1);
            float angleDegree = MAX_ANGLE_DEGREE * interpolator.getInterpolation(progress);

            view.setRotation(sign*angleDegree);

            return;
        }

        float zoomFactor = 0;
        if (mBeingDragged != null){
            float interpolation = 0;
            float distance = (float) Math.sqrt(mXDelta*mXDelta + mYDelta*mYDelta);
            float progress = Math.min(distance/mMinDragDistance, 1);
            interpolation = interpolator.getInterpolation(progress);
            interpolation = Math.min(interpolation, 1);
            zoomFactor = interpolation;
        }

        int position = STACK_SIZE - cardIndex;
        float step = 0.025f;

        Resources r = getContext().getResources();
        float translateStep = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                10, r.getDisplayMetrics());
        float scale = step * (position - zoomFactor);
        float translate = translateStep * (position - zoomFactor);
        view.setTranslationY(translate);
        view.setTranslationX(0);
        view.setRotation(0);
        view.setScaleY(1-scale);
        view.setScaleX(1-scale);

        return;

    }

    private boolean adapterHasMoreItems() {
        return mCurrentPosition < mAdapter.getCount();
    }

    public CardView getTopCard() { return mCards.peek(); }
    private boolean isTopCard(CardView card) {
        return card == mCards.peek();
    }

    public List<CardView> getCards() { return mCards; }

    private void setup() {
        Resources r = getContext().getResources();
        mMinDragDistance = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                50, r.getDisplayMetrics());
        mMinAcceptDistance = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                40, r.getDisplayMetrics());

        mCurrentPosition = 0;
    }

    public void setAdapter(CardAdapter adapter){
        mAdapter = adapter;
        mRecycledCards.clear();
        mCards.clear();
        removeAllViews();
        mCurrentPosition = 0;
        refreshStack();
    }

    public void refreshStack() {
        int position = 0;
        // fixed: possible fix for the unusual error of the 4th product repeating, mCurrentPosition + STACK_SIZE -1 or
        // fixed: mCurrentPosition += position instead of position - 1, in the end of this function
        for (; position < mCurrentPosition + STACK_SIZE; position++){
            if (position >= mAdapter.getCount())
                break;

            Object item = mAdapter.getItem(position);
            mCardStack.offer(item);

            CardView card = (CardView)mAdapter.getView(position, null, null);
            mCards.offer(card);

            addView(card, 0);

            mOnCardTouchListener = new OnCardTouchListener();
        }

        mCurrentPosition += position;
    }

    private class OnCardTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(final View view, MotionEvent motionEvent) {
            CardView card = (CardView)view;
            if (!isTopCard(card)){
                return false;
            }

            final int X = (int) motionEvent.getRawX();
            final int Y = (int) motionEvent.getRawY();

            final int action = motionEvent.getAction();
            switch (action & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                    mXStart = X;
                    mYStart = Y;
                    mCardStackListener.onBeginProgress(view);
                    break;
                case MotionEvent.ACTION_UP:
                    if (mBeingDragged == null)
                        return false;

                    if (!canAcceptChoice()){
                        requestLayout();
                        AnimatorSet set = new AnimatorSet();

                        ObjectAnimator yTranslation = ObjectAnimator.ofFloat(mBeingDragged, "translationY", 0);
                        ObjectAnimator xTranslation = ObjectAnimator.ofFloat(mBeingDragged, "translationX", 0);
                        set.playTogether(xTranslation, yTranslation);

                        set.setDuration(100).start();
                        set.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                View finalView = mBeingDragged;
                                mBeingDragged = null;
                                mXDelta = 0;
                                mYDelta = 0;
                                mXStart = 0;
                                mYStart = 0;
                                requestLayout();
                                if (mCardStackListener != null){
                                    mCardStackListener.onCancelled(finalView);
                                }
                            }
                        });

                        ValueAnimator.AnimatorUpdateListener onUpdate = new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                mXDelta = (int) view.getTranslationX();
                                mYDelta = (int) view.getTranslationY();
                                requestLayout();
                            }
                        };
                        yTranslation.addUpdateListener(onUpdate);
                        xTranslation.addUpdateListener(onUpdate);
                        set.start();
                    } else {
                        final CardView last = mCards.poll();
                        CardView recycled = getRecycledOrNew();
                        if (recycled != null){
                            RelativeLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            params.addRule(RelativeLayout.CENTER_IN_PARENT);

                            mCards.offer(recycled);
                            addView(recycled, 0, params);
                        }

                        int sign = mXDelta > 0 ? +1 : -1;
                        final boolean finalChoice = mXDelta > 0;
                        mBeingDragged = null;
                        mXDelta = 0;
                        mYDelta = 0;
                        mXStart = 0;
                        mYStart = 0;

                        ObjectAnimator animation = ObjectAnimator.ofFloat(last, "translationX", sign*1000)
                                .setDuration(300);
                        animation.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (mCardStackListener != null){
                                    boolean choice = finalChoice;
                                    mCardStackListener.onChoiceMade(choice, last);
                                }

                                recycleView(last);

                                final ViewGroup parent = (ViewGroup) view.getParent();
                                if (null != parent) {
                                    parent.removeView(view);
                                    parent.addView(view, 0);
                                }

                                last.setScaleX(1);
                                last.setScaleY(1);
                                setTranslationY(0);
                                setTranslationX(0);
                                requestLayout();
                            }
                        });
                        animation.start();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    boolean choiceBoolean = getStackChoice();
                    float progress = getStackProgress();

                    view.setTranslationX(X - mXStart);
                    view.setTranslationY(Y - mYStart);

                    mXDelta = X - mXStart;
                    mYDelta = Y - mYStart;

                    mBeingDragged = view;
                    requestLayout();

                    if (mCardStackListener != null){
                        mCardStackListener.onUpdateProgress(choiceBoolean, progress, mBeingDragged);
                    }

                    break;
            }
            return true;
        }
    }

    private float getStackProgress() {
        LinearInterpolator interpolator = new LinearInterpolator();
        float progress = Math.min((float)Math.abs(mYDelta) / (mMinAcceptDistance * 5.0f), 1);
        progress = interpolator.getInterpolation(progress);
        return progress;
    }

    private void recycleView(CardView last) {
        ((ViewGroup)last.getParent()).removeView(last);
        mRecycledCards.offer(last);
    }

    private CardView getRecycledOrNew() {
        if (adapterHasMoreItems()){
            CardView card = mRecycledCards.poll();
            card = (CardView)mAdapter.getView(mCurrentPosition++, card, null);

            return card;
        } else {
            return null;
        }
    }

    private boolean canAcceptChoice() {
        return (mXDelta * mXDelta + mYDelta * mYDelta) > mMinAcceptDistance * mMinAcceptDistance;
    }

    private boolean getStackChoice() {
        boolean result = Math.abs(mXDelta) < mMinAcceptDistance && mYDelta > 0;
        //Log.i("Stack Choice", Boolean.toString(result));
        return result;
    }
}
