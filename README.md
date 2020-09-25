# DownloadAPK

Permission 需要 安裝<REQUEST_INSTALL_PACKAGES>, 網路<INTERNET>, 讀取寫入硬碟權限<READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE>
     
        <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES"/>
        <uses-permission android:name="android.permission.INTERNET" />
        <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
        <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
        
SDK 大於 24 的機種 有限制 讀取路徑 ，所以我們需要設定 provider 在manifest.xml
在 Android Nougat (API 24) 之後，跨 App 分享檔案路徑必須透過 FileProvider ，FileProvider 會隱藏的檔案的實際路徑，讓檔案分享更安全。
        
FileUriExposedException
如果沒有透過 FileProvider 分享檔案路徑至其他 App 會拋出 FileUriExposedException ，

# FileProvider  https://medium.com/@picapro33/android-%E8%B7%A8-app-%E5%88%86%E4%BA%AB%E6%AA%94%E6%A1%88-fileprovider-introduced-on-android-n-3dced9bf9572
要使用 FileProvider ，需要完成 2 個前置設定：
在 AndroidManifest.xml 宣告 androidx.core.content.FileProvider

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${packageName}.provider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/provider_paths"/>
        </provider>
       
 android:authorities 要填入暫時授予的權限名稱，
 如果 App 的 package name 是 com.example.app 這裡就填入 com.example.app.provider 。
 或是直接使用 ${packagename} 來自動設定。
 
 新增 res/xml/provider_paths 並設定開放的目錄
  
      <?xml version="1.0" encoding="utf-8"?>
      <paths xmlns:android="http://schemas.android.com/apk/res/android">
          <cache-path name="private" path="userTemp/" />
          <external-path name="shared" path="Download/" />
      </paths>
 
 cache-path 和 external-path 分別代表了 Android 裝置上的不同檔案目錄，
cache-path 路徑等於 getCacheDir()
 依照上面的設定，如果分享了一個檔案位於
cacheDir/userTemp/file.txt
其他 App 收到的路徑會是
content://com.example.app.fileprovider/private/file.txt
可以看出 FileProvider 確實隱藏了檔案的實際路徑。
3. 使用 FileProvider 替換檔案實際路徑
範例如下：

    val intent = Intent(Intent.ACTION_VIEW)
    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(
            context,
            "com.example.app.fileprovider",
            file
        ).also {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    } else {
        Uri.fromFile(file)
    }
    intent.setDataAndType(uri, mimeType)
因為 FileProvider 是 Android N (API 24) 以後才有的功能，我們需要先檢查裝置的 OS 版本，再決定是否使用 FileProvider 。
FileProvider.getUriForFile() 用來取得替換的檔案路徑，第二個參數就是 AndroidManifest.xml 中宣告的 android:authorities 。
取得替換路徑後，需要呼叫 intent.addFlags() 設定檔案的讀寫權限，這邊設定成唯讀。如果要讓其他 App 可以寫入檔案，可以替換成 Intent.FLAG_GRANT_WRITE_URI_PERMISSION 。
最後就是呼叫 intent.setDataAndType(uri, mimeType) 將 Uri 放入 Intent ，第二個參數是檔案格式，也可以傳入 null 。
 
 #PERMISSIONS
  由於我們需要儲存下載的apk 以及在安裝時需要讀取，所以需要  READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE 兩個PERMISSIONS。
  而這兩個PERMISSIONS 是需要詢問使用者是否要賦予權限的。
  所以在下載apk之前，我們需要詢問權限 PermissionsChecker。
 
 
 
 
