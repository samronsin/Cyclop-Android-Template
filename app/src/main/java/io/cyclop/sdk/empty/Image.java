package io.cyclop.sdk.empty;

import android.graphics.Bitmap;

import java.util.Arrays;

/**
 * Created by Samuel Ronsin on 17/09/14.
 */
public abstract class Image {
    int nx,ny;
    abstract void resize(int nx, int ny);
    abstract Bitmap getBitmap();
}

abstract class Lab extends ColorFloatImage {
    abstract void process();
    Lab(Bitmap bitmap){
        super(bitmap);
    }
    Lab(byte[] yuv420sp, int width, int height){
        super(yuv420sp, width, height);
    }
}

class ColorFloatImage extends Image {

    float[][][] floats;

    ColorFloatImage(int nx, int ny) {
        this.nx = nx;
        this.ny = ny;
        floats = new float[3][nx][ny];
    }
    ColorFloatImage(float[][][] some_floats) {
        nx = some_floats[0].length;
        ny = some_floats[0][0].length;
        this.floats = some_floats;
    }
    ColorFloatImage(FloatImage r, FloatImage g, FloatImage b){
        if (r.floats.length == g.floats.length && r.floats.length == b.floats.length && r.floats[0].length == g.floats[0].length && r.floats[0].length == b.floats[0].length){
            this.floats[0] = r.floats;
            this.floats[1] = g.floats;
            this.floats[2] = b.floats;
        }
        else throw new Error("The sizes of 3 FloatImages don't match! Can't compound into a ColorFloatImage");
    }

    ColorFloatImage(Bitmap bmp) {
        int c, x, y;
        nx = bmp.getWidth();
        ny = bmp.getHeight();
        int[] pix = new int[nx * ny];
        bmp.getPixels(pix, 0, nx, 0, 0, nx, ny);
        floats = new float[3][nx][ny];
        for (y = 0; y < ny; y++) {
            for (x = 0; x < nx; x++) {
                int index = y * nx + x;
                for(c = 0; c < 3; c++){
                    floats[c][x][y] = (pix[index] >> 8*(2-c) & 0xff);
                }
            }
        }
    }

    ColorFloatImage(byte[] yuv420sp, int width, int height) {
        nx = width;
        ny = height;
        this.floats = new float[3][nx][ny];
        // Convert YUV to RGB
        int i, j;
        int frameSize = width * height;
        int uvp, u, v;
        int y1192, r, g ,b, yp;
        for (i = 0, yp = 0; i < width; i++) {
            uvp = frameSize + (i >> 1) * height; u = 0; v = 0;
            for (j = 0; j < height; j++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                if ((j & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128; u = (0xff & yuv420sp[uvp++]) - 128;
                }
                y1192 = 1192 * y;
                r = (y1192 + 1634 * v); g = (y1192 - 833 * v - 400 * u); b = (y1192 + 2066 * u);
                if (r < 0) r = 0; else if (r > 262143) r = 262143; floats[0][nx-1-i][j] = ((float) r)/1024;
                if (g < 0) g = 0; else if (g > 262143) g = 262143; floats[1][nx-1-i][j] = ((float) g)/1024;
                if (b < 0) b = 0; else if (b > 262143) b = 262143; floats[2][nx-1-i][j] = ((float) b)/1024;
            }
        }
    }

	/*	Bilinear scaling of floats
	 *	Warning: introduces a sub-pixel translation
	 */
    @Override
    void resize(int nx1, int ny1){
        int c, x, y, nx0, ny0;
        float tmp_x, tmp_y, ax, ay;
        float[][][] tmp = new float[3][nx1][ny1];
        float zx = (float)nx1 / (float)nx;
        float zy = (float)ny1 / (float)ny;
        nx0 = nx;
        ny0 = ny;
        nx = nx1;
        ny = ny1;
        for(c = 0; c < 3; c++)
            for(x = 0; x < nx; x++){
                tmp_x = x / zx;
                ax = tmp_x - (int)tmp_x;
                for(y = 0; y < ny; y++){
                    tmp_y = y / zy ;
                    ay = tmp_y - (int)tmp_y;
                    tmp[c][x][y] =	ax * ay * floats[c][(int)tmp_x][(int)tmp_y]
                                    +	(1 - ax) * ay * (tmp_x + 1 < nx0 ? floats[c][(int)tmp_x + 1][(int)tmp_y] : floats[c][(int)tmp_x - 1][(int)tmp_y])
                                    +	ax * (1 - ay) * (tmp_y + 1 < ny0 ? floats[c][(int)tmp_x][(int)tmp_y + 1] : floats[c][(int)tmp_x][(int)tmp_y - 1])
                                    +	(1 - ax) * (1 - ay) * (tmp_x + 1 < nx0 && tmp_y + 1 < ny0 ? floats[c][(int)tmp_x + 1][(int)tmp_y + 1] :
                                            (tmp_x + 1 < nx0 ? floats[c][(int)tmp_x + 1][(int)tmp_y - 1] :
                                                (tmp_y + 1 < ny0 ? floats[c][(int)tmp_x - 1][(int)tmp_y + 1] :
                                                    floats[c][(int)tmp_x - 1][(int)tmp_y - 1] )));
                }
            }
        floats = tmp;
    }

    @Override
    public Bitmap getBitmap() {
        //String TAG = "getBitmap()";
        int[] pix = new int[nx*ny];
        int[] val = new int[3];
        int x,y,c;
        for (x = 0; x < nx; x++){
            for (y = 0; y < ny; y++){
                for (c = 0; c < 3; c++){
                    // restore the values after RGB modification
                    val[c] = (int)floats[c][x][y];
                }
                int index = y * nx + x ;
                pix[index] = 0xff000000 | ((val[0]>255?255:(val[0]<0?0:val[0])) << 16) | ((val[1]>255?255:(val[1]<0?0:val[1])) << 8) | ((val[2]>255?255:(val[2]<0?0:val[2])));
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(nx, ny, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pix, 0, nx, 0, 0, nx, ny);
        return bitmap;
    }
}


class FloatImage extends Image {

    float[][] floats;

    FloatImage(int nx, int ny) { floats = new float[nx][ny]; }
    FloatImage(float[][] im) { this.floats = im; }
    FloatImage(Bitmap bmp){ Image(bmp); }
    FloatImage(byte[] bytes, int nx, int ny){ Image(bytes, nx, ny); }


    public void Image(Bitmap bmp) {
        int nx = bmp.getWidth();
        int ny = bmp.getHeight();
        int[] pix = new int[nx * ny];
        bmp.getPixels(pix, 0, nx, 0, 0, nx, ny);
        floats = new float[nx][ny];
        for (int y = 0; y < ny; y++) {
            for (int x = 0; x < nx; x++) {
                int index = y * nx + x;
                floats[x][y] = 0.3086f * (float) ((pix[index] >> 16) & 0xff)
                         + 0.6094f * (float) ((pix[index] >> 8) & 0xff)
                         + 0.0820f * (float) (pix[index] & 0xff);
            }
        }
    }

    public void Image(byte[] yuv420sp, int width, int height) {
        int nx = width;
        int ny = height;
        this.floats = new float[nx][ny];
        // Convert YUV to RGB
        int i, j;
        int frameSize = width * height;
        int uvp, u, v;
        int y1192, r, g ,b, yp;
        for (i = 0, yp = 0; i < width; i++) {
            uvp = frameSize + (i >> 1) * height; u = 0; v = 0;
            for (j = 0; j < height; j++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                if ((j & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128; u = (0xff & yuv420sp[uvp++]) - 128;
                }
                y1192 = 1192 * y;
                r = (y1192 + 1634 * v); g = (y1192 - 833 * v - 400 * u); b = (y1192 + 2066 * u);
                if (r < 0) r = 0; else if (r > 262143) r = 262143;
                if (g < 0) g = 0; else if (g > 262143) g = 262143;
                if (b < 0) b = 0; else if (b > 262143) b = 262143;
                //index = i * height + (height - j - 1);
                floats[nx-1-i][j] = (float)( r*0.3086 + g*0.6094 + b*0.0820)/1024;
            }
        }
    }

    /*	Bilinear scaling of floats
     *	Warning: introduces a sub-pixel translation
     */
    @Override
    void resize(int nx1, int ny1){
        int x, y, nx0, ny0;
        float tmp_x, tmp_y, ax, ay;
        float[][] tmp = new float[nx1][ny1];
        float zx = (float)nx1 / (float)nx;
        float zy = (float)ny1 / (float)ny;
        nx0 = nx;
        ny0 = ny;
        nx = nx1;
        ny = ny1;
        for(x = 0; x < nx; x++){
            tmp_x = x / zx;
            ax = tmp_x - (int)tmp_x;
            for(y = 0; y < ny; y++){
                tmp_y = y / zy ;
                ay = tmp_y - (int)tmp_y;
                tmp[x][y] =	ax * ay * floats[(int)tmp_x][(int)tmp_y]
                            +	(1 - ax) * ay * (tmp_x + 1 < nx0 ? floats[(int)tmp_x + 1][(int)tmp_y] : floats[(int)tmp_x - 1][(int)tmp_y])
                            +	ax * (1 - ay) * (tmp_y + 1 < ny0 ? floats[(int)tmp_x][(int)tmp_y + 1] : floats[(int)tmp_x][(int)tmp_y - 1])
                            +	(1 - ax) * (1 - ay) * (tmp_x + 1 < nx0 && tmp_y + 1 < ny0 ? floats[(int)tmp_x + 1][(int)tmp_y + 1] :
                                (tmp_x + 1 < nx0 ? floats[(int)tmp_x + 1][(int)tmp_y - 1] :
                                    (tmp_y + 1 < ny0 ? floats[(int)tmp_x - 1][(int)tmp_y + 1] :
                                        floats[(int)tmp_x - 1][(int)tmp_y - 1] )));
            }
        }
    floats = tmp;
    }

    @Override
    public Bitmap getBitmap() {
        String TAG = "getBitmap()";
        int nx = floats.length;
        int ny = floats[0].length;
        int[] pix = new int[nx*ny];
        int[] val = new int[3];
        int x,y,c;
        for (x = 0; x < nx; x++){
            for (y = 0; y < ny; y++){
                for (c = 0; c < 3; c++){
                    // restore the values after RGB modification
                    val[c] = (int)floats[x][y];
                }
                int index = y * nx + x ;
                pix[index] = 0xff000000 | ((val[0]>255?255:(val[0]<0?0:val[0])) << 16) | ((val[1]>255?255:(val[1]<0?0:val[1])) << 8) | ((val[2]>255?255:(val[2]<0?0:val[2])));
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(nx, ny, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pix, 0, nx, 0, 0, nx, ny);
        return bitmap;
    }
}

