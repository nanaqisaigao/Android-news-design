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
//使用FragmentTabHost实现底部标签栏导航的Android应用程序主活动。

//        onActivityResult方法：在Activity之间传递数据时调用，这里用于处理从子Activity返回的数据。
//        如果结果码为789，表示从频道管理页面返回，然后获取当前标签的Fragment，通过调用setCurrentChannel和notifyChannelChange方法来更新当前显示的频道。
//
//        总体来说，这段代码实现了一个具有底部标签栏导航功能的主活动。通过FragmentTabHost来管理标签和相关联的Fragment，
//        在标签切换时可以执行相应的逻辑。此外，通过onActivityResult方法实现了从子Activity返回数据并进行相应处理的功能。

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();

//    用于设置标签和关联的Fragment
    private FragmentTabHost mTabHost;
    //    Layout是一个用于加载布局的系统服务，就是实例化与Layout XML文件对应的View对象，不能直接使用，
    //    需要通过getLayoutInflater( )方法或getSystemService( )方法来获得与当前Context绑定的 LayoutInflater实例
    private LayoutInflater mInflater;
    //底部标签栏内容的List集合
    private final List<BottomTab> mBottomTabs = new ArrayList<>(5);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTab();

    }

    //    该方法用于初始化底部标签栏，首先创建了三个BottomTab对象，每个对象表示一个底部标签，
//        包括Fragment类、标签标题和图标资源。然后将这些标签对象添加到mBottomTabs列表中。
//        接着，通过FragmentTabHost来设置标签和关联的Fragment。

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

        //LayoutInflacter用法  1.获取LayoutInflater实例
        mInflater = LayoutInflater.from(this);
        //获取标签栏
        mTabHost = findViewById(android.R.id.tabhost);
        //获取标签栏和其fragment
        // FragmentManager 类负责在应用的 fragment 上执行一些操作，如添加、移除或替换操作，以及将操作添加到返回堆栈。
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        //FragmentTab获取内容

//       将每个BottomTab对象转换为TabHost.TabSpec并添加到mTabHost（一个FragmentTabHost实例）
        for (BottomTab bottomTab : mBottomTabs){
//            TabHost相当于浏览器中浏览器分布的集合，而Tabspec则相当于浏览器中的每一个分页面。
//            在Android中，每一个TabSpec分布可以是一个组件，也可以是一个布局，然后将每一个分页装入TabHost中，
//            TabHost即可将其中的每一个分页一并显示出来。

            //用于表示一个分页
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(getString(bottomTab.getTitle()));

//           tabSpec.setIndicator()：使用setIndicator()方法设置分页的指示器（标题和图标等）。
//                              buildIndicator(bottomTab)返回一个视图对象，该视图将在分页选项卡上显示
            tabSpec.setIndicator(buildIndicator(bottomTab));

//            mTabHost.addTab()：通过addTab()方法将创建的TabSpec对象添加到mTabHost中。
//            第一个参数是分页的标签，第二个参数是分页对应的Fragment类，第三个参数是传递给Fragment的参数，这里设置为null。
            mTabHost.addTab(tabSpec, bottomTab.getFragment(),null);

        }

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

                LogUtils.d(TAG, "onTabChanged: mTabHost.setOnTabChangedListener" + R.string.news_fragment);

            }
        });
//        用于设置选项卡之间的分割线显示。通过getTabWidget()方法获取mTabHost的标签部件，
//        然后使用setShowDividers()方法将分割线的显示设置为LinearLayout.SHOW_DIVIDER_NONE，即不显示分割线。
        mTabHost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
//        设置当前显示的选项卡为索引为0的选项卡。它将默认显示第一个选项卡。
        mTabHost.setCurrentTab(0);

    }
    //buildIndicator方法：用于构建底部标签的视图，从tab_indicator.xml布局文件中加载视图，设置图标和文本，然后返回构建好的视图。

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
