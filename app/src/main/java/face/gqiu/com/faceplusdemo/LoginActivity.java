package face.gqiu.com.faceplusdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facepp.library.EmptyActivity;
import com.facepp.library.FaceppActionActivity;
import com.facepp.library.OpenglActivity;
import com.facepp.library.util.ConUtil;
import com.facepp.library.util.Util;
import com.megvii.facepp.sdk.Facepp;
import com.megvii.licensemanager.sdk.LicenseManager;

/**
 * 认证
 * Created by gqiu on 2017/11/19.
 */

public class LoginActivity extends Activity implements View.OnClickListener {
    private TextView msgTv;
    private Button retryBtn;
    private boolean mRequesting;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        msgTv = (TextView) findViewById(R.id.msg_tv);
        retryBtn = (Button) findViewById(R.id.retry_btn);
        retryBtn.setOnClickListener(this);
        network();
    }

    @Override
    public void onClick(View v) {
        network();
    }


    private void network() {
        if (mRequesting) {
            return;
        }

        if (Facepp.getSDKAuthType(ConUtil.getFileContent(this, R.raw.megviifacepp_0_4_7_model)) == 2) {// 非联网授权
            authState(true);
            return;
        }
        mRequesting = true;

        retryBtn.setVisibility(View.GONE);
        msgTv.setText(getResources().getString(R.string.auth_progress));
        LicenseManager licenseManager = new LicenseManager(this);
        licenseManager.setExpirationMillis(Facepp.getApiExpirationMillis(this, ConUtil.getFileContent(this, R.raw
                .megviifacepp_0_4_7_model)));

        String uuid = ConUtil.getUUIDString(this);
        long apiName = Facepp.getApiName();

        licenseManager.setAuthTimeBufferMillis(0);

        licenseManager.takeLicenseFromNetwork(uuid, Util.API_KEY, Util.API_SECRET, apiName,
                LicenseManager.DURATION_30DAYS, "Landmark", "1", true, new LicenseManager.TakeLicenseCallback() {
                    @Override
                    public void onSuccess() {
                        mRequesting = false;
                        authState(true);
                    }

                    @Override
                    public void onFailed(int i, byte[] bytes) {
                        mRequesting = false;
                        authState(false);
                    }


                });
    }

    private void authState(boolean success) {
        if (success) {
            msgTv.setText("授权成功");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, EmptyActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 500);
        } else {
            msgTv.setText(getResources().getString(R.string.auth_fail));
            retryBtn.setVisibility(View.VISIBLE);
        }

    }

}
