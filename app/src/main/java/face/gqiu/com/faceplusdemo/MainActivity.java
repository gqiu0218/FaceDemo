package face.gqiu.com.faceplusdemo;

import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facepp.library.util.ConUtil;
import com.facepp.library.util.DialogUtil;
import com.facepp.library.util.ICamera;
import com.facepp.library.util.OpenGLDrawRect;
import com.facepp.library.util.Screen;
import com.facepp.library.util.SensorEventUtil;
import com.megvii.facepp.sdk.Facepp;

import net.bither.util.NativeUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import face.gqiu.com.faceplusdemo.utils.FileUtil;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener, GLSurfaceView.Renderer {
    private Camera mCamera;
    private GLSurfaceView mGlSurfaceView;
    private Facepp facepp;
    private SensorEventUtil sensorUtil;
    private ICamera mICamera;
    private DialogUtil mDialogUtil;
    private HashMap<String, Integer> resolutionMap;
    private int mAngle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        Screen.initialize(this);
        setContentView(R.layout.activity_main);
        findViewById(R.id.takephoto_btn).setOnClickListener(this);
        mGlSurfaceView = (GLSurfaceView) findViewById(R.id.surfaceView);
        init();
    }

    private void init() {
        facepp = new Facepp();
        sensorUtil = new SensorEventUtil(this);
        mGlSurfaceView.setEGLContextClientVersion(2); // 创建一个OpenGL ES 2.0
        mGlSurfaceView.setRenderer(this);// 设置渲染器进入gl
        mGlSurfaceView.setRenderMode(mGlSurfaceView.RENDERMODE_WHEN_DIRTY);// 设置渲染器模式
        mGlSurfaceView.setOnClickListener(this);
        mICamera = new ICamera();
        mDialogUtil = new DialogUtil(this);

        resolutionMap = new HashMap<>();
        resolutionMap.put("width", 1080);
        resolutionMap.put("height", 1920);
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
        ConUtil.acquireWakeLock(this);
        mCamera = mICamera.openCamera(false, this, resolutionMap);
        if (mCamera != null) {
            mAngle = 360 - mICamera.Angle;
            RelativeLayout.LayoutParams layout_params = mICamera.getLayoutParam();
            mGlSurfaceView.setLayoutParams(layout_params);

            int width = mICamera.cameraWidth;
            int height = mICamera.cameraHeight;

            int left = 0;
            int top = 0;
            int right = width;
            int bottom = height;

            String errorCode = facepp.init(this, ConUtil.getFileContent(this, com.facepp.library.R.raw.megviifacepp_0_4_7_model));
            Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
            faceppConfig.interval = 100;
            faceppConfig.minFaceSize = 200;
            faceppConfig.roi_left = left;
            faceppConfig.roi_top = top;
            faceppConfig.roi_right = right;
            faceppConfig.one_face_tracking = 1;
            faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING;
            facepp.setFaceppConfig(faceppConfig);
        } else {
            mDialogUtil.showDialog(getResources().getString(com.facepp.library.R.string.camera_error));
        }
    }


    private void setConfig(int rotation) {
        Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
        if (faceppConfig.rotation != rotation) {
            faceppConfig.rotation = rotation;
            facepp.setFaceppConfig(faceppConfig);
        }
    }

    /**
     * 画绿色框
     */
    private void drawShowRect() {
        mPointsMatrix.vertexBuffers = OpenGLDrawRect.drawCenterShowRect(isBackCamera, mICamera.cameraWidth,
                mICamera.cameraHeight, roi_ratio);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onClick(View v) {

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
