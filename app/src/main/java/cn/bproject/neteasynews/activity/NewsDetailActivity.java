package cn.bproject.neteasynews.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.bproject.neteasynews.R;
import cn.bproject.neteasynews.Utils.IOUtils;
import cn.bproject.neteasynews.Utils.LogUtils;
import cn.bproject.neteasynews.Utils.ThreadManager;
import cn.bproject.neteasynews.bean.NewsDetailBean;
import cn.bproject.neteasynews.common.Api;
import cn.bproject.neteasynews.common.DefineView;
import cn.bproject.neteasynews.http.DataParse;
import cn.bproject.neteasynews.http.HttpCallbackListener;
import cn.bproject.neteasynews.http.HttpHelper;
import cn.bproject.neteasynews.widget.LoadingPage;

import static cn.bproject.neteasynews.R.id.details_content;

/**
 *显示新闻详情的活动
 */

public class NewsDetailActivity extends BaseActivity implements DefineView {
    private final String TAG = NewsDetailActivity.class.getSimpleName();
    //    显示新闻标题、作者和发布时间
    private TextView details_title, details_name, details_time;
    //上下文    通过servlet绑定数据使用 作为中间的通道让Servlet 和Web容器进行交互
    private Context mContext;
    //用于显示新闻内容    WebView 类是 Android 的 View 类的扩展，让网页显示为 Activity 布局的一部分
    private WebView mWebView;
    private ThreadManager.ThreadPool mThreadPool;   // 线程池
    //WebView的配置。
    private WebSettings mWebSettings;
    //存储偏好设置
    private SharedPreferences sharedPreferences;
    //新闻的唯一ID。
    private String mDocid;
    //存储获取的新闻详情数据。
    private NewsDetailBean mNewsDetailBeen;
    //包裹内容的布局
    private LinearLayout mPage_content;
//    加载页面部件
    private LoadingPage mLoadingPage;
    //处理UI更新  Handler 主要用于在后台线程中与UI线程进行通信，以便在UI线程上执行一些任务。
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //绑定数据到UI元素上
            bindData();
            //显示新闻页面
            showNewsPage();
        }
    };

    //在活动创建时调用，初始化视图、数据和监听器
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsdetail);
        mContext = this;
        Intent intent = getIntent();
        mDocid = intent.getStringExtra("DOCID");
        initView();
        initValidata();
        initListener();
    }


    //初始化视图，包括设置Toolbar、获取相关视图和部件，展示加载页面
    @Override
    public void initView() {
        initToolbar();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mPage_content = findViewById(R.id.page_content);
        mLoadingPage = findViewById(R.id.loading_page);
        details_title = this.findViewById(R.id.details_title);
        // 设置标题加粗
        TextPaint tp = details_title.getPaint();
        tp.setFakeBoldText(true);
        details_name = this.findViewById(R.id.details_name);
        details_time = this.findViewById(R.id.details_time);
        mWebView = this.findViewById(details_content);

        showLoadingPage();
    }
    //初始化工具栏，包括设置工具栏的标题和返回按钮。
    private void initToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.icon_back);
        }
    }


    //初始化数据，设置WebView的配置，添加Javascript接口对象，发起网络请求获取新闻详情。
    @Override
    public void initValidata() {
        setWebView();
        // 将设置好的JavaScriptInterface对象传入，第二个参数则是为这个对象设置名称（可随意）
        mWebView.addJavascriptInterface(new JavaScriptInterface(), "androidMethod");
        requestData();
    }
    /**
     * 设置WebView相关配置
     * 包括自适应屏幕、支持缩放和JavaScript等
     */
    private void setWebView(){
        mWebSettings = mWebView.getSettings();
        //自适应屏幕
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mWebSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        // 打开页面时， 自适应屏幕
        mWebSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        mWebSettings.setSupportZoom(true); //支持缩放
        mWebSettings.setJavaScriptEnabled(true);  //开启javascript
        mWebSettings.setDomStorageEnabled(true);  //开启DOM
        mWebSettings.setDefaultTextEncodingName("utf-8"); //设置编码
        // // web页面处理
        mWebSettings.setAllowFileAccess(true);// 支持文件流

        //提高网页加载速度，暂时阻塞图片加载，然后网页加载好了，再进行加载图片
        mWebSettings.setBlockNetworkImage(true);
        //开启缓存机制
        mWebSettings.setAppCacheEnabled(true);
        setTextSize();
        //设置webview
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.setWebViewClient(new MyWebViewClient());
    }
    //发起网络请求，获取新闻详情数据。
    private void requestData(){
        // 创建线程池,用于执行网络请求的任务
        mThreadPool = ThreadManager.getThreadPool();
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                //通过拼接 mDocid 到请求URL中，构建了获取新闻详情的完整URL地址 url
                String url = Api.DetailUrl + mDocid + Api.endDetailUrl;
                Log.d(TAG, "文章url为: " + url);
                //发起HTTP GET请求，该请求会异步执行
                HttpHelper.get(url, new HttpCallbackListener() {
                    //在请求的回调函数中，如果请求成功（onSuccess 方法），则会解析获取到的结果字符串 result，
                    // 并将解析后的数据存储到 mNewsDetailBeen 对象中。
                    @Override
                    public void onSuccess(String result) {
                        mNewsDetailBeen = DataParse.NewsDetail(result, mDocid);
                        handler.sendMessage(handler.obtainMessage());
                    }

                    @Override
                    public void onError(final Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG).show();
                                showErroPage();
                            }
                        });

                    }
                });

            }
        });
    }

    @Override
    public void initListener() {

    }

    private void setTextSize(){
        int textSize = Integer.valueOf(sharedPreferences.getString("text_size", "2"));
        switch (textSize) {
            case 0:
                // 超大字体
                mWebSettings.setTextSize(WebSettings.TextSize.LARGEST);
                // settings.setTextZoom(22);
                break;
            case 1:
                // 大字体
                mWebSettings.setTextSize(WebSettings.TextSize.LARGER);
                break;
            case 2:
                // 正常字体
                mWebSettings.setTextSize(WebSettings.TextSize.NORMAL);
                break;
            case 3:
                // 小字体
                mWebSettings.setTextSize(WebSettings.TextSize.SMALLER);
                break;
            case 4:
                // 超小字体
                mWebSettings.setTextSize(WebSettings.TextSize.SMALLEST);
                break;

            default:
                break;
        }
    }

    @Override
    public void bindData() {
        //数据获取成功后
        if (mNewsDetailBeen != null) {
            //对新闻详情进行处理
            changeNewsDetail(mNewsDetailBeen);
            String body = mNewsDetailBeen.getBody();
            // 使用css样式的方式设置图片大小
            //用于设置图片的大小、页边距和字体大小等。这样可以确保新闻详情在 WebView 中以可读的方式显示
            String css = "<style type=\"text/css\"> img {" +
                    "width:100%;" +
                    "height:auto;" +
                    "}" +
                    "body {" +
                    "margin-right:15px;" +
                    "margin-left:15px;" +
                    "margin-top:15px;" +
                    "font-size:24px;" +
                    "}" +
                    "</style>";
            String html = "<html><header>" + css + "</header><body>" + body + "</body></html>";
            Log.d(TAG, "html: " + html);
            String title = mNewsDetailBeen.getTitle();
            String ptime = mNewsDetailBeen.getPtime();
            String source = mNewsDetailBeen.getSource();

            details_title.setText(title);
            details_name.setText(source);
            details_time.setText(ptime);
            //details_content.loadData(articleBean.getContext(),"text/html","UTF-8");

            //加载 HTML 内容到 WebView 中，以便显示新闻详情的正文内容。
            mWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", "");
        } else{
            showEmptyPage();
        }
    }

    /**
     * 如果有新闻就展示新闻页面
     */
    private void showNewsPage() {
        mPage_content.setVisibility(View.VISIBLE);
        mLoadingPage.setSuccessView();
    }

    /**
     * 展示加载页面
     */
    private void showLoadingPage() {
        mPage_content.setVisibility(View.INVISIBLE);
        mLoadingPage.setLoadingView();
    }

    /**
     * 如果没有网络就展示空消息页面
     */
    private void showEmptyPage() {
        mPage_content.setVisibility(View.INVISIBLE);
        mLoadingPage.setEmptyView();
    }

    private void showErroPage() {
        mPage_content.setVisibility(View.INVISIBLE);
        mLoadingPage.setErrorView();
        mLoadingPage.setLoadingClickListener(new LoadingPage.LoadingClickListener() {
            @Override
            public void clickListener() {
                requestData();
            }
        });
    }

    /**
     * 对获得的文章详情判断是否有图片，
     * 如果有图片则通过替换字符串的方式获取图片url
     *
     * @param newsDetail
     */
    private void changeNewsDetail(NewsDetailBean newsDetail) {
        List<NewsDetailBean.ImgBean> imgSrcs = newsDetail.getImg();
        if (isChange(imgSrcs)) {
            String newsBody = newsDetail.getBody();
            newsBody = changeNewsBody(imgSrcs, newsBody);
            newsDetail.setBody(newsBody);
        }
    }

    /**
     * 判断是否有图片集合
     * @param imgSrcs
     * @return
     */
    private boolean isChange(List<NewsDetailBean.ImgBean> imgSrcs) {
        return imgSrcs != null && imgSrcs.size() >= 0;
    }

    /**
     * 将图片替换为能够直接使用的标签，即将<!--IMG#3-->  =》<img src="xxx">
     *
     * @param imgSrcs  获取到的图片资源数组
     * @param newsBody 修改后的文章详情
     * @return
     */
    private String changeNewsBody(List<NewsDetailBean.ImgBean> imgSrcs, String newsBody) {
        String oldChars = "";
        String newChars = "";
        for (int i = 0; i < imgSrcs.size(); i++) {
            oldChars = "<!--IMG#" + i + "-->";
            // 在客户端解决WebView图片屏幕适配的问题，在<img标签下添加style='max-width:90%;height:auto;'即可
            // 如："<img" + " style=max-width:100%;height:auto; " + "src=\"" + imgSrcs.get(i).getSrc() + "\"" + "/>"
            newChars = "<img" + " src=\"" + imgSrcs.get(i).getSrc() + "\"" + "/>";
            newsBody = newsBody.replace(oldChars, newChars);
        }
        Log.d(TAG, "changeNewsBody: " + newsBody);
        return newsBody;
    }

    /**
     * 被JavaScript调用的Android方法
     *  点击webView中的图片能够跳转到PhotoActivity中查看大图
     */
    class JavaScriptInterface {
        /**
         * 在javaScript中获得html中的图片url
         * @param imageUrl  图片url
         */
        @android.webkit.JavascriptInterface
        public void startPhotoActivity(String imageUrl) {
            Intent intent = new Intent(NewsDetailActivity.this, PhotoActivity.class);
            intent.putExtra("image_url", imageUrl);
            startActivity(intent);
        }
    }

    class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            LogUtils.d(TAG, "加载进度发生变化:" + newProgress);
        }
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            LogUtils.d(TAG, "网页开始加载:" + url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            LogUtils.d(TAG, "网页加载完成..." + url);
            mWebSettings.setBlockNetworkImage(false);
            // 网页加载完毕后，将其js方法注入到网页中
            mWebView.loadUrl("javascript:(" + IOUtils.readFromFile("js.txt") + ")()");
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            LogUtils.d(TAG, "加载的资源:" + url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            LogUtils.d(TAG, "拦截到URL信息为:" + url);
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

}
