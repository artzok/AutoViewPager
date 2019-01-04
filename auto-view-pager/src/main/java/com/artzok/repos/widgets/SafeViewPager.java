package com.artzok.repos.widgets;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class SafeViewPager extends ViewPager {
    public SafeViewPager(Context context) {
        super(context);
    }

    public SafeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /***
     * -avoid trigger ViewPager multi-point touch bug-
     ***/
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            if (forbidSlide) return false;
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /***
     * -avoid trigger ViewPager multi-point touch bug-
     ***/
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return !forbidSlide && super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean forbidSlide;


    public void forbidSlide() {
        forbidSlide = true;
    }
}