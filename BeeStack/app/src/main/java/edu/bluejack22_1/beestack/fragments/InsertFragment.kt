package edu.bluejack22_1.beestack.fragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.jakewharton.threetenabp.AndroidThreeTen
import edu.bluejack22_1.beestack.R
import edu.bluejack22_1.beestack.activities.CreateTagActivity
import edu.bluejack22_1.beestack.activities.HomeActivity
import edu.bluejack22_1.beestack.databinding.FragmentInsertBinding
import edu.bluejack22_1.beestack.model.CurrentUser
import edu.bluejack22_1.beestack.model.Tag
import edu.bluejack22_1.beestack.model.Thread


class InsertFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var _binding: FragmentInsertBinding? = null
    private val binding get() = _binding!!
    var imageUri: Uri? = null
    val db = Firebase.firestore;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentInsertBinding.inflate(inflater,container,false);

        createBtnOnClick();
        imageIconOnClick();

        return binding.root;
    }

    private fun createBtnOnClick(){
        binding.createBtn.setOnClickListener {
            val title = binding.title.text.toString()
            val description = binding.description.text.toString();
            val tag = binding.tag.text.toString()
            val user_id = FirebaseAuth.getInstance().currentUser?.uid.toString();

            if(title.isEmpty() || description.isEmpty() || tag.isEmpty()){
                Toast.makeText(context, getString(R.string.you_need_all_validation), Toast.LENGTH_SHORT).show()
                return@setOnClickListener;
            }
//            Find Tag
            Tag.REF.whereEqualTo("name", tag).get()
                .addOnSuccessListener { docs ->
//                  If no tag name exists navigate user to create tag
                    if(docs.isEmpty ){
                        showDialog()
                    }else{
//                       If tag name exists
                        val doc : DocumentSnapshot = docs.documents.get(0)
                        val desc = doc.get("description").toString()
                        val name = doc.get("name").toString()
                        val tag: Tag = Tag(uid=doc.id, name=name, description=desc)

                        // desc = for tag || description = thread
                        createThread(title, description, user_id, tag);
                    }


                }

        }
    }

    private fun navigateToCreateTag(){
        val createTagIntent = Intent(context, CreateTagActivity::class.java)
        createTagIntent.putExtra("name", binding.tag.text.toString())
        startActivity(createTagIntent)
    }

    private fun showDialog(){
        val alert: AlertDialog.Builder = AlertDialog.Builder(context)
        alert.setTitle("Bee Stack")
        alert.setMessage(getString(R.string.tag_not_available))
        alert.setPositiveButton(getString(R.string.create_new_tag), DialogInterface.OnClickListener { dialogInterface, i ->
            navigateToCreateTag()
        })
        alert.setNegativeButton(getString(R.string.back), DialogInterface.OnClickListener { dialogInterface, i ->

        })
        alert.create().show()
    }

    private fun replaceFragment(fragment: Fragment){
        (activity as HomeActivity).replaceFragment(fragment);
    }

    private fun navigateHome(){
        replaceFragment(HomeFragment())
    }

    private fun createThread(title: String, description: String, user_id:String, tag :Tag?){
//        Init local time
        AndroidThreeTen.init(context)


        val newThread : Thread = Thread(title =title, desc = description, user_id = user_id, tag = tag);



        db.collection("threads")
            .add(newThread.getNewHashMap())
            .addOnSuccessListener { doc ->
                if (imageUri != null){
                    newThread.uid = doc.id;
                    uploadImage(imageUri!!, newThread);
                }else{
                    navigateHome()
                }
            }
    }


    private fun imageIconOnClick(){
        binding.imageIcon.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK)
            galleryIntent.type = "image/*"
            imagePickerActivityResult.launch(galleryIntent)
        }
    }

    private fun uploadImage(image:Uri, thread: Thread){
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Uploading Image ... ");
        progressDialog.setCancelable(false)
        progressDialog.show()

        val ref:String = "threads/${thread.uid}"
        val storageRef = FirebaseStorage.getInstance().getReference(ref);
        storageRef.putFile(image).addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener {
                thread.updatePhotoProfile(it.toString());
            }

            if(progressDialog.isShowing) progressDialog.dismiss()
            Toast.makeText(context, getString(R.string.succesfully_create_thread), Toast.LENGTH_SHORT).show()
            navigateHome()
        }
    }


    private var imagePickerActivityResult: ActivityResultLauncher<Intent> =
        registerForActivityResult( ActivityResultContracts.StartActivityForResult()) { result ->
            if (result != null) {
                imageUri = result.data?.data
                binding.newImage.setImageURI(imageUri);
            }
        }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null;
    }
}