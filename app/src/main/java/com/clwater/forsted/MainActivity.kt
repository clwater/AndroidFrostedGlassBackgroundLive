package com.clwater.forsted

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key.Companion.Sleep
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.clwater.forsted.ui.theme.AndroidFrostedGlassBackgroundLiveTheme
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import java.util.concurrent.Executors
import kotlin.concurrent.thread


class MainActivity : ComponentActivity() {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    var scope = MainScope()
    private val mBitmap = mutableStateOf(Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DefaultView() }
//        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(
            this,
            arrayOf<String>(Manifest.permission.CAMERA),
            100
        )//请求权限
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)//获得provider实例
    }


    @Composable
    fun DefaultView(){
        AndroidFrostedGlassBackgroundLiveTheme {
            Image(bitmap = mBitmap.value.asImageBitmap(), contentDescription = "6")
        }
    }



    @SuppressLint("UnsafeOptInUsageError")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //默认你会同意权限，不同意就是自己的事了

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        val executor = Executors.newFixedThreadPool(5)
        imageAnalysis.setAnalyzer(executor, ImageAnalysis.Analyzer { image ->
            //这里的回调会回调每一帧的信息
            val bitmap = Bitmap.createBitmap(
                image.width,
                image.height,
                Bitmap.Config.ARGB_8888
            )//创建一个空的Bitmap
            thread {

                YuvToRgbConverter(this@MainActivity).yuvToRgb(
                    image = image.image!!,
                    bitmap
                )
                image.close()//这里调用了close就会继续生成下一帧图片，否则就会被阻塞不会继续生成下一帧
                // 暂停100ms
//                Thread.sleep(100)
                mBitmap.value = bitmap.blur(this@MainActivity, 25f, 1)
            }

        })


        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                val cameraSelector: CameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
                cameraProvider.bindToLifecycle(//绑定生命周期
                    this as LifecycleOwner,
                    cameraSelector,
                    imageAnalysis,
                )
        }
            , ContextCompat.getMainExecutor(this))

    }



    private fun Bitmap.blur(context: Context, radius: Float = 10f, iterator: Int): Bitmap {
        val outputBitmap = Bitmap.createBitmap(
            this.width,
            this.height,
            Bitmap.Config.ARGB_8888
        )
        val rs = RenderScript.create(context)
        val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val tmpIn = Allocation.createFromBitmap(rs, this)
        val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
        blurScript.setRadius(radius)
        blurScript.setInput(tmpIn)
        blurScript.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)

        return if (iterator > 1) {
            outputBitmap.blur(context, radius, iterator - 1)
        }else{
            outputBitmap.alpha(context, 30)
        }
    }

    private fun Bitmap.alpha(context: Context, alpha: Int): Bitmap {
        val outputBitmap = Bitmap.createBitmap(
            this.width,
            this.height,
            Bitmap.Config.ARGB_8888
        )

        outputBitmap.setHasAlpha(true)


        val canvas = Canvas(outputBitmap)
        val paint = Paint()
        paint.alpha = alpha

        canvas.drawColor(Color(0x80ffffff).toArgb())

        canvas.drawBitmap(this, 0f, 0f, paint)


        return outputBitmap
    }




    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}