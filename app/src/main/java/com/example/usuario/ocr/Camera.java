package com.example.usuario.ocr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewDebug;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Camera extends Activity implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private Mat mRgba;

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat viewFinder;
    List<MatOfPoint> contours = new ArrayList<>();

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    startDisplay();
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(Camera.this);

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);
        Log.i(TAG, "Trying to load OpenCV library");
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mOpenCVCallBack))
        {
            Log.e(TAG, "Cannot connect to OpenCV Manager");
        }
        else{ Log.i(TAG, "opencv successfull");
            Log.v(TAG, java.lang.Runtime.getRuntime().maxMemory()+"");
        }
    }

    private void startDisplay(){
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mOpenCVCallBack);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mOpenCVCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        Bitmap bmp = Bitmap.createBitmap(viewFinder.cols(), viewFinder.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(viewFinder, bmp);

        Intent i = new Intent(this, Principal.class);
        i.putExtra("img",bmp);
        startActivity(i);
        finish();
        return false; // don't need subsequent touch events
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Imgproc.rectangle(mRgba, new Point(500 * 1 / 3, 500 * 1 / 3), new Point(900 * 2 / 3, 500 * 2 / 3), new Scalar(0));

        Rect roi = new Rect(new Point(500 * 1 / 3 , 500 * 1 / 3), new Point(900 * 2 / 3, 500 * 2 / 3));

        viewFinder = mRgba.submat(roi);
        /*Bitmap bmp = Bitmap.createBitmap(viewFinder.cols(), viewFinder.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(viewFinder, bmp);
        bmp = toEscalaDeGris(bmp);
        Utils.bitmapToMat(bmp,viewFinder);*/
        //findBiggestContour(viewFinder);


        return mRgba;
    }
    private void findBiggestContour(Mat src) {
        contours.clear();
        Mat image32S = new Mat();

        src.convertTo(image32S, CvType.CV_8UC1);
        Mat matObject = new Mat(new Size(576,648),CvType.CV_8UC1,new Scalar(0));
        //Log.v("estado","External:" + Imgproc.RETR_EXTERNAL + ", Floodfill:" + Imgproc.RETR_FLOODFILL);
        Imgproc.findContours(src, contours, matObject, Imgproc.RETR_FLOODFILL, Imgproc.CHAIN_APPROX_NONE);

        double maxArea = 0;
        int idxMax = 0;
        for (int i = 0; i < contours.size(); i++) {
            double rozmiar = Math.abs(Imgproc.contourArea(contours.get(i)));
            if (rozmiar > maxArea) {
                maxArea = rozmiar;
                idxMax = i;
            }
        }

        Imgproc.drawContours(mRgba, contours, idxMax, new Scalar(100, 255, 99, 255), Core.FILLED);

        if (contours.size() >= 1) {
            Rect r = Imgproc.boundingRect(contours.get(idxMax));
            Imgproc.rectangle(mRgba, r.tl(), r.br(), new Scalar(255, 0, 0, 255), 3, 8, 0); //draw rectangle
        }
    }

    public static Bitmap toEscalaDeGris(Bitmap bmpOriginal) {
        Bitmap bmpGrayscale = Bitmap.createBitmap(bmpOriginal.getWidth(),bmpOriginal.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
}