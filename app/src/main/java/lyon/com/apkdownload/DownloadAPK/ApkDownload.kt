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
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import lyon.com.apkdownload.Permissions.PermissionsActivity
import lyon.com.apkdownload.Permissions.PermissionsActivity.PERMISSIONS
import lyon.com.apkdownload.Permissions.PermissionsChecker
import lyon.com.apkdownload.Utils
import org.json.JSONObject
import java.io.File
val REQUEST_CODE = 2 // 请求码
var OVERLAY_PERMISSION_REQ_CODE = 1234
var packageName = "/FriDay"
var fileName = "/friDay.apk"
class ApkDownload (val activity: Activity){
    val TAG = this::class.java.simpleName
    val downloadUrl = "https://event.3rd-evo.com/hanks/app-productionRelease_1.3.30.7_70_NewTV.apk"
    lateinit var mPermissionsChecker: PermissionsChecker
    var startTime =0.0
    var nowTime = 0.0
    var totalSize=0
    var currentSize=0
    fun DefaultDataCheck() {
        mPermissionsChecker = PermissionsChecker(activity)
        // 缺少权限时, 进入权限配置页面
        if (mPermissionsChecker.lacksPermissions(*PERMISSIONS)) {
            startPermissionsActivity()
        }  else {
            Thread(object :Runnable{
                override fun run() {
                    startTime = System.currentTimeMillis().toDouble()
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
                            totalSize = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            currentSize = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))

                            nowTime = System.currentTimeMillis().toDouble()
                            val progress =  (currentSize * 100.0 / totalSize).toInt()
                            var totleTime = nowTime-startTime;
                            val rate = Utils.getRate(currentSize,nowTime,startTime)
                            var ratevalue:String
                            var cSize:String
                            var tSize:String
                            if(rate > 1000)
                                ratevalue = String.format("%.2f",rate/1024)+" Mbps"
                            else
                                ratevalue =  String.format("%.2f",rate).toString()+(" Kbps");
                            Log.d(TAG,"20201117 網路= "+ratevalue+" bps, 花費時間："+totleTime)
                            Log.d(TAG,"currentSize:"+currentSize+"/totalSize:"+totalSize+" =progress:"+progress)
                            val msg = Message()
                            msg.what = DownloadManager.STATUS_RUNNING
                            if(totalSize > 1000*1000*1000)
                                tSize = String.format("%.2f",(totalSize/1024/1024/1024).toDouble())+" GB"
                            else if(totalSize > 1000*1000)
                                tSize = String.format("%.2f",(totalSize/1024/1024).toDouble())+" MB"
                            else if(totalSize > 1000)
                                tSize = String.format("%.2f",(totalSize/1024).toDouble())+" KB"
                            else
                                tSize = String.format("%.2f",totalSize.toDouble()).toString()+(" B")

                            if(currentSize > 1000*1000*1000)
                                cSize = String.format("%.2f",(currentSize/1024/1024/1024).toDouble())+" GB"
                            else if(currentSize > 1000*1000)
                                cSize = String.format("%.2f",(currentSize/1024/1024).toDouble())+" MB"
                            else if(currentSize > 1000)
                                cSize = String.format("%.2f",(currentSize/1024).toDouble())+" KB"
                            else
                                cSize = String.format("%.2f",currentSize.toDouble()).toString()+(" B")
                            var json = JSONObject()
                            json.put("rate",ratevalue)
                            json.put("size",cSize+" / " +tSize)
                            msg.obj = json
                            msg.arg1 = progress
                            msg.arg2 = totleTime.toInt()/1000
                            handler.sendMessage(msg)

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
                    var tSize:String
                    var cSize:String
                    if(totalSize > 1000*1000*1000)
                        tSize = String.format("%.2f",(totalSize/1024/1024/1024).toDouble())+" GB"
                    else if(totalSize > 1000*1000)
                        tSize = String.format("%.2f",(totalSize/1024/1024).toDouble())+" MB"
                    else if(totalSize > 1000)
                        tSize = String.format("%.2f",(totalSize/1024).toDouble())+" KB"
                    else
                        tSize = String.format("%.2f",totalSize.toDouble()).toString()+(" B")

                    if(currentSize > 1000*1000*1000)
                        cSize = String.format("%.2f",(currentSize/1024/1024/1024).toDouble())+" GB"
                    else if(currentSize > 1000*1000)
                        cSize = String.format("%.2f",(currentSize/1024/1024).toDouble())+" MB"
                    else if(currentSize > 1000)
                        cSize = String.format("%.2f",(currentSize/1024).toDouble())+" KB"
                    else
                        cSize = String.format("%.2f",currentSize.toDouble()).toString()+(" B")
//                    Toast.makeText(activity, "下載任務已經完成!", Toast.LENGTH_SHORT).show()
//                    AppController.getInstance().getMainActivity().finish();
                    val file = File(downLoadPath+fileName)
                    if(file.exists()) {
                        progressBar.setInstallButtonVisible(View.VISIBLE)
                        Log.d(TAG, "install start()!")
                        progressBar.setMsg("下載任務已經完成!")
                        progressBar.setSize(tSize+" / "+tSize)
                        Toast.makeText(activity, "下載任務已經完成!", Toast.LENGTH_SHORT).show()
//                        activity.finish()
                    }else{
                        progressBar.setInstallButtonVisible(View.GONE)
                        Log.d(TAG, "install fail()!")
                        progressBar.setMsg("找不到下載檔案!下載失敗！！")
                        progressBar.setSize(cSize+" / "+tSize)
                        Toast.makeText(activity, "找不到檔案!", Toast.LENGTH_SHORT).show()
                    }
                }
                DownloadManager.STATUS_RUNNING ->{
                    val progress =  msg.arg1 as Int;
                    progressBar.setProgress(progress)
                    var json = JSONObject(msg.obj.toString())
                    progressBar.setRateValue(json.optString("rate"))
                    progressBar.setSize(json.optString("size"))
                    progressBar.setTime(msg.arg2)
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