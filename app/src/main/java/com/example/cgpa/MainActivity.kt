package com.example.cgpa

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File



class MainActivity : AppCompatActivity() {
    private lateinit var myFolder:File;
    private val result = ResultCalculator();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        myFolder = File(getExternalFilesDir(null), Helper.FOLDER)

        if (!myFolder.exists()) {
            val success = myFolder.mkdirs()
            var file = File(myFolder, Helper.TEXT_FILE);
            if(!file.exists()) file.createNewFile()
            file = File(myFolder, Helper.PDF_FILE)
            if(!file.exists()) file.createNewFile()
            Log.i(Helper.TAG, "Directory created:  at ${myFolder.absolutePath}")
        }

        //read previously saved data
        result.readSaveFile(myFolder)

        val addButton: Button =findViewById(R.id.addGrade);
        val gradeType:Spinner = findViewById(R.id.gradeType);
        val gradeText:EditText? = findViewById(R.id.grade);

        var gradeTypeText:String? =null;

        //handle grade selection
        gradeType.onItemSelectedListener =object : AdapterView.OnItemSelectedListener{

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val item = parent?.getItemAtPosition(position).toString();
                gradeTypeText = item;
                if(item.equals("CGPA",true))
                    gradeText?.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL;
                if(item.equals("Grade",true))
                    gradeText?.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
                if(item.equals("Marks",true))
                    gradeText?.inputType = InputType.TYPE_CLASS_NUMBER
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

        //add student information
        addButton.setOnClickListener{
            addStudentInfo(gradeTypeText,gradeText);
        }


        //advance mode
        val advance = findViewById<Button>(R.id.advanceMode);
        advance.setOnClickListener {
            val intent = Intent(this, AdvanceMode::class.java);
            startActivity(intent);
            finish();
        }

        //generate result
        val generateResult:Button = findViewById(R.id.generateResult);
        generateResult.setOnClickListener {
            result.show();
            result.generateResult(this,myFolder);
            Log.i(Helper.TAG,"Result Generation Successful");
            Toast.makeText(this,"Result Generation Successful",Toast.LENGTH_SHORT).show();
        }

        //clear saved data
        val clearSavedData:Button = findViewById(R.id.clearSaved);
        clearSavedData.setOnClickListener {
            confirmDialog();
        }


        //menu action
        findViewById<ImageButton>(R.id.menu).setOnClickListener {
            val intent = Intent(this, ExpenseTrackerMain::class.java);
            startActivity(intent);
            finish();
        }
    }

    private fun addStudentInfo(gradeTypeText:String?,gradeText:EditText?)
    {
        val nameText:EditText = findViewById(R.id.name);
        val regNoText:EditText = findViewById(R.id.regNo);
        val creditText:EditText? = findViewById(R.id.creditA);

        val name = nameText.text.toString().trim();
        val regNo = regNoText.text.toString().trim();
        val grade = gradeText?.text.toString().trim();
        val credit = creditText?.text.toString().trim();
        val info = arrayOf(name,regNo,grade,credit);

        var ok = true
        for (i in info)
        {
            if(i.isBlank() || i. isEmpty())
                ok=false;
        }
        if(!ok)
            Toast.makeText(this,"Please Fill All The Required Filled Correctly!",Toast.LENGTH_SHORT).show();
        else
        {
            if(gradeTypeText.equals("CGPA",true))
                result.addStudent(name,regNo,credit.toDouble(),grade.toDouble());
            if(gradeTypeText.equals("Grade",true))
                result.addStudent(name,regNo,credit.toDouble(),grade);
            if(gradeTypeText.equals("Marks",true))
                result.addStudent(name,regNo,credit.toDouble(),grade.toInt());
            Toast.makeText(this,"Grade: $grade And Credit: $credit, Added For The Student: $name",Toast.LENGTH_SHORT).show();
            creditText?.text = null;
            gradeText?.text = null;
        }
    }



    private fun confirmDialog()
    {
        Helper.showConfirmationDialog(this,"All Data You Have Entered Up to Now Will Be Cleared.\nDo You Want To Proceed?"){ confirm->
            if(confirm) {
                result.clearSavedData(myFolder);
                Toast.makeText(
                    this,
                    "All Data You Have Entered Up to Now Has Been Cleared",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else Toast.makeText(
                this,
                "All Data Remains Unchanged",
                Toast.LENGTH_SHORT
            ).show()
        };
    }



}
