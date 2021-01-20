# GLSurfaceView 简单介绍

在 Android 应用层我们如果要使用 OpenGL 进行绘制，可以选择 Android 平台给我提供的 GLSurfaceView 和 GLSurfaceView.Renderer。
其中 GLSurfaceView 是 SurfaceView 的子类，GLSurfaceView.Renderer 为我们在 GLSurfaceView 上渲染提供了回调。

简单写一个例子，首先是定义 Renderer：

```kotlin
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
```
在上面的代码中，根据以往经验首先被回调的应该是 `onSurfaceCreated` 方法，在这个方法中，我们设置 OpenGL 的清屏颜色为红色。接着在 `onSurfaceChanged` 设置 OpenGL 的视窗大小。最后在 `onDrawFrame` 回调中进行 OpenGL 的清屏操作。

自定义一个 Renderer 就是这么简单，接下来是自定义 GLSurfaceView：

```kotlin
class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: Renderer

    init {
        renderer = MyRenderer()
        setRenderer(renderer)
    }
}
```
自定义 GLSurfaceView 也是非常简单，将 MyRenderer 初始化，然后通过 `setRenderer` 方法设置给 GLSurfaceView 即可。

最后是在 Activity 中使用 MyGLSurfaceView：
```kotlin
class GLSurfaceViewActivity : AppCompatActivity() {

    private lateinit var glSurfaceView: SurfaceView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glSurfaceView = MyGLSurfaceView(this)
        setContentView(glSurfaceView)
    }
}
```
很简答，不多说了，最后是运行结果图：

![](https://blog-1256162814.cos.ap-nanjing.myqcloud.com/opengl/glsurfaceview01.jpeg)

我们代码中没有调用绘制命令，这个红色是怎么绘制出来的呢？其实这是 OpenGL 的一个内置方法，在 OpenGL 每次渲染前，都需要对上一次的绘制做清屏操作，不然两次绘制的内容就会叠加在一起。
从上面的代码中可以看出来 GLSurfaceView 帮我们做了很多 OpenGL 的初始化工作，当然想要使用 OpenGL 绘制，在 Android 平台上还少不了 EGL 库，GLSurfaceView 同样给我们做了 EGL 的初始化，让我们将注意力集中在 OpenGL 的绘制上。

后面我会化点时间，重点讲一下 EGL 的初始化以及更多 OpenGL 的渲染知识。