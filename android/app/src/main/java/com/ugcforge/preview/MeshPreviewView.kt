package com.ugcforge.preview

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.MotionEvent
import com.ugcforge.mesh.MeshData
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Orbitable GLES2 mesh viewer. Drag rotates, pinch zooms.
 */
class MeshPreviewView(context: Context, mesh: MeshData) : GLSurfaceView(context) {

    private val renderer = MeshRenderer(mesh)
    private var mode: Int = RENDER_SOLID

    companion object {
        const val RENDER_SOLID = 0
        const val RENDER_WIRE = 1

        private const val VERTEX_SHADER = """
            uniform mat4 uMVP;
            uniform mat4 uModel;
            attribute vec3 aPos;
            attribute vec3 aNormal;
            varying vec3 vNormal;
            varying vec3 vPos;
            void main() {
              vNormal = mat3(uModel) * aNormal;
              vPos = vec3(uModel * vec4(aPos, 1.0));
              gl_Position = uMVP * vec4(aPos, 1.0);
            }
        """

        private const val FRAGMENT_SHADER = """
            precision mediump float;
            uniform vec3 uBaseColor;
            varying vec3 vNormal;
            varying vec3 vPos;
            void main() {
              vec3 n = normalize(vNormal);
              vec3 lightDir = normalize(vec3(0.35, 0.9, 0.4));
              vec3 halfDir = normalize(lightDir + vec3(0.0, 0.0, 1.0));
              float diff = max(dot(n, lightDir), 0.0);
              float spec = pow(max(dot(n, halfDir), 0.0), 24.0);
              vec3 color = uBaseColor * (0.35 + 0.65 * diff) + vec3(0.25) * spec;
              gl_FragColor = vec4(color, 1.0);
            }
        """
    }

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    fun setRenderMode(m: Int) { mode = m }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        renderer.handleTouch(event)
        return true
    }

    private fun loadShader(type: Int, src: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, src)
        GLES20.glCompileShader(shader)
        return shader
    }

    inner class MeshRenderer(mesh: MeshData) : GLSurfaceView.Renderer {

        private var program: Int = 0
        private var mesh: MeshData? = null
        private val vBuffer: FloatBuffer
        private val nBuffer: FloatBuffer
        private val iBuffer: IntBuffer
        private val vertexCount: Int

        private var lastX = 0f
        private var lastY = 0f
        private var pinching = false
        private var pinchDist = 0f

        private var yaw = 0f
        private var pitch = 0f
        private var distance = 3f

        private val viewM = FloatArray(16)
        private val projM = FloatArray(16)
        private val mvpM = FloatArray(16)
        private val modelM = FloatArray(16)
        private val tmpM = FloatArray(16)

        private var baseColor = floatArrayOf(0.55f, 0.42f, 1.0f)

        init {
            val m = com.ugcforge.mesh.MeshMath.normalize(mesh)
            this.mesh = m
            vertexCount = m.vertexCount
            vBuffer = toBuffer(m.vertices, m.vertices.size)
            nBuffer = toBuffer(m.normals, m.normals.size)
            iBuffer = toIndex(m.triangles, m.triangles.size)
        }

        private fun toBuffer(arr: FloatArray, length: Int): FloatBuffer {
            val bb = ByteBuffer.allocateDirect(length * 4).order(ByteOrder.nativeOrder())
            val fb = bb.asFloatBuffer().put(arr)
            fb.position(0)
            return fb
        }

        private fun toIndex(arr: IntArray, length: Int): IntBuffer {
            val bb = ByteBuffer.allocateDirect(length * 4).order(ByteOrder.nativeOrder())
            val ib = bb.asIntBuffer().put(arr)
            ib.position(0)
            return ib
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            program = createProgram(loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER), loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER))
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            val aspect = if (height > 0) width.toFloat() / height else 1f
            Matrix.perspectiveM(projM, 0, 45f, aspect, 0.1f, 100f)
        }

        override fun onDrawFrame(gl: GL10?) {
            GLES20.glClearColor(0.043f, 0.055f, 0.078f, 1f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            val m = mesh ?: return
            if (program == 0) return

            Matrix.setIdentityM(modelM, 0)
            Matrix.rotateM(modelM, 0, pitch, 1f, 0f, 0f)
            Matrix.rotateM(modelM, 0, yaw, 0f, 1f, 0f)

            Matrix.setIdentityM(viewM, 0)
            Matrix.translateM(viewM, 0, 0f, 0f, -distance)
            Matrix.multiplyMM(tmpM, 0, projM, 0, viewM, 0)
            Matrix.multiplyMM(mvpM, 0, tmpM, 0, modelM, 0)

            GLES20.glUseProgram(program)
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "uMVP"), 1, false, mvpM, 0)
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "uModel"), 1, false, modelM, 0)
            GLES20.glUniform3fv(GLES20.glGetUniformLocation(program, "uBaseColor"), 1, baseColor, 0)

            val aPos = GLES20.glGetAttribLocation(program, "aPos")
            val aNormal = GLES20.glGetAttribLocation(program, "aNormal")

            if (mode == RENDER_WIRE) {
                GLES20.glLineWidth(1.4f)
                GLES20.glUniform3fv(GLES20.glGetUniformLocation(program, "uBaseColor"), 1, floatArrayOf(0.9f, 0.9f, 1.0f), 0)
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
                // draw wireframe as GL_LINES over triangle edges
                val edgeIndices = buildEdgeIndices(m)
                val eb = toIndex(edgeIndices, edgeIndices.size)
                GLES20.glEnableVertexAttribArray(aPos)
                GLES20.glVertexAttribPointer(aPos, 3, GLES20.GL_FLOAT, false, 0, vBuffer)
                GLES20.glEnableVertexAttribArray(aNormal)
                GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_FLOAT, false, 0, nBuffer)
                GLES20.glDrawElements(GLES20.GL_LINES, edgeIndices.size, GLES20.GL_UNSIGNED_INT, eb)
                return
            }

            GLES20.glEnableVertexAttribArray(aPos)
            GLES20.glVertexAttribPointer(aPos, 3, GLES20.GL_FLOAT, false, 0, vBuffer)
            GLES20.glEnableVertexAttribArray(aNormal)
            GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_FLOAT, false, 0, nBuffer)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, m.triangleCount * 3, GLES20.GL_UNSIGNED_INT, iBuffer)
        }

        fun handleTouch(event: MotionEvent) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    pinching = false
                    lastX = event.x; lastY = event.y
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    pinching = event.pointerCount >= 2
                    if (pinching) pinchDist = spacing(event)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (pinching && event.pointerCount >= 2) {
                        val newDist = spacing(event)
                        val scale = pinchDist / maxOf(newDist, 1f)
                        distance = (distance * scale).coerceIn(1.4f, 14f)
                        pinchDist = newDist
                    } else {
                        val dx = event.x - lastX
                        val dy = event.y - lastY
                        yaw += dx * 0.6f
                        pitch = (pitch + dy * 0.6f).coerceIn(-89f, 89f)
                    }
                    lastX = event.x; lastY = event.y
                }
                MotionEvent.ACTION_POINTER_UP -> pinching = false
            }
        }

        private fun spacing(e: MotionEvent): Float {
            if (e.pointerCount < 2) return 0f
            val dx = e.getX(0) - e.getX(1)
            val dy = e.getY(0) - e.getY(1)
            return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        }

        private fun createProgram(vs: Int, fs: Int): Int {
            val prog = GLES20.glCreateProgram()
            GLES20.glAttachShader(prog, vs)
            GLES20.glAttachShader(prog, fs)
            GLES20.glLinkProgram(prog)
            return prog
        }

        private fun buildEdgeIndices(m: MeshData): IntArray {
            val out = IntArray(m.triangleCount * 6)
            var o = 0
            for (i in 0 until m.triangleCount) {
                val a = m.triangles[i * 3]; val b = m.triangles[i * 3 + 1]; val c = m.triangles[i * 3 + 2]
                out[o++] = a; out[o++] = b
                out[o++] = b; out[o++] = c
                out[o++] = c; out[o++] = a
            }
            return out
        }
    }
}