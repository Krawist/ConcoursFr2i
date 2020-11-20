package cm.seeds.concoursfr2i.Helper

import android.app.Activity
import android.app.Dialog
import android.graphics.Point
import android.net.Uri
import android.text.format.DateUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import cm.seeds.concoursfr2i.R
import com.bumptech.glide.Glide
import java.io.File

const val REQUEST_CODE_FOR_CAMERA_USE_PERMISSION = 11
const val REQUEST_CODE_FOR_SCAN_ACTIVITY = 12
const val REQUEST_CODE_FOR_ACCESS_LOCATION_PERMISSION = 13

const val USERS_LIST_PREFERENCES_KEY = "LISTE_PERSONNES"

fun loadImage(imageView: ImageView, imageUri : Uri){
    Glide.with(imageView)
            .load(imageUri)
            .into(imageView)
}

fun formatDate(timeInMilli: Long): String {
    return DateUtils.getRelativeTimeSpanString(
        timeInMilli,
        System.currentTimeMillis(),
        DateUtils.DAY_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()
}

fun getNumberOfItemInLine(activity: Activity, dimensRes : Int): Int {
    val display = activity.windowManager.defaultDisplay
    val size = Point()
    display.getSize(size)
    val with = size.x
    val oneItemWidth =
        activity.resources.getDimension(dimensRes).toInt()
    return with / oneItemWidth
}

fun showToast(activity: Activity, message : String){
    Toast.makeText(activity,message,Toast.LENGTH_SHORT).show()
}

fun getDialog(activity: Activity, title : String, message: String, positiveButtontext : String, negativeButtonText : String) : Dialog{
    val dialog = Dialog(activity)
    dialog.setContentView(R.layout.simple_dialog_layout)
    dialog.findViewById<TextView>(R.id.dialog_title).text = title
    dialog.findViewById<TextView>(R.id.dialog_content).text = message
    dialog.findViewById<Button>(R.id.dialog_positive_button).text = positiveButtontext
    dialog.findViewById<Button>(R.id.dialog_negative_button).text = negativeButtonText
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    return dialog
}

fun createANewFile(activity: Activity): File? {
    return File(activity.getExternalFilesDir(null), "${System.currentTimeMillis()}.jpg")
}

