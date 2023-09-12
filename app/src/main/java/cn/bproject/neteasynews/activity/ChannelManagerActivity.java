package cn.bproject.neteasynews.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.example.channelmanager.APPConst;
import com.example.channelmanager.ProjectChannelBean;
import com.example.channelmanager.adapter.ChannelAdapter;
import com.example.channelmanager.base.IChannelType;
import com.example.channelmanager.utils.GridItemDecoration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.bproject.neteasynews.R;
import cn.bproject.neteasynews.Utils.ListDataSave;

/**
 * 主要用于频道管理界面的展示和数据操作。
 * 它包括初始化界面元素、管理用户选择的频道、编辑频道位置等功能。
 * 此外，它还根据用户的操作，将相应的数据保存并返回给上一个 Activity。
 * */
public class ChannelManagerActivity extends BaseActivity implements ChannelAdapter.ChannelItemClickListener{

    private RecyclerView mRecyclerView;//用于显示频道列表
    private ChannelAdapter mRecyclerAdapter;//管理频道的适配器。
    private List<ProjectChannelBean> mMyChannelList;//存储用户选择的频道列表。
    private List<ProjectChannelBean> mRecChannelList;//储更多频道的列表。
    private Context context;
    private int tabposition;//标签的位置
    private ListDataSave listDataSave;//保存数据的工具类

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /* 获取意图中的数据，主要是 tabposition。
        初始化工具栏（Toolbar）。
        初始化 RecyclerView 的布局和装饰。
        调用 initData() 方法初始化数据。
        创建并设置 ChannelAdapter，然后将其绑定到 RecyclerView。*/
        setContentView(R.layout.activity_channel_manager);
        getIntentData();
        context = this;
        initToolbar();
        listDataSave = new ListDataSave(this, "channel");
        mRecyclerView = (RecyclerView) findViewById(com.example.channelmanager.R.id.id_tab_recycler_view);
        GridLayoutManager gridLayout = new GridLayoutManager(context, 4);
        gridLayout.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                boolean isHeader = mRecyclerAdapter.getItemViewType(position) == IChannelType.TYPE_MY_CHANNEL_HEADER ||
                        mRecyclerAdapter.getItemViewType(position) == IChannelType.TYPE_REC_CHANNEL_HEADER;
                return isHeader ? 4 : 1;
            }
        });
        mRecyclerView.setLayoutManager(gridLayout);
        mRecyclerView.addItemDecoration(new GridItemDecoration(APPConst.ITEM_SPACE));
        initData();
        mRecyclerAdapter = new ChannelAdapter(context, mRecyclerView, mMyChannelList, mRecChannelList, 1, 1);
        mRecyclerAdapter.setChannelItemClickListener(this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    private void getIntentData(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        tabposition = bundle.getInt("TABPOSITION");
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("");
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("频道管理");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.icon_back);
        }
    }

    /**
     * 初始化数据
     * 初始化 mMyChannelList，这是用户选择的频道列表。
     * 从数据存储中获取 myChannel 数据，这是用户已选频道的列表。
     * 对列表中的每个频道进行处理：
     * 如果是当前浏览的 tab 标签，将其标记为默认项。
     * 否则，根据位置判断是否可编辑移动，设置对应的 tabType。
     * 初始化 mRecChannelList，这是更多频道的列表。
     * 从数据存储中获取 moreChannel 数据，这是更多频道的列表。
     */
    private void initData() {
        mMyChannelList = new ArrayList<>();

        List<ProjectChannelBean> list = listDataSave.getDataList("myChannel", ProjectChannelBean.class);
        for (int i = 0; i < list.size(); i ++){
            ProjectChannelBean projectChannelBean = list.get(i);
            if (i == tabposition){
                projectChannelBean.setTabType(APPConst.ITEM_DEFAULT);
            } else {
                // 判断i是否为0或者1,如果为0设置标题为红色（当前浏览的tab标签），如果为1则设置type为1（不可编辑移动），不为1则type为2
                // type为2表示该标签可供编辑移动
                int type;
                if (i == 0  || i == 1){
                    type = 1;
                } else {
                    type = 2;
                }
                projectChannelBean.setTabType(type);
            }
            mMyChannelList.add(projectChannelBean);
        }

        mRecChannelList = new ArrayList<>();
        List<ProjectChannelBean> moreChannelList = listDataSave.getDataList("moreChannel", ProjectChannelBean.class);
        for (ProjectChannelBean projectChannelBean : moreChannelList) {
            mRecChannelList.add(projectChannelBean);
        }
    }

    @Override
    protected void onPause() {
      /*  在 onPause 方法中，将当前模式设置为不可编辑状态（编辑状态用于用户移动频道位置）。
        将 mMyChannelList 和 mRecChannelList 中的数据保存到数据存储中。*/
        Iterator<ProjectChannelBean> iterator = mMyChannelList.iterator();
        while (iterator.hasNext()){
            ProjectChannelBean projectChannelBean = iterator.next();
            // 将当前模式设置为不可编辑状态
            projectChannelBean.setEditStatus(0);
        }
        listDataSave.setDataList("myChannel", mMyChannelList);
        listDataSave.setDataList("moreChannel", mRecChannelList);

        super.onPause();
    }

    @Override
    public void finish() {
 /*       在 finish 方法中，取消编辑模式，即退出编辑状态。
        遍历 mMyChannelList，查找当前浏览的 tab 标签位置，并记录到 tabposition。
        创建一个意图，将 tabposition 作为附加数据放入意图中。
        使用 setResult 设置返回的结果代码和意图。
        最后调用 super.finish() 完成 Activity 的销毁。*/
        mRecyclerAdapter.doCancelEditMode(mRecyclerView);

        for (int i = 0; i < mMyChannelList.size(); i ++) {
            ProjectChannelBean projectChannelBean = mMyChannelList.get(i);
            if (projectChannelBean.getTabType() == 0){
                tabposition = i;
            }
        }
        Intent intent = new Intent();
        intent.putExtra("NewTabPostion", tabposition);
        setResult(789, intent);

        super.finish();
    }



    @Override
    public void onChannelItemClick(List<ProjectChannelBean> list, int position) {

    }
}
