package lyon.com.apkdownload.DownloadAPK

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import lyon.com.apkdownload.Permissions.PermissionsActivity
import lyon.com.apkdownload.Permissions.PermissionsActivity.PERMISSIONS
import lyon.com.apkdownload.Permissions.PermissionsChecker
import lyon.com.apkdownload.Utils
import java.io.File
val REQUEST_CODE = 2 // 请求码
var OVERLAY_PERMISSION_REQ_CODE = 1234
var packageName = "/FriDay"
var fileName = "/friDay.apk"
class ApkDownload (val activity: Activity){
    val TAG = this::class.java.simpleName
    val downloadUrl = "https://event.3rd-evo.com/hanks/app-productionRelease_1.3.30.7_70_NewTV.apk"
    lateinit var mPermissionsChecker: PermissionsChecker
    

    
    fun DefaultDataCheck() {
        mPermissionsChecker = PermissionsChecker(activity)
        // 缺少权限时, 进入权限配置页面
        if (mPermissionsChecker.lacksPermissions(*PERMISSIONS)) {
            startPermissionsActivity()
        }  else {
            Thread(object :Runnable{
                override fun run() {
                    startDownload();
                }
            }).start()
        }
    }


    private fun startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(
            activity,
            REQUEST_CODE,
            *PERMISSIONS
        )
    }
    
    var progressBar = ProgressDialog(activity)
    fun download(){
        progressBar = object :ProgressDialog(activity){
            override fun finish(){
                activity.finish()
            }
        }
        DefaultDataCheck()

    }

    fun startDownload():Long{
        val downloadManager:DownloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val requestId: Long = downloadManager.enqueue(CreateRequest(downloadUrl))
        Log.d(TAG,"startDownload() requestId :"+requestId)
        //查詢下載資訊方法
        queryDownloadProgress(requestId,downloadManager);
        return requestId
    }
    lateinit var downLoadPath:String
    fun CreateRequest(url:String): DownloadManager.Request{
        val request = DownloadManager.Request(Uri.parse(url))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN) // 隱藏notification
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI) //設定下載網路環境為wifi
        downLoadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path //activity.getExternalFilesDir(packageName).path
        val dir = File(downLoadPath)
        if (!dir.exists()) {
            Log.d(TAG,"  file path:"+downLoadPath+ fileName+" mkdirs.")
            dir.mkdirs()
        }
        val dirfile = File(downLoadPath+fileName)
        if (dirfile.exists()) {
            Log.d(TAG," delete file path:"+downLoadPath+ fileName+" exists.")
            dirfile.delete()
        }
        //request.setDestinationInExternalFilesDir(activity,Environment.DIRECTORY_DOWNLOADS, fileName) //指定apk快取路徑,預設是在SD卡中的Download資料夾
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        Log.d(TAG," download install path:"+downLoadPath+ fileName)
        return request
    }

    fun queryDownloadProgress( requestId:Long,  downloadManager:DownloadManager){
        val query = DownloadManager.Query()
        //根據任務編號id查詢下載任務資訊
        query.setFilterById(requestId);
        try {
            var isGoging=true;
            while (isGoging) {
                var cursor = downloadManager.query(query);
                if (cursor != null && cursor.moveToFirst()) {
//獲得下載狀態
                    var state = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    when (state) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            //下載成功
                            isGoging = false
                            handler.obtainMessage (DownloadManager.STATUS_SUCCESSFUL).sendToTarget();//傳送到主執行緒,更新ui
                        }
                        DownloadManager.STATUS_FAILED->{
                            //下載失敗
                            isGoging=false
                            handler.obtainMessage(DownloadManager.STATUS_FAILED).sendToTarget();//傳送到主執行緒,更新ui
                        }
                        DownloadManager.STATUS_RUNNING->{
                            //下載中
                            /**
                             * 計算下載下載率;
                             */
                            val totalSize = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            var currentSize = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            val progress =  (currentSize * 100.0 / totalSize).toInt()
                            Log.d(TAG,"currentSize:"+currentSize+"/totalSize:"+totalSize+" =progress:"+progress)
                            handler.obtainMessage(DownloadManager.STATUS_RUNNING, progress.toInt()).sendToTarget();//傳送到主執行緒,更新ui
                        }
                        DownloadManager.STATUS_PAUSED->{
                            //下載停止
                            handler.obtainMessage(DownloadManager.STATUS_PAUSED).sendToTarget();
                        }
                        DownloadManager.STATUS_PENDING->{
                            //準備下載
                            handler.obtainMessage(DownloadManager.STATUS_PENDING).sendToTarget();

                        }
                    }
                }
                if(cursor!=null){
                    cursor.close();
                }
            }
        }catch ( e:Exception){
            Log.e(TAG, Utils.FormatStackTrace(e)+"")
        }
    }

    var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    progressBar.setProgress(100)
//                    Toast.makeText(activity, "下載任務已經完成!", Toast.LENGTH_SHORT).show()
//                    AppController.getInstance().getMainActivity().finish();
                    val file = File(downLoadPath+fileName)
                    if(file.exists()) {
                        Log.d(TAG, "install start()!")
                        Toast.makeText(activity, "下載任務已經完成!", Toast.LENGTH_SHORT).show()
//                        activity.finish()
                    }else{
                        Log.d(TAG, "install fail()!")
                        Toast.makeText(activity, "找不到檔案!", Toast.LENGTH_SHORT).show()
                    }
                }
                DownloadManager.STATUS_RUNNING ->{
                    val progress =  msg.obj as Int;
                    progressBar.setProgress(progress)
                }
                DownloadManager.STATUS_FAILED -> canceledDialog()
                DownloadManager.STATUS_PENDING -> progressBar.show()
            }
        }
    }

    fun canceledDialog(){
        progressBar.dismiss()
        Toast.makeText(activity, "下載任務失敗!", Toast.LENGTH_SHORT).show()
    }

    
}