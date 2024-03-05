package com.kevin.swipetoloadlayout.sample

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.kevin.slidingtab.SlidingTabLayout
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private val immersiveBuilder by lazy { ImmersiveBuilder.builder(window) }
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: SlidingTabLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var toolbar: Toolbar
    private lateinit var ivBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)
        appBarLayout = findViewById(R.id.app_bar_layout)
        toolbar = findViewById(R.id.toolbar)
        ivBack = findViewById(R.id.iv_back)

        val pagerAdapter = PersonalPageAdapter(supportFragmentManager)
        viewPager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(viewPager)

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
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val maxScroll = appBarLayout.totalScrollRange
            val percent = abs(verticalOffset).toFloat() / maxScroll.toFloat()
            toolbar.alpha = percent
            if (percent > 0.7f) {
                ivBack.setImageResource(R.mipmap.back_normal_dark)
                immersiveBuilder.statusBarColorHint(Color.WHITE).apply()
            } else {
                ivBack.setImageResource(R.mipmap.back_normal_light)
                immersiveBuilder.statusBarColorHint(Color.BLACK).apply()
            }
        })
    }
}
