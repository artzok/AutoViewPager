# Introduction
---
This project is a simple custom View that can automatically cycle play Image or View, use the ViewPager & Handler implementation, the basic features include:
 1. Infinite cycle switch
 2. automatically cycle play

# usage
---
1. Compile the `library` module, then copy the `library-release.aar` file to the lib folder. In module `build.gradle` file add：
    ```
    compile(name: 'library-release', ext: 'aar')
    ```
    In project `build.gradle` file add：
    ```
    allprojects {
      repositories {
          jcenter()
          flatDir {
              dirs 'libs'
          }
      }
    }
    ```
  
2. New a `Activity` page, and add `AutoViewPager` view in layout file：
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
    * `showPageTitle`: Show the title or not, default show, need override `getPageTitle` method of adapter and can't return null.
    * `pageTitleFontSize`: Title font size, default 16sp.
    * `pageTitleFontColor`: Title font color, default is white.
    * `pageTitleTextStyle`: Title text style, Optional values include: `normal`, `bold`, `italic`, `bold_italic`，default is `normal`.

3. Set adapter for `AutoViewPager` in `Activity`:
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

    You can see this is no difference with original `ViewPager` in usage. Now, You have been implemented an infinite cycle switch, if you want to start automatically play, just call the following method:
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
