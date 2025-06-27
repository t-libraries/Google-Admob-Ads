package com.admobads.loading.skeleton.view

import com.admobads.loading.skeleton.drawer.ISkeletonDrawer
import com.admobads.loading.skeleton.drawer.SkeletonViewDrawer
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatButton

class SkeletonButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(
    ContextThemeWrapper(
        context,
        androidx.appcompat.R.style.Widget_AppCompat_Button
    ),
    attrs,
    defStyleAttr
), ISkeletonDrawer {

    private var skeletonDrawer = SkeletonViewDrawer(this).apply {
        getStyles(attrs, defStyleAttr)
    }

    override fun getSkeletonDrawer() = skeletonDrawer

    override fun isLoading() = skeletonDrawer.isLoading()

    override fun startLoading() {
        post {
            skeletonDrawer.startLoading()
        }
    }

    override fun stopLoading() {
        post {
            skeletonDrawer.stopLoading()
        }
    }



    override fun onDraw(canvas: Canvas) {
        if (!skeletonDrawer.draw(canvas)) {
            super.onDraw(canvas)
        }
    }

    override fun onDetachedFromWindow() {
        stopLoading()
        super.onDetachedFromWindow()
    }

}
