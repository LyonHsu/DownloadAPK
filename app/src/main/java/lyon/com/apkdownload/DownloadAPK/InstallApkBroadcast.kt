package lyon.com.apkdownload.DownloadAPK

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import lyon.com.apkdownload.Utils
import java.io.File


open class InstallApkBroadcast : BroadcastReceiver() {

    val TAG =this::class.java.simpleName +"_ApkDownload"
    override fun onReceive(context: Context?, intent: Intent?) {
        Utils.installApk(context!!);
    }


}