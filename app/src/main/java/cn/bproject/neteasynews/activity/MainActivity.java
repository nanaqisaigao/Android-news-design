package cn.bproject.neteasynews.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.bproject.neteasynews.R;
import cn.bproject.neteasynews.Utils.LogUtils;
import cn.bproject.neteasynews.bean.BottomTab;
import cn.bproject.neteasynews.fragment.NewsFragment;
import cn.bproject.neteasynews.fragment.PhotoFragment;
import cn.bproject.neteasynews.fragment.VideoFragment;
import cn.bproject.neteasynews.widget.FragmentTabHost;
//使用FragmentTabHost实现底部标签栏导航的Android应用程序主活动。下面我会对代码进行逐步解析：
//
//        onCreate方法：这是活动的生命周期方法，在活动被创建时调用。在这里，首先调用父类的onCreate方法，
//        然后设置布局文件，接着调用initTab()方法来初始化底部标签栏。
//
//        initTab方法：该方法用于初始化底部标签栏，首先创建了三个BottomTab对象，每个对象表示一个底部标签，
//        包括Fragment类、标签标题和图标资源。然后将这些标签对象添加到mBottomTabs列表中。接着，通过FragmentTabHost来设置标签和关联的Fragment。
//
//        buildIndicator方法：用于构建底部标签的视图，从tab_indicator.xml布局文件中加载视图，设置图标和文本，然后返回构建好的视图。
//
//        onActivityResult方法：在Activity之间传递数据时调用，这里用于处理从子Activity返回的数据。
//        如果结果码为789，表示从频道管理页面返回，然后获取当前标签的Fragment，通过调用setCurrentChannel和notifyChannelChange方法来更新当前显示的频道。
//
//        总体来说，这段代码实现了一个具有底部标签栏导航功能的主活动。通过FragmentTabHost来管理标签和相关联的Fragment，
//        在标签切换时可以执行相应的逻辑。此外，通过onActivityResult方法实现了从子Activity返回数据并进行相应处理的功能。

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();


    private FragmentTabHost mTabHost;
    private LayoutInflater mInflater;
    private final List<BottomTab> mBottomTabs = new ArrayList<>(5);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTab();

    }

    // 初始化底部标签栏
    private void initTab() {
        // 新闻标签
        BottomTab bottomTab_news = new BottomTab(NewsFragment.class,R.string.news_fragment,R.drawable.select_icon_news);
        // 图片标签
        BottomTab bottomTab_photo = new BottomTab(PhotoFragment.class,R.string.photo_fragment,R.drawable.select_icon_photo);
        // 视频标签
        BottomTab bottomTab_video = new BottomTab(VideoFragment.class,R.string.video_fragment,R.drawable.select_icon_video);

        mBottomTabs.add(bottomTab_news);
        mBottomTabs.add(bottomTab_photo);
        mBottomTabs.add(bottomTab_video);


        // 设置FragmentTab
        mInflater = LayoutInflater.from(this);
        mTabHost = findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);


        for (BottomTab bottomTab : mBottomTabs){

            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(getString(bottomTab.getTitle()));

            tabSpec.setIndicator(buildIndicator(bottomTab));

            mTabHost.addTab(tabSpec, bottomTab.getFragment(),null);

        }

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

                LogUtils.d(TAG, "onTabChanged: mTabHost.setOnTabChangedListener" + R.string.news_fragment);

            }
        });

        mTabHost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        mTabHost.setCurrentTab(0);

    }

    // 设置底部tab的图片和文字
    private View buildIndicator(BottomTab bottomTab){

        View view = mInflater.inflate(R.layout.tab_indicator, null);
        ImageView img = view.findViewById(R.id.icon_tab);
        TextView text = view.findViewById(R.id.txt_indicator);

        img.setBackgroundResource(bottomTab.getIcon());
        text.setText(bottomTab.getTitle());

        return  view;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String tag =  mTabHost.getCurrentTabTag();
        if (resultCode == 789){
            Bundle bundle = data.getExtras();
            int tabPosition = bundle.getInt("NewTabPostion");
            NewsFragment newsFragment = (NewsFragment) getSupportFragmentManager().findFragmentByTag(tag);
            newsFragment.setCurrentChannel(tabPosition);
            newsFragment.notifyChannelChange();
        }
    }
}
