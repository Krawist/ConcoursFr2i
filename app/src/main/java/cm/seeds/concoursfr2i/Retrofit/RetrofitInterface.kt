package cm.seeds.concoursfr2i.Retrofit

import cm.seeds.concoursfr2i.Modeles.Personne
import cm.seeds.concoursfr2i.Modeles.RPersonne
import cm.seeds.concoursfr2i.Modeles.RPersonnes
import retrofit2.Call
import retrofit2.http.*

interface RetrofitInterface {

    companion object{
        public const val HOST_LINK = " https://www.squares.seeds.cm/"
    }

    @POST("General/dummy_data")
    fun submitInformations(@Body personne: Personne) : Call<RPersonne>

    @GET("General/dummy_data")
    fun loadInformations() : Call<RPersonnes>
}