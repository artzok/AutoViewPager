package com.artzok.repos;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.artzok.repos.widgets.AutoViewPager;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private AutoViewPager mAutoViewPager;
    private Random mRandom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAutoViewPager = findViewById(R.id.auto_view_pager);
        mRandom = new Random();
        mAutoViewPager.setPagerAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 10;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            public Object instantiateItem(ViewGroup container, int position) {
                ImageView item = new ImageView(container.getContext());
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                item.setLayoutParams(params);
                int c = mRandom.nextInt(255);
                int color = (c << 16) | (c << 8) | c | Color.BLACK;
                item.setBackgroundColor(color);
                container.addView(item);
                return item;
            }

            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(container);
            }
        });
        mAutoViewPager.start();
    }
}
