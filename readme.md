# Introduction
---
This project is a simple custom View that can automatically cycle play Image or View, use the ViewPager & Handler implementation, the basic features include:
 1. Infinite cycle switch
 2. automatically cycle play

# Usage
---
## Add the dependency

1. Add it in your root build.gradle at the end of repositories:
    ```
    allprojects {
        repositories {
            ...
            maven { url "https://jitpack.io" }
        }
    }
    ```

2. Add the dependency：
    ```
	dependencies {
	        compile 'com.github.artzok:AutoViewPager:v0.1.1'
	}
    ```

## Simple Usage

1. New a `Activity` page, and add `AutoViewPager` view in layout file：
    ```xml
    <com.art.zok.autoview.AutoViewPager
        android:id="@+id/auto_view_pager"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:indicatorWidth="5dp"
        app:indicatorHeight="5dp"
        app:indicatorPadding="2dp"
        app:intervalTime="2000"
        app:selectedDrawable="@mipmap/indicator_normal"
        app:unselectedDrawable="@mipmap/indicator_selected"/>
    ```
    This project provides the following several kinds of custom properties:
    * `indicatorWidth`: Indicator width, default 8dp.
    * `indicatorHeight`: Indicator height, default 8dp.
    * `indicatorPadding`: The spacing between indicator, default value is equal to half the width of indicator.
    * `intervalTime`:  Time interval of automatically cycle play, default 2000ms.
    * `selectedDrawable`: The Drawable resource of current selected indicator, default is white color fill effect.
    * `unselectedDrawable`: The Drawable resource of all unselected indicator, default is gray color fill effect.
    * `showPageTitle`: Show the title or not, default is not showing, need override `getPageTitle` method of adapter and can't return null.
    * `pageTitleFontSize`: Title font size, default 16sp.
    * `pageTitleFontColor`: Title font color, default is white.
    * `pageTitleTextStyle`: Title text style, Optional values include: `normal`, `bold`, `italic`, `bold_italic`，default is `normal`.
    * `indicatorContainerID`:You can set a LinearLayout container for indicator user this attribute.
    * `pageTitleTextViewID`: You can set a TextView use this attribute that use show Page Title.

2. Set adapter for `AutoViewPager` in `Activity`:
    ```java
      public class MainActivity extends AppCompatActivity {
          @Override
          protected void onCreate(Bundle savedInstanceState) {
              ...
              AutoViewPager autoViewPager = (AutoViewPager) findViewById(R.id.auto_view_pager);

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
          }
      }
    ```

3. You can see this is no difference with original `ViewPager` in usage. Now, You have been implemented an infinite cycle switch, if you want to start automatically play, just call the following method:
    ```java
    autoViewPager.start();
    ```

    You can also set touch events listener and page switch event listener for `AutoViewPager`, just like the original `ViewPager`:
    ```java
     autoViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
               ...
                return false;
            }
        });

     autoViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d("tag", "onPageScrolled");
            }
            ...
        });
    ```
4. If you want custom position of indicator and page title, you can :
    ```xml
    <RelativeLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <com.art.zok.autoview.AutoViewPager
            android:background="@color/colorPrimary"
            android:id="@+id/auto_view_pager"
            android:layout_width="match_parent"
            android:layout_height="200dp"

            app:indicatorHeight="10dp"
            app:indicatorWidth="10dp"
            app:indicatorPadding="10dp"

            app:indicatorContainerID="@+id/indicators"
            app:pageTitleTextViewID="@+id/title"

            app:intervalTime="2000"
            app:showPageTitle="true">

            <TextView
                android:id="@id/title"
                android:gravity="center"
                android:textSize="20sp"
                android:layout_marginTop="10dp"
                android:layout_marginLeft="10dp"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textColor="@color/colorAccent"/>

            <LinearLayout
                android:id="@id/indicators"
                android:layout_marginBottom="10dp"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentBottom="true"
                android:layout_centerHorizontal="true"
                android:orientation="horizontal"/>
        </com.art.zok.autoview.AutoViewPager>
    </RelativeLayout>
    ```
    I changed super class of `AutoViewPager` to RelativeLayout, so you can add some other layout in AutoViewPager, also you
    can define pos of page title or indicator by use `indicatorContainerID` and `pageTitleTextViewID` attributes.

# Show
----
1. Just infinite cycle switch:

  ![](arts/static.gif)

2. Start infinite cycle play:

  ![](arts/auto.gif)

# NEW & FIX
---
1. add `notifyDataSetChanged` implement
2. add title
3. fix bug: Can't update UI when data set changed
4. add title text style control
5. fix bug: Crash when data set is empty