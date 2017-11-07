package face.gqiu.com.faceplusdemo.network;


import java.util.concurrent.TimeUnit;

import face.gqiu.com.faceplusdemo.entity.ResponseResult;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * 网络请求
 * Created by gqiu on 2016/6/12.
 */
public class HttpEngine {
    private static HttpEngine instance;
    private static Retrofit mRetrofit;
    private static ApiInterface mApiInterface;

    private HttpEngine() {
    }

    public static HttpEngine getInstance() {
        if (instance == null) {
            instance = new HttpEngine();
        }

        if (mRetrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(false)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(new GzipRequestInterceptor())
                    .build();

            mRetrofit = new Retrofit.Builder()
                    .baseUrl(ApiInterface.BASE_URL)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(DecodeConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }

        if (mApiInterface == null) {
            mApiInterface = mRetrofit.create(ApiInterface.class);
        }
        return instance;
    }


    /**
     * 登录
     *
     * @param username 用户名
     * @param pwd      密码
     */
    public void login(String username, String pwd, Action0 doBefore, Subscriber<ResponseResult> subscriber) {
        mApiInterface.login(username, pwd)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(doBefore)
                .subscribe(subscriber);
    }

}
