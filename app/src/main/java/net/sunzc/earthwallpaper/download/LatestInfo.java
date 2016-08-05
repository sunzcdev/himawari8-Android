package net.sunzc.earthwallpaper.download;

/**
 * Created by Administrator on 2016/8/5.
 */
class LatestInfo {
    private String date, file;

    public LatestInfo(String date, String file) {
        this.date = date;
        this.file = file;
    }

    public String getDateUrl() {
        String[] dates = date.split(" ");
        String[] ymd = dates[0].split("-");
        String[] hms = dates[1].split(":");
        char pathSpeator = '/';
        StringBuilder builder = new StringBuilder();
        for (String s : ymd) {
            builder.append(s).append(pathSpeator);
        }
        for (String s : hms) {
            builder.append(s);
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return "LatestInfo{" +
                "date='" + date + '\'' +
                ", file='" + file + '\'' +
                '}';
    }
}
