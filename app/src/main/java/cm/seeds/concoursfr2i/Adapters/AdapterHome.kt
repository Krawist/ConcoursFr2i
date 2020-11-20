package cm.seeds.concoursfr2i.Adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cm.seeds.concoursfr2i.Helper.ToDoOnItemClick
import cm.seeds.concoursfr2i.R
import cm.seeds.concoursfr2i.Helper.getNumberOfItemInLine
import java.io.File
import java.util.ArrayList

public class AdapterHome(var imagesMap : Map<String, MutableList<File>>, val activity: Activity, val toDoOnItemClick: ToDoOnItemClick) : RecyclerView.Adapter<AdapterHome.HomeDataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeDataViewHolder {
        return HomeDataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_home_data,parent,false))
    }

    override fun getItemCount(): Int {
        return imagesMap.entries.size
    }

    override fun onBindViewHolder(holder: HomeDataViewHolder, position: Int) {
        holder.bindData(ArrayList(imagesMap.entries)[position])
    }

    fun setData(contructsData: Map<String, MutableList<File>>) {
        imagesMap = contructsData
        notifyDataSetChanged()
    }

    inner class HomeDataViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val textData = view.findViewById<TextView>(R.id.textview_date_image)
        private val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview_item_home_data_list)

        init {
            recyclerView.layoutManager = GridLayoutManager(activity,
                getNumberOfItemInLine(
                    activity,
                    R.dimen.width_image
                )
            )
        }

        fun bindData(entry: Map.Entry<String, List<File>>?) {

            textData.text = entry?.key

            if(entry?.value!=null){
                recyclerView.adapter =
                    AdapterImage(entry.value, object : ToDoOnItemClick{
                        override fun doOnClick(item: Any, position: Int) {
                            toDoOnItemClick.doOnClick(item,position)
                        }
                    })
            }
        }

    }
}