package artzok.myapplication;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.art.zok.autoview.AutoViewPager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AutoViewPager autoViewPager = (AutoViewPager) findViewById(R.id.auto_view_pager);
        //  autoViewPager.start();

        autoViewPager.setPagerAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 10;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                TextView view = (TextView) getLayoutInflater().inflate(R.layout.pager_item, container, false);
                view.setText("Pager " + position);
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });

        autoViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("tag", "Event:" + event.getAction());
                return false;
            }
        });

        autoViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d("tag", "onPageScrolled");
            }

            @Override
            public void onPageSelected(int position) {
                Log.d("tag", "onPageSelected");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d("tag", "onPageScrollStateChanged");
            }
        });
    }
}
