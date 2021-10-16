package com.kevin.swipetoloadlayout.sample;

import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

/**
 * 沉浸式体验构造
 * <p>
 * 1. 支持给出状态栏颜色 hint，自动设置状态栏 light/dark 样式；
 * <p>
 * 注：该类使用了 {@link View#setOnSystemUiVisibilityChangeListener(View.OnSystemUiVisibilityChangeListener)}，请注意冲突。
 */
public class ImmersiveBuilder {
    /** 最低支持版本 */
    private static final int MIN_SDK_VERSION = Build.VERSION_CODES.M; // 6.0

    public static ImmersiveBuilder builder(@NonNull Window window) {
        return new ImmersiveBuilder(window);
    }

    private final Window window;
    private final View decoView;

    private boolean useStatusBar;
    private boolean hideStatusBar;
    private int statusBarColor = Color.TRANSPARENT;
    private boolean hasSettedStatusBarColor;
    private int statusBarColorHint = Color.TRANSPARENT;
    private boolean hasSettedStatusBarColorHint;

    private boolean useNavigationBar;
    private boolean showNavigationBar = true;
    // 这里可以使用 NavigatorBarObserver，同 OnSystemUiVisibilityChangeListener 不冲突（因为都使用了 setValueSafelyIfUnequal）
//    private final MutableLiveData<Boolean> navigationBarVisible = new NavigatorBarObserver();
    private boolean navigationBarSticky;
    private boolean isNotFocusable;

    private ImmersiveBuilder(Window window) {
        this.window = window;
        this.decoView = window.getDecorView();

        decoView.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener());
    }

    /** 占用状态栏空间（页面布局将占用该空间，但可能还悬浮在布局上面） */
    public ImmersiveBuilder useStatusBar() {
        this.useStatusBar = true;
        if (!hasSettedStatusBarColor) statusBarColor(Color.TRANSPARENT);
        return this;
    }

    /** 隐藏状态栏，全屏 */
    public ImmersiveBuilder hideStatusBar() {
        this.hideStatusBar = true;
        return this;
    }

    /** 设置状态栏本身颜色 */
    public ImmersiveBuilder statusBarColor(int color) {
        this.statusBarColor = color;
        hasSettedStatusBarColor = true;
        return this;
    }

    /** 设置状态栏颜色提示，用于适配状态栏 light/dark 样式，防止撞色（若设置，则优先使用该项） */
    public ImmersiveBuilder statusBarColorHint(int color) {
        this.statusBarColorHint = color;
        hasSettedStatusBarColorHint = true;
        return this;
    }

    /** 占用导航栏空间（页面布局将占用该空间，但可能还悬浮在布局上面） */
    public ImmersiveBuilder useNavigationBar() {
        this.useNavigationBar = true;
        return this;
    }

    /** 隐藏导航栏 */
    public ImmersiveBuilder hideNavigation() {
        this.showNavigationBar = false;
        return this;
    }

    /** 显示导航栏 */
    public ImmersiveBuilder showNavigation() {
        this.showNavigationBar = true;
        return this;
    }

    /**
     * 设置为 View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY 模式 不需要另外设置hideNavigation()
     * 若需要状态栏也有沉浸效果可设置hideStatusBar或单独设置全屏效果
     */
    public ImmersiveBuilder setNavigationSticky() {
        this.navigationBarSticky = true;
        return this;
    }

    /**
     * window 失去焦点
     *
     * @return
     */
    public ImmersiveBuilder setNotFocusable() {
        this.isNotFocusable = true;
        return this;
    }

    /**
     * 清除 window 失去焦点
     *
     * @return
     */
    public ImmersiveBuilder clearNotFocusable() {
        this.isNotFocusable = false;
        return this;
    }

    /** 观察导航栏显隐 */
//    public LiveData<Boolean> observeIsNavigationBarVisible() {
//        return navigationBarVisible;
//    }

    /** 配置后，需要调用该方法才会生效 */
    // 注：因为 setOnApplyWindowInsetsListener() 在 5.0 才有效，故 4.x 禁止使用状态栏/导航栏空间
    public void apply() {
        if (Build.VERSION.SDK_INT >= MIN_SDK_VERSION) {
            // FLAG_TRANSLUCENT_STATUS
            // - add: 灰色半透明，能看清状态栏内容（此时暂不用该 flag，后续看需求再说）
            // - clear: 可以做到完全透明，不过可能出现状态栏同背景撞色看不清的问题
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        int visibility = 0;
        if (useStatusBar && Build.VERSION.SDK_INT >= MIN_SDK_VERSION) { // 6.0 以下禁止使用状态栏（还可以隐藏）
            // layout 占用状态栏空间
            // 状态栏显示/隐藏时不触发 layout resize，而是浮在 layout 之上（可能挡住内容）
            visibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        }
        if (hideStatusBar) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        // 6.0 以下禁止使用导航栏（还可以隐藏）
        if ((useNavigationBar || !showNavigationBar) && Build.VERSION.SDK_INT >= MIN_SDK_VERSION) {
            // layout 占用导航栏空间
            // 导航栏显示/隐藏时不触发 layout resize，而是浮在 layout 之上（可能挡住内容）
            visibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        if (!showNavigationBar) {
            visibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION; // 隐藏导航栏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                visibility |= View.SYSTEM_UI_FLAG_IMMERSIVE; // 从边上触摸才会出现，而非触摸屏幕就会重新出现
            }
        }
        if (navigationBarSticky) {
            visibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION; // 隐藏导航栏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                visibility |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY; // 触摸出现，点击其他位置消失或几秒后自动消失
            }
        }
        if (needSetStatusBarLuminance() && isLightStatusBar()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        }
        decoView.setSystemUiVisibility(visibility);

        if (hasSettedStatusBarColor && Build.VERSION.SDK_INT >= MIN_SDK_VERSION) {
            // SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 在 M 以上才能用，这里也对状态栏颜色做此限制，否则容易出错
            // 设置透明时比 FLAG_TRANSLUCENT_STATUS 优先级低
            window.setStatusBarColor(statusBarColor);
        }
        if (isNotFocusable) {
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }

    }

    private boolean needSetStatusBarLuminance() { return hasSettedStatusBarColorHint || hasSettedStatusBarColor; }

    private boolean isLightStatusBar() { // 没手动设置 hint color，则尝试用设置的 color
        int colorHint = hasSettedStatusBarColorHint ? statusBarColorHint : statusBarColor;
        return ColorUtils.calculateLuminance(colorHint) > 0.5;
    }

    private class OnSystemUiVisibilityChangeListener implements View.OnSystemUiVisibilityChangeListener {
        @Override
        public void onSystemUiVisibilityChange(int visibility) {
//            setValueSafelyIfUnequal(navigationBarVisible, (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0);
        }
    }
}
