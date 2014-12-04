package io.cyclop.sdk.empty;

import android.graphics.Bitmap;
import android.util.Log;



public class YourLab extends Lab {
    /**
     * Additional parameters such as e.g. an index computed during the processing
     * should be declared here **/

    YourLab(Bitmap bitmap){
        super(bitmap);
    }


    YourLab(byte[] yuv420sp, int width, int height){
        super(yuv420sp, width, height);
    }

    /**
     * process() is the main method -- it is called by UI thread in MainActivity
     * and should implement the processing you want to apply to the image
     */

    @Override
    void process(){
        example_method();
    }

    /**
     * This is your workspace! You should implement your methods here...
     */

    /*  turn image into black   */
    void example_method(){
        int c, x, y;
        for(c = 0; c < 3; c++)
            for(x = 0; x < nx; x++)
                for(y = 0; y < ny; y++)
                    floats[c][x][y] = 0;

    }
}
