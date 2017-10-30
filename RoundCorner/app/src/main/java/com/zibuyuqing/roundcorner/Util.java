package com.zibuyuqing.roundcorner;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

/**
 * Created by Xijun.Wang on 2017/10/28.
 */

public class Util {
    public static Point getScreenSize (Context context){
        WindowManager manager = (WindowManager)
                context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        manager.getDefaultDisplay().getSize(point);
        return point;
    }
}
