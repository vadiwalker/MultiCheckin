package ru.ifmo.droid2016.korchagin.multicheckin.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Created by ME on 20.12.2016.
 */

public class BitmapFileUtil {

    private static final int TAG_LENGTH = 20;

    public static File getTempImageFile(Context context){
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File tempFile = null;
        try {
            tempFile = File.createTempFile("img", ".png", storageDir);
        } catch (IOException e) {
            return null;
        }
        tempFile.deleteOnExit();
        return tempFile;
    }

    public static String writeToCacheAndGivePath(Bitmap image){

        boolean result = false;
        File tempFile = null;
        FileOutputStream fos = null;
        try {
            tempFile = File.createTempFile("img", ".png");
            fos = new FileOutputStream(tempFile);
            result = image.compress(Bitmap.CompressFormat.PNG, 100, fos);
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
}
