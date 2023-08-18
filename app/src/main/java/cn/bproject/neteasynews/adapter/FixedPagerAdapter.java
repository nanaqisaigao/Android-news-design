package cn.bproject.neteasynews.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import com.example.channelmanager.ProjectChannelBean;

import java.lang.reflect.Method;
import java.util.List;

import cn.bproject.neteasynews.fragment.BaseFragment;

/**
 *处理内容片段的实例化、销毁、标题获取等操作，并允许在不同频道之间进行切换。
 * 通过设置频道数据和内容片段列表，适配器可以根据位置动态获取对应的内容片段。
 */

public class FixedPagerAdapter extends FragmentStatePagerAdapter {
    //存储频道数据的列表。
    private List<ProjectChannelBean> channelBeanList;
    //管理片段
    private FragmentManager fm;
    //存储不同频道对应的内容片段列表
    private List<BaseFragment> fragments;

    //初始化适配器
    public FixedPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fm = fm;
    }
    //设置频道数据列表
    public void setChannelBean(List<ProjectChannelBean> newsBeans) {
        this.channelBeanList = newsBeans;
    }
    //设置内容片段列表
    public void setFragments(List<BaseFragment> fragments) {
        this.fragments = fragments;
    }
    //根据位置获取对应的内容片段
    @Override
    public BaseFragment getItem(int position) {
        return fragments.get(position);
    }
    //返回内容片段的数量
    @Override
    public int getCount() {
        return fragments.size();
    }
    //实例化内容片段
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        BaseFragment fragment = null;
        try {
            removeFragment(container, position);
            fragment = (BaseFragment) super.instantiateItem(container, position);
        } catch (Exception e) {

        }
        return fragment;
    }
    //移除指定位置的片段
    private void removeFragment(ViewGroup container,int index) {
        String tag = getFragmentTag(container.getId(), index);
        Fragment fragment = fm.findFragmentByTag(tag);
        if (fragment == null)
            return;
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(fragment);
        ft.commit();
        ft = null;
        fm.executePendingTransactions();
    }
    //根据视图 ID 和索引获取片段的标签
    private String getFragmentTag(int viewId, int index) {
        try {
//            通过Class知道某个类中有多少方法，有多少字段，每个字段叫什么名字，
//            每个字段的类型是什么，每个方法的方法名是什么，某个方法有几个参数
            Class<FragmentPagerAdapter> cls = FragmentPagerAdapter.class;
            Class<?>[] parameterTypes = { int.class, long.class };
            Method method = cls.getDeclaredMethod("makeFragmentName",
                    parameterTypes);
            method.setAccessible(true);
            String tag = (String) method.invoke(this, viewId, index);
            return tag;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String tname = channelBeanList.get(position % channelBeanList.size()).getTname();
        return tname;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
