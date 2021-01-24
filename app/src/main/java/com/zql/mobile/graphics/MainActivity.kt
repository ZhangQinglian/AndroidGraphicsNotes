package com.zql.mobile.graphics

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.zql.mobile.graphics.camera.Camera2BasicActivity
import com.zql.mobile.graphics.opengl.GLSurfaceViewActivity
import com.zql.mobile.graphics.opengl.OpenGLES_EGLActivity
import com.zql.mobile.graphics.opengl.OpenGLES_VertexAttributePointer_Activity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SectionCallback {

    companion object {
        const val SECTION_NORMAL_GROUP = 0
        const val SECTION_CAMERA = 1
        const val SECTION_BASIC_GL_SURFACE_VIEW = 2
        const val SECTION_OPEN_GL_ES_EGL = 3
        const val SECTION_OPEN_GL_VERTEX_ATTRIBUTE_POINTER = 5
    }

    private val sections = arrayOf(
        SectionItem.SectionGroup("Camera", SECTION_NORMAL_GROUP),
        SectionItem.SectionTitle("Camera2 Basic", SECTION_CAMERA),
        SectionItem.SectionGroup("OpenGL", SECTION_NORMAL_GROUP),
        SectionItem.SectionTitle("GLSurfaceView Basic", SECTION_BASIC_GL_SURFACE_VIEW),
        SectionItem.SectionTitle("OpenGL ES and EGL", SECTION_OPEN_GL_ES_EGL),
        SectionItem.SectionTitle("OpenGL ES VertextAttributePointer", SECTION_OPEN_GL_VERTEX_ATTRIBUTE_POINTER)
    )
    private val sectionAdapter = HomeSectionAdapter(sections, this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        list_view.adapter = sectionAdapter
        list_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    override fun onSectionClick(sectionId: Int) {
        when (sectionId) {
            SECTION_CAMERA -> {
                Dexter.withContext(this).withPermission(Manifest.permission.CAMERA)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                            startActivity(
                                Intent(
                                    this@MainActivity,
                                    Camera2BasicActivity::class.java
                                )
                            )
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            p0: PermissionRequest?,
                            p1: PermissionToken?
                        ) {
                        }

                        override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        }

                    }).check()
            }
            SECTION_BASIC_GL_SURFACE_VIEW -> {
                startActivity(
                    Intent(
                        this@MainActivity,
                        GLSurfaceViewActivity::class.java
                    )
                )
            }
            SECTION_OPEN_GL_ES_EGL -> {
                startActivity(
                    Intent(
                        this@MainActivity,
                        OpenGLES_EGLActivity::class.java
                    )
                )
            }
            SECTION_OPEN_GL_VERTEX_ATTRIBUTE_POINTER->{
                startActivity(
                    Intent(
                        this@MainActivity,
                        OpenGLES_VertexAttributePointer_Activity::class.java
                    )
                )
            }
        }
    }
}


class HomeSectionAdapter(
    private val sections: Array<out SectionItem>,
    private val sectionCallback: SectionCallback
) :
    RecyclerView.Adapter<SectionItemHolder>(), SectionCallback {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionItemHolder {
        return when (viewType) {
            SectionItem.typeGroup -> SectionGroupHolder.create(parent)
            SectionItem.typeTitle -> SectionTitleHolder.create(parent, this)
            else -> SectionDefaultHolder.obtain(parent)
        }
    }

    override fun getItemCount() = sections.size

    override fun onBindViewHolder(holder: SectionItemHolder, position: Int) {
        holder.bind(sections[position])
    }

    override fun getItemViewType(position: Int): Int {
        return sections[position].type
    }

    override fun onSectionClick(sectionId: Int) {
        sectionCallback.onSectionClick(sectionId)
    }

}

interface SectionCallback {
    fun onSectionClick(sectionId: Int)
}

abstract class SectionItemHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(sectionItem: SectionItem)
}

class SectionDefaultHolder(view: View) : SectionItemHolder(view) {
    override fun bind(sectionItem: SectionItem) {}

    companion object {
        @SuppressLint("SetTextI18n")
        fun obtain(parent: ViewGroup): SectionItemHolder {
            return SectionDefaultHolder(TextView(parent.context).apply {
                text = "wtf ??? why I am here"
            })
        }
    }
}

class SectionGroupHolder(view: View) : SectionItemHolder(view) {
    override fun bind(sectionItem: SectionItem) {
        if (sectionItem is SectionItem.SectionGroup) {
            (itemView as? TextView)?.text = sectionItem.groupName
        }
    }

    companion object {
        fun create(parent: ViewGroup): SectionItemHolder {
            return LayoutInflater.from(parent.context)
                .inflate(R.layout.section_list_item_group, parent, false).let {
                    SectionGroupHolder(it)
                }
        }
    }
}

class SectionTitleHolder(view: View, val sectionCallback: SectionCallback) :
    SectionItemHolder(view) {
    override fun bind(sectionItem: SectionItem) {
        if (sectionItem is SectionItem.SectionTitle) {
            (itemView as? TextView)?.text = sectionItem.title
            itemView.setOnClickListener { view ->
                sectionCallback.onSectionClick(sectionItem.id)
            }
        }
    }

    companion object {
        fun create(parent: ViewGroup, sectionCallback: SectionCallback): SectionItemHolder {
            return LayoutInflater.from(parent.context)
                .inflate(R.layout.section_list_item_title, parent, false).let {
                    SectionTitleHolder(it, sectionCallback)
                }
        }
    }
}

sealed class SectionItem(val type: Int, val id: Int) {
    companion object {
        const val typeGroup = 1
        const val typeTitle = 2
    }

    class SectionGroup(val groupName: String, id: Int) : SectionItem(typeGroup, id)
    class SectionTitle(val title: String, id: Int) :
        SectionItem(typeTitle, id)
}