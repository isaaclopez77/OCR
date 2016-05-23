package com.example.usuario.ocr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

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
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Camera extends Activity implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private Mat mRgba;
    private int contador;
    private TextView tvMatricula;
    private Bitmap currentBitmap;
    private String currentLicensePlate;

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat viewFinder;
    //List<MatOfPoint> contours = new ArrayList<>();

    public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/SimpleAndroidOCR/";
    public static final String lang = "eng";


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
        tvMatricula = (TextView)findViewById(R.id.tvMatricula);
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

    /*private Mat segundoIntento(Mat mat){
        // Consider the image for processing
        Mat imageHSV = new Mat(mat.size(), 1);
        Mat imageBlurr = new Mat(mat.size(), 1);
        Mat imageA = new Mat(mat.size(), 127);
        Imgproc.cvtColor(mat, imageHSV, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(imageHSV, imageBlurr, new Size(5,5), 0);
        Imgproc.adaptiveThreshold(imageBlurr, imageA, 255,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,7, 5);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(imageA, contours, new Mat(), Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);

        //Imgproc.drawContours(imageBlurr, contours, 1, new Scalar(0,0,255));
        for(int i=0; i< contours.size();i++){
            if (Imgproc.contourArea(contours.get(i)) > 50 ){
                Rect rect = Imgproc.boundingRect(contours.get(i));
                System.out.println(rect.height);
                if (rect.height > 28){
                    //System.out.println(rect.x +","+rect.y+","+rect.height+","+rect.width);
                    Imgproc.rectangle(mat, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height),new Scalar(0,0,255));
                }
            }
        }
        Log.e("devolviendo","ulti");
        return imageHSV;
    }*/

    public boolean onTouch(View v, MotionEvent event) {
        /*viewFinder = toEscalaDeGris(viewFinder);


        Bitmap bmp = Bitmap.createBitmap(viewFinder.cols(), viewFinder.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(viewFinder, bmp);*/


        Intent i = new Intent(this, Principal.class);
        i.putExtra("img",currentBitmap);
        i.putExtra("matricula",currentLicensePlate);
        startActivity(i);
        finish();
        return false; // don't need subsequent touch events
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Imgproc.rectangle(mRgba, new Point(500 * 1 / 3, 550 * 1 / 3), new Point(1000 * 2 / 3, 450 * 2 / 3), new Scalar(0, 255, 0));

        Rect roi = new Rect(new Point(500 * 1 / 3 , 550 * 1 / 3), new Point(1000 * 2 / 3, 450 * 2 / 3));

        viewFinder = mRgba.submat(roi);
        //viewFinder = segundoIntento(viewFinder);
        //findBiggestContour(viewFinder);
        if(contador % 20 == 0){
            new OCRTask().execute(viewFinder);
        }
        contador++;
        return mRgba;
    }

    /*private void findBiggestContour(Mat src) {
        contours.clear();
        Mat image32S = new Mat();

        src.convertTo(image32S, CvType.CV_8UC1);
        Mat matObject = new Mat(new Size(576,648),CvType.CV_8UC1,new Scalar(0));
        //Log.v("estado","External:" + Imgproc.RETR_EXTERNAL + ", Floodfill:" + Imgproc.RETR_FLOODFILL);
        Imgproc.findContours(src, contours, matObject, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

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
    }*/

    public static Mat toEscalaDeGris(Mat mat) {
        Mat imageHSV = new Mat(mat.size(), 1);
        Mat imageBlurr = new Mat(mat.size(), 1); // blanco y negro
        Mat imageA = new Mat(mat.size(), 127); // eliminar ruido
        Imgproc.cvtColor(mat, imageHSV, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(imageHSV, imageBlurr, new Size(5,5), 0);
        Imgproc.adaptiveThreshold(imageBlurr, imageA, 255,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,7, 5);
        return imageBlurr;
    }

    class OCRTask extends AsyncTask<Mat,Void,String>{

        @Override
        protected String doInBackground(Mat... params) {

            Mat mat = params[0];

            Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
            mat = toEscalaDeGris(mat);

            Utils.matToBitmap(mat, bmp);

            TessBaseAPI baseApi = new TessBaseAPI();
            baseApi.setDebug(true);
            baseApi.init(DATA_PATH, lang);
            baseApi.setImage(bmp);
            String recognizedText = baseApi.getUTF8Text();
            baseApi.end();

            currentBitmap = bmp;
            return recognizedText;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            s = s.replaceAll("[^a-zA-Z0-9]+", " ");
            s = s.trim();

            tvMatricula.setText(s);

            Pattern pat = Pattern.compile("^[0-9]{4}\\s*[a-zA-Z]{3}$");
            Matcher mat = pat.matcher(s);
            if (mat.matches()) {
                tvMatricula.setTextColor(getResources().getColor(R.color.colorNoError));
            } else {
                tvMatricula.setTextColor(getResources().getColor(R.color.colorError));
            }

            currentLicensePlate = s;
            tvMatricula.setText(s);
        }
    }
}