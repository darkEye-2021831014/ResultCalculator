package com.example.cgpa

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getString
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.rpc.Help

class ProfileManager(private val context: Context,
                     private val viewModel: SharedViewModel) {



    init {
        //default value
        val accountInfo:AccountInfo = viewModel.userAccount.value?:AccountInfo(
            name = "Guest",
            email = "Sign In",
            photoUrl = null,
            uid = "N/A",

            signInButtonText = "Sign In With Google",
            buttonColor = Color.WHITE,
            startIcon = Helper.getIcon(context,R.drawable.google_icon),
            endIcon = null
        )
        viewModel.userAccount.value = accountInfo
    }




    fun signInSuccessful(){
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val accountInfo = viewModel.userAccount.value
            accountInfo?.apply {
                name = it.displayName
                email = it.email
                photoUrl = it.photoUrl?.toString()
                uid =it.uid
                signInButtonText = "Sign Out"
                buttonColor = Color.RED
                startIcon = null
                endIcon = Helper.getIcon(context,R.drawable.ic_right)
            }
            viewModel.userAccount.value = accountInfo
        }
    }



    fun signOut(){
        val auth = FirebaseAuth.getInstance()
        auth.signOut() // Firebase sign-out

        // Also sign out from Google
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        ).signOut().addOnCompleteListener {
            Utility.log("Signed out successfully")
        }

        val accountInfo = viewModel.userAccount.value
        accountInfo?.apply {
            name = "Guest"
            email = "Sign In"
            photoUrl = null
            uid = "N/A"

            signInButtonText = "Sign In With Google"
            buttonColor = Color.WHITE
            startIcon = Helper.getIcon(context,R.drawable.google_icon)
            endIcon = null
        }
        viewModel.userAccount.value = accountInfo
    }





}
