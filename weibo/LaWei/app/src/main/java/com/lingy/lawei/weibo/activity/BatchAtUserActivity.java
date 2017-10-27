package com.lingy.lawei.weibo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.lingy.lawei.R;
import com.lingy.lawei.utils.Logger;
import com.lingy.lawei.weibo.base.BaseActivity;
import com.lingy.lawei.weibo.bean.Status;
import com.lingy.lawei.weibo.util.BatchCommentManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import me.gujun.android.taggroup.TagGroup;

/**
 * Created by Xijun.Wang on 2017/10/26.
 */

public class BatchAtUserActivity extends BaseActivity implements BatchCommentManager.OnRequestStateChangedListener {
    private static final String STATUS = "status";
    @BindView(R.id.tags)
    TagGroup tgTag;
    List<String> tagList = new ArrayList<>();
    private Status mStatus;
    @Override
    protected void init() {
        String[] tags = {"80后","90后","购物狂","运动","吃货","驴友","英雄联盟","王者荣耀"};
        for(String tag : tags){
            tagList.add(tag);
        }
        tgTag.setTags(tagList);
        Intent intent = getIntent();
        if(intent != null){
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                mStatus = (Status) bundle.getSerializable(STATUS);
            }
        }
    }
    public static void toBatchAtUser(Context context, Status status){
        Intent intent = new Intent(context,BatchAtUserActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(STATUS,status);
        context.startActivity(intent);
    }
    private void batchAtUser(){
        BatchCommentManager manager = new BatchCommentManager(20,mStatus.getIdstr());
        manager.setRequestStateChangedListener(this);
        manager.batchAtUser();
    }
    @Override
    protected boolean canBack() {
        return true;
    }

    @Override
    protected int providedLayoutId() {
        return R.layout.activity_batch_at_user;
    }

    @Override
    public void onFinish() {
        finishAtUsers();
    }
    private void finishAtUsers(){

    }
}
