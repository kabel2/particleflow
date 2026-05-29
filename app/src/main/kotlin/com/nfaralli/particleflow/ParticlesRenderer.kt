package com.nfaralli.particleflow

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ParticlesRenderer(context: Context) : GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "ParticlesRenderer"

        init {
            System.loadLibrary("particleflow")
        }
    }

    private lateinit var mPointVertices: FloatBuffer
    private lateinit var mPointColors: FloatBuffer

    private val mMVPMatrix = FloatArray(16)
    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)

    private val mPrefs: SharedPreferences

    private var mProgram = 0
    private var maPositionHandle = 0
    private var maColorHandle = 0
    private var muMVPMatrixHandle = 0
    private var muPointSizeHandle = 0
    private var mWidth = 0
    private var mHeight = 0

    private var initialized = false
    private var posDirty = false
    private var mNumTouch = 0
    private var mPartCount = 0
    private var mParticleSize = 0
    private lateinit var touchPos: FloatArray
    private lateinit var pos: FloatArray
    private lateinit var delta: FloatArray
    private lateinit var col: FloatArray

    private val mVertexShader = """
        uniform mat4 uMVPMatrix;
        uniform float uPointSize;
        attribute vec4 aPosition;
        attribute vec4 aColor;
        varying vec4 vColor;
        void main() {
          gl_Position = uMVPMatrix * aPosition;
          gl_PointSize = uPointSize;
          vColor = aColor;
        }
    """.trimIndent()

    private val mFragmentShader = """
        precision mediump float;
        varying vec4 vColor;
        void main() {
          gl_FragColor = vColor;
        }
    """.trimIndent()

    init {
        mPrefs = context.getSharedPreferences(ParticlesSurfaceView.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        init()
    }

    fun onPrefsChanged() {
        init()
        initScript(true)
        val bgColor = mPrefs.getInt("BGColor", ParticlesSurfaceView.DEFAULT_BG_COLOR)
        val bgRed = Color.red(bgColor) / 255f
        val bgGreen = Color.green(bgColor) / 255f
        val bgBlue = Color.blue(bgColor) / 255f
        GLES20.glClearColor(bgRed, bgGreen, bgBlue, 1.0f)
    }

    private fun init() {
        mPartCount = mPrefs.getInt("NumParticles", ParticlesSurfaceView.DEFAULT_NUM_PARTICLES)
        mParticleSize = mPrefs.getInt("ParticleSize", ParticlesSurfaceView.DEFAULT_PARTICLE_SIZE)
        mNumTouch = mPrefs.getInt("NumAttPoints", ParticlesSurfaceView.DEFAULT_MAX_NUM_ATT_POINTS)
        touchPos = FloatArray(2 * mNumTouch)
        pos = FloatArray(2 * mPartCount)
        delta = FloatArray(2 * mPartCount)
        col = FloatArray(4 * mPartCount)
        mPointVertices = ByteBuffer.allocateDirect(mPartCount * 2 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        mPointColors = ByteBuffer.allocateDirect(mPartCount * 4 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
    }

    fun setTouch(index: Int, x: Float, y: Float) {
        if (index >= mNumTouch) return
        val i = index * 2
        touchPos[i] = x
        touchPos[i + 1] = mHeight - y
        posDirty = true
    }

    fun syncTouch() {
        if (!posDirty) return
        posDirty = false
    }

    private external fun nativeInitParticles(
        pos: FloatArray, delta: FloatArray, color: FloatArray,
        count: Int, width: Float, height: Float,
        slowHue: Float, slowSaturation: Float, slowValue: Float,
        fastHue: Float, fastSaturation: Float, fastValue: Float, hueDirection: Int
    )

    private external fun nativeUpdateParticles(
        pos: FloatArray, delta: FloatArray, color: FloatArray,
        count: Int, width: Float, height: Float,
        touchPos: FloatArray, numTouch: Int,
        attractionCoef: Float, dragCoef: Float,
        slowHue: Float, slowSaturation: Float, slowValue: Float,
        fastHue: Float, fastSaturation: Float, fastValue: Float, hueDirection: Int
    )

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        val bgColor = mPrefs.getInt("BGColor", ParticlesSurfaceView.DEFAULT_BG_COLOR)
        val bgRed = Color.red(bgColor) / 255f
        val bgGreen = Color.green(bgColor) / 255f
        val bgBlue = Color.blue(bgColor) / 255f
        GLES20.glClearColor(bgRed, bgGreen, bgBlue, 1.0f)

        mProgram = createProgram(mVertexShader, mFragmentShader)
        if (mProgram == 0) return

        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")
        checkGlError("glGetAttribLocation aPosition")
        if (maPositionHandle == -1) throw RuntimeException("Could not get attrib location for aPosition")

        maColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor")
        checkGlError("glGetAttribLocation aColor")
        if (maColorHandle == -1) throw RuntimeException("Could not get attrib location for aColor")

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        checkGlError("glGetUniformLocation uMVPMatrix")
        if (muMVPMatrixHandle == -1) throw RuntimeException("Could not get uniform location for uMVPMatrix")

        muPointSizeHandle = GLES20.glGetUniformLocation(mProgram, "uPointSize")
        if (muPointSizeHandle == -1) throw RuntimeException("Could not get uniform location for uPointSize")
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        val oldWidth = mWidth
        val oldHeight = mHeight
        mWidth = width
        mHeight = height
        GLES20.glViewport(0, 0, width, height)

        Matrix.orthoM(mProjectionMatrix, 0, 0f, -width.toFloat(), 0f, height.toFloat(), 3f, 7f)
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0f)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)

        if (width == oldWidth && height == oldHeight && initialized) return
        initScript(false)
    }

    private fun initScript(forceAllocationsInit: Boolean) {
        val hsv = FloatArray(3)
        Color.colorToHSV(mPrefs.getInt("SlowColor", ParticlesSurfaceView.DEFAULT_SLOW_COLOR), hsv)
        val slowHue = hsv[0] / 360f
        val slowSaturation = hsv[1]
        val slowValue = hsv[2]
        Color.colorToHSV(mPrefs.getInt("FastColor", ParticlesSurfaceView.DEFAULT_FAST_COLOR), hsv)
        val fastHue = hsv[0] / 360f
        val fastSaturation = hsv[1]
        val fastValue = hsv[2]
        val hueDirection = mPrefs.getInt("HueDirection", ParticlesSurfaceView.DEFAULT_HUE_DIRECTION)

        if (!initialized || forceAllocationsInit) {
            nativeInitParticles(pos, delta, col, mPartCount, mWidth.toFloat(), mHeight.toFloat(),
                slowHue, slowSaturation, slowValue,
                fastHue, fastSaturation, fastValue, hueDirection)
            initialized = true
        }
        resetAttractionPoints()
    }

    fun resetAttractionPoints() {
        if (initialized && mWidth > 0 && mHeight > 0) {
            val l = (if (mWidth < mHeight) mWidth else mHeight) / 3f
            setTouch(0, mWidth / 2f, mHeight / 2f + if (mNumTouch == 1) 0f else l)
            for (i in 1 until mNumTouch) {
                setTouch(
                    i,
                    (mWidth / 2f + l * kotlin.math.sin(i * 2 * kotlin.math.PI / mNumTouch)).toFloat(),
                    (mHeight / 2f + l * kotlin.math.cos(i * 2 * kotlin.math.PI / mNumTouch)).toFloat()
                )
            }
            syncTouch()
        }
    }

    override fun onDrawFrame(unused: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glUseProgram(mProgram)
        checkGlError("glUseProgram")

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0)
        GLES20.glUniform1f(muPointSizeHandle, mParticleSize.toFloat())

        val hsv = FloatArray(3)
        Color.colorToHSV(mPrefs.getInt("SlowColor", ParticlesSurfaceView.DEFAULT_SLOW_COLOR), hsv)
        val slowHue = hsv[0] / 360f
        val slowSaturation = hsv[1]
        val slowValue = hsv[2]
        Color.colorToHSV(mPrefs.getInt("FastColor", ParticlesSurfaceView.DEFAULT_FAST_COLOR), hsv)
        val fastHue = hsv[0] / 360f
        val fastSaturation = hsv[1]
        val fastValue = hsv[2]
        val hueDirection = mPrefs.getInt("HueDirection", ParticlesSurfaceView.DEFAULT_HUE_DIRECTION)
        val f01AttractionCoef = mPrefs.getInt("F01Attraction", ParticlesSurfaceView.DEFAULT_F01_ATTRACTION_COEF).toFloat()
        val f01DragCoef = 1 - mPrefs.getInt("F01Drag", ParticlesSurfaceView.DEFAULT_F01_DRAG_COEF) / 100f

        nativeUpdateParticles(pos, delta, col, mPartCount, mWidth.toFloat(), mHeight.toFloat(), touchPos, mNumTouch,
            f01AttractionCoef, f01DragCoef,
            slowHue, slowSaturation, slowValue,
            fastHue, fastSaturation, fastValue, hueDirection)

        mPointVertices.position(0)
        mPointVertices.put(pos)
        mPointVertices.position(0)
        GLES20.glVertexAttribPointer(maPositionHandle, 2, GLES20.GL_FLOAT, false, 8, mPointVertices)
        checkGlError("glVertexAttribPointer maPosition")
        GLES20.glEnableVertexAttribArray(maPositionHandle)

        mPointColors.position(0)
        mPointColors.put(col)
        mPointColors.position(0)
        GLES20.glVertexAttribPointer(maColorHandle, 4, GLES20.GL_FLOAT, false, 16, mPointColors)
        checkGlError("glVertexAttribPointer maColor")
        GLES20.glEnableVertexAttribArray(maColorHandle)

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mPartCount)
        checkGlError("glDrawArrays")
    }

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) return 0

        val pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) return 0

        var program = GLES20.glCreateProgram()
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader)
            checkGlError("glAttachShader")
            GLES20.glAttachShader(program, pixelShader)
            checkGlError("glAttachShader")
            GLES20.glLinkProgram(program)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ")
                Log.e(TAG, GLES20.glGetProgramInfoLog(program))
                GLES20.glDeleteProgram(program)
                program = 0
            }
        }
        return program
    }

    fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }

    fun checkGlError(glOperation: String) {
        var error: Int
        while (GLES20.glGetError().also { error = it } != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "$glOperation: glError $error")
            throw RuntimeException("$glOperation: glError $error")
        }
    }
}
