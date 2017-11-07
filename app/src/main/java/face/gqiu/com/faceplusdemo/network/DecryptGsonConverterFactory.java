package face.gqiu.com.faceplusdemo.network;



import  android.util.Log;

import com.google.gson.TypeAdapter;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;


/**
 * 解密
 * Created by gqiu on 2016/8/9.
 */

class DecryptGsonConverterFactory<T> implements Converter<ResponseBody, T> {
    private final TypeAdapter<T> adapter;

    DecryptGsonConverterFactory(TypeAdapter<T> adapter) {
        this.adapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        String newValue = value.string();
        Log.e("gqiu", "返回数据:" + newValue);

        return adapter.fromJson(newValue);
    }

}