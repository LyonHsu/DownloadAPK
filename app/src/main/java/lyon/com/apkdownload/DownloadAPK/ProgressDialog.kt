package lyon.com.apkdownload.DownloadAPK

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import kotlinx.android.synthetic.main.apk_download_layout.*
import lyon.com.apkdownload.R
import lyon.com.apkdownload.Utils


open class ProgressDialog(context: Context): Dialog(context){

    val TAG = this::class.java.simpleName+"_ApkDownload"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apk_download_layout)
        //設置對話框樣式
//        setStyle();

        dialog_button_agree.setOnClickListener {
            Utils.installApk(context!!)
        }
        dialog_button_back.setOnClickListener {
            dismiss()
            finish()
        }
    }

    open fun finish(){

    }

    fun setInstallButtonVisible(v:Int){
        dialog_button_agree.visibility=v
    }

    fun setProgress(process:Int){
        try {
            textView3.setText(process.toString()+ " %")
            progressBar.setProgress(process)
        }catch (e:NullPointerException){
//            Log.e(TAG,""+Utils.FormatStackTrace(e))
        }
    }

    fun setMsg(msg:String){
        try {
            msg_text.setText(msg)
            msg_text.visibility = View.VISIBLE
        }catch (e:NullPointerException){
//            Log.e(TAG,""+Utils.FormatStackTrace(e))
        }
    }

    fun setSize(size:String){
        try{
            size_text.visibility= View.VISIBLE
            size_text.setText(size)
        } catch (e: NullPointerException) {
//            Log.e(TAG,""+Utils.FormatStackTrace(e))
        }
    }

    fun setTime(time:Int){
        try{
            time_text.visibility= View.VISIBLE
            time_text.setText(time.toString()+" 秒")
        } catch (e: NullPointerException) {
//            Log.e(TAG,""+Utils.FormatStackTrace(e))
        }
    }

    fun setRateValue(rate:String) {
        try {
            textView2.setText(rate)
        } catch (e: NullPointerException) {
//            Log.e(TAG,""+Utils.FormatStackTrace(e))
        }
    }

    open fun setStyle() {
        //設置對話框不可取消
        setCancelable(false)
        //設置觸摸對話框外面不可取消
        setCanceledOnTouchOutside(false)
        val displaymetrics = DisplayMetrics()
        window!!.windowManager.defaultDisplay.getMetrics(displaymetrics)
        //獲得應用窗口大小
        val layoutParams = this.window!!.attributes
        //設置對話框居中顯示
        layoutParams.gravity = Gravity.CENTER
        //設置對話框寬度為屏幕的3/5
        layoutParams.width = displaymetrics.widthPixels / 5 * 3
    }

}