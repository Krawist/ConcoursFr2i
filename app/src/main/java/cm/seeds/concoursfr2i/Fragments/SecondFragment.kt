package cm.seeds.concoursfr2i.Fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import cm.seeds.concoursfr2i.Activities.ShareActivity
import cm.seeds.concoursfr2i.Helper.getDialog
import cm.seeds.concoursfr2i.Helper.loadImage
import cm.seeds.concoursfr2i.Helper.showToast
import cm.seeds.concoursfr2i.R
import cm.seeds.concoursfr2i.ml.ResnetV21011Metadata1
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.lang.StringBuilder
import kotlin.properties.Delegates


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    var image by Delegates.notNull<ImageView>()
    var buttonClassify by Delegates.notNull<ExtendedFloatingActionButton>()
    var fabShareConetnt by Delegates.notNull<FloatingActionButton>()
    var fileUri : Uri? = null
    var model : ResnetV21011Metadata1? = null
    var bitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.let {
            fileUri = Uri.parse(it?.getString(Intent.EXTRA_TEXT, null))
        }

        initClassifier()

        transformUriToBitmap()
    }

    private fun initClassifier() {
        model = ResnetV21011Metadata1.newInstance(requireContext())
    }

    private fun transformUriToBitmap() {


        Glide.with(this)
            .asBitmap()
            .load(fileUri)
            .into(object : CustomTarget<Bitmap>(){
                override fun onLoadCleared(placeholder: Drawable?) {

                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    this@SecondFragment.bitmap = resource
                }
            })
    }

    private fun classify(){

// Creates inputs for reference.
        val image = TensorImage.fromBitmap(bitmap)

// Runs model inference and gets result.
        val outputs = model?.process(image)
        val probability = outputs?.probabilityAsCategoryList

// Releases model resources if no longer used.
        model?.close()
        if(probability!=null){
            probability.sortByDescending { it.score }
            val builder = StringBuilder()
            for(i in 0..5){
                val category = probability[i]
                builder.append(category.label)
                builder.append(" => ")
                builder.append(category.score)
                builder.append("\n \n")
            }

            val dialog = getDialog(requireActivity(),"Resultats de la classification",builder.toString(),"OK",getString(R.string.annuler))
            dialog.findViewById<Button>(R.id.dialog_positive_button).setOnClickListener {
                dialog.dismiss()
            }

            dialog.findViewById<Button>(R.id.dialog_negative_button).visibility =  GONE

            dialog.show()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        model?.close()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialiseViews(view)

        addDataToViews()

        addActionsOnViews()
    }

    private fun addActionsOnViews() {
        buttonClassify.setOnClickListener {
            if(bitmap!=null){
                classify()
            }else{
                showToast(requireActivity(),"Impossible de charger l'image")
            }
        }

        fabShareConetnt.setOnClickListener {
            startActivity(Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM,fileUri)
            })
        }
    }

    private fun addDataToViews() {
        if(fileUri!=null){
            loadImage(image,fileUri!!)
        }else{
            showToast(requireActivity(),"Aucune image")
        }
    }

    private fun initialiseViews(view: View) {
        image = view.findViewById(R.id.image_captured)
        buttonClassify = view.findViewById(R.id.button_classify_image)
        fabShareConetnt = view.findViewById(R.id.fab_action_share_content)
    }
}