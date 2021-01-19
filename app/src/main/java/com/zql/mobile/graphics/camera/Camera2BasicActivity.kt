package com.zql.mobile.graphics.camera

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zql.mobile.graphics.R

class Camera2BasicActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera2_basic)
        if (null == savedInstanceState) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, Camera2BasicFragment.newInstance())
                .commit()
        }
    }
}
