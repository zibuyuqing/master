package com.zibuyuqing.roundcorner;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<CornerView> corners = new ArrayList<>();
    WindowManager manager;
    private boolean added = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = (WindowManager) this.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        findViewById(R.id.btn_add_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!added) {
                    addCornerViews();
                    added = true;
                }
            }
        });
        findViewById(R.id.btn_remove_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove();
            }
        });
    }

    private void remove() {
        for (CornerView view : corners) {
            manager.removeView(view);
        }
        corners.clear();
        added = false;
    }

    private void addCornerViews() {
        WindowManager.LayoutParams param = new WindowManager.LayoutParams();

        param.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;     // 系统提示类型,重要
        param.format = 1;
        param.flags = param.flags | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        param.alpha = 1.0f;
        param.x = 0;
        param.y = 0;
        param.width = Util.getScreenSize(this).x;
        param.height = Util.getScreenSize(this).y;
        for (int i = 0; i < 4; i++) {
            CornerView corner = new CornerView(this);
            corner.setColor(getColor(R.color.black));
            switch (i) {
                case 0:
                    param.gravity = Gravity.TOP | Gravity.LEFT;
                    break;
                case 1:
                    param.gravity = Gravity.TOP | Gravity.RIGHT;
                    break;
                case 2:
                    param.gravity = Gravity.BOTTOM | Gravity.LEFT;
                    break;
                case 3:
                    param.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                    break;
            }
            corner.setLocation(param.gravity);
            if (!corners.contains(corner)) {
                corners.add(corner);
                manager.addView(corner, param);
            }
        }
    }
}
