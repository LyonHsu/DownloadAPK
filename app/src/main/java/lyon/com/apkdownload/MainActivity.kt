package lyon.com.apkdownload

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import lyon.com.apkdownload.DownloadAPK.ApkDownload
import lyon.com.apkdownload.DownloadAPK.OVERLAY_PERMISSION_REQ_CODE
import lyon.com.apkdownload.DownloadAPK.REQUEST_CODE
import lyon.com.apkdownload.R
import lyon.com.apkdownload.Permissions.PermissionsActivity

class MainActivity : AppCompatActivity() {
    val TAG = this::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val apkDownload = ApkDownload(this)
        apkDownload.download()
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        // 拒绝时, 关闭页面, 缺少主要权限, 无法运行
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // SYSTEM_ALERT_WINDOW permission not granted...
                Toast.makeText(getBaseContext(),"Permission Denieddd by user.Please Check it in Settings!!", Toast.LENGTH_SHORT).show();
            }
            finish()
        } else if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish()
        } else if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_GRANTED) {
            val apkDownload = ApkDownload(this)
            apkDownload.download()
        }
    }
}