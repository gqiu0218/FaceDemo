package com.facepp.library;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.facepp.library.util.CameraMatrix;
import com.facepp.library.util.ConUtil;
import com.facepp.library.util.DialogUtil;
import com.facepp.library.util.ICamera;
import com.facepp.library.util.OpenGLUtil;
import com.facepp.library.util.PointsMatrix;
import com.facepp.library.util.Screen;
import com.facepp.library.util.SensorEventUtil;
import com.megvii.facepp.sdk.Facepp;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends Activity implements PreviewCallback, Renderer, SurfaceTexture.OnFrameAvailableListener {

    private GLSurfaceView mGlSurfaceView;
    private ICamera mICamera;
    private Camera mCamera;
    private DialogUtil mDialogUtil;
    private HandlerThread mHandlerThread = new HandlerThread("facepp");
    private Handler mHandler;
    private Facepp facepp;
    private HashMap<String, Integer> resolutionMap;
    private SensorEventUtil sensorUtil;
    private int mAngle;
    private boolean isTiming = true; // 是否是定时去刷新界面;

    boolean isSuccess = false;
    float confidence;
    float pitch, yaw, roll;
    long startTime;
    int rotation = mAngle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Screen.initialize(this);
        setContentView(R.layout.activity_main);
        mGlSurfaceView = (GLSurfaceView) findViewById(R.id.surfaceView);
        init();
    }

    private void init() {
        resolutionMap = new HashMap<>();
        resolutionMap.put("width", 1080);
        resolutionMap.put("height", 1920);
        facepp = new Facepp();

        sensorUtil = new SensorEventUtil(this);

        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        mGlSurfaceView.setEGLContextClientVersion(2);// 创建一个OpenGL ES 2.0
        mGlSurfaceView.setRenderer(this);// 设置渲染器进入gl
        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);// 设置渲染器模式

        mICamera = new ICamera();
        mDialogUtil = new DialogUtil(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        ConUtil.acquireWakeLock(this);
        startTime = System.currentTimeMillis();
        mCamera = mICamera.openCamera(false, this, resolutionMap);
        if (mCamera != null) {
            mAngle = 360 - mICamera.Angle;
            int width = mICamera.cameraWidth;
            int height = mICamera.cameraHeight;

            int left = 0;
            int top = 0;

            String errorCode = facepp.init(this, ConUtil.getFileContent(this, R.raw.megviifacepp_0_4_7_model));
            Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
            faceppConfig.interval = 25;
            faceppConfig.minFaceSize = 200;
            faceppConfig.roi_left = left;
            faceppConfig.roi_top = top;
            faceppConfig.roi_right = width;
            faceppConfig.roi_bottom = height;
            faceppConfig.one_face_tracking = 1;
            faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING;
            facepp.setFaceppConfig(faceppConfig);
        } else {
            mDialogUtil.showDialog(getResources().getString(R.string.camera_error));
        }
    }

    private void setConfig(int rotation) {
        Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
        if (faceppConfig.rotation != rotation) {
            faceppConfig.rotation = rotation;
            facepp.setFaceppConfig(faceppConfig);
        }
    }

    @Override
    public void onPreviewFrame(final byte[] imgData, final Camera camera) {
        if (isSuccess)
            return;
        isSuccess = true;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int width = mICamera.cameraWidth;
                int height = mICamera.cameraHeight;

                long faceDetectTime_action = System.currentTimeMillis();
                int orientation = sensorUtil.orientation;
                if (orientation == 0)
                    rotation = mAngle;
                else if (orientation == 1)
                    rotation = 0;
                else if (orientation == 2)
                    rotation = 180;
                else if (orientation == 3)
                    rotation = 360 - mAngle;

                setConfig(rotation);

                final Facepp.Face[] faces = facepp.detect(imgData, width, height, Facepp.IMAGEMODE_NV21);
                final long algorithmTime = System.currentTimeMillis() - faceDetectTime_action;

                if (faces != null) {
                    long actionMaticsTime = System.currentTimeMillis();
                    ArrayList<ArrayList> pointsOpengl = new ArrayList<ArrayList>();
                    confidence = 0.0f;

                    if (faces.length >= 0) {
                        for (int c = 0; c < faces.length; c++) {
                            facepp.getLandmark(faces[c], Facepp.FPP_GET_LANDMARK106);
                            Facepp.Face face = faces[c];

                            pitch = faces[c].pitch;
                            yaw = faces[c].yaw;
                            roll = faces[c].roll;
                            confidence = faces[c].confidence;

                            if (orientation == 1 || orientation == 2) {
                                width = mICamera.cameraHeight;
                                height = mICamera.cameraWidth;
                            }
                            ArrayList<FloatBuffer> triangleVBList = new ArrayList<>();
                            for (int i = 0; i < faces[c].points.length; i++) {
                                float x = (faces[c].points[i].x / height) * 2 - 1;
                                float y = 1 - (faces[c].points[i].y / width) * 2;
                                float[] pointf = new float[]{x, y, 0.0f};
                                if (orientation == 1)
                                    pointf = new float[]{-y, x, 0.0f};
                                if (orientation == 2)
                                    pointf = new float[]{y, -x, 0.0f};
                                if (orientation == 3)
                                    pointf = new float[]{-x, -y, 0.0f};

                                FloatBuffer fb = mCameraMatrix.floatBufferUtil(pointf);
                                triangleVBList.add(fb);
                            }

                            pointsOpengl.add(triangleVBList);
                        }
                    } else {
                        pitch = 0.0f;
                        yaw = 0.0f;
                        roll = 0.0f;
                    }
                    mPointsMatrix.bottomVertexBuffer = null;
                    synchronized (mPointsMatrix) {
                        mPointsMatrix.points = pointsOpengl;
                    }
                }
                isSuccess = false;
                if (!isTiming) {
                    timeHandle.sendEmptyMessage(1);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        ConUtil.releaseWakeLock();
        mICamera.closeCamera();
        mCamera = null;

        timeHandle.removeMessages(0);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        facepp.release();
    }

    private int mTextureID = -1;
    private SurfaceTexture mSurface;
    private CameraMatrix mCameraMatrix;
    private PointsMatrix mPointsMatrix;

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 黑色背景
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        mTextureID = OpenGLUtil.createTextureID();
        mSurface = new SurfaceTexture(mTextureID);
        // 这个接口就干了这么一件事，当有数据上来后会进到onFrameAvailable方法
        mSurface.setOnFrameAvailableListener(this);// 设置照相机有数据时进入
        mCameraMatrix = new CameraMatrix(mTextureID);
        mPointsMatrix = new PointsMatrix();
        mICamera.startPreview(mSurface);// 设置预览容器
        mICamera.actionDetect(this);
        if (isTiming) {
            timeHandle.sendEmptyMessageDelayed(0, 30);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 设置画面的大小
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        ratio = 1; // 这样OpenGL就可以按照屏幕框来画了，不是一个正方形了
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];

    @Override
    public void onDrawFrame(GL10 gl) {
        final long actionTime = System.currentTimeMillis();
        // Log.w("ceshi", "onDrawFrame===");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);// 清除屏幕和深度缓存
        float[] mtx = new float[16];
        mSurface.getTransformMatrix(mtx);
        mCameraMatrix.draw(mtx);
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1f, 0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

        mPointsMatrix.draw(mMVPMatrix);
        mSurface.updateTexImage();// 更新image，会调用onFrameAvailable方法
    }

    Handler timeHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mGlSurfaceView.requestRender();// 发送去绘制照相机不断去回调
                    timeHandle.sendEmptyMessageDelayed(0, 31);
                    break;
                case 1:
                    mGlSurfaceView.requestRender();// 发送去绘制照相机不断去回调
                    break;
            }
        }
    };

}
