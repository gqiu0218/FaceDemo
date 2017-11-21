package com.facepp.library;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;


public class EmptyActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }


}
