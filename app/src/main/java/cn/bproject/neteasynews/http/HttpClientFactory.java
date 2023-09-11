package cn.bproject.neteasynews.http;

import org.apache.http.HttpVersion;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

/**
 * HTTP（HyperText Transfer Protocol：超文本传输协议）是一种用于分布式、协作式和超媒体信息系统的应用层协议。
 * 简单来说就是一种发布和接收 HTML 页面的方法，被用于在 Web 浏览器和网站服务器之间传递信息。
 *
 * HTTP 默认工作在 TCP 协议 80 端口，用户访问网站 http:// 打头的都是标准 HTTP 服务。
 *
 * HTTP 协议以明文方式发送内容，不提供任何方式的数据加密，如果攻击者截取了Web浏览器和网站服务器之间的传输报文，
 * 就可以直接读懂其中的信息，因此，HTTP协议不适合传输一些敏感信息，比如：信用卡号、密码等支付信息。
 *
 * HTTPS（Hypertext Transfer Protocol Secure：超文本传输安全协议）是一种透过计算机网络进行安全通信的传输协议。
 * HTTPS 经由 HTTP 进行通信，但利用 SSL/TLS 来加密数据包。HTTPS 开发的主要目的，是提供对网站服务器的身份认证，
 * 保护交换数据的隐私与完整性。
 *
 * HTTPS 默认工作在 TCP 协议443端口，它的工作流程一般如以下方式：
 *
 * 1、TCP 三次同步握手
 * 2、客户端验证服务器数字证书
 * 3、DH 算法协商对称加密算法的密钥、hash 算法的密钥
 * 4、SSL 安全加密隧道协商完成
 * 5、网页以加密的方式传输，用协商的对称加密算法和密钥加密，保证数据机密性；用协商的hash算法进行数据完整性保护，
 * 保证数据不被篡改。
 *
 *
 * 配置HTTPS相关请求，执行HTTP请求
 *
*/
public class HttpClientFactory {
	/** http请求最大并发连接数 ，即同时可以执行的最大HTTP请求数量*/
	private static final int MAX_CONNECTIONS = 10;
	/** 超时时间。表示如果一个HTTP请求在10秒内没有完成，它将会超时 */
	private static final int TIMEOUT = 10 * 1000;
	/** 缓存大小 ，8 * 1024字节，即8KB*/
	private static final int SOCKET_BUFFER_SIZE = 8 * 1024;

	public static DefaultHttpClient create(boolean isHttps) {//是否支持HTTPS协议
//		HTTPParams对象包含了一系列的HTTP连接参数的配置，如连接超时时间、Socket超时时间、缓存大小等
		HttpParams params = createHttpParams();
		DefaultHttpClient httpClient = null;
		if (isHttps) {//支持http与https 进行HTTPS的相关配置
			//  注册支持的协议
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			// ThreadSafeClientConnManager线程安全管理类 用于线程安全地管理连接
			ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
			httpClient = new DefaultHttpClient(cm, params);
		} else {
			//如果 isHttps 为 false，表示只支持HTTP协议，不进行HTTPS相关的配置。
			httpClient = new DefaultHttpClient(params);
		}
		return httpClient;
	}

	private static HttpParams createHttpParams() {
		final HttpParams params = new BasicHttpParams();
		// 设置是否启用旧连接检查，默认是开启的。关闭这个旧连接检查可以提高一点点性能，但是增加了I/O错误的风险（当服务端关闭连接时）。
		// 开启这个选项则在每次使用老的连接之前都会检查连接是否可用，这个耗时大概在15-30ms之间
		HttpConnectionParams.setStaleCheckingEnabled(params, false);
		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);// 设置链接超时时间
		HttpConnectionParams.setSoTimeout(params, TIMEOUT);// 设置socket超时时间
		HttpConnectionParams.setSocketBufferSize(params, SOCKET_BUFFER_SIZE);// 设置缓存大小
		HttpConnectionParams.setTcpNoDelay(params, true);// 是否不使用延迟发送(true为不延迟)
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1); // 设置协议版本
		HttpProtocolParams.setUseExpectContinue(params, true);// 设置异常处理机制
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);// 设置编码
		HttpClientParams.setRedirecting(params, false);// 设置是否采用重定向

		ConnManagerParams.setTimeout(params, TIMEOUT);// 设置超时
		ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(MAX_CONNECTIONS));// 多线程最大连接数
		ConnManagerParams.setMaxTotalConnections(params, 10); // 多线程总连接数
		return params;
	}

}
