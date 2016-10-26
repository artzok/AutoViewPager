package artzok.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.art.zok.autoview.AutoViewPager;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find control
        AutoViewPager autoViewPager = (AutoViewPager) findViewById(R.id.auto_view_pager);
        autoViewPager.start();

        // create adapter for view pager
        mAdapter = new MyAdapter();
        autoViewPager.setPagerAdapter(mAdapter);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String[] data = {"one", "two", "three"};
                mAdapter.setDataAndRefresh(Arrays.asList(data));
            }
        }, 5000);
    }

    private class MyAdapter extends PagerAdapter {

        private List<String> data;

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return data == null ? " " : data.get(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (data == null) return null;
            TextView textView = new TextView(getApplicationContext());
            textView.setText(data.get(position));
            textView.setTextSize(48);
            textView.setTextColor(Color.MAGENTA);
            textView.setGravity(Gravity.CENTER);
            container.addView(textView);
            return textView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        public void setDataAndRefresh(List<String> data) {
            this.data = data;
            notifyDataSetChanged();
        }
    }
}
