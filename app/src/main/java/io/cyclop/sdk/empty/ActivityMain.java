package io.cyclop.sdk.empty;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityMain extends Activity {

    TextView tv;
    Button b_camera, b_process;
    Bitmap bm0, bm1;
    boolean flip;
    ImageView image_view;
    static final int REQUEST_NEW_IMAGE = 1111;
    String currentDateTimeString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tv = (TextView) findViewById(R.id.textView);
        tv.setText(R.string.hello);

        image_view = (ImageView) findViewById(R.id.imageView);

        bm0 = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.lena512), 512, 512, false);

        bm1 = bm0;
        flip = false;
        image_view.setImageBitmap(bm0);
        image_view.setOnClickListener(flip_image);

        b_camera = (Button) findViewById(R.id.camera_button);
        b_camera.setOnClickListener(load_camera);

        b_process = (Button) findViewById(R.id.sharpness_button);
        b_process.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new ProcessTask().execute();
                return;
            }
        });


    }

    private OnClickListener load_camera = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent launchCamera = new Intent();
            launchCamera.setClassName(ActivityMain.this, "io.cyclop.sdk.empty.ActivityCamera");
            launchCamera.putExtra("newimage", "");
            startActivityForResult(launchCamera, REQUEST_NEW_IMAGE);
            return;
        }
    };

    private OnClickListener flip_image = new OnClickListener() {
        @Override
        public void onClick(View v) {
            flip = !flip;
            if(flip){
                image_view.setImageBitmap(bm1);
            }
            else{
                image_view.setImageBitmap(bm0);
            }
            return;
        }
    };


    private class ProcessTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog = new ProgressDialog(ActivityMain.this);
        void show() {
            dialog.setMessage("Processing...");
            dialog.show();
        }
        void hide() {
            dialog.dismiss();
        }
        @Override
        protected void onPreExecute() {
            show();
        }
        @Override
        protected Void doInBackground(Void... ok) {
            Lab lab = new YourLab(bm0);
            lab.process();
            bm1 = lab.getBitmap();
            return null;
        }
        protected void onPostExecute(Void ok) {
            image_view.setImageBitmap(bm1);
            flip = true;
            hide();
            return;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        // See which child activity is calling us back.
        switch (requestCode) {
            case REQUEST_NEW_IMAGE:
                if(resultCode==RESULT_OK){
                    Bundle extras = intent.getExtras();
                    byte[] imagebytes = extras.getByteArray("newimage");
                    //Log.d("",String.valueOf(imagebytes.length));
                    bm0 = rotate(BitmapFactory.decodeByteArray(imagebytes, 0, imagebytes.length),-1);
                    bm1 = bm0;
                    image_view.setImageBitmap(bm0);
                    currentDateTimeString = SimpleDateFormat.getDateTimeInstance().format(new Date());
                    currentDateTimeString = currentDateTimeString.replace(":","");
                    currentDateTimeString = currentDateTimeString.replace(" ","");

                    //new File(rootsd + "/rectangles/prefilters").mkdirs();
                    //String imagename = currentDateTimeString;
                    //String path = rootsd.getAbsolutePath() + "/rectangles/prefilters/" + imagename + ".png";
                    //File file = new File(path);
                    /*FileOutputStream out;
                    try {
                        out = new FileOutputStream(file);
                        bmp0.compress(Bitmap.CompressFormat.PNG, 100, out);

                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    */
                    image_view.setImageBitmap(bm0);

                }
                else{
                    Toast.makeText(ActivityMain.this, "image capture failed", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }

    }

    public Bitmap rotate(Bitmap image, int q) {
        int nx = image.getWidth();
        int ny = image.getHeight();

        int[] pix0 = new int[nx * ny];
        int[] pix1 = new int[nx * ny];

        image.getPixels(pix0, 0, nx, 0, 0, nx, ny);

        switch (q) {
            case 1:
                for (int y = 0; y < ny; y++) {
                    for (int x = 0; x < nx; x++) {
                        int index0 = y * nx + x;
                        int index1 = (nx - x - 1) * ny + y;
                        pix1[index1] = pix0[index0];

                    }
                }
                break;
            case -1:
                for (int y = 0; y < ny; y++) {
                    for (int x = 0; x < nx; x++) {
                        int index0 = y * nx + x;
                        int index1 = x * ny + ny - 1 - y;
                        pix1[index1] = pix0[index0];

                    }
                }
                break;
        }
        Bitmap bitmap = Bitmap.createBitmap(ny, nx, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pix1,0,ny, 0, 0, ny, nx);
        return bitmap;
    }

}
