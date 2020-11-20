package cm.seeds.concoursfr2i.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import cm.seeds.concoursfr2i.Activities.MainActivity
import cm.seeds.concoursfr2i.Helper.ToDoOnItemClick
import cm.seeds.concoursfr2i.R
import cm.seeds.concoursfr2i.Helper.loadImage
import java.io.File

public class AdapterImage(var images : List<File>, val toDoOnItemClick: ToDoOnItemClick) : RecyclerView.Adapter<AdapterImage.HomeImageViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeImageViewHolder {
        return HomeImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_image,parent,false))
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: HomeImageViewHolder, position: Int) {
        holder.bindData(images[position], position)
    }

    inner class HomeImageViewHolder (view : View) : RecyclerView.ViewHolder(view){

        private val image : ImageView = view.findViewById(R.id.imageview_item_image_image)

        fun bindData( file : File, position : Int){
            loadImage(image, file.toUri())

            itemView.setOnClickListener {
                if(image.context is MainActivity){
                    toDoOnItemClick.doOnClick(file,position)
                }
            }
        }

    }
}