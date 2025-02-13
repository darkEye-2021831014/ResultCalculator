package com.example.cgpa

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ExpenseTrackerMain : AppCompatActivity() {
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


        //code starts here
        val homeButton = findViewById<Button>(R.id.records)
        val searchButton = findViewById<Button>(R.id.Charts)
        val profileButton = findViewById<Button>(R.id.budget)

        val addItemButton = findViewById<FloatingActionButton>(R.id.addItem)
        addItemButton.setOnClickListener {
            val bottomSheet = AddItemFragment()
            bottomSheet.show(supportFragmentManager, "AddItemFragment")
        }

        homeButton.setOnClickListener { replaceFragment(RecordsFragment()) }
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
}
