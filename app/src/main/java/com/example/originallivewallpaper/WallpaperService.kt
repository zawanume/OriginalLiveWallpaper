package com.example.originallivewallpaper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.Choreographer
import android.view.SurfaceHolder

class WallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return WallpaperEngine()
    }

    inner class WallpaperEngine : Engine() {
        private val imgPath = arrayOf(
            R.drawable.sample1,
            R.drawable.sample2,
            R.drawable.sample3,
        )

        private val displayX = 1179
        private val displayY = 2556
        private val background = Color.rgb(255, 255, 255)

        private var currentIndex = 0
        private var nextIndex = 1
        private var alpha = 0
        private val paint = Paint()

        private val choreographer = Choreographer.getInstance()
        private val handler = Handler()
        private var running = true
        private var isFading = false

        private val frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if(running) {
                    drawFrame()
                    choreographer.postFrameCallback(this)
                }
            }
        }

        private val fadeRunnable = object : Runnable {
            override fun run() {
                isFading = true
                handler.postDelayed(this, 10000)
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            running = true
            choreographer.postFrameCallback(frameCallback)
            handler.post(fadeRunnable)
        }

        override fun onDestroy() {
            super.onDestroy()
            running = false
            choreographer.removeFrameCallback(frameCallback)
            handler.removeCallbacks(fadeRunnable)
        }

        private fun drawFrame() {
            val canvas = surfaceHolder.lockCanvas()

            val currentImg = decodeSampledBitmap(imgPath[currentIndex], displayX, displayY)
            val nextImg = decodeSampledBitmap(imgPath[nextIndex], displayX, displayY)

            val imgWidth = currentImg.width.toFloat()
            val imgHeight = currentImg.height.toFloat()
            val canvasWidth = canvas.width.toFloat()
            val canvasHeight = canvas.height.toFloat()

            val scale = maxOf(canvasWidth / imgWidth, canvasHeight / imgHeight)

            val matrix = android.graphics.Matrix()
            val scaledWidth = imgWidth * scale
            val scaledHeight = imgHeight * scale
            val offsetX = (canvasWidth - scaledWidth) / 2
            val offsetY = (canvasHeight - scaledHeight) / 2
            matrix.setScale(scale, scale)
            matrix.postTranslate(offsetX, offsetY)
            canvas.drawColor(background)

            if (isFading) {
                paint.alpha = 255 - alpha
                canvas.drawBitmap(currentImg, matrix, paint)

                paint.alpha = alpha
                canvas.drawBitmap(nextImg, matrix, paint)

                alpha += 15
                if (alpha > 255) {
                    alpha = 0
                    isFading = false
                    currentIndex = nextIndex
                    nextIndex = (nextIndex + 1) % imgPath.size
                }
            } else {
                paint.alpha = 255
                canvas.drawBitmap(currentImg, matrix, paint)
            }
            surfaceHolder.unlockCanvasAndPost(canvas)
        }

        private fun decodeSampledBitmap(resourceId: Int, reqWidth: Int, reqHeight: Int): Bitmap {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(resources, resourceId, options)

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeResource(resources, resourceId, options)
        }

        private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            val (height: Int, width: Int) = options.run { outHeight to outWidth }
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {
                val halfHeight: Int = height / 2
                val halfWidth: Int = width / 2

                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }
    }
}