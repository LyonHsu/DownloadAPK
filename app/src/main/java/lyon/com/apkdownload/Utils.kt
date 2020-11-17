package lyon.com.apkdownload

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import lyon.com.apkdownload.DownloadAPK.fileName
import java.io.*


object Utils {
    val TAG = this::class.java.simpleName
    fun FormatStackTrace(throwable: Throwable?): String? {
        if (throwable == null) return ""
        var rtn = throwable.stackTrace.toString()
        try {
            val writer: Writer = StringWriter()
            val printWriter = PrintWriter(writer)
            throwable.printStackTrace(printWriter)
            printWriter.flush()
            writer.flush()
            rtn = writer.toString()
            printWriter.close()
            writer.close()
        } catch (e: IOException) {
            Log.e(TAG, "FormatStackTrace " + e)
        } catch (ex: Exception) {
            Log.e(TAG, "FormatStackTrace " + ex)
        }
        return rtn
    }

    fun installApk(context: Context) {

        // 在Boradcast中啟動活動需要新增Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + fileName///storage/emulated/0/Download/friDay.apk
            Log.e(TAG," download install path2:"+path)
            val file = File(path)
            if(file.exists()) {
                file.setReadable(true, false)
                val type = "application/vnd.android.package-archive"
                val intent = Intent()
                intent.setAction(Intent.ACTION_VIEW)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val contentUri = FileProvider.getUriForFile(context, context.packageName+".provider", file)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.setDataAndType(contentUri, type)
                } else {
                    val contentUri = Uri.fromFile(file)
                    intent.setDataAndType(contentUri, type)
                }
                context.startActivity(intent)
//                AppController.getInstance().getMainActivity().finish()
                Log.d(TAG, "install start()!")
                Toast.makeText(context, "安裝開始!", Toast.LENGTH_SHORT).show()
            }else{
                Log.d(TAG, "install fail()!")
                Toast.makeText(context, "找不到檔案!", Toast.LENGTH_SHORT).show()
            }
        }catch (e: Exception){
            Log.e(TAG,""+ Utils.FormatStackTrace(e))
        }
    }

    fun getRate(size:Int,endTime:Double,startTime:Double):Double{
        if(endTime<=0)
            return 0.0
        if(startTime<=0)
            return 0.0
        var rate: Double = (size / 1024 / ((endTime - startTime) / 1000.0) * 8).toDouble()
        rate = Math.round(rate * 100.0) / 100.0

        return rate;
    }
}