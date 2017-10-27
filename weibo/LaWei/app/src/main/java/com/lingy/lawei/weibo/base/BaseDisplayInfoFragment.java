package com.lingy.lawei.weibo.base;

import android.support.v7.widget.LinearLayoutManager;
import android.widget.ImageView;

import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.lingy.lawei.MyApp;
import com.lingy.lawei.R;
import com.lingy.lawei.utils.Logger;
import com.lingy.lawei.weibo.activity.UserInfoDisplayActivity;
import com.lingy.lawei.weibo.api.WeiBoApi;
import com.lingy.lawei.weibo.api.WeiBoFactory;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;

/**
 * Created by Xijun.Wang on 2017/10/24.
 */

public abstract class BaseDisplayInfoFragment extends BaseFragment {
    protected static final String TAG = "WeiBoFragment";
    protected boolean hasMore = true;
    protected int mPage = 1;
    protected String mToken;
    protected WeiBoApi mApi = WeiBoFactory.getWeiBoApiSingleton();
    protected boolean refresh = true;
    protected String mUid;
    @BindView(R.id.xr_info_list)
    protected XRecyclerView mXRContentList;
    @BindView(R.id.iv_empty_view)
    protected ImageView mEmptyView;
    @Override
    protected void init() {
        LinearLayoutManager manager = new LinearLayoutManager(mActivity);
        mXRContentList.setLayoutManager(manager);
        mXRContentList.setLoadingListener(new MyLoadingListener());
        mToken = MyApp.getInstance().getAccessTokenHack();
        mUid = ((UserInfoDisplayActivity)mActivity).getUserId();
    }
    protected abstract void load();

    @Override
    protected int providedLayoutId() {
        return R.layout.fragment_user_info;
    }
    public Map<String,Object> getRequestMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("access_token", mToken);
        map.put("uid", mUid);
        return map;
    }
    private class MyLoadingListener implements XRecyclerView.LoadingListener {
        @Override
        public void onRefresh() {
            refresh = true;
            hasMore = true;
            Logger.logE("刷新.......");
            load();
        }

        @Override
        public void onLoadMore() {
            refresh = false;
            Logger.logE("加载更多.......");
            load();
        }
    }
    public void loadError(Throwable throwable) {
        throwable.printStackTrace();
        showTips("加载出错");
    }
}
