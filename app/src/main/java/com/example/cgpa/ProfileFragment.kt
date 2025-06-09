package com.example.cgpa

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ProfileFragment : Fragment() {

    private val viewModel:SharedViewModel by activityViewModels()

    private lateinit var auth:FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var profileManager:ProfileManager

    private lateinit var signInButton:Button
    private lateinit var userName:TextView
    private lateinit var userGmail:TextView
    private lateinit var userIcon:ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(ContextCompat.getString(requireContext(), R.string.default_web_client_id)) // your client ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)



        signInButton = view.findViewById(R.id.signIn)
        userName = view.findViewById(R.id.userName)
        userGmail = view.findViewById(R.id.gmail)
        userIcon = view.findViewById(R.id.userIcon)

        profileManager = ProfileManager(requireContext(),viewModel)
        profileManager.signInSuccessful()
        viewModel.userAccount.observe(viewLifecycleOwner){
            user->
            signInButton.text = user.signInButtonText
            signInButton.setTextColor(user.buttonColor)
            signInButton.setCompoundDrawablesWithIntrinsicBounds(user.startIcon,null,user.endIcon,null)

            userName.text = user.name
            userGmail.text = user.email
            if(user.photoUrl!=null)
                Glide.with(requireContext()).load(user.photoUrl)
                    .circleCrop()
                    .into(userIcon)
            else
                userIcon.setImageResource(R.drawable.ic_user)
        }


        signInButton.setOnClickListener {
            val curUser = FirebaseAuth.getInstance().currentUser
            if (curUser != null)
                profileManager.signOut()
            else
                signIn()
        }

        //shared button
        view.findViewById<Button>(R.id.shared).setOnClickListener{
            Utility.bottomSheet(requireActivity(),SharedRecordsManager(),"SharedRecordsManager")
        }

        return view
    }


    fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }
    // Handle result from Google Sign-In
    // Define the launcher at the top of your LoginActivity
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data: Intent? = result.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Utility.showToast(requireContext(), "Google Sign-In failed: ${e.message}")
        }
    }


    // Authenticate with Firebase using Google account
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Utility.showToast(requireContext(),"Sign In Successful.")
                    lifecycleScope.launch(Dispatchers.Main) {
                        profileManager.signInSuccessful()
                        loadData()
                    }
                } else {
                    // If sign-in fails, display a message to the user
                    Utility.showToast(requireContext(),"Authentication Failed")
                }
            }
    }


    private suspend fun loadData(){
        Helper.loadSavedData(requireContext(), viewModel)
    }








    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
