package artzok.myapplication;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * @author 赵坤
 * @email artzok@163.com
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
