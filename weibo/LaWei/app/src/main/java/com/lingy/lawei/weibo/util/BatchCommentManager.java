package com.lingy.lawei.weibo.util;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.lingy.lawei.MyApp;
import com.lingy.lawei.utils.Logger;
import com.lingy.lawei.weibo.api.WeiBoApi;
import com.lingy.lawei.weibo.api.WeiBoFactory;
import com.lingy.lawei.weibo.bean.User;
import com.lingy.lawei.weibo.bean.UserList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Xijun.Wang on 2017/10/26.
 */

public class BatchCommentManager {
    private static final int DURATION = 5000;
    public static final int AT_USER_TYPE_NORMAL = 0;
    public static final int AT_USER_TYPE_FANS = 1;
    public static final int AT_USER_TYPE_BY_TAG = 2;
    private int perCount = 10;
    private boolean hasMoreUsers = true;
    private int page = 1;
    private int currentCount = 0;
    private boolean stop = false;
    private int requireCount;
    private MyApp app = MyApp.getInstance();
    private String  token = app.getAccessTokenHack();
    private String uid = app.getMyId();
    private WeiBoApi weiBoApi = WeiBoFactory.getWeiBoApiSingleton();
    private Handler handler = new Handler(Looper.getMainLooper());
    private int atUserType = AT_USER_TYPE_NORMAL;
    private String weiBoId;
    private boolean normalComment = true;
    private OnRequestStateChangedListener listener;
    public BatchCommentManager(String weiBoId){
        this.weiBoId = weiBoId;
    }
    public BatchCommentManager(int requireCount,String weiBoId){
        this.requireCount = requireCount;
        this.weiBoId = weiBoId;
    }
    public void normalComment(String comment){
        normalComment = true;
        comment(comment,weiBoId);
    }
    public void setRequestStateChangedListener(OnRequestStateChangedListener listener){
        this.listener = listener;
    }
    private void finishAndReset(){
        normalComment = true;
        currentCount = 0;
        page = 1;
        hasMoreUsers = true;
        stop = false;
        if(listener != null){
            listener.onFinish();
        }
    }
    public void batchComment(String comment){
        normalComment = false;
        if(stop){
            return;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                comment(comment,weiBoId);
            }
        },DURATION);

    }
    private void comment(String comment,String id){
        if(comment.length() < 140) {
            weiBoApi.setComment(getCommentMap(token, comment, id))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(postComments -> {
                        if(!normalComment) {
                            loadUsers();
                        } else {
                            if(listener != null) {
                                listener.onFinish();
                            }
                        }
                    }, BatchCommentManager.this::loadError);
        }
    }
    private void loadError(Throwable throwable) {
        throwable.printStackTrace();
    }
    private Map<String,Object> getSendMap(String token, int page){
        Map<String,Object> map = new HashMap<>();
        map.put("access_token", token);
        map.put("count", perCount);
        map.put("page", page);
        map.put("q", "2");
        return map;
    }
    public void batchAtUser(){
        loadUsers();
    }
    private void loadUsers() {
        if(currentCount > requireCount){
            stop = true;
            finishAndReset();
            return;
        }
        switch (atUserType){
            case AT_USER_TYPE_BY_TAG:
                loadUsersByTag();
                break;
            case AT_USER_TYPE_FANS:
                loadFans();
                break;
            case AT_USER_TYPE_NORMAL:
                loadNormalUsers();
                break;
        }

    }
    private void loadUsersByTag(){

    }
    private void loadFans(){
        weiBoApi.getFollowersById(getRequestMap())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userList -> {
                    loadComplete(userList);
                }, this::loadError);
    }
    private void loadNormalUsers(){
        weiBoApi.searchUsers(getSendMap(token, page))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userList -> {
                    loadComplete(userList);
                }, this::loadError);
    }
    private void loadComplete(UserList userList){
        if(userList != null){
            List<User> users = userList.getUsers();
            int size = users.size();
            if(size > 0){
                hasMoreUsers = true;
            } else {
                hasMoreUsers = false;
            }
            if(hasMoreUsers){
                String comment = getAtUserNameStr(userList.getUsers());
                currentCount += size;
                page++;
                Logger.logE("batchComment = :" + comment + ",currentCount =:" + currentCount+",page =:" + page);
                batchComment(comment);
            }
        }
    }
    public String getAtUserNameStr(List<User> users){
        StringBuilder builder = new StringBuilder();
        for(User user : users){
            builder.append("@").append(user.getScreen_name()).append(" ");
        }
        return builder.toString();
    }
    private Map<String, Object> getCommentMap(String token, String comment, String id) {
        Map<String, Object> map = new HashMap<>();
        map.put("access_token", token);
        map.put("comment", comment);
        map.put("id", id);
        return map;
    }
    public Map<String,Object> getRequestMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("access_token", token);
        map.put("uid", uid);
        return map;
    }
    public interface OnRequestStateChangedListener {
        void onFinish();
    }
}
