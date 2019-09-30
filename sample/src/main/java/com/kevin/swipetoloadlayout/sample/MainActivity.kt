package com.kevin.swipetoloadlayout.sample

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private val immersiveBuilder by lazy { ImmersiveBuilder.builder(window) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pagerAdapter = PersonalPageAdapter(supportFragmentManager)
        view_pager.adapter = pagerAdapter
        tab_layout.setupWithViewPager(view_pager)

        addAppBarListener()
    }

    override fun onResume() {
        super.onResume()
        immersiveBuilder.useStatusBar()
            .statusBarColorHint(Color.WHITE)
            .apply()
    }

    /**
     * 添加AppBarLayout折叠监听，用于控制顶部用户信息显示隐藏及透明度变化
     */
    private fun addAppBarListener() {
        app_bar_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val maxScroll = appBarLayout.totalScrollRange
            val percent = abs(verticalOffset).toFloat() / maxScroll.toFloat()
            toolbar.alpha = percent
            if (percent > 0.7f) {
                iv_back.setImageResource(R.mipmap.back_normal_dark)
                immersiveBuilder.statusBarColorHint(Color.WHITE).apply()
            } else {
                iv_back.setImageResource(R.mipmap.back_normal_light)
                immersiveBuilder.statusBarColorHint(Color.BLACK).apply()
            }
        })
    }
}
