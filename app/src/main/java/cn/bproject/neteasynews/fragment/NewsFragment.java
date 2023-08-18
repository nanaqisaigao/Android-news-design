package cn.bproject.neteasynews.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.channelmanager.APPConst;
import com.example.channelmanager.ProjectChannelBean;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.bproject.neteasynews.R;
import cn.bproject.neteasynews.Utils.CategoryDataUtils;
import cn.bproject.neteasynews.Utils.IOUtils;
import cn.bproject.neteasynews.Utils.ListDataSave;
import cn.bproject.neteasynews.activity.ChannelManagerActivity;
import cn.bproject.neteasynews.adapter.FixedPagerAdapter;
import cn.bproject.neteasynews.fragment.news.NewsListFragment;

import static cn.bproject.neteasynews.R.id.tab_layout;

/**
 * 新闻模块
 */

public class NewsFragment extends BaseFragment {

    private final String TAG = NewsFragment.class.getSimpleName();

    private TabLayout mTabLayout;//显示标签，切换不同的新闻频道
    private ViewPager mNewsViewpager;//显示不同频道对应的内容页面
    private View mView;//Fragment 的根视图
    private FixedPagerAdapter fixedPagerAdapter;//ViewPager 的适配器，用于管理不同频道的内容 Fragment
    private List<BaseFragment> fragments;//存储各个频道对应的内容 Fragment
    private List<ProjectChannelBean> myChannelList;//用户设置的新闻频道列表
    private List<ProjectChannelBean> moreChannelList;//更多的新闻频道列表
    private ImageButton mChange_channel;//切换频道的按钮。
    private int tabPosition;// 当前新闻频道的位置
    private SharedPreferences sharedPreferences;//用于存储应用设置信息。
    private ListDataSave listDataSave;//保存频道数据的工具类。  将List集合转为json数据保存在sharedPreferences的工具类
    private boolean isFirst;//标记是否为第一次进入应用。
    private BaseFragment baseFragment;//用于存储创建的 Fragment 对象。



    @Nullable
    @Override
//    在片段被创建时调用，加载片段的布局。使用布局文件tablayout_pager.xml来创建视图。
    //LayoutInflater inflater：用于从布局资源文件创建视图的工具。
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.tablayout_pager, container, false);
        return mView;
    }

    @Override
    //初始化视图中的各个控件和数据
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }


    @Override
    public void initView() {
        mTabLayout = mView.findViewById(tab_layout);//新闻标签
        mNewsViewpager = mView.findViewById(R.id.news_viewpager);//新闻内容
        mChange_channel = mView.findViewById(R.id.change_channel);//改变频道

        Toolbar myToolbar = initToolbar(mView, R.id.my_toolbar, R.id.toolbar_title, R.string.news_home);
        //初始化，设置 TabLayout 和 ViewPager 的关联，并绑定数据
        initValidata();
        //点击标签，更换频道内容
        initListener();
    }

    @Override
    //于初始化一些数据和实例，设置 TabLayout 和 ViewPager 的关联，并绑定数据
    public void initValidata() {
        //只有当前应用可以访问，获取的Setting共选项
        sharedPreferences = getActivity().getSharedPreferences("Setting", Context.MODE_PRIVATE);
        //保存频道数据为Json，取名为channel
        listDataSave = new ListDataSave(getActivity(), "channel");
        //用于存储各个频道对应的内容片段
        fragments = new ArrayList<BaseFragment>();
        //管理 ViewPager 中的内容的适配器
        fixedPagerAdapter = new FixedPagerAdapter(getChildFragmentManager());
        //将 TabLayout 与 ViewPager 进行关联，实现选项卡与页面内容的同步切换。
        mTabLayout.setupWithViewPager(mNewsViewpager);
        //将数据绑定到 ViewPager 和适配器上，实现不同频道内容的显示
        bindData();
    }

    @Override
    //点击标签，更换频道内容
    public void initListener() {
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabPosition = tab.getPosition();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        //切换频道
        mChange_channel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChannelManagerActivity.class);
                //传递的数据为当前频道位置
                intent.putExtra("TABPOSITION", tabPosition);
                startActivityForResult(intent, 999);
            }
        });
    }

    @Override
    public void bindData() {
        //获取所有频道
        getDataFromSharedPreference();
        //存储频道列表
        fixedPagerAdapter.setChannelBean(myChannelList);
        //存储各个频道内容
        fixedPagerAdapter.setFragments(fragments);
        //连接后端数据和前端显示的适配器接口  给文章的显示设置适配器
        mNewsViewpager.setAdapter(fixedPagerAdapter);
    }

    /**
     * 判断是否第一次进入程序
     * 如果第一次进入，直接获取设置好的频道
     * 如果不是第一次进入，则从sharedPrefered中获取设置好的频道
     */
    private void getDataFromSharedPreference() {
        isFirst = sharedPreferences.getBoolean("isFirst", true);
        if (isFirst) {
            //获取频道数据列表。
            myChannelList = CategoryDataUtils.getChannelCategoryBeans();
            //获取更多频道数据。
            moreChannelList = getMoreChannelFromAsset();
            //设置频道数据的类型。
            myChannelList = setType(myChannelList);
            moreChannelList = setType(moreChannelList);
            //将设置好的频道数据保存到共享偏好设置中，并将 "isFirst" 设置为 false。
            listDataSave.setDataList("myChannel", myChannelList);
            listDataSave.setDataList("moreChannel", moreChannelList);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean("isFirst", false);
            edit.commit();
        } else {
            //不是第一次进入程序,从共享偏好设置中获取之前保存的频道数据列表。
            myChannelList = listDataSave.getDataList("myChannel", ProjectChannelBean.class);
        }
        //清空 fragments 列表，用于重新填充内容片段列表
        fragments.clear();
        //为每个频道创建对应的内容片段，并将内容片段添加到 fragments 列表中。
        for (int i = 0; i < myChannelList.size(); i++) {
            baseFragment = NewsListFragment.newInstance(myChannelList.get(i).getTid());
            fragments.add(baseFragment);
        }
        //根据 myChannelList 的大小判断是否启用 TabLayout 的固定模式（TabLayout.MODE_FIXED）
        // 或滚动模式（TabLayout.MODE_SCROLLABLE）。
        if (myChannelList.size() <= 4) {
            mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        } else {
            mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        }

    }

    /**
     * 在ManiActivty中被调用，当从ChanelActivity返回时设置当前tab的位置
     * @param tabPosition
     */
    public void setCurrentChannel(int tabPosition) {
        mNewsViewpager.setCurrentItem(tabPosition);
        mTabLayout.setScrollPosition(tabPosition, 1, true);
    }

    /**
     * 在myChannelList发生改变的时候更新ui，在MainActivity调用
     */
    public void notifyChannelChange() {
        getDataFromSharedPreference();
        fixedPagerAdapter.setChannelBean(myChannelList);

        fixedPagerAdapter.setFragments(fragments);
        fixedPagerAdapter.notifyDataSetChanged();

    }

    private List<ProjectChannelBean> setType(List<ProjectChannelBean> list) {
        Iterator<ProjectChannelBean> iterator = list.iterator();
        while (iterator.hasNext()) {
            ProjectChannelBean channelBean = iterator.next();
            channelBean.setTabType(APPConst.ITEM_EDIT);
        }
        return list;
    }

    /**
     * 从Asset目录中读取更多频道
     *
     * @return
     */
    public List<ProjectChannelBean> getMoreChannelFromAsset() {
        String moreChannel = IOUtils.readFromFile("projectChannel.txt");
        List<ProjectChannelBean> projectChannelBeanList = new ArrayList<>();
        JsonArray array = new JsonParser().parse(moreChannel).getAsJsonArray();
        for (final JsonElement elem : array) {
            projectChannelBeanList.add(new Gson().fromJson(elem, ProjectChannelBean.class));
        }
        return projectChannelBeanList;
    }
}
