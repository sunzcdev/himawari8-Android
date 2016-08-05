package net.sunzc.earthwallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SyncBroadcastListener extends BroadcastReceiver {
    public SyncBroadcastListener() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //TODO 当电量低于15%的时候暂停同步
        //TODO 当使用移动数据的时候，调整更新的频率
        //TODO 当系统开机的时候开启此服务
        //TODO 当充电开始的时候开启此服务
        //TODO 当wifi连接的时候开启此服务
    }
}
