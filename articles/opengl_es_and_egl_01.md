# 什么是 EGL？

在 [EGL](https://www.khronos.org/egl) 官网是这么介绍 EGL 的：

> EGL™ is an interface between Khronos rendering APIs such as OpenGL ES or OpenVG and the underlying native platform window system. It handles graphics context management, surface/buffer binding, and rendering synchronization and enables high-performance, accelerated, mixed-mode 2D and 3D rendering using other Khronos APIs. EGL also provides interop capability between Khronos to enable efficient transfer of data between APIs – for example between a video subsystem running OpenMAX AL and a GPU running OpenGL ES.

这段介绍很长，但意思可以总结为 EGL 是设备显示与渲染引擎之间的。展开讲就是说设备上的渲染引擎比如 OpenGL ES，它只负责如何将用户输入的模型数据渲染成图形数据，但它却不知道怎么将图像显示在设备显示器上。而 EGL 专门就是做这个的，OpenGL ES 把数据给 EGL，EGL 负责将图形数据显示在设备屏幕上。

那么如果我们想要使用 OpenGL ES 进行渲染，前提就是要有 EGL 环境。那 EGL 环境该如何初始化呢？

# EGL 环境初始化

[EGL 参考文档](https://www.khronos.org/registry/EGL/sdk/docs/man/)给出了下面这个 c 的代码片段，展示了如何创建 EGL 环境。

```c
#include <stdlib.h>
#include <unistd.h>
#include <EGL/egl.h>
#include <GLES/gl.h>
typedef ... NativeWindowType;
extern NativeWindowType createNativeWindow(void);
static EGLint const attribute_list[] = {
        EGL_RED_SIZE, 1,
        EGL_GREEN_SIZE, 1,
        EGL_BLUE_SIZE, 1,
        EGL_NONE
};
int main(int argc, char ** argv)
{
        EGLDisplay display;
        EGLConfig config;
        EGLContext context;
        EGLSurface surface;
        NativeWindowType native_window;
        EGLint num_config;

        /* get an EGL display connection */
        display = eglGetDisplay(EGL_DEFAULT_DISPLAY);

        /* initialize the EGL display connection */
        eglInitialize(display, NULL, NULL);

        /* get an appropriate EGL frame buffer configuration */
        eglChooseConfig(display, attribute_list, &config, 1, &num_config);

        /* create an EGL rendering context */
        context = eglCreateContext(display, config, EGL_NO_CONTEXT, NULL);

        /* create a native window */
        native_window = createNativeWindow();

        /* create an EGL window surface */
        surface = eglCreateWindowSurface(display, config, native_window, NULL);

        /* connect the context to the surface */
        eglMakeCurrent(display, surface, surface, context);

        /* clear the color buffer */
        glClearColor(1.0, 1.0, 0.0, 1.0);
        glClear(GL_COLOR_BUFFER_BIT);
        glFlush();

        eglSwapBuffers(display, surface);

        sleep(10);
        return EXIT_SUCCESS;
}
```

在 Android 的 NDK 中是提供了 EGL 库的，所以我们是可以通过 Android NDK 来验证上述代码，但目前我并不准备深入到 NDK 层，应为 Android 也为我们提供了 EGL 的 java 层封装。我们可以按照 C 语言中的步骤，通过 java 层的 EGL 接口来初始化 EGL 环境。

首先要准备 SurfaceView，代码如下：

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".opengl.OpenGLES_EGLActivity">
    <SurfaceView
        android:id="@+id/surface_view"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
</androidx.constraintlayout.widget.ConstraintLayout>
```

```java
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {}

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}
}

```

上面的代码做的工作就是在布局文件中添加 SurfaceView，然后给 SurfaceView 的 SurfaceHolder 添加回调。

接着我们来写 EGL 的初始化方法。

```java
 private void initEGL(SurfaceHolder holder) {
        //1. get an EGL display connection
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);

        int[] version = new int[2];
        //2. initialize EGL display connection
        EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1);
        
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        //3. get appropriate EGL frame buffer configuration
        EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length,
                numConfigs, 0);
        mEGLConfig = configs[0];

        int[] attrib3_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                EGL14.EGL_NONE
        };

        //4. create an EGL rendering context
        mEGLContext = EGL14.eglCreateContext(mEGLDisplay, mEGLConfig, EGL14.EGL_NO_CONTEXT,
                attrib3_list, 0);

        //5. create an EGL window surface
        int[] surfaceAttribs = {
                EGL14.EGL_NONE
        };
        mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, holder.getSurface(),
                surfaceAttribs, 0);

        //6. connect the context to the surface
        EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
    }
```

初始化 EGL 环境大概分了 6 个步骤，在代码中都标记出来了。我们一个个来看。
1. 获得 EGLDisplay 对象，官网解释说是一个 `display connection`。可能其内部封装了连接设备显示器，获取显示器信息的方法。
2. 初始化 1 中获得的 `display connection`。这一步传入了 version 数组，作用是用来存放调用 `eglInitialize` 方法后获取的 EGL 的主版本和次版本。因为在 c 语言中一般是通过入参来传递返回值的，所以这里也是类似。
3. 获得显示器支持的图像缓冲配置，这里主要指定了各个颜色的深度和 alpha 通道的深度。然后调用 `eglChooseConfig` 方法后会返回多个支持我们指定配置的配置。这些配置会按匹配程度排序，数组第一个是最接近我们需要的配置。
4. 拿到配置后就可以创建 EGLContext，它为后续 OpenGL ES 渲染提供了上下文。
5. 创建 EGLSurface，已经有了 SurfaceView 了，这里为什么又来了一个 EGLSurface？其实 EGL 并不认识 SurfaceView，他只认识 EGLSurface，所以就用 EGLSurface 对 SurfaceView 中的 Surface 做了一层代理，实际上绘制还是绘制在 SurfaceView 中的 BufferQueue 中然后给屏幕进行显示的。
6. 最后一步即将 EGL 绑定到当前的 EGLSurface 上来，并指定了 OpenGL ES 的渲染上下文。

经过以上这 6 步，我们已经具备使用 OpenGL ES 进行渲染的能力了，下面来看下该怎么做。

首先在 SurfaceHolder 的 `surfaceCreated` 方法中调用上面的 `initEGL`，然后指定 OpenGL ES 的清屏颜色。

```java
@Override
    public void surfaceCreated(SurfaceHolder holder) {
        initEGL(holder);
        GLES32.glClearColor(1.0F, 0F, 0F, 1F);
    }
```

接着在 `surfaceChanged` 回调中设置 OpenGL ES 中的视窗大小，并进行清屏操作。但仅仅这两步是无法将清屏颜色渲染到屏幕上的，因为此时只是将颜色渲染在了 EGLSurface 中的缓存中，另外还需要调用 `EGL14.eglSwapBuffers` 将缓存中的数据给到显示设备，这样才能渲染成功。
```java
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        GLES32.glViewport(0, 0, width, height);
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT);
        EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
    }
```

最终显示效果如下图：
![](https://blog-1256162814.cos.ap-nanjing.myqcloud.com/opengl/glsurfaceview01.jpeg)

本文主要笼统介绍了 EGL 是什么，以及如何在 Android Java 层使用 EGL 和 OpenGL ES 进行渲染。