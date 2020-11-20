package cm.seeds.concoursfr2i.Activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cm.seeds.concoursfr2i.Helper.REQUEST_CODE_FOR_CAMERA_USE_PERMISSION
import cm.seeds.concoursfr2i.Helper.getDialog
import cm.seeds.concoursfr2i.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.util.*
import kotlin.properties.Delegates

class ScanningActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private var button: Button? = null
    private var textcontentScanned by Delegates.notNull<TextView>()
    private var flashButton: ImageButton? = null
    private var scannerView: ZXingScannerView? = null
    var flashActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanning_activity)
        initialiseViews()
        addActionsOnViews()
        startScanning()
    }

    override fun handleResult(result: Result) {
        val intent = Intent().also {
            it.putExtra(Intent.EXTRA_TEXT,result.text)
        }

        setResult(Activity.RESULT_OK,intent)

        finish()
    }

    private fun startScanning() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                val formatList: MutableList<BarcodeFormat> = ArrayList()
                formatList.add(BarcodeFormat.CODABAR)
                formatList.add(BarcodeFormat.QR_CODE)
                scannerView!!.setFormats(formatList)
                scannerView!!.setResultHandler(this)
                scannerView!!.setAutoFocus(true)
                scannerView!!.stopCamera()
                scannerView!!.startCamera()
                scannerView!!.setAutoFocus(true)
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                val dialog: Dialog = getDialog(this,getString(R.string.permission),getString(R.string.permission_explanation_message),getString(R.string.autoriser),getString(R.string.refuser))
                dialog.findViewById<Button>(R.id.dialog_negative_button).setOnClickListener {
                    dialog.dismiss()
                    finish()
                }

                dialog.findViewById<Button>(R.id.dialog_positive_button).setOnClickListener {
                    dialog.dismiss()
                    askPermissions()
                }
                dialog.show()
            }
            else -> {
                askPermissions()
            }
        }
    }

    private fun askPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_FOR_CAMERA_USE_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_FOR_CAMERA_USE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // La permission est accordée
                    startScanning()
                } else {
                    finish()
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun addActionsOnViews() {
        flashButton!!.setOnClickListener { changeFlashSetting() }
    }

    private fun changeFlashSetting() {
        flashActive = !flashActive
        scannerView!!.flash = flashActive
        if (flashActive) {
            flashButton!!.setImageResource(R.drawable.ic_round_flash_on_24)
        } else {
            flashButton!!.setBackgroundResource(R.drawable.ic_round_flash_off_24)
        }
    }

    private fun initialiseViews() {
        textcontentScanned = findViewById(R.id.textview_content_scanned)
        flashButton = findViewById(R.id.imagebutton_change_flash)
        val frameLayout = findViewById<FrameLayout>(R.id.content_frame)
        scannerView = ZXingScannerView(this)
        frameLayout.addView(scannerView)
    }
}