package com.zql.mobile.graphics.opengl

import android.content.Context
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLSurfaceViewActivity : AppCompatActivity() {

    private lateinit var glSurfaceView: SurfaceView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glSurfaceView = MyGLSurfaceView(this)
        setContentView(glSurfaceView)
    }
}


class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: Renderer

    init {
        renderer = MyRenderer()
        setRenderer(renderer)
    }
}

class MyRenderer : GLSurfaceView.Renderer {
    override fun onDrawFrame(gl: GL10?) {
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES32.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES32.glClearColor(1.0F, 0F, 0F, 1F)
    }
}