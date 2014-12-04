package io.cyclop.sdk.empty;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.IOException;


public class ActivityCamera extends Activity {

    private CameraView camera_view;
    public static YourLab lab;
    Button b_capture;

    LinearLayout linear_layout;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        b_capture = (Button) findViewById(R.id.capture_button);
        b_capture.setOnClickListener(capture_image);

        camera_view = new CameraView(this);
        linear_layout = (LinearLayout) findViewById(R.id.linearLayout);
        linear_layout.addView(camera_view);

    }

    private OnClickListener capture_image = new OnClickListener() {
        public void onClick(View v){
            camera_view.camera.takePicture(null, null, myPictureCallback_JPG);
        }
    };

    PictureCallback myPictureCallback_JPG = new PictureCallback(){

        @Override
        public void onPictureTaken(byte[] argb, Camera cam) {
            Log.d("length of byte array in CameraActivity", String.valueOf(argb.length));
            Intent intent = new Intent();
            intent.setClassName(ActivityCamera.this, "io.cyclop.test.sharpness.MainActivity");
            intent.putExtra("newimage",argb);
            //NB: image sent without rotation set from the preview
            setResult(RESULT_OK, intent);
            finish();
        }};


    public static class CameraView extends SurfaceView implements SurfaceHolder.Callback {

        SurfaceHolder holder;
        static Camera camera;

        CameraView(Context context) {
            super(context);
            holder = getHolder();
            holder.addCallback(this);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            camera = Camera.open();
            try {
                camera.setPreviewDisplay(holder);
            }
            catch (IOException exception) {
                camera.release();
                camera = null;
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            //TODO: The size of the preview should be better controlled (640 x 480 seems pretty universally accepted)
            Camera.Parameters parameters = camera.getParameters();
            //parameters.setPreviewSize(640, 480);
            //TODO: Allow for bigger picture size
            parameters.setPictureSize(512, 512);
            camera.setDisplayOrientation(90);//This does not affect the order of byte array passed in onPreviewFrame(byte[], Camera)
            camera.setParameters(parameters);
            camera.startPreview();
        }
    }
}


