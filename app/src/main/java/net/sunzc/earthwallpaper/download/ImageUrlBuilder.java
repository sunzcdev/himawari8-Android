package net.sunzc.earthwallpaper.download;

import java.util.Arrays;

/**
 * Created by Administrator on 2016/8/5.
 */
public class ImageUrlBuilder {
    private static final String IMAGE_BASE_URL = "http://himawari8.nict.go.jp/img/D531106/";

    /**
     * level必须是1，2，4，8，16中的一个
     *
     * @param level
     * @param info
     * @return
     */
    public static String[] getImageUrl(int level, LatestInfo info) {
        int[] levels = {1, 2, 4, 8, 16};
        if (Arrays.binarySearch(levels, level) < 0) {
            throw new IllegalArgumentException("参数错误:" + level);
        }
        String[] imageUrls = new String[level * level];
        int index = 0;
        for (int column = 0; column < level; column++) {
            for (int row = 0; row < level; row++) {
                imageUrls[index] = IMAGE_BASE_URL + level + "d/550/" + info.getDateUrl() + '_' + column + '_' + row + ".png";
                index++;
            }
        }
        return imageUrls;
    }
}
