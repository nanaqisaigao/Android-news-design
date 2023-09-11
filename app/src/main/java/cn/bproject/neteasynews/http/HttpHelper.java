package cn.bproject.neteasynews.http;

import android.text.TextUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import cn.bproject.neteasynews.Utils.IOUtils;
import cn.bproject.neteasynews.Utils.LogUtils;
import cn.bproject.neteasynews.Utils.StringUtils;

/**
 * HttpClient链接封装，执行HTTP请求
 *
 * 无论是使用HttpGet，还是使用HttpPost，都必须通过如下3步来访问HTTP资源。
 *
 * 1.创建HttpGet或HttpPost对象，将要请求的URL通过构造方法传入HttpGet或HttpPost对象。
 *
 * 2.使用DefaultHttpClient类的execute方法发送HTTP GET或HTTP POST请求，并返回HttpResponse对象。
 *
 * 3.通过HttpResponse接口的getEntity方法返回响应信息，并进行相应的处理。
 *
 * 如果使用HttpPost方法提交HTTP POST请求，则需要使用HttpPost类的setEntity方法设置请求参数。
 * 参数则必须用NameValuePair[]数组存储。
 *
 *
 */
//表示不检测过期的方法,不显示使用了不赞成使用的类或方法时的警告
@SuppressWarnings("deprecated")
public class HttpHelper {

    private static final String TAG = HttpHelper.class.getSimpleName();

    /**
     * get请求，获取返回的字符串内容
     */
    public static void get(String url, HttpCallbackListener httpCallbackListener) {
        HttpGet httpGet = new HttpGet(url);
        //调用 execute() 方法执行HTTP请求
        execute(url, httpGet, httpCallbackListener);
    }

    /**
     * post请求，向服务器提交数据。
     */
    public static void post(String url, byte[] bytes, HttpCallbackListener httpCallbackListener) {
        HttpPost httpPost = new HttpPost(url);
        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(bytes);
        httpPost.setEntity(byteArrayEntity);
        execute(url, httpPost, httpCallbackListener);
    }

    /**
     * 下载
     */
    public static void download(String url, HttpCallbackListener httpCallbackListener) {
        HttpGet httpGet = new HttpGet(url);
        execute(url, httpGet, httpCallbackListener);
    }

    /**
     * 执行网络访问
     */
    private static void execute(String url, HttpRequestBase requestBase, HttpCallbackListener httpCallbackListener) {
        //检查 URL 是否以 "https://" 开头来判断是否需要采用 HTTPS 协议。如果 URL 以 "https://" 开头
        boolean isHttps = url.startsWith("https://");
        //则创建一个支持 HTTP 和 HTTPS 协议的客户端(HttpClientFactory中)
        AbstractHttpClient httpClient = HttpClientFactory.create(isHttps);
        //创建一个 HttpContext 对象，用于管理 HTTP 上下文信息
        HttpContext httpContext = new SyncBasicHttpContext(new BasicHttpContext());
        //获取 HTTP 客户端的请求重试处理器。这是用于处理网络请求失败后的重试机制
        HttpRequestRetryHandler retryHandler = httpClient.getHttpRequestRetryHandler();
        int retryCount = 0;//初始化重试次数计数器，用于记录重试的次数
        boolean retry = true;//初始化重试标志，表示是否需要继续重试
        while (retry) {
            try {
        //使用 httpClient 执行 requestBase 所代表的 HTTP 请求(GET。POST……)，访问网络，并将响应存储在 response 变量中。
                HttpResponse response = httpClient.execute(requestBase, httpContext);
                //获取 HTTP 响应的状态码，以便后续处理
                int stateCode  = response.getStatusLine().getStatusCode();
//                LogUtils.e(TAG, "http状态码：" + stateCode);
                if (response != null) {
                    //如果 HTTP 响应状态码为 200（HTTP_OK），表示请求成功，进入处理成功的分支
                    if (stateCode == HttpURLConnection.HTTP_OK){
                        //进一步处理响应数据  获取返回的字符串或者流
                        HttpResult httpResult = new HttpResult(response, httpClient, requestBase);
                        String result = httpResult.getString();
                        //响应数据为空，则抛出运行时异常，表示数据为空
                        if (!TextUtils.isEmpty(result)){
                            httpCallbackListener.onSuccess(result);
                            return;
                        } else {
                            throw new RuntimeException("数据为空");
                        }
                    } else {//如果响应状态码不是 200（HTTP_OK），则抛出运行时异常，表示请求失败
                        throw new RuntimeException(HttpRequestCode.ReturnCode(stateCode));
                    }
                }
            } catch (Exception e) {//捕获异常，表示在执行 HTTP 请求时发生了错误。这可能包括网络连接问题、超时等
                IOException ioException = new IOException(e.getMessage());
                //将异常交给重试机制，并检查是否需要继续重试。如果需要重试
                retry = retryHandler.retryRequest(ioException, ++retryCount, httpContext);
                LogUtils.e(TAG, "重复次数：" + retryCount + "   :"+ e);
                if (!retry){
                    httpCallbackListener.onError(e);
                }
            }
        }
    }

    /**
     * http的返回结果的封装，可以直接从中获取返回的字符串或者流
     */
    public static class HttpResult {
        private HttpResponse mResponse;//用于存储 HTTP 响应对象，包括响应头和响应体
        private InputStream mIn;//存储响应体的输入流。这个输入流用于从响应体中读取原始数据。
        private String mStr;//成员变量 mStr 用于存储响应数据的字符串形式。这个字符串是从输入流中读取的，并且在第一次获取后会被缓存
        private HttpClient mHttpClient;//用于存储 HTTP 客户端对象。这个对象在构造 HttpResult 时传递进来，用于处理 HTTP 请求
        private HttpRequestBase mRequestBase;//用于存储 HTTP 请求对象
        public HttpResult(HttpResponse response, HttpClient httpClient, HttpRequestBase requestBase) {
            mResponse = response;
            mHttpClient = httpClient;
            mRequestBase = requestBase;
        }
        //用于获取 HTTP 响应的状态码
        public int getCode() {
            StatusLine status = mResponse.getStatusLine();
            return status.getStatusCode();
        }


        /**
         * 从结果中获取字符串，一旦获取，会自动关流，并且把字符串保存，方便下次获取
         */
        public String getString() {
            //方法会检查成员变量 mStr 是否已经存储了响应数据的字符串。如果已经存储了，说明之前已经获取过响应数据，
            // 就直接返回缓存的字符串，避免重复获取
            if (!StringUtils.isEmpty(mStr)) {
                return mStr;
            }
            //如果 mStr 为空（即没有缓存数据），则调用 getInputStream() 方法获取响应的输入流。getInputStream()
            // 方法用于获取响应体的输入流，这是响应数据的原始形式
            InputStream inputStream = getInputStream();
            //创建一个 ByteArrayOutputStream 对象 out，它用于将输入流中的数据写入内存中的字节数组
            ByteArrayOutputStream out = null;
            if (inputStream != null) {
                try {
                    //接收从输入流中读取的数据
                    out = new ByteArrayOutputStream();
                    //创建一个字节数组 buffer，用于临时存储从输入流读取的数据。
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;//用于记录每次从输入流中读取的字节数
                    //从输入流中读取数据并写入 out 流。循环会一直执行，直到读取完所有数据。
                    while ((len = inputStream.read(buffer)) != -1) {
                        //成功读取数据，就将 buffer 中的数据写入 out 流，从而将数据存储在内存中
                        out.write(buffer, 0, len);
                    }
                    //将 out 流中的数据转换为字节数组 data。
                    byte[] data = out.toByteArray();
                    mStr = new String(data, "utf-8");
                } catch (Exception e) {
                    LogUtils.e(TAG, e);
                } finally {
                    //关闭 out 流，释放资源。
                    IOUtils.close(out);
                    close();
                }
            }
            return mStr;
        }

        /**
         * 获取流，需要使用完毕后调用close方法关闭网络连接
         */
        public InputStream getInputStream() {
            if (mIn == null && getCode() < 300) {
                HttpEntity entity = mResponse.getEntity();
                try {
                    mIn = entity.getContent();
                } catch (Exception e) {
                    LogUtils.e(TAG, e);
                }
            }
            return mIn;
        }

        /**
         * 关闭网络连接
         */
        public void close() {
            if (mRequestBase != null) {
                mRequestBase.abort();
            }
            IOUtils.close(mIn);
            if (mHttpClient != null) {
                mHttpClient.getConnectionManager().closeExpiredConnections();
            }
        }
    }
}
