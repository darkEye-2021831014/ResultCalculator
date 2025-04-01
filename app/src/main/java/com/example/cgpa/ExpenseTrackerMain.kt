package com.example.cgpa

import android.app.Activity
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class ExpenseTrackerMain : AppCompatActivity() {

    private lateinit var myFolder:File;
    private val viewModel: SharedViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expense_tracker_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = ContextCompat.getColor(this, R.color.lessBlack)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.lessBlack)

        //create file inside folder if it doesn't exist
        myFolder = File(getExternalFilesDir(null), Helper.FOLDER)

        if (!myFolder.exists()) {
            val success = myFolder.mkdirs()
        }

        //load saved data
        loadItemInfo();

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
//        searchButton.setOnClickListener { replaceFragment(SearchFragment()) }
//        profileButton.setOnClickListener { replaceFragment(ProfileFragment()) }

        // Set default fragment
        replaceFragment(RecordsFragment())


    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
    private fun setNavBarColor(activity: Activity, colorResId: Int) {
            activity.window.navigationBarColor = ContextCompat.getColor(activity, colorResId)
    }


    private fun loadItemInfo()
    {
//        Helper.deleteAllSavedIcons(requireContext())
//        Helper.saveItemInfoList(viewModel.userData,requireContext());
        Log.i(Helper.TAG,"Loaded Saved ItemInfo")



        val itemInfo = Helper.retrieveItemInfo(this);
        for(item in itemInfo)
        {
//            if(item.month==month && item.year==year) //get the data of current month
            viewModel.setData(item);
        }
    }
}
