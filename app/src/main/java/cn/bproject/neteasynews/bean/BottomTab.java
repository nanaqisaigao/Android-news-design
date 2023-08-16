package cn.bproject.neteasynews.bean;

/**
 * 底部标签栏标签
 * 新闻、图片、视频
 */
public class BottomTab {

    private  int title;
    private  int icon;
    private Class fragment;
//    Fragment 表示应用界面中可重复使用的一部分。fragment 定义和管理自己的布局，具有自己的生命周期，
//    并且可以处理自己的输入事件。fragment 不能独立存在。它们必须由 activity 或其他 fragment 托管。
//    fragment 的视图层次结构会成为宿主的视图层次结构的一部分，或附加到宿主的视图层次结构。
//    fragment 允许将界面划分为离散的区块，从而将模块化和可重用性引入 activity 的界面。

//    activity 是围绕应用的界面放置全局元素（如抽屉式导航栏）的理想位置。
//    相反，Fragment 更适合定义和管理单个屏幕或部分屏幕的界面。

//    Fragment是一种可以嵌入在Activity当中的UI片段用来组建Activity界面的局部模块,
//    可以在同一 activity 或多个 activity 中使用同一 fragment 类的多个实例
    public BottomTab(Class fragment, int title, int icon) {
        this.title = title;
        this.icon = icon;
        this.fragment = fragment;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public Class getFragment() {
        return fragment;
    }

    public void setFragment(Class fragment) {
        this.fragment = fragment;
    }
}
