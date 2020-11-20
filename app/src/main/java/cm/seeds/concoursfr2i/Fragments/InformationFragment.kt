package cm.seeds.concoursfr2i.Fragments

import android.os.Bundle
import android.os.RecoverySystem
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cm.seeds.concoursfr2i.Adapters.AdapterPersonne
import cm.seeds.concoursfr2i.AppViewModel
import cm.seeds.concoursfr2i.Modeles.Personne
import cm.seeds.concoursfr2i.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.properties.Delegates

class InformationFragment : Fragment() {

    var fabAddUser by Delegates.notNull<FloatingActionButton>()
    var recyclerView by Delegates.notNull<RecyclerView>()
    var layoutUserInsertion by Delegates.notNull<LinearLayout>()
    var editTextNom by Delegates.notNull<EditText>()
    var editTextEmail by Delegates.notNull<EditText>()
    var editTextTelelphone by Delegates.notNull<EditText>()
    var editTextLocalisation by Delegates.notNull<EditText>()
    var editTextAge by Delegates.notNull<EditText>()
    var radiogroupGenre by Delegates.notNull<RadioGroup>()
    var buttonLocateAutomatically by Delegates.notNull<ImageButton>()
    var closeInsertionView by Delegates.notNull<ImageButton>()

    var adapterPersonne : AdapterPersonne? = null

    var model : AppViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialiseViews(view)

        addDataToViews()

        addActionsOnViews()

        configureViewmodel()
    }

    private fun configureViewmodel() {
        model = AppViewModel.getInstance(requireActivity().application)

        model?.listOfPersonnes?.observe(viewLifecycleOwner, Observer {
            configureAdater(it)
        })
    }

    private fun configureAdater(it: MutableList<Personne>?) {
        if(it.isNullOrEmpty().not()){
            if(adapterPersonne!=null){
                adapterPersonne?.setList(it)
            }else{
                adapterPersonne = AdapterPersonne(it)
            }

            if(recyclerView.adapter != adapterPersonne){
                recyclerView.adapter = adapterPersonne
            }
        }
    }

    private fun addActionsOnViews() {

        fabAddUser.setOnClickListener {
            if(layoutUserInsertion.visibility == GONE){
                layoutUserInsertion.visibility = VISIBLE
                fabAddUser.setImageResource(R.drawable.ic_round_check_24)
            }else{
                verifyAndValidateData()
            }
        }

        closeInsertionView.setOnClickListener {
            fabAddUser.setImageResource(R.drawable.ic_round_add_24)
            layoutUserInsertion.visibility = GONE
        }
    }

    private fun verifyAndValidateData() {
        if(allIsCorrect()){
            val nom = editTextNom.text.toString()
            val email = editTextEmail.text.toString()
            val telephone = editTextTelelphone.text.toString()
            val age = editTextAge.text.toString()
            val isWomen = radiogroupGenre.checkedRadioButtonId == R.id.radiobutton_femme
            val localisation = editTextLocalisation.text.toString()

            val personne = Personne(nom = nom, isWoman = isWomen, email = email, telephone = telephone, age = age.toInt(), localisation = localisation, isSynchronized = false)

            model?.insertPerson(personne)
        }
    }

    private fun allIsCorrect(): Boolean {
        var allIsCorrect = true
        if(TextUtils.isEmpty(editTextNom.text)){
            editTextNom.error = getString(R.string.error_input_message)
            allIsCorrect = false
        }else{
            editTextNom.error = null
        }


        if(TextUtils.isEmpty(editTextEmail.text)){
            editTextEmail.error = getString(R.string.error_input_message)
            allIsCorrect = false
        }else{
            editTextEmail.error = null
        }

        if(TextUtils.isEmpty(editTextTelelphone.text)){
            editTextTelelphone.error = getString(R.string.error_input_message)
            allIsCorrect = false
        }else{
            editTextTelelphone.error = null
        }

        if(TextUtils.isEmpty(editTextAge.text)){
            editTextAge.error = getString(R.string.error_input_message)
            allIsCorrect = false
        }else{
            editTextAge.error = null
        }

        if(TextUtils.isEmpty(editTextLocalisation.text)){
            editTextLocalisation.error = getString(R.string.error_input_message)
            allIsCorrect = false
        }else{
            editTextLocalisation.error = null
        }

        if(!(radiogroupGenre.checkedRadioButtonId==R.id.radiobutton_femme || radiogroupGenre.checkedRadioButtonId == R.id.radiobutton_homme)){
            allIsCorrect = false
        }

        return allIsCorrect
    }

    private fun addDataToViews() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun initialiseViews(view: View) {
        fabAddUser = view.findViewById(R.id.fab_add_user)
        recyclerView = view.findViewById(R.id.recycler_list_person)
        layoutUserInsertion = view.findViewById(R.id.view_person_insertion)
        editTextNom = view.findViewById(R.id.editetxt_nom)
        editTextEmail = view.findViewById(R.id.editetxt_email)
        editTextTelelphone = view.findViewById(R.id.editetxt_telephone)
        editTextLocalisation = view.findViewById(R.id.editetxt_localisation)
        editTextAge = view.findViewById(R.id.editetxt_age)
        radiogroupGenre = view.findViewById(R.id.radiogroup_genre)
        buttonLocateAutomatically = view.findViewById(R.id.button_locate_automatically)
        closeInsertionView = view.findViewById(R.id.dropdown_menu)
    }
}