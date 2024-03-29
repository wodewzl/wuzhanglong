
package com.bs.bsims.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bs.bsims.R;
import com.bs.bsims.adapter.IdeaAdapter;
import com.bs.bsims.application.BSApplication;
import com.bs.bsims.constant.Constant;
import com.bs.bsims.interfaces.UpdateCallback;
import com.bs.bsims.model.IdeaResultVO;
import com.bs.bsims.model.IdeaVO;
import com.bs.bsims.utils.HttpClientUtil;
import com.bs.bsims.utils.ThreadUtil;
import com.bs.bsims.utils.UrlUtil;
import com.bs.bsims.view.BSRefreshListView;
import com.bs.bsims.view.BSRefreshListView.OnRefreshListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ValidFragment")
public class CreativeIdeaAllFragment extends BaseFragment implements UpdateCallback {
    private static final String TAG = "CreativeIdeaAll";
    private Activity mActivity;
    private TextView mMsgTv;
    private IdeaResultVO mIdeaResultVO;

    // listView
    private View mFootLayout;
    private TextView mMoreTextView;
    private ProgressBar mProgressBar;
    // 0为首次,1为上拉刷新 ，2为下拉刷新
    private int mState = 0;
    public static final String FIRSTID = "firstid";
    public static final String LASTID = "lastid";
    private BSRefreshListView mListView;
    public IdeaAdapter mAdapter;

    private List<IdeaVO> mMyIdeaList;
    private List<IdeaVO> mMysuggestList;
    private List<IdeaVO> mAllList;
    private String mType = "1";
    private String mIsboos = "0";
    private String mIsall = "0";
    private String mUnread;
    private String mToDo;

    public String getmType() {
        return mType;
    }

    public void setmType(String mType) {
        this.mType = mType;
    }

    public CreativeIdeaAllFragment() {
    }

    public CreativeIdeaAllFragment(IdeaResultVO ideaResultVO, String type, String isboss, String isall, String unread, String todo) {
        this.mType = type;
        this.mIsboos = isboss;
        this.mIsall = isall;
        this.mUnread = unread;
        this.mToDo = todo;
        this.mIdeaResultVO = ideaResultVO;
        if (ideaResultVO != null) {
            if (Constant.RESULT_CODE.equals(ideaResultVO.getCode())) {
                this.mAllList = ideaResultVO.getArray();
            } else {
                this.mAllList = new ArrayList<IdeaVO>();
            }
        } else {
            this.mAllList = new ArrayList<IdeaVO>();
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.creative_idea_all, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        bindViewsListener();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public String getFragmentName() {
        return TAG;
    }

    public void initViews(View view) {
        if (mAllList == null) {
            return;
        }
        mListView = (BSRefreshListView) view.findViewById(R.id.lv_crate_idea);
        mAdapter = new IdeaAdapter(mActivity, mIsboos);
        mListView.setAdapter(mAdapter);
        mAdapter.mList.addAll(mAllList);
        mAdapter.notifyDataSetChanged();
        initFoot(view);
        footViewIsVisibility(mAdapter.mList);
    }

    public boolean getData() {

        if (0 == mState) {
            return getData(Constant.CREATIVE_IDEA, "", "");
        } else if (1 == mState) {
            if (mAdapter.mList.size() > 0) {
                String id = mAdapter.mList.get(0).getArticleid();
                return getData(Constant.CREATIVE_IDEA, FIRSTID, id);
            } else {
                return getData(Constant.CREATIVE_IDEA, "", "");
            }
        } else if (2 == mState) {
            String id = mAdapter.mList.get(mAdapter.mList.size() - 1).getArticleid();
            return getData(Constant.CREATIVE_IDEA, LASTID, id);
        }

        return false;
    }

    public boolean getData(String interfaceurl, String refresh, String id) {
        Gson gson = new Gson();
        try {
            String urlStr = UrlUtil.getIdeaUrl(interfaceurl, refresh, id, BSApplication.getInstance().getUserId(), mType, mIsboos, mIsall, mUnread, mToDo);
            String jsonUrlStr = HttpClientUtil.get(urlStr, Constant.ENCODING).trim();
            mIdeaResultVO = gson.fromJson(jsonUrlStr, IdeaResultVO.class);
            if (Constant.RESULT_CODE.equals(mIdeaResultVO.getCode())) {
                if (FIRSTID.equals(refresh)) {
                    if (mIdeaResultVO.getCount() != null) {
                    }

                } else if (LASTID.equals(refresh)) {
                } else {
                }
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public void bindViewsListener() {
        if (mAllList == null) {
            return;
        }
        mListView.setonRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                mState = 1;
                new ThreadUtil(mActivity, CreativeIdeaAllFragment.this).start();
            }
        });
        if (mAllList.size() == 0) {
            mListView.setRefreshable(false);
        }
        mFootLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mMoreTextView.setText("正在加载...");
                mProgressBar.setVisibility(View.VISIBLE);
                mState = 2;
                new ThreadUtil(mActivity, CreativeIdeaAllFragment.this).start();
            }
        });
    }

    public void initFoot(View view) {
        mFootLayout = LayoutInflater.from(mActivity).inflate(R.layout.listview_bottom_more, null);
        mMoreTextView = (TextView) mFootLayout.findViewById(R.id.txt_loading);
        mMoreTextView.setText("更多");
        mProgressBar = (ProgressBar) mFootLayout.findViewById(R.id.progressBar);
        mFootLayout.setVisibility(View.GONE);
        mListView.addFooterView(mFootLayout);
    }

    /**
     * 加载更多是否隐藏
     * 
     * @param datas
     */
    protected void footViewIsVisibility(List<IdeaVO> datas) {
        if (mIdeaResultVO == null || mIdeaResultVO.getCount() == null) {
            return;
        }
        if (Integer.parseInt(mIdeaResultVO.getCount()) <= 15) {
            mFootLayout.setVisibility(View.GONE);
        } else {
            mFootLayout.setVisibility(View.VISIBLE);
            mMoreTextView.setText("更多");
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean execute() {
        return getData();
    }

    @Override
    public void executeSuccess() {

        if (1 == mState) {
            mAdapter.updateDataFrist(mIdeaResultVO.getArray());
        } else if (2 == mState) {
            mAdapter.updateDataLast(mIdeaResultVO.getArray());
        } else {
            mAdapter.updateData(mIdeaResultVO.getArray());
        }
        mState = 0;
        mAdapter.notifyDataSetChanged();
        // mAdapter.updateData(mAllList);
        mListView.onRefreshComplete();

        if (mState != 1) {
            footViewIsVisibility(mIdeaResultVO.getArray());
        }
        mState = 0;
    }

    @Override
    public void executeFailure() {
        mAdapter.notifyDataSetChanged();
        mListView.onRefreshComplete();
        footViewIsVisibility(mAdapter.mList);
        if (mIdeaResultVO != null) {
        } else {
        }
    }

}
