package ru.ifmo.droid2016.korchagin.multicheckin.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Random;

/**
 * Created by ME on 20.12.2016.
 */

public class BitmapFileUtil {

    private static final int TAG_LENGTH = 20;

    private static final String filePrefix = "img";

    private static final String fileSuffix = ".jpg";

    public static File getTempImageFile(Context context){
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File tempFile = null;
        try {
            tempFile = File.createTempFile(filePrefix, fileSuffix, storageDir);
        } catch (IOException e) {
            return null;
        }
        tempFile.deleteOnExit();
        return tempFile;
    }

    public static String writeToCacheAndGivePath(Uri uri, Context context){
        try {
            Bitmap image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            return writeToCacheAndGivePath(image);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String writeToCacheAndGivePath(Bitmap image){

        boolean result = false;
        File tempFile = null;
        FileOutputStream fos = null;
        try {
            tempFile = File.createTempFile("img", ".jpg");
            fos = new FileOutputStream(tempFile);
            result = image.compress(Bitmap.CompressFormat.JPEG, 92, fos);
        } catch (IOException e){
        } finally {
            if(fos != null)
                try{
                    fos.close();
                }  catch (IOException e){}
        }
        if(result){
            return tempFile.toString();
        } else {
            return null;
        }
    }
    public static File getFileFromPath(String path){
        return new File(path);
    }

    public static Bitmap getFromPath(String path){
        return BitmapFactory.decodeFile(path);
    }

    public static void clearCacheFromImages(){
        try {
            File dir = File.createTempFile("dummy", ".dm").getParentFile();
            for (File f : dir.listFiles(new TempImageFilter())
                 ) {
                f.delete();
            }
        } catch (IOException e) {
            // silent
        }
    }

    private static class TempImageFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return (name.startsWith(filePrefix) && name.endsWith(fileSuffix));
        }
    }

}
