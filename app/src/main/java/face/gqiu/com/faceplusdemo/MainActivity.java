package face.gqiu.com.faceplusdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import face.gqiu.com.faceplusdemo.network.HttpEngine;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    private void test() {
//        HttpEngine.getInstance().login();
    }
}
