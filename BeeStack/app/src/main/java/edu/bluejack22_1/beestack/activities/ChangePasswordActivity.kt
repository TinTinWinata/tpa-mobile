package edu.bluejack22_1.beestack.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import edu.bluejack22_1.beestack.databinding.ActivityChangePasswordBinding
import edu.bluejack22_1.beestack.model.CurrentUser

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding;

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityChangePasswordBinding.inflate(layoutInflater);
        changePasswordBtnListener();

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }



    private fun changePasswordBtnListener(){
        binding.changePasswordBtn.setOnClickListener {
            val lastPw = binding.lastPasswordET.text.toString();
            val newPw = binding.newPasswordET.text.toString();
            FirebaseAuth.getInstance().signInWithEmailAndPassword(CurrentUser.email, lastPw).addOnSuccessListener {
                FirebaseAuth.getInstance().currentUser!!.updatePassword(newPw).addOnCompleteListener {
                    if(it.isSuccessful){
                        Toast.makeText(this, "Succesfully change password!", Toast.LENGTH_SHORT).show();
                    }
                }.addOnFailureListener{
                    Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show();
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Last password must be same with current password!", Toast.LENGTH_SHORT).show();
            };
        }
    }

}