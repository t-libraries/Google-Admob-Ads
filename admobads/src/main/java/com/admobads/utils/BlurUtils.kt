package com.admobads.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable

object BlurUtils {

    @Suppress("DEPRECATION")
    fun applyBlurToBackground(activity: Activity, blurRadius: Float = 25f) {
        // First, set a semi-transparent overlay
        val rootView = activity.window.decorView as ViewGroup

        // Create a dark overlay view
        val overlayView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000")) // 50% black overlay
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            tag = "blur_overlay"
        }

        // Add overlay to the root view
        rootView.addView(overlayView)

        // For Android 12+, use system blur with maximum radius
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activity.window.attributes.blurBehindRadius = blurRadius.toInt()
            activity.window.setBackgroundBlurRadius(blurRadius.toInt())
            // Enable blur for the overlay
            overlayView.setRenderEffect(android.graphics.RenderEffect.createBlurEffect(
                blurRadius,
                blurRadius,
                android.graphics.Shader.TileMode.CLAMP
            ))
        } else {
            // For older versions, use enhanced blur technique
            applyEnhancedBlur(activity, blurRadius)
        }
    }

    @Suppress("DEPRECATION")
    private fun applyEnhancedBlur(activity: Activity, blurRadius: Float) {
        try {
            // Get the root view
            val decorView = activity.window.decorView
            val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)

            // Create a bitmap of smaller size for performance and stronger blur
            val scaleFactor = 0.1f // Smaller scale = stronger blur effect
            val downscaledWidth = (rootView.width * scaleFactor).toInt()
            val downscaledHeight = (rootView.height * scaleFactor).toInt()

            if (downscaledWidth <= 0 || downscaledHeight <= 0) return

            // Create downscaled bitmap
            val screenshot = Bitmap.createBitmap(downscaledWidth, downscaledHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(screenshot)
            canvas.scale(scaleFactor, scaleFactor)
            rootView.draw(canvas)

            // Apply multiple blur passes for stronger effect
            var blurredBitmap = screenshot
            val passes = 3 // Multiple passes create stronger blur

            for (i in 0 until passes) {
                blurredBitmap = applySingleBlurPass(activity, blurredBitmap, blurRadius / passes)
            }

            // Scale back up (this will make the blur more pixelated/stronger)
            val finalBitmap = Bitmap.createScaledBitmap(
                blurredBitmap,
                rootView.width,
                rootView.height,
                true
            )

            // Apply dark overlay on top of blur
            val overlayBitmap = Bitmap.createBitmap(finalBitmap.width, finalBitmap.height, Bitmap.Config.ARGB_8888)
            val overlayCanvas = Canvas(overlayBitmap)
            overlayCanvas.drawBitmap(finalBitmap, 0f, 0f, null)

            // Draw dark overlay on top
            val paint = android.graphics.Paint().apply {
                color = Color.parseColor("#80000000") // 50% black overlay
                isAntiAlias = true
            }
            overlayCanvas.drawRect(0f, 0f, finalBitmap.width.toFloat(), finalBitmap.height.toFloat(), paint)

            // Set as background
            rootView.background = overlayBitmap.toDrawable(activity.resources)

        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: Just set a dark semi-transparent background
            val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
            rootView.background = ColorDrawable(Color.parseColor("#80000000"))
        }
    }

    @Suppress("DEPRECATION")
    private fun applySingleBlurPass(context: Context, bitmap: Bitmap, radius: Float): Bitmap {
        return try {
            val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val rs = RenderScript.create(context)
            val input = Allocation.createFromBitmap(rs, bitmap)
            val outputAlloc = Allocation.createTyped(rs, input.type)
            val blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

            // Set maximum allowable radius (25 is max for RenderScript)
            blur.setRadius(radius.coerceAtMost(25f))
            blur.setInput(input)
            blur.forEach(outputAlloc)
            outputAlloc.copyTo(output)

            rs.destroy()
            output
        } catch (e: Exception) {
            bitmap
        }
    }

    fun clearBlurEffect(activity: Activity) {
        val rootView = activity.window.decorView as ViewGroup

        // Remove overlay view
        val overlayView = rootView.findViewWithTag<View?>("blur_overlay")
        overlayView?.let {
            rootView.removeView(it)
        }

        // Clear system blur effects
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activity.window.setBackgroundBlurRadius(0)
            activity.window.attributes.blurBehindRadius = 0
        }

        // Clear custom background
        val contentView = activity.findViewById<ViewGroup>(android.R.id.content)
        contentView.background = null
    }

    // Alternative method using faster blur algorithm
    fun applyFastBlur(activity: Activity, blurRadius: Int = 25) {
        val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)

        // Create a bitmap and apply stack blur (faster than RenderScript)
        try {
            val bitmap = Bitmap.createBitmap(
                rootView.width,
                rootView.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            rootView.draw(canvas)

            val blurred = fastBlur(bitmap, blurRadius, true)

            // Apply dark overlay
            val overlayBitmap = Bitmap.createBitmap(blurred?.width ?: 0, blurred?.height ?: 0, Bitmap.Config.ARGB_8888)
            val overlayCanvas = Canvas(overlayBitmap)
            blurred?.let { overlayCanvas.drawBitmap(it, 0f, 0f, null) }

            val paint = android.graphics.Paint().apply {
                color = Color.parseColor("#99000000") // 60% black overlay for stronger effect
            }
            blurred?.width?.toFloat()?.let { blurred?.height?.toFloat()?.let { bottom -> overlayCanvas.drawRect(0f, 0f, it, bottom, paint) } }

            rootView.background = overlayBitmap.toDrawable(activity.resources)
        } catch (e: Exception) {
            rootView.background = ColorDrawable(Color.parseColor("#CC000000")) // 80% black
        }
    }

    private fun fastBlur(sentBitmap: Bitmap, radius: Int, canReuseInBitmap: Boolean): Bitmap? {
        val bitmap: Bitmap? = if (canReuseInBitmap) sentBitmap else sentBitmap.config?.let { sentBitmap.copy(it, true) }

        if (radius < 1) return bitmap

        val w = bitmap!!.width
        val h = bitmap!!.height

        val pix = IntArray(w * h)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)

        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(Math.max(w, h))

        var divsum = div + 1 shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }

        yi = 0
        yw = yi

        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int

        y = 0
        while (y < h) {
            bsum = 0
            gsum = 0
            rsum = 0
            boutsum = 0
            goutsum = 0
            routsum = 0
            binsum = 0
            ginsum = 0
            rinsum = 0
            i = -radius
            while (i <= radius) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - Math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius

            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm)
                }
                p = pix[yw + vmin[x]]

                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi++
                x++
            }
            yw += w
            y++
        }
        x = 0
        while (x < w) {
            bsum = 0
            gsum = 0
            rsum = 0
            boutsum = 0
            goutsum = 0
            routsum = 0
            binsum = 0
            ginsum = 0
            rinsum = 0
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = Math.max(0, yp) + x
                sir = stack[i + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = r1 - Math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {
                // Preserve alpha by setting 0xff000000
                pix[yi] = -0x1000000 or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w
                }
                p = x + vmin[y]

                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi += w
                y++
            }
            x++
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return bitmap
    }
}