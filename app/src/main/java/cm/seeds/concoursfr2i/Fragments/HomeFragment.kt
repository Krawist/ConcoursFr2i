package cm.seeds.concoursfr2i.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cm.seeds.concoursfr2i.Activities.ScanningActivity
import cm.seeds.concoursfr2i.Activities.ShareActivity
import cm.seeds.concoursfr2i.Adapters.AdapterHome
import cm.seeds.concoursfr2i.AppViewModel
import cm.seeds.concoursfr2i.Helper.*
import cm.seeds.concoursfr2i.Modeles.Personne
import cm.seeds.concoursfr2i.R
import cm.seeds.concoursfr2i.Retrofit.RetrofitClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.IOException
import kotlin.properties.Delegates


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {

    val listOfNotSync = mutableListOf<Personne>()
    val listofSynched = mutableListOf<Personne>()

    var recyclerView by Delegates.notNull<RecyclerView>()
    var buttonAction by Delegates.notNull<FloatingActionButton>()
    var buttonShare by Delegates.notNull<FloatingActionButton>()
    var buttonSync by Delegates.notNull<FloatingActionButton>()
    var buttonSscan by Delegates.notNull<FloatingActionButton>()
    var buttonCapture by Delegates.notNull<FloatingActionButton>()
    var listOfFiles : List<File>? = null
    var adapterHome : AdapterHome? = null
    var model : AppViewModel? = null

    var photoFile: File? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialiseViews(view)

        addActionsOnViews()

        addDataToViews()

        configureViewModel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_FOR_CAMERA_USE_PERMISSION && resultCode == AppCompatActivity.RESULT_OK) {
            if(photoFile!=null){

                openDetailImageFragment(photoFile!!)
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun configureViewModel() {
        model = AppViewModel.getInstance(requireActivity().application)
        model?.mutableListOfImage?.observe(viewLifecycleOwner, Observer {
            listOfFiles = it
            configureAdapter()
        })
    }

    private fun configureAdapter() {
        if(false == listOfFiles?.isNullOrEmpty()){
            val contructsData = buildDataSet(listOfFiles!!)

            if(contructsData.isNullOrEmpty().not()){
                if(adapterHome!=null){
                    adapterHome?.setData(contructsData)
                }else{
                    adapterHome = AdapterHome(
                        contructsData,
                        requireActivity(),
                        object : ToDoOnItemClick {
                            override fun doOnClick(item: Any, position: Int) {
                                openDetailImageFragment(item as File)
                            }
                        })
                }
                if(recyclerView.adapter!=adapterHome){
                    recyclerView.adapter = adapterHome
                }
            }
        }
    }

    private fun buildDataSet(listOfFiles: List<File>): Map<String, MutableList<File>> {
        val map : MutableMap<String, MutableList<File>> = mutableMapOf()
        for (file in listOfFiles) {
            val date =
                formatDate(file.lastModified())
            var filesOfThatDate  = map[(date)]
            if(filesOfThatDate==null){
                filesOfThatDate = mutableListOf()
            }

            filesOfThatDate.add(file)

            map[date] = filesOfThatDate
        }

        return map
    }

    private fun addDataToViews() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun addActionsOnViews() {
        buttonAction.setOnClickListener {
            if(buttonCapture.visibility == VISIBLE){
                buttonCapture.hide()
                buttonSync.hide()
                //buttonShare.hide()
                buttonSscan.hide()
            }else{
                buttonCapture.show()
                buttonSync.show()
                //buttonShare.show()
                buttonSscan.show()
            }
        }

        buttonSync.setOnClickListener {
            val allUsers = model?.listOfPersonnes?.value

            if(allUsers.isNullOrEmpty().not()){
                for(personne in allUsers!!){
                    if(personne.isSynchronized.not()){
                        listOfNotSync.add(personne)
                    }
                }
            }

            if(listOfNotSync.isNotEmpty()){
                for(personne in listOfNotSync){
                    RetrofitClient(requireContext(), object : RetrofitClient.OnOperationTerminated{
                        override fun onSuccess(code: Int) {
                            personne.isSynchronized = true
                            model?.insertPerson(personne)
                            //showToast(requireActivity(),"Synchronisation OK pour ${personne.nom}")
                        }

                        override fun onError(code: Int) {
                            personne.isSynchronized = false
                            model?.insertPerson(personne)
                            //showToast(requireActivity(),"erreur de synchro pour ${personne.nom}")
                        }
                    }).synchedInformation(personne)
                }
            }

            showToast(requireActivity(),"Synchronisation en cours")
        }

        buttonShare.setOnClickListener {
            startActivity(Intent(requireContext(),ShareActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                it.action = ShareActivity.ACTION_PREPARE_SHARING
            })
        }

        buttonSscan.setOnClickListener {
            val intent = Intent(requireContext(),ScanningActivity::class.java).also{
                it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        }

        buttonCapture.setOnClickListener {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                // Ensure that there's a camera activity to handle the intent
                takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                    // Create the File where the photo should go
                    photoFile = try {
                        createANewFile(requireActivity())
                    } catch (ex: IOException) {
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            requireContext(),
                            getString(R.string.authority),
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, REQUEST_CODE_FOR_CAMERA_USE_PERMISSION)
                    }
                }
            }
        }
    }

    private fun openDetailImageFragment(file: File) {
        val uri = FileProvider.getUriForFile(requireContext(),getString(R.string.authority),file)
        val bundle : Bundle = Bundle().also {
            it.putString(Intent.EXTRA_TEXT,uri.toString())
        }
        showToast(requireActivity(),"Lancement")
        findNavController(this).navigate(R.id.SecondFragment,bundle)
    }

    private fun initialiseViews(view: View) {
        recyclerView = view.findViewById(R.id.layout_all_images_takes)
        buttonAction  = view.findViewById(R.id.fab_perform_action)
        buttonSync = view.findViewById(R.id.fab_action_synchronise)
        buttonCapture = view.findViewById(R.id.fab_action_capture_image)
        buttonSscan = view.findViewById(R.id.fab_action_scan)
        buttonShare = view.findViewById(R.id.fab_action_share_content)
    }
}