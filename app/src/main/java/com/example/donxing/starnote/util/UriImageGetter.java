package com.example.donxing.starnote.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

public class UriImageGetter implements Html.ImageGetter {

    Context context;

    public UriImageGetter(Context context) {
        this.context = context;
    }

    @Override
    public Drawable getDrawable(String s) {
        Drawable drawable = null;
        Uri uri = Uri.parse(s);
        try{
            drawable = Drawable.createFromStream(context.getContentResolver().openInputStream(uri),null);
            drawable.setBounds(0,0,2 * drawable.getIntrinsicWidth(),2 * drawable.getIntrinsicHeight());

        }catch (Exception FileNotFindException){
            Log.d("找到图片","不能根据当前Uri找到图片");
        }


        return drawable;
    }
}
