package edu.bluejack22_1.beestack.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.beestack.activities.HomeActivity
import edu.bluejack22_1.beestack.databinding.FragmentTeamBinding
import edu.bluejack22_1.beestack.model.CurrentUser

class TeamFragment : Fragment() {

    private lateinit var binding: FragmentTeamBinding;
    // Firestore
    private val db = Firebase.firestore;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // View Binding
        binding = FragmentTeamBinding.inflate(inflater, container, false);

        if (CurrentUser.teamId.isNotEmpty()) {
//                  Navigate to detail team fragment
            replaceFragment(TeamDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("teamId", CurrentUser.teamId)
                }
            });
        }else{
            createTeamBtnOnClick();
        }

        return binding.root;
    }



    fun createTeamBtnOnClick(){
        binding.createTeamBtn.setOnClickListener {
            replaceFragment(CreateTeamFragment());
        }
    }

    fun replaceFragment(fragment: Fragment){
        (activity as HomeActivity).replaceFragment(fragment);
    }


}