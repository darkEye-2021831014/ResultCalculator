package com.example.cgpa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File

class ExpenseTrackerMain : AppCompatActivity() {

    private lateinit var myFolder:File;
    private val viewModel: SharedViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //create file inside folder if it doesn't exist
        myFolder = File(getExternalFilesDir(null), Helper.FOLDER)

        if (!myFolder.exists()) {
            val success = myFolder.mkdirs()
        }

        //save all the image icons
        saveIcons()


        var isDataLoaded = false
        // Install the system's splash screen
        val splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition {
            // This condition will keep the splash screen visible until your data loading is complete
            !isDataLoaded
        }


        enableEdgeToEdge()
        setContentView(R.layout.activity_expense_tracker_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = ContextCompat.getColor(this, R.color.lessBlack)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.lessBlack)


        // Load saved data while splash screen is visible
        val context = this
        lifecycleScope.launch {
            Helper.loadSavedData(context,viewModel);
            isDataLoaded=true
        }




        //code starts here
        val homeButton = findViewById<Button>(R.id.records)
        val chartsButton = findViewById<Button>(R.id.Charts)
        val budgetButton = findViewById<Button>(R.id.budget)
        val profileButton = findViewById<Button>(R.id.profile)

        val addItemButton = findViewById<FloatingActionButton>(R.id.addItem)
        addItemButton.setOnClickListener {
            val bottomSheet = AddItemFragment()
            bottomSheet.show(supportFragmentManager, "AddItemFragment")
        }

        homeButton.setOnClickListener { replaceFragment(RecordsFragment()) }
        chartsButton.setOnClickListener { replaceFragment(ChartFragment()) }
        budgetButton.setOnClickListener { replaceFragment(BudgetFragment()) }
        profileButton.setOnClickListener { replaceFragment(ProfileFragment()) }
//        searchButton.setOnClickListener { replaceFragment(SearchFragment()) }

        // Set default fragment
        replaceFragment(RecordsFragment())



    }



    private fun saveIcons(){
        Helper.saveIcon(R.drawable.food_record,this)
        Helper.saveIcon(R.drawable.drinks_records,this)
        Helper.saveIcon(R.drawable.cloth_records,this)
        Helper.saveIcon(R.drawable.education_records,this)
        Helper.saveIcon(R.drawable.healthcare_records,this)
        Helper.saveIcon(R.drawable.transportation_records,this)
        Helper.saveIcon(R.drawable.phone_records,this)
        Helper.saveIcon(R.drawable.other_records,this)

        Helper.saveIcon(R.drawable.salary_records,this)
        Helper.saveIcon(R.drawable.tution_records,this)
        Helper.saveIcon(R.drawable.investment_records,this)
        Helper.saveIcon(R.drawable.ic_right,this)
    }


    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
    private fun setNavBarColor(activity: Activity, colorResId: Int) {
            activity.window.navigationBarColor = ContextCompat.getColor(activity, colorResId)
    }

}
