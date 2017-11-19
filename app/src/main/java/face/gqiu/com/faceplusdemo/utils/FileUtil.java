package face.gqiu.com.faceplusdemo.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class FileUtil {
    public static final String TEMP_PIC = "temp.jpg";
    public static final String COMPRESS_PIC = "compress.jpg";

    private static String getDiskCacheDir(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            if (context.getExternalCacheDir() == null) {
                cachePath = context.getCacheDir().getPath();
            } else {
                cachePath = context.getExternalCacheDir().getPath();
            }
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }


    private static String getFilePath(String path, boolean delete) {
        File file = new File(path);
        if (file.exists()) {
            if (delete) {
                file.delete();
            } else {
                return path;
            }
        }
        try {
            boolean result = file.createNewFile();
            if (result) {
                return path;
            } else {
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getTempPic(Context context) {
        String path = getDiskCacheDir(context) + File.separator + TEMP_PIC;
        return getFilePath(path, true);
    }


    public static String getCompressPic(Context context) {
        String path = getDiskCacheDir(context) + File.separator + COMPRESS_PIC;
        return getFilePath(path, true);
    }

}
