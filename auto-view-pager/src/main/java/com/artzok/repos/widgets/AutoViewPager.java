package com.artzok.repos.widgets;


import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.lang.ref.WeakReference;


public class AutoViewPager extends RelativeLayout implements AutoPlayCallback, OnPageChangeListener, OnTouchListener {
    private SafeViewPager mRawViewPager;
    private RawPageAdapter mRawAdapter;
    private LinearLayout mIndicatorContainer;
    private TextView mPageTitle;
    private PagerAdapter mPagerAdapter;
    private Handler mHandler;
    private boolean mIsStart;
    private boolean mIsPlaying;
    private int lastPosition;
    private int mIntervalTime;
    private int mIndicatorWidth;
    private int mIndicatorHeight;
    private int mIndicatorPadding;
    private boolean mShowPageTitle;
    private int mSelectedDrawableRes;
    private int mUnselectedDrawableRes;
    private float mPageTitleFontSize;
    private int mPageTitleFontColor;
    private int mPageTitleTextStyle;
    private int mIndicatorContainerId;
    private int mPageTitleTextViewId;
    private SwipeRefreshLayout swipeWidget;
    private int mStartIndex = 0;
    private ImageView mShadowImage;
    private float mHorAsp;
    private float mVerAsp;
    private boolean mEnableShadow;
    private OnPageChangeListener mPageChangeListener;

    public AutoViewPager(Context context) {
        this(context, null);
    }

    public AutoViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initAttrs(context, attrs);
        this.initView(context);
        this.initListener();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float size = TypedValue.applyDimension(1, 8.0F, dm);
        float fontSize = TypedValue.applyDimension(2, 16.0F, dm);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AutoViewPager);
        this.mSelectedDrawableRes = array.getResourceId(R.styleable.AutoViewPager_selectedDrawable, R.drawable.default_indicated_shape);
        this.mUnselectedDrawableRes = array.getResourceId(R.styleable.AutoViewPager_unselectedDrawable, R.drawable.default_unindicated_shape);
        this.mIntervalTime = array.getInt(R.styleable.AutoViewPager_intervalTime, 2000);
        this.mIndicatorWidth = (int) array.getDimension(R.styleable.AutoViewPager_indicatorWidth, size);
        this.mIndicatorHeight = (int) array.getDimension(R.styleable.AutoViewPager_indicatorHeight, size);
        mHorAsp = array.getFloat(R.styleable.AutoViewPager_unselectedHorAsp, .4f);
        mVerAsp = array.getFloat(R.styleable.AutoViewPager_unselectedVerAsp, .4f);
        this.mIndicatorPadding = (int) array.getDimension(R.styleable.AutoViewPager_indicatorPadding, (float) (this.mIndicatorWidth / 2));
        this.mShowPageTitle = array.getBoolean(R.styleable.AutoViewPager_showPageTitle, false);
        this.mPageTitleFontSize = array.getDimension(R.styleable.AutoViewPager_pageTitleFontSize, fontSize);
        this.mPageTitleFontColor = array.getColor(R.styleable.AutoViewPager_pageTitleFontColor, -1);
        this.mPageTitleTextStyle = array.getInt(R.styleable.AutoViewPager_pageTitleTextStyle, 0);
        this.mIndicatorContainerId = array.getResourceId(R.styleable.AutoViewPager_indicatorContainerID, -1);
        this.mPageTitleTextViewId = array.getResourceId(R.styleable.AutoViewPager_pageTitleTextViewID, -1);
        this.mEnableShadow = array.getBoolean(R.styleable.AutoViewPager_enableShadow, false);
        array.recycle();
    }

    public void setPageChangeListener(OnPageChangeListener pageChangeListener) {
        mPageChangeListener = pageChangeListener;
    }

    public void forbidTouch() {
        if (mRawViewPager != null)
            mRawViewPager.forbidSlide();
    }


    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.auto_view_pager_layout, this, true);
        this.mRawViewPager = (SafeViewPager) view.findViewById(R.id.raw_view_pager);
        this.mRawViewPager.setOnTouchListener(this);
        this.mIndicatorContainer = (LinearLayout) this.findViewById(R.id.indicator_container);
        this.mPageTitle = (TextView) this.findViewById(R.id.page_title);
        this.mPageTitle.setTextSize(0, this.mPageTitleFontSize);
        this.mPageTitle.setTextColor(this.mPageTitleFontColor);
        Typeface typeface = this.mPageTitle.getTypeface();
        this.mPageTitle.setTypeface(typeface, this.mPageTitleTextStyle);
        mShadowImage = (ImageView) view.findViewById(R.id.shadow);
        enableShadow(mEnableShadow);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        if (this.mIndicatorContainerId != -1) {
            this.mIndicatorContainer.setVisibility(GONE);
            this.mIndicatorContainer = (LinearLayout) this.findViewById(this.mIndicatorContainerId);
        }

        if (this.mPageTitleTextViewId != -1) {
            this.mPageTitle.setVisibility(GONE);
            this.mPageTitle = (TextView) this.findViewById(this.mPageTitleTextViewId);
        }
    }

    private void initState() {
        this.mIsPlaying = true;
        this.mIsStart = false;
    }

    private void initListener() {
        this.mRawViewPager.setOnPageChangeListener(this);
    }


    public void setPagerAdapter(PagerAdapter adapter) {
        if (adapter == null) {
            throw new RuntimeException("Adapter cannot be null.");
        } else if (this.mPagerAdapter != null) {
            throw new RuntimeException("Adapter has already exist!");
        } else {
            this.mPagerAdapter = adapter;
            this.mPagerAdapter.registerDataSetObserver(new DataObserver());
            this.mRawAdapter = new RawPageAdapter();
            this.mPagerAdapter.notifyDataSetChanged();
        }
    }

    public void setPagerAdapter(PagerAdapter adapter, int startIndex) {
        this.mStartIndex = startIndex;
        setPagerAdapter(adapter);
    }

    /**
     * @param index 真实索引
     */
    private void updateIndicator(int index) {
        int count = this.mPagerAdapter.getCount();
        if (count != this.mIndicatorContainer.getChildCount()) {
            this.initIndicator();
        }
        if (count > 0) {
            ImageView last = (ImageView) this.mIndicatorContainer.getChildAt(this.lastPosition);
            ImageView now = (ImageView) this.mIndicatorContainer.getChildAt(index);
            if (last != null && now != null) {
                last.setImageResource(this.mUnselectedDrawableRes);
                now.setImageResource(this.mSelectedDrawableRes);
                updateLayoutSize(last, true);
                updateLayoutSize(now, false);
            }
        }
    }

    private void updateLayoutSize(ImageView image, boolean small) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) image.getLayoutParams();
        if (params == null) {
            params = new LinearLayout.LayoutParams(0, 0);
        }

        params.width = (int) (mIndicatorWidth * (small ? mHorAsp : 1));
        params.height = (int) (mIndicatorHeight * (small ? mVerAsp : 1));
        image.setPadding(0, 0, 0, 0);
        params.leftMargin = mIndicatorPadding / 2;
        params.rightMargin = mIndicatorPadding / 2;
        image.setLayoutParams(params);
        if (image.getParent() != null)
            image.requestLayout();
    }

    /**
     * 根据虚拟的索引获得数据索引
     */
    private int swap(int index) {
        int fake = mRawAdapter.getCount();
        int real = mPagerAdapter.getCount();
        if (index == 0) index = real - 1;
        else if (index == fake - 1) index = 0;
        else index -= 1;
        return index;
    }

    private int reSwap(int index) {
        return index + 1;
    }

    private void initIndicator() {
        this.mIndicatorContainer.removeAllViews();
        int count = this.mPagerAdapter.getCount();
        for (int i = 0; i < count; ++i) {
            ImageView image = new ImageView(this.getContext());
            image.setImageResource(this.mUnselectedDrawableRes);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            updateLayoutSize(image, true);
            this.mIndicatorContainer.addView(image);
        }
    }

    private void initSelectedItem() {
        this.mRawViewPager.setCurrentItem(mStartIndex + 1, false);
    }

    public void start() {
        if (mHandler == null) {
            this.mHandler = new AutoPlayHandler(this);
            initState();
        }
        this.mIsStart = true;
        if (this.mPagerAdapter != null && this.mPagerAdapter.getCount() > 0) {
            this.play();
        }

    }

    private Message obtainMsg() {
        return Message.obtain(this.mHandler, 0);
    }

    public void play() {
        if (this.mIsStart && !this.mIsPlaying) {
            this.mIsPlaying = true;
            this.mHandler.sendMessageDelayed(this.obtainMsg(), (long) this.mIntervalTime);
        }
    }

    @Override
    public void autoPlay() {
        if (mRawAdapter.getCount() != mPagerAdapter.getCount())
            this.mRawAdapter.notifyDataSetChanged();
        this.mRawViewPager.setCurrentItem(this.mRawViewPager.getCurrentItem() + 1, true);
        if (this.mIsPlaying)
            this.mHandler.sendMessageDelayed(this.obtainMsg(), (long) this.mIntervalTime);
    }

    public void pause() {
        if (this.mIsStart && this.mIsPlaying) {
            this.mHandler.removeMessages(0);
            this.mIsPlaying = false;
        }
    }

    public void setCurrentItem(int pos) {
        pause();
        pos = reSwap(pos);
        mRawViewPager.setCurrentItem(pos);
        play();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (this.mIsStart) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    this.play();
                    if (swipeWidget != null)
                        swipeWidget.setEnabled(true);
                    break;
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    this.pause();
                    if (swipeWidget != null)
                        swipeWidget.setEnabled(false);
                    break;
            }
        }
        return false;
    }

    public void onPageSelected(int pos) {
        int position = swap(pos);
        if (mPageChangeListener != null)
            mPageChangeListener.onPageSelected(position);
        this.updateIndicator(position);
        this.lastPosition = position;
        if (this.mShowPageTitle) {
            CharSequence title = this.mPagerAdapter.getPageTitle(position);
            if (TextUtils.isEmpty(title))
                throw new RuntimeException("Must be overloaded getPageTitle(int) method and can't return null or empty.");
            this.mPageTitle.setText(title);
        }
    }

    public int getIntervalTime() {
        return this.mIntervalTime;
    }

    public void setIntervalTime(int intervalTime) {
        this.mIntervalTime = intervalTime;
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mPageChangeListener != null)
            mPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    public void onPageScrollStateChanged(int state) {
        if (mPageChangeListener != null) mPageChangeListener.onPageScrollStateChanged(state);
        if (state == ViewPager.SCROLL_STATE_IDLE || state == ViewPager.SCROLL_STATE_DRAGGING) {
            int pos = mRawViewPager.getCurrentItem();
            if (pos == 0) {
                mRawViewPager.setCurrentItem(mRawAdapter.getCount() - 2, false);
            } else if (pos == mRawAdapter.getCount() - 1) {
                mRawViewPager.setCurrentItem(1, false);
            }
        }
    }

    public void setRelateSwipe(SwipeRefreshLayout layout) {
        this.swipeWidget = layout;
    }

    public void addOnPageChangeListener(final OnPageChangeListener simpleOnPageChangeListener) {
        mRawViewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                simpleOnPageChangeListener.onPageScrolled(swap(position), positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                simpleOnPageChangeListener.onPageSelected(swap(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                simpleOnPageChangeListener.onPageScrollStateChanged(state);
            }
        });
    }

    public void enableShadow(boolean enable) {
        mShadowImage.setVisibility(enable ? VISIBLE : GONE);
    }

    private class DataObserver extends DataSetObserver {
        private DataObserver() {
        }

        public void onChanged() {
            if (mPagerAdapter == null)
                throw new RuntimeException("Adapter can't be null.");

            if (mRawViewPager.getAdapter() == null) {
                mRawViewPager.setAdapter(mRawAdapter);
            }

            pause();
            mRawAdapter.notifyDataSetChanged();
            initSelectedItem();
            updateIndicator(mStartIndex);
            play();
        }

        @Override
        public void onInvalidated() {
            onChanged();
        }
    }

    private class RawPageAdapter extends PagerAdapter {

        private RawPageAdapter() {
        }

        public int getCount() {
            if (mPagerAdapter == null || mPagerAdapter.getCount() == 0) return 0;
            return mPagerAdapter.getCount() + 2; // first and last
        }

        public boolean isViewFromObject(View view, Object object) {
            return mPagerAdapter.isViewFromObject(view, object);
        }

        public Object instantiateItem(ViewGroup container, int position) {
            position = swap(position);
            if (mPagerAdapter != null && position >= 0 && position < mPagerAdapter.getCount()) {
                return mPagerAdapter.instantiateItem(container, position);
            } else
                throw new RuntimeException("error: " + (mPagerAdapter == null) + ", pos:" +
                        position + ", size:" + (mPagerAdapter == null ? -1 : mPagerAdapter.getCount()));
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            position = swap(position);
            mPagerAdapter.destroyItem(container, position, object);
        }

        public int getItemPosition(Object object) {
            return -2;
        }
    }

    private static class AutoPlayHandler extends Handler {
        private WeakReference<AutoPlayCallback> mViewWeakRef;

        AutoPlayHandler(AutoPlayCallback handler) {
            this.mViewWeakRef = new WeakReference<>(handler);
        }

        public void handleMessage(Message msg) {
            AutoPlayCallback callback = this.mViewWeakRef.get();
            if (callback != null) callback.autoPlay();
        }
    }
}

