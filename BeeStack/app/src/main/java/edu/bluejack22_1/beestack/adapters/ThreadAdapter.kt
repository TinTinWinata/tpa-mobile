import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import edu.bluejack22_1.beestack.activities.ThreadDetailActivity
import edu.bluejack22_1.beestack.databinding.ThreadItemBinding
import edu.bluejack22_1.beestack.model.Thread

class ThreadAdapter (val items : MutableList<Thread>)
    : RecyclerView.Adapter<ThreadAdapter.ViewHolder>(){
    private lateinit var binding: ThreadItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreadAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding= ThreadItemBinding.inflate(inflater,parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ThreadAdapter.ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    //View Holder
    inner class ViewHolder(itemView : ThreadItemBinding) : RecyclerView.ViewHolder(itemView.root){


        fun bind(item : Thread){
            itemView.setOnClickListener {
                val i = Intent(itemView.context, ThreadDetailActivity::class.java);
                i.putExtra("thread", item);
                itemView.context.startActivity(i);
            }
            binding.apply {
                val profileBg: String? = item.user?.photoProfile
                if(!profileBg.isNullOrEmpty()){
                    Picasso.get().load(profileBg).into(image);
                }
                upVoteTV.text = item.topCount.toString();
                downVoteTV.text = item.downCount.toString();
                title.text= item.title
                description.text= item.description
                credential.text = item.user?.username
                
//              Get how many answer document in collections is

                item.getAnswerCollection().addOnSuccessListener {
                    answerTV.text = it.size().toString()
                }
            }
        }
    }
}