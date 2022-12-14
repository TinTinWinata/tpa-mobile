package edu.bluejack22_1.beestack.activities

import ThreadAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.beestack.databinding.ActivityTagDetailBinding
import edu.bluejack22_1.beestack.model.Tag
import edu.bluejack22_1.beestack.model.Thread
import edu.bluejack22_1.beestack.model.User

class TagDetailActivity : AppCompatActivity() {


    private var threadList : MutableList<Thread> = mutableListOf()

    private lateinit var binding: ActivityTagDetailBinding;

    private lateinit var threadAdapter: ThreadAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagDetailBinding.inflate(layoutInflater)
        val tag : Tag= intent.getSerializableExtra("tag") as Tag
        searchThread(tag.uid);
        setTagDetail(tag);
        setToolbar();
        setContentView(binding.root)
    }

    fun setToolbar(){
        setSupportActionBar(binding.toolbar)
        binding.pageName.setText("Tag Detail");
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowTitleEnabled(false);
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun searchThread(uid :String?){
        if(uid != null){
            fetchThreadWithTag(uid)
        }
    }

    private fun setTagDetail(tag: Tag){
        binding.tagName.setText(tag.name);
        binding.tagDesc.setText(tag.description);
    }

    private fun applyAdapter(){
//        hideProgressBar()
        threadAdapter = ThreadAdapter(threadList)
        binding.apply {
            threadRV.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = threadAdapter
            }
        }
    }


    private fun fetchThreadWithTag(str: String){
        threadList.clear();
        val db = Firebase.firestore;
        db.collection("threads")
            .orderBy("tag_id")
            .startAt(str)
            .endAt(str+"\uf8ff")
            .get()
            .addOnSuccessListener{ value ->
                if(value.isEmpty){
                    applyAdapter()
                }else{
                }
                for (doc in value!!) {
//                  Get Thread Data
                    val title = doc.data["title"].toString()
                    val description = doc.data["description"].toString()
                    val user_id = doc.data["user_id"].toString()
                    val uid = doc.id
                    val createdAt = doc.data["created_at"].toString();
                    val topCount = doc.data["top_count"].toString();
                    val downCount = doc.data["down_count"].toString();


//                  Then Get User Data
                    val docRef = db.collection("users").document(user_id);
                    docRef.get()
                        .addOnSuccessListener { doc ->
                            if (doc != null) {
                                val username = doc.data!!["username"].toString()
                                val email = doc.data!!["email"].toString()
                                val location = doc.data!!["location"].toString()
                                val url = doc.data!!["photo_profile_url"].toString();
                                var tagName = doc.data!!["tag_name"].toString();

                                val user: User = User(doc.id, username, email, location, url, tagName = tagName);

//                           Add add getted data to the thread list (Vector)
                                threadList.add(Thread(uid =uid, title = title, desc = description, user_id = user_id, user = user, createdAt = createdAt, topCount = topCount.toInt(), downCount = downCount.toInt()));
                                applyAdapter()
                            }
                        }
                }

            }.addOnFailureListener {
            }

    }

}