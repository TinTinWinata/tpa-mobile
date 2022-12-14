package edu.bluejack22_1.beestack.model

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import edu.bluejack22_1.beestack.R
import java.io.File
import kotlin.properties.Delegates

object CurrentUser {

    var uid:String= ""
    var email:String = "";
    var tagName:String = "";
    var username:String = "";
    var location:String = "";
    var teamId:String = "";
    var photoProfileURL: String = "";
    var photoProfileListListener = ArrayList<() -> Unit>()
    var topVote : ArrayList<String> = ArrayList<String>()
    var downVote: ArrayList<String> = ArrayList<String>()

    var photoProfileBitmap:Bitmap? by Delegates.observable(null){
        property, oldValue, newValue ->
        photoProfileListListener.forEach {
            it()
        }
    }

    fun isEmpty() : Boolean{
        return uid.isEmpty()
    }
    private fun photoProfileRefString():String {
//        Log.d("user-activity", "images/${this.uid}")
        return "images/${this.uid}"
    }

    private fun photoProfileRef() : StorageReference{
        return FirebaseStorage.getInstance().reference.child(photoProfileRefString())
    }

    fun setBitmap(){
        val ref:StorageReference = this.photoProfileRef()
        val localFile = File.createTempFile(uid, "jpg");
        Log.d("user-activity", "setting bitmap...")
        ref.getFile(localFile).addOnSuccessListener {
            this.photoProfileBitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            Log.d("user-activity", this.photoProfileBitmap.toString())
        }.addOnFailureListener{
            Log.d("user-activity", it.message.toString())
        }
    }

    private fun getUserRef(): DocumentReference{
        return Firebase.firestore.collection("users").document(uid);
    }

    fun login(uid : String){
        val db = Firebase.firestore
        val ref = db.collection("users").document(uid)
        ref.addSnapshotListener{ snapshot, e ->
            if(e != null){
                return@addSnapshotListener
            }
            if(snapshot != null && snapshot.exists()){
//              Succesfully snapshot
                this.uid = uid;
                this.username = snapshot.data!!.get("username").toString()
                this.email = snapshot.data!!.get("email").toString()
                this.location = snapshot.data!!.get("location").toString()
                this.teamId = snapshot.data!!.get("team_id").toString();
                this.photoProfileURL = snapshot.data!!.get("photo_profile_url").toString();
                this.tagName = snapshot.data!!.get("tag_name").toString();
                this.topVote = snapshot.data!!.get("top_vote") as ArrayList<String>;
                this.downVote = snapshot.data!!.get("down_vote") as ArrayList<String>;

                if(this.tagName.isEmpty()){
                    this.tagName = this.username;
                }

//              Set bitmap for photo profile always when login
                this.setBitmap()
            }else if(snapshot != null && !snapshot.exists()){
//                If no exists Create new default user
                createDocBasedOnFirebase(uid)
            }
        }
    }

    private fun getHashMap() : HashMap<String, Any>{
//        Get object map
        return hashMapOf(
            "uid" to this.uid,
            "email" to this.email,
            "username" to this.username,
            "tag_name" to this.tagName,
            "location" to this.location,
            "team_id" to this.teamId,
            "photo_profile_url" to this.photoProfileURL,
            "top_vote" to this.topVote,
              "down_vote" to this.downVote
        )
    }
    public fun getUser(): User{
        return User(uid= this.uid, username = this.username, email = this.email, location = this.location, photoProfile = this.photoProfileURL, this.tagName)
    }

    public fun update() : Task<Void>{
        return getUserRef().set(getHashMap());
    }

    private fun createDocBasedOnFirebase(uid: String){
        val firebaseAuth = FirebaseAuth.getInstance()

        if(firebaseAuth.currentUser!!.displayName != null){
//            First time user login with google will create with their own display name
            createNewDoc(firebaseAuth.currentUser!!.displayName.toString(), location, uid, firebaseAuth.currentUser!!.email.toString())
        }else{
//            Register with register_fragment will input username to this current user
            createNewDoc(this.username, location, uid, firebaseAuth.currentUser!!.email.toString())
        }
    }

    private fun makeUserDoc(){
//      Make user doc can't be empty
        if(isEmpty()){
            return;
        }

        val db = Firebase.firestore
        db.collection("users")
            .document(this.uid).set(getHashMap())
            .addOnSuccessListener {
                login(this.uid)
            }
            .addOnFailureListener{
                Log.d("MyFirestoreDb", it.message.toString())
            }
    }

    fun createNewDoc(username:String, location: String, uid: String, email: String){
//        Assign every variable to this user singleton variable
        this.uid = uid
        this.username = username
        this.email = email
        this.location = location
        makeUserDoc()
    }

    private fun emptyAllAttr(){
        this.uid = "";
        this.email = "";
        this.location = "";
        this.username = "";
        this.photoProfileBitmap = null;
        this.photoProfileURL = ""
        this.uid= ""
        this.email = "";
        this.username = "";
        this.location = "";
        this.teamId = "";
    }

    fun getGSO(activity : Activity) : GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(activity.getString(
            R.string.default_web_client_id))
            .requestEmail()
            .build();
    }

    fun logout(activity : Activity){
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()

        val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(activity, getGSO(activity));
        googleSignInClient.signOut();
        emptyAllAttr()
    }
}