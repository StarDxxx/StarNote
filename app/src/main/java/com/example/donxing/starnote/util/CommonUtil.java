package com.example.donxing.starnote.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtil {
    public static String date2string(Date date) {
        String strDate = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        strDate = sdf.format(date);
        return strDate;
    }
}
