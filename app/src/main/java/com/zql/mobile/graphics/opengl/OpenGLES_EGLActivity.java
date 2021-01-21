package com.zql.mobile.graphics.opengl;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES32;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zql.mobile.graphics.R;

public class OpenGLES_EGLActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private EGLDisplay mEGLDisplay;
    private EGLContext mEGLContext;
    private EGLSurface mEGLSurface;
    private EGLConfig mEGLConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_g_l_e_s__e_g_l);
        SurfaceView sv = findViewById(R.id.surface_view);
        sv.getHolder().addCallback(this);
    }

    private void initEGL(SurfaceHolder holder) {
        // get an EGL display connection
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);

        int[] version = new int[2];
        // initialize EGL display connection
        EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1);

        // target attribute list
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        //get appropriate EGL frame buffer configuration
        EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length,
                numConfigs, 0);
        mEGLConfig = configs[0];

        int[] attrib3_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                EGL14.EGL_NONE
        };

        //create an EGL rendering context
        mEGLContext = EGL14.eglCreateContext(mEGLDisplay, mEGLConfig, EGL14.EGL_NO_CONTEXT,
                attrib3_list, 0);

        // create an EGL window surface
        int[] surfaceAttribs = {
                EGL14.EGL_NONE
        };
        mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, holder.getSurface(),
                surfaceAttribs, 0);

        // connect the context to the surface
        EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initEGL(holder);
        GLES32.glClearColor(1.0F, 0F, 0F, 1F);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        GLES32.glViewport(0, 0, width, height);
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT);
        EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
