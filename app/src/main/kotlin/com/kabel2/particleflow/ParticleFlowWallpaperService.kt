package com.kabel2.particleflow

import android.content.Context
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder

class ParticleFlowWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return WallpaperEngine()
    }

    inner class WallpaperEngine : Engine() {
        private lateinit var mGLView: WPSurfaceView

        override fun onCreate(holder: SurfaceHolder?) {
            super.onCreate(holder)
            mGLView = WPSurfaceView(this@ParticleFlowWallpaperService)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                mGLView.resetAttractionPoints()
                mGLView.onResume()
            } else {
                mGLView.onPause()
            }
        }

        override fun onDestroy() {
            mGLView.onPause()
            super.onDestroy()
        }

        override fun onTouchEvent(event: MotionEvent?) {
            event?.let { mGLView.onTouchEvent(it) }
        }

        inner class WPSurfaceView(context: Context) : ParticlesSurfaceView(context, null) {
            override fun getHolder(): SurfaceHolder {
                return getSurfaceHolder()
            }
        }
    }
}
