package com.lingy.lawei.weibo.util;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.lingy.lawei.MyApp;
import com.lingy.lawei.utils.Logger;
import com.lingy.lawei.utils.StringUtil;
import com.lingy.lawei.weibo.api.WeiBoApi;
import com.lingy.lawei.weibo.api.WeiBoFactory;
import com.lingy.lawei.weibo.bean.Status;
import com.lingy.lawei.weibo.bean.User;
import com.lingy.lawei.weibo.bean.UserList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    private List<String> queryStrs = new ArrayList<>();
    private String currentTag = "";
    private int currentTagIndex = 0;
    public BatchCommentManager(String weiBoId){
        this.weiBoId = weiBoId;
    }

    public void setRequireCount(int requireCount){
        this.requireCount = requireCount;
    }
    public void setQueryStrings(List<String> queryStrings){
        this.queryStrs = queryStrings;
        if(queryStrs.size() > 0){
            currentTag = queryStrs.get(0);
        }
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
        currentTagIndex = 0;
        currentTag = "";
        if(listener != null){
            listener.commentFinish();
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
                                listener.commentFinish();
                            }
                        }
                    }, BatchCommentManager.this::loadError);
        }
    }
    private void loadError(Throwable throwable) {
        throwable.printStackTrace();
        if(listener!=null){
            listener.onError(throwable);
        }
    }
    private Map<String,Object> getSendMap(String token, int page){
        Map<String,Object> map = new HashMap<>();
        map.put("access_token", token);
        map.put("count", perCount);
        map.put("page", page);
        map.put("q", currentTag);
        return map;
    }
    public void batchAtUser(){
        loadUsers();
    }
    private void loadUsers() {
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
        String comment = "@haha @hasc @wkkdl @来那个鬼 @第四季度1 @解答 @akldkcl @多看看 @ 据了解司法拘留";
        if(currentCount > requireCount){
            if(listener != null){
                listener.commentFinish();
            }
            return;
        }
        if(queryStrs.size() > 1) {
            currentTagIndex = new Random().nextInt(queryStrs.size() - 1);
            currentTag = queryStrs.get(currentTagIndex);
        }
        currentCount += 10;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(listener != null){
                    listener.publishProgress(comment,currentTag,currentCount);
                    loadUsersByTag();
                }
            }
        },2000);
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
                if(currentCount > requireCount){
                    stop = true;
                    finishAndReset();
                    return;
                }
                if(listener != null){
                    listener.publishProgress(comment,currentTag,currentCount);
                }
                batchComment(comment);
            } else {
                currentTagIndex ++;
                if(currentTagIndex < queryStrs.size()){
                    currentTag = queryStrs.get(currentTagIndex);
                    page = 1;
                    batchAtUser();
                }
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
        void commentFinish();
        void publishProgress(String comment,String tag,int currentUserCount);
        void onError(Throwable throwable);
    }
}
