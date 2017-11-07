package face.gqiu.com.faceplusdemo.network;


import face.gqiu.com.faceplusdemo.entity.ResponseResult;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * 接口
 * Created by gqiu on 2016/10/18.
 */
interface ApiInterface {
    String BASE_URL = "https://api-cn.faceplusplus.com/facepp/v3/face/analyze";

    //登录
    @POST("/api/user/login")
    Observable<ResponseResult> login(@Query("username") String userName, @Query("password") String pwd);

}
