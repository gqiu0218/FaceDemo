package face.gqiu.com.faceplusdemo.network;


import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 拦截器
 * Created by gqiu on 2016/8/5.
 */
 class GzipRequestInterceptor implements Interceptor {


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Log.e("gqiu", " 请求url=" + originalRequest.url().toString());


        return chain.proceed(originalRequest);
    }


}
