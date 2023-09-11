package cn.bproject.neteasynews.http;

/**
 */

public interface HttpCallbackListener {
    void onSuccess(String result);
    void onError(Exception e);
}
