package cm.seeds.concoursfr2i.Retrofit

import android.content.Context
import android.util.Log
import cm.seeds.concoursfr2i.Modeles.Personne
import cm.seeds.concoursfr2i.Modeles.RPersonne
import cm.seeds.concoursfr2i.Modeles.RPersonnes
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

public class RetrofitClient(val context: Context, val listener: OnOperationTerminated){

    private val okHttpClient : OkHttpClient
    private val retrofitClient : RetrofitInterface
    init {
        okHttpClient = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .build()
/*                val response = chain.proceed(newRequest);
                Log.e("TAG"," data square transaction"+response.body()?.string())*/
                chain.proceed(newRequest)
            }
            .build()

        //Log.e(TAG,"token: "+token);
        retrofitClient = Retrofit.Builder()
            .baseUrl(RetrofitInterface.HOST_LINK)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitInterface::class.java)
    }


    fun synchedInformation(personne: Personne){
        val call: Call<RPersonne> = retrofitClient.submitInformations(personne)
        call.enqueue(object : Callback<RPersonne?> {
            override fun onResponse(
                call: Call<RPersonne?>,
                response: Response<RPersonne?>
            ) {
                val any: RPersonne? = response.body()
                val code = response.code()
                if (code == 200) {
                    listener.onSuccess(code)
                } else {
                    listener.onError(code)
                }
            }

            override fun onFailure(
                call: Call<RPersonne?>,
                t: Throwable
            ) {
                listener.onError(0)
            }
        })
    }

    interface OnOperationTerminated{
        fun onSuccess(code: Int)
        fun onError(code: Int)
    }
}