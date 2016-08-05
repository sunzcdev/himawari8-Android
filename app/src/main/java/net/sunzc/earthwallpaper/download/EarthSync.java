package net.sunzc.earthwallpaper.download;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;


/**
 * 这是处理从网络取图片的管理类
 * 由于网络的图片，每隔十分钟更新一次，因此，取图片的时候，以网络时间为准
 * Created by Administrator on 2016/8/5.
 */
public class EarthSync {
    public static final int SAVE_BATTERY = 0x10;
    public static final int NORMAL = 0x00;
    public static final int HIGH_EFFICIENCY = 0x00;
    private static final String TAG = "EarthSync";
    public static String imageDirPath;
    private static EarthSync sync;
    private final Context mContext;
    private WallpaperHandler mWallpaperHandler;
    private HandlerThread syncThread;
    private boolean mQuit = true;
    private BroadcastReceiver mSystemTimer;
    private Handler mSyncHandler;

    private EarthSync(Context context) {
        startSyncThread();
        this.mContext = context;
        File rootDir = context.getFilesDir();
        File imageDir = new File(rootDir, "image");
        if (!imageDir.exists()) {
            imageDir.mkdir();
        }
        imageDirPath = imageDir.getAbsolutePath();
        mSystemTimer = new SystemTimer();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SystemTimer.TIME_UP);
        context.registerReceiver(mSystemTimer, filter);
    }

    public static EarthSync startSync(Context context) {
        if (sync == null || sync.mQuit) {
            sync = new EarthSync(context);
        }
        return sync;
    }

    public void stop() {
        mContext.unregisterReceiver(mSystemTimer);
        syncThread.quit();
        mQuit = true;
    }

    private void startSyncThread() {
        syncThread = new HandlerThread(EarthSync.class.getName());
        syncThread.start();
        mWallpaperHandler = new WallpaperHandler(this);
        mSyncHandler = new Handler(syncThread.getLooper());
    }

    public void sync() {
        mSyncHandler.post(new SyncWallpaperRunnable(mWallpaperHandler));
        Log.i(TAG, "开始同步");
    }

    public void pauseSync() {

    }

    public void switchSyncMode() {

    }

    /**
     * 同步线程跟主线程交互
     */
    private static class WallpaperHandler extends Handler {


        static final int SYNC_FINISHED = 1;
        private final EarthSync mEarthSync;

        WallpaperHandler(EarthSync earthSync) {
            super(Looper.getMainLooper());
            this.mEarthSync = earthSync;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SYNC_FINISHED:
                    //TODO 在这里设置壁纸
                    Log.i(TAG, "开始设置壁纸");
                    setWallpaper((Bitmap) msg.obj);
                    break;
                default:
                    break;
            }
        }

        private void setWallpaper(Bitmap data) {
            WallpaperManager manager = WallpaperManager.getInstance(mEarthSync.mContext);
            try {
                manager.setBitmap(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Created by Administrator on 2016/8/5.
     */
    private static class SyncWallpaperRunnable implements Runnable {
        private static final String LATEST_URL = "http://himawari8-dl.nict.go.jp/himawari8/img/D531106/latest.json";
        private static final String TAG = "SyncWallpaperRunnable";
        private static final int LEVEL = 2;
        private final WallpaperHandler mWallpaperHandler;
        private int retryTime;

        SyncWallpaperRunnable(WallpaperHandler wallpaperHandler) {
            this.mWallpaperHandler = wallpaperHandler;
        }

        @Override
        public void run() {
            //TODO 这里写下载图片的方法
            Log.i(TAG, "开始下载图片更新信息");
            try {

                String[] urls = fetchImageUrl();
                if (urls == null) {
                    Log.e(TAG, "获取图片url失败");
                    return;
                }
                String[] imageFilePaths = new String[urls.length];
                Log.i(TAG, "准备下载图片" + Arrays.toString(urls));
                for (int index = 0; index < urls.length; index++) {
                    imageFilePaths[index] = imageDirPath + "/" + index + ".png";
                    downloadImage(urls[index], imageFilePaths[index]);
                    Log.i(TAG, urls[index] + "图片下载一张" + imageFilePaths[index]);
                }
                Bitmap wallpaperBitmap = collageImage(imageFilePaths);
                Message msg = Message.obtain(mWallpaperHandler, WallpaperHandler.SYNC_FINISHED, wallpaperBitmap);
                mWallpaperHandler.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 合并图片成一张壁纸
         *
         * @param imageFilePaths
         * @return
         */
        private Bitmap collageImage(String[] imageFilePaths) {
//            if (LEVEL == 1) {
//                BitmapFactory.Options option = new BitmapFactory.Options();
//                option.inJustDecodeBounds = false;
//                return BitmapFactory.decodeFile(imageFilePaths[0], option);
//            }
            Bitmap collageBitmap = Bitmap.createBitmap(LEVEL * 550, LEVEL * 550, Bitmap.Config.ARGB_4444);
            Canvas canvas = new Canvas(collageBitmap);
            int index = 0;
            Bitmap bitmap;
            for (int column = 0; column < LEVEL; column++) {
                for (int row = 0; row < LEVEL; row++) {
                    BitmapFactory.Options option = new BitmapFactory.Options();
                    option.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeFile(imageFilePaths[index++], option);
                    canvas.drawBitmap(bitmap, column * 550, row * 550, null);
                }
            }
            return collageBitmap;
        }

        /**
         * {"date":"2016-08-05 04:00:00","file":"PI_H08_20160805_0400_TRC_FLDK_R10_PGPFD.png"}
         *
         * @return
         * @throws IOException
         */
        private String[] fetchImageUrl() throws IOException {
            URL url = new URL(LATEST_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5 * 1000);
            int respCode = connection.getResponseCode();
            switch (respCode) {
                case 200:
                    InputStream inputstream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
                    String line = reader.readLine();
                    try {
                        JSONObject json = new JSONObject(line);
                        LatestInfo latestInfo = new LatestInfo(
                                json.optString("date"),
                                json.optString("file")
                        );
                        Log.i(TAG, "获取图片更新信息成功" + latestInfo.toString());
                        return ImageUrlBuilder.getImageUrl(2, latestInfo);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                default:
                    Log.e(TAG, "连接错误" + connection.getResponseMessage());
                    return null;
            }
        }

        private void downloadImage(String downloadUrl, String savePath) {
            HttpURLConnection connection;
            InputStream inputStream = null;
            BufferedInputStream bis = null;
            FileOutputStream fos = null;
            try {
                URL url = new URL(downloadUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5 * 1000);
                connection.setRequestMethod("GET");
                int respCode = connection.getResponseCode();
                if (respCode != 200) {
                    if (retryTime < 3) {
                        downloadImage(downloadUrl, savePath);
                        retryTime++;
                    } else {
                        return;
                    }
                }
                inputStream = connection.getInputStream();
                fos = new FileOutputStream(new File(savePath));
                bis = new BufferedInputStream(inputStream);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = bis.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            } catch (IOException e) {
                Log.e(TAG, "下载失败:" + e.getMessage());
                if (retryTime < 3) {
                    downloadImage(downloadUrl, savePath);
                    retryTime++;
                }
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                    if (bis != null) {
                        bis.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 使用广播的形式来接收系统的时间事件，以此来形成心跳
     */
    private class SystemTimer extends BroadcastReceiver {

        static final String TIME_UP = "net.sunzc.earthwallpaper/SystemTimer.TimeUp";

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case TIME_UP:
                    //TODO 在这里控制频率
                    if (!mQuit)
                        sync();
                    break;
                default:
                    break;
            }
        }
    }
}
