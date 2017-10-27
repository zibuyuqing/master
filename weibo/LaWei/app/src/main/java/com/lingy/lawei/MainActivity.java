package com.lingy.lawei;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.lingy.lawei.utils.Logger;
import com.lingy.lawei.utils.StateUtils;
import com.lingy.lawei.weibo.activity.SearchActivity;
import com.lingy.lawei.weibo.activity.SendWeiboActivity;
import com.lingy.lawei.weibo.activity.UserInfoDisplayActivity;
import com.lingy.lawei.weibo.api.WeiBoFactory;
import com.lingy.lawei.weibo.base.BaseActivity;
import com.lingy.lawei.weibo.bean.User;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

import java.util.HashMap;
import java.util.Map;

import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseActivity {

    private User authUser;
    @Override
    protected void init() {
        if(StateUtils.isNetworkAvailable(this)) {
            getUserInfo();
        }
    }

    @Override
    protected boolean canBack() {
        return true;
    }

    @OnClick(R.id.btn_to_search) void toSearch(){
        startActivity(new Intent(MainActivity.this, SearchActivity.class));
    }
    @OnClick(R.id.btn_write_weibo) void writeWeibo(){
        SendWeiboActivity.toSendNewWeiBo(this);
    }
    @OnClick(R.id.btn_user_info) void displayUserInfo(){
        UserInfoDisplayActivity.displayUserInfo(this,authUser);
    }
    @Override
    protected int providedLayoutId() {
        return R.layout.activity_main;
    }
    @OnClick(R.id.btn_at_users) void batchAtUsers(){

    }
    public void getUserInfo(){
        Oauth2AccessToken token = MyApp.getInstance().getAccessToken();
        if(token.isSessionValid()){
            String tokenStr = token.getToken();
            String uId = token.getUid();
            WeiBoFactory.getWeiBoApiSingleton().getUserInfo(getRequestMap(tokenStr,uId))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(user -> {
                        authUser = user;
                    }, this::loadError);
        }
    }

    private void loadError(Throwable throwable) {
        throwable.printStackTrace();
        showTips("加载错误");
    }
    private Map<String,String> getRequestMap(String token, String uId){
        Map<String,String> map = new HashMap<>();
        map.put("access_token",token);
        map.put("uid",uId);
        return map;
    }
}
