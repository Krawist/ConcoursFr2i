package cm.seeds.concoursfr2i.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import cm.seeds.concoursfr2i.Modeles.Personne
import cm.seeds.concoursfr2i.R

public class AdapterPersonne(var personnes : MutableList<Personne>?) : RecyclerView.Adapter<AdapterPersonne.PersonneViewHolder>(){

    init {
        personnes?.reverse()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonneViewHolder {
        return PersonneViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_person,parent,false))
    }

    override fun getItemCount(): Int {
        if(personnes!=null){
            return personnes!!.size
        }

        return 0
    }

    override fun onBindViewHolder(holder: PersonneViewHolder, position: Int) {
        holder.bindData(personnes!![position],position)
    }

    fun setList(it: MutableList<Personne>?) {
        this.personnes = it
        personnes?.reverse()
        notifyDataSetChanged()
    }

    inner class PersonneViewHolder(view: View) : RecyclerView.ViewHolder(view){

        fun bindData(personne: Personne, position : Int){
            itemView.findViewById<TextView>(R.id.textview_nom_personne).text = "${personne.nom} ${personne.age} Ans"
            itemView.findViewById<TextView>(R.id.textview_telephone_email).text = "${personne.email} / ${personne.telephone}"
            itemView.findViewById<TextView>(R.id.textview_localistation).text = "${personne.localisation}"
            if(personne.isWoman){
                itemView.findViewById<ImageView>(R.id.image_person).setImageResource(R.drawable.human_female)
            }else{
                itemView.findViewById<ImageView>(R.id.image_person).setImageResource(R.drawable.human_male)
            }

            if(personne.isSynchronized){
                itemView.findViewById<ImageView>(R.id.imageview_issynched).setColorFilter(Color.GREEN)
            }else{
                itemView.findViewById<ImageView>(R.id.imageview_issynched).setColorFilter(Color.RED)
            }
        }
    }
}