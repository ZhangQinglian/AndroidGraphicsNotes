package com.zql.mobile.graphics.opengl;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES32;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.appcompat.app.AppCompatActivity;

import com.zql.mobile.graphics.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class OpenGLES_VertexAttributePointer_Activity extends AppCompatActivity implements SurfaceHolder.Callback {

    private EGLDisplay mEGLDisplay;
    private EGLContext mEGLContext;
    private EGLSurface mEGLSurface;
    private EGLConfig mEGLConfig;
    private static final String VERTEX_SHADER =
            "attribute vec4 aPosition;" +
                    "void main() {" +
                    "    gl_Position = aPosition;" +
                    "}";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
                    "uniform vec4 uColor;" +
                    "void main() {" +
                    "   gl_FragColor = uColor;" +
                    "}";

    private static final float TRIANGLE_COORDS[] = {
            0.5f, 0.5f,   // 0 top
            -0.5f, 0.5f,   // 1 bottom left
            0f, -0.5f    // 2 bottom right
    };

    private static final float TRIANGLE_COLOR[] = {0.1F, 0.9F, 0.4F, 1.0F};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_g_l_vertex_attribute_pointer);
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
        int program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        GLES32.glUseProgram(program);
        int triangleColorLocation = GLES32.glGetUniformLocation(program, "uColor");
        GLES32.glUniform4fv(triangleColorLocation, 1, createFloatBuffer(TRIANGLE_COLOR));
        int aPositionLocation = GLES32.glGetAttribLocation(program, "aPosition");
        GLES32.glEnableVertexAttribArray(aPositionLocation);
        FloatBuffer vertexBuffer = createFloatBuffer(TRIANGLE_COORDS);
        GLES32.glVertexAttribPointer(aPositionLocation, 2, GLES32.GL_FLOAT, false, 2 * 4, vertexBuffer);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_STRIP, 0, 3);
        EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES32.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES32.glCreateProgram();
        GLES32.glAttachShader(program, vertexShader);
        GLES32.glAttachShader(program, pixelShader);
        GLES32.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES32.glGetProgramiv(program, GLES32.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES32.GL_TRUE) {
            GLES32.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }


    public int loadShader(int shaderType, String source) {
        int shader = GLES32.glCreateShader(shaderType);
        GLES32.glShaderSource(shader, source);
        GLES32.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES32.glGetShaderiv(shader, GLES32.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            GLES32.glDeleteShader(shader);
            GLES32.glGetShaderInfoLog(shader);
            shader = 0;
        }
        return shader;
    }

    public FloatBuffer createFloatBuffer(float[] coords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.position(0);
        return fb;
    }
}
