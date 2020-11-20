package cm.seeds.concoursfr2i

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import cm.seeds.concoursfr2i.Helper.USERS_LIST_PREFERENCES_KEY
import cm.seeds.concoursfr2i.Modeles.Personne
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.net.ServerSocket
import java.net.Socket

class AppViewModel(application : Application) : AndroidViewModel(application){

    val mutableListOfImage : MutableLiveData<List<File>> = MutableLiveData()
    val listOfPersonnes : MutableLiveData<MutableList<Personne>> = MutableLiveData()
    val listOfConnectedDevice : MutableList<Socket> = mutableListOf()
    var serverSocket : ServerSocket? = null

    init {
        refreshData(application)
    }

    private fun refreshData(application: Application) {
        val files = mutableListOf<File>()
        application.getExternalFilesDir(null)?.listFiles()?.forEach {
            files.add(it)
        }
        mutableListOfImage.value = files

        loadPersonnes()
    }

    public fun insertPerson(personne: Personne){
        var persones = listOfPersonnes.value

        if(personne.idUser == 0.toLong()){
            // c'est un nouvel utilisateur
            personne.idUser = System.currentTimeMillis()
        }

        if(true == persones?.isNullOrEmpty()){
            persones = mutableListOf()
        }else{
            var personneIn : Personne? = null
            for(pers in persones){
                if(pers.idUser == personne.idUser){
                    personneIn = pers
                }
            }
            persones.remove(personneIn)
        }


        persones.add(personne)


        listOfPersonnes.value = persones

        savePersonnes(persones)
    }

    private fun loadPersonnes(){
        val type = object : TypeToken<List<Personne?>?>() {}.type
        var personnes : MutableList<Personne>
        val users = getApplication<Application>().getSharedPreferences(USERS_LIST_PREFERENCES_KEY, Context.MODE_PRIVATE).getString(
            USERS_LIST_PREFERENCES_KEY, null)

        personnes = if(users==null){
            mutableListOf()
        }else{
            Gson().fromJson(users,type)
        }

        listOfPersonnes.value = personnes
    }

    private fun savePersonnes(persones: MutableList<Personne>) {
        getApplication<Application>().getSharedPreferences(USERS_LIST_PREFERENCES_KEY, Context.MODE_PRIVATE)
            .edit()
            .putString(USERS_LIST_PREFERENCES_KEY,Gson().toJson(persones))
            .apply()
    }

    companion object{

        private var instance : AppViewModel? = null

        @JvmStatic
        fun getInstance(): AppViewModel? {
            return instance
        }

        @JvmStatic
        fun getInstance(application: Application): AppViewModel {
            if(instance==null){
                instance = AppViewModel(application)
            }
            return instance!!
        }
    }

}