package com.dev.eipeks.matrixplayer.global;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;

/**
 * Created by eipeks on 3/27/18.
 */

public class Utils {

    public static Bitmap getSongBitmap(String path){
        Bitmap bitmap = null;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);

        byte[] embeddedPictureBytes = retriever.getEmbeddedPicture();

        if (embeddedPictureBytes != null){
            bitmap = BitmapFactory.decodeByteArray(embeddedPictureBytes, 0, embeddedPictureBytes.length);
        }

        return bitmap;
    }

    public static Uri getImageUri(Context context, Bitmap bitmap){
        if (bitmap == null){
            return null;
        }
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),
                bitmap, "Album art", null);

        return Uri.parse(path);
    }

}
