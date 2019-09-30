package com.kevin.swipetoloadlayout.sample

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jaeger.library.StatusBarUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val immersiveBuilder by lazy { ImmersiveBuilder.builder(window) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pagerAdapter = PersonalPageAdapter(supportFragmentManager)
        view_pager.adapter = pagerAdapter
        tab_layout.setupWithViewPager(view_pager)
    }

    override fun onResume() {
        super.onResume()
        immersiveBuilder.useStatusBar()
            .statusBarColorHint(Color.WHITE)
            .apply()
    }
}
