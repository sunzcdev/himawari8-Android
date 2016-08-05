package net.sunzc.earthwallpaper;

import android.app.Application;
import org.xutils.x;

/**
 * Created by Administrator on 2016/8/5.
 */
public class EarthApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
    }
}
