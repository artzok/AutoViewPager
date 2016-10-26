package artzok.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.art.zok.autoview.AutoViewPager;

public class MainActivity extends AppCompatActivity {
    private String[] data = {"one", "two", "three", "four", "five"};
    private PagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AutoViewPager autoViewPager = (AutoViewPager) findViewById(R.id.auto_view_pager);
        autoViewPager.start();
        mAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return data.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                TextView view = (TextView) getLayoutInflater().inflate(R.layout.pager_item, container, false);
                view.setText(data[position] + " Page");
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return "Page Title " + position;
            }
        };

        autoViewPager.setPagerAdapter(mAdapter);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < data.length; i++) {
                    data[i] = "# " + i;
                }
                mAdapter.notifyDataSetChanged();
            }
        }, 3000);
    }
}
