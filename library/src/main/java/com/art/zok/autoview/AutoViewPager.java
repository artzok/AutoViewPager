package com.art.zok.autoview;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import static android.support.v4.view.ViewPager.OnPageChangeListener;

/**
 * @author 赵坤(artzok)
 * @email artzok@163.com
 */
public class AutoViewPager extends FrameLayout implements
        AutoPlayCallback, View.OnTouchListener, OnPageChangeListener {
    // raw viewpager
    private ViewPager mRawViewPager;
    private LinearLayout mIndicatorContainer;
    private TextView mPageTitle;

    private OnTouchListener mTouchListener;
    private OnPageChangeListener mOnPageChangeListener;

    //The adapter should be used to set up.
    private PagerAdapter mPagerAdapter;
    // The raw adapter
    private RawPageAdapter mRawAdapter;

    // The Handler is used for automatic cycle.
    private Handler mHandler;

    // play flag
    private boolean mIsStart;
    private boolean mIsPlaying;
    private int lastPosition;

    // attributes
    private int mIntervalTime;
    private int mIndicatorWidth;
    private int mIndicatorHeight;
    private int mIndicatorPadding;
    private boolean mShowPageTitle;
    private int mSelectedDrawableRes;
    private int mUnselectedDrawableRes;
    private float mPageTitleFontSize;
    private int mPageTitleFontColor;

    public AutoViewPager(Context context) {
        this(context, null);
    }

    public AutoViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initView(context);
        initState();
        initListener();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        // default indicator size
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, dm);
        // default title font size
        float fontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, dm);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AutoViewPager);
        mSelectedDrawableRes = array.getResourceId(R.styleable.AutoViewPager_selectedDrawable, R.drawable.default_selected_shape);
        mUnselectedDrawableRes = array.getResourceId(R.styleable.AutoViewPager_unselectedDrawable, R.drawable.default_unselected_shape);
        mIntervalTime = array.getInt(R.styleable.AutoViewPager_intervalTime, 2000);
        mIndicatorWidth = (int) array.getDimension(R.styleable.AutoViewPager_indicatorWidth, size);
        mIndicatorHeight = (int) array.getDimension(R.styleable.AutoViewPager_indicatorHeight, size);
        // default padding is equal to half the indicator size
        mIndicatorPadding = (int) array.getDimension(R.styleable.AutoViewPager_indicatorPadding, mIndicatorWidth / 2);
        mShowPageTitle = array.getBoolean(R.styleable.AutoViewPager_showPageTitle, true);
        mPageTitleFontSize = array.getDimension(R.styleable.AutoViewPager_pageTitleFontSize, fontSize);
        mPageTitleFontColor = array.getColor(R.styleable.AutoViewPager_pageTitleFontColor, Color.WHITE);
        array.recycle();
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.auto_view_pager_layout, this, true);
        mRawViewPager = (ViewPager) view.findViewById(R.id.raw_view_pager);
        mIndicatorContainer = (LinearLayout) view.findViewById(R.id.indicator_container);
        mPageTitle = (TextView) view.findViewById(R.id.page_title);
        mPageTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mPageTitleFontSize);
        mPageTitle.setTextColor(mPageTitleFontColor);
    }

    private void initState() {
        mHandler = new AutoPlayHandler(this);
        // a continuous loop by default
        mIsPlaying = true;
        mIsStart = false;
    }

    private void initListener() {
        mRawViewPager.setOnTouchListener(this);
        mRawViewPager.setOnPageChangeListener(this);
    }

    /**
     * set adapter for ViewPager.
     */
    public void setPagerAdapter(PagerAdapter adapter) {
        if (adapter == null)
            throw new RuntimeException("The adapter cannot be null.");
        if (mPagerAdapter != null)
            throw new RuntimeException("Adapter has already exist!");
        // user adapter
        mPagerAdapter = adapter;
        mPagerAdapter.registerDataSetObserver(new DataObserver());
        // raw adapter for raw ViewPager
        mRawAdapter = new RawPageAdapter();
        mRawViewPager.setAdapter(mRawAdapter);
        // init indicator
        updateIndicator(0, false);
        // init first page
        initSelectedItem();
    }

    private void updateIndicator(int index, boolean isLeft) {
        //  according to the sliding direction to update indicator
        int count = mPagerAdapter.getCount();
        if (count > mIndicatorContainer.getChildCount()) initIndicator();
        ((ImageView) mIndicatorContainer.getChildAt(swap(index, count)))
                .setImageResource(mSelectedDrawableRes);
        ((ImageView) mIndicatorContainer.getChildAt(swap(index + (isLeft ? -1 : 1), count)))
                .setImageResource(mUnselectedDrawableRes);
    }

    // cycles index
    private int swap(int index, int max) {
        if (max == 0) return 0;
        if (index >= 0) return index % max;
        return max + index % max;
    }

    // add imageView for indicator container
    private void initIndicator() {
        mIndicatorContainer.removeAllViews();
        int count = mPagerAdapter.getCount();
        for (int i = 0; i < count; i++) {
            ImageView image = new ImageView(getContext());
            image.setImageResource(mUnselectedDrawableRes);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(mIndicatorWidth, mIndicatorHeight);
            params.leftMargin = mIndicatorPadding;
            mIndicatorContainer.addView(image, params);
        }
    }

    // set first item approximate a half of Integer.MAX_VALUE
    private void initSelectedItem() {
        int startIndex = Integer.MAX_VALUE / 2;
        startIndex -= startIndex % mPagerAdapter.getCount();
        mRawViewPager.setCurrentItem(startIndex);
    }

    /**
     * Start auto circle play all view.
     */
    public void start() {
        mIsStart = true;
        play();
    }

    // auto play msg
    private Message obtainMsg() {
        return Message.obtain(mHandler, 0);
    }

    private void play() {
        if (!mIsStart) return;
        mIsPlaying = true;
        mHandler.sendMessageDelayed(obtainMsg(), mIntervalTime);
    }

    @Override
    public void autoPlay() {
        mRawViewPager.setCurrentItem(mRawViewPager.getCurrentItem() + 1);
        if (mIsPlaying)
            mHandler.sendMessageDelayed(obtainMsg(), mIntervalTime);
    }

    private void stop() {
        if (!mIsStart) return;
        mHandler.removeMessages(0);
        mIsPlaying = false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mIsStart) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    stop();
                    handleUserEvent(v, event);
                    return true;
                case MotionEvent.ACTION_UP:
                    play();
            }
        }
        return handleUserEvent(v, event);
    }

    private boolean handleUserEvent(View v, MotionEvent event) {
        return mTouchListener != null && mTouchListener.onTouch(v, event);
    }

    @Override
    public void onPageSelected(int position) {
        // update indicator and last position
        updateIndicator(position, lastPosition < position);
        lastPosition = position;

        // update page title
        if (mShowPageTitle) {
            int index = swap(position, mPagerAdapter.getCount());
            CharSequence title = mPagerAdapter.getPageTitle(index);
            if(TextUtils.isEmpty(title))
                throw new RuntimeException("Must be overloaded " +
                        "getPageTitle(int) method and can't return null.");
            mPageTitle.setText(title);
        }
        // handle user event listener
        if (mOnPageChangeListener != null)
            mOnPageChangeListener.onPageSelected(position);
    }

    public int getIntervalTime() {
        return mIntervalTime;
    }

    public void setIntervalTime(int intervalTime) {
        mIntervalTime = intervalTime;
    }

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        mOnPageChangeListener = onPageChangeListener;
    }

    public void setOnTouchListener(OnTouchListener touchListener) {
        mTouchListener = touchListener;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mOnPageChangeListener != null)
            mOnPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (mOnPageChangeListener != null)
            mOnPageChangeListener.onPageScrollStateChanged(state);
    }

    private static class AutoPlayHandler extends Handler {
        private WeakReference<AutoPlayCallback> mViewWeakRef;

        AutoPlayHandler(AutoPlayCallback handler) {
            mViewWeakRef = new WeakReference<AutoPlayCallback>(handler);
        }

        @Override
        public void handleMessage(Message msg) {
            AutoPlayCallback handler = mViewWeakRef.get();
            if (handler == null)
                return;
            handler.autoPlay();
            Log.d("tag", "mHandler running");
        }
    }

    private class RawPageAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return mPagerAdapter.isViewFromObject(view, object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            position %= mPagerAdapter.getCount();
            return mPagerAdapter.instantiateItem(container, position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            position %= mPagerAdapter.getCount();
            mPagerAdapter.destroyItem(container, position, object);
        }
    }

    private class DataObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            stop();
            updateIndicator(0, false);
            initSelectedItem();
            // May not need
            mRawAdapter.notifyDataSetChanged();
            start();
        }
    }
}

interface AutoPlayCallback {
    void autoPlay();
}
