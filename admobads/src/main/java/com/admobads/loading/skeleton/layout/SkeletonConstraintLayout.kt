package com.admobads.loading.skeleton.layout

import com.admobads.loading.skeleton.drawer.ISkeletonDrawer
import com.admobads.loading.skeleton.drawer.SkeletonViewGroupDrawer
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout

class SkeletonConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr),
    ISkeletonDrawer {

    private var skeletonDrawer = SkeletonViewGroupDrawer(this).apply {
        getStyles(attrs, defStyleAttr)
    }

    init {
        setWillNotDraw(false)
        invalidate()
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

    override fun onDetachedFromWindow() {
        stopLoading()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        if (!skeletonDrawer.draw(canvas)) {
            super.onDraw(canvas)
        }
    }


    fun setSkeletonColor(color: Int) {
        skeletonDrawer.skeletonColor = color
        skeletonDrawer.applyStyles()
        invalidate()
    }


}
