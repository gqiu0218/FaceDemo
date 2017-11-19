package face.gqiu.com.faceplusdemo;

import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import net.bither.util.NativeUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import face.gqiu.com.faceplusdemo.utils.FileUtil;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {
    private Camera mCamera;
    private SurfaceView mSurfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.takephoto_btn).setOnClickListener(this);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        init();
    }

    private void init() {
        initCamera();
        SurfaceHolder holder = mSurfaceView.getHolder();
        int[] screenWH = getScreenAndHeight();
        holder.setFixedSize(screenWH[0], screenWH[1]);
        holder.setKeepScreenOn(true);// 屏幕常亮
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);
    }


    private void initCamera() {
        try {
            mCamera = Camera.open(1);
        } catch (Exception e) {
            mCamera = null;
        }
    }

    private void showViews(SurfaceHolder holder) {
        // 预览相机,绑定
        if (mCamera == null) {
            return;
        }


        try {
            mCamera.setPreviewDisplay(holder);
            Camera.Parameters params = mCamera.getParameters();// 获取相机参数集
            Camera.Size pictureSize = params.getSupportedPictureSizes().get(0);// 从List取出Size
            params.setPictureSize(pictureSize.width, pictureSize.height);// 设置照片的大小
            mCamera.setParameters(params);

            mCamera.setDisplayOrientation(90);

            // 开始预览
            mCamera.startPreview();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            initCamera();
            SurfaceHolder holder = mSurfaceView.getHolder();
            holder.addCallback(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        clearCamera();

    }


    /**
     * 释放相机的内存
     */
    private void clearCamera() {
        if (mCamera != null) {
            // 停止预览
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            // 释放相机资源
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        showViews(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        showViews(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        clearCamera();
    }

    private int[] getScreenAndHeight() {
        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels;
        int height = outMetrics.heightPixels;
        return new int[]{width, height};
    }

    @Override
    public void onClick(View v) {
        mCamera.takePicture(null, null, new PictureCallback());
    }

    private class PictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                Bundle bundle = new Bundle();
                bundle.putByteArray("bytes", data); //将图片字节数据保存在bundle当中，实现数据交换
                saveToSDCard(data); // 保存图片到sd卡中
                Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
                camera.startPreview(); // 拍完照后，重新开始预览

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public String saveToSDCard(byte[] data) throws IOException {
        String path = FileUtil.getTempPic(this);
        File file = new File(path);
        FileOutputStream outputStream = new FileOutputStream(file); // 文件输出流
        outputStream.write(data);
        outputStream.close();

        String destPath = FileUtil.getCompressPic(this);
        NativeUtil.compressBitmap(BitmapFactory.decodeFile(path), destPath, true);

        return path;
    }

}
