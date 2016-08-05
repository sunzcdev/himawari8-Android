package net.sunzc.earthwallpaper;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import net.sunzc.earthwallpaper.download.EarthSync;

/**
 * 地球同步服务，在这里定时从网上获取图片。
 * 在这里接收系统的网络变化及电池电量的通知以优化耗电量及流量
 */
public class EarthSyncService extends Service {
    private EarthSync mEarthSync;

    public EarthSyncService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mEarthSync = EarthSync.startSync(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mEarthSync.sync();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mEarthSync.stop();
    }
}
