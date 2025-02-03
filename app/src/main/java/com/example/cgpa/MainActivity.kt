package com.example.cgpa

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File


private const val TAG = "MainActivity";
private const val TEXT_FILE_PATH = "ResultOutput.txt"
private const val PDF_FILE_PATH = "/storage/emulated/0/Documents/ResultOutput.pdf"
private const val FOLDER_NAME = "MyFolder"


class MainActivity : AppCompatActivity() {
//    private val myFolder = File(TEXT_FILE_PATH);
    private lateinit var myFolder:File;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        myFolder = File(getExternalFilesDir(null), FOLDER_NAME)

        if (!myFolder.exists()) {
            val success = myFolder.mkdirs()
            val file = File(myFolder, TEXT_FILE_PATH);
            if(!file.exists()) file.createNewFile()
            Log.i(TAG, "Directory created:  at ${myFolder.absolutePath}")
        }



        val addButton: Button =findViewById(R.id.addGrade);
        val gradeType:Spinner = findViewById<Spinner>(R.id.gradeType);
        val gradeText:EditText? = findViewById<EditText>(R.id.grade);

        var gradeTypeText:String? =null;

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

        val result:ResultCalculator = ResultCalculator();
        result.readSaveFile(TEXT_FILE_PATH,myFolder)

        addButton.setOnClickListener{
            val nameText:EditText = findViewById<EditText>(R.id.name);
            val regNoText:EditText = findViewById<EditText>(R.id.regNo);
            val creditText:EditText? = findViewById<EditText>(R.id.creditA);

            val name = nameText.text.toString().trim();
            val regNo = regNoText.text.toString().trim();
            val grade = gradeText?.text.toString().trim();
            val credit = creditText?.text.toString().trim();
            val info = arrayOf(name,regNo,grade,credit);

            var ok:Boolean = true
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


        //advance mode
        val advance = findViewById<Button>(R.id.advanceMode);
        advance.setOnClickListener {
            val intent = Intent(this, AdvanceMode::class.java);
            startActivity(intent);
            finish();
        }








        val generateResult:Button = findViewById(R.id.generateResult);
        generateResult.setOnClickListener {
            Toast.makeText(this,"Result Successfully Generated At DOCUMENTS Folder",Toast.LENGTH_LONG).show()
            result.show();
            result.writeFinal(PDF_FILE_PATH, TEXT_FILE_PATH,myFolder);
        }

        val clearSavedData:Button = findViewById(R.id.clearSaved);
        clearSavedData.setOnClickListener {
            showConfirmationDialog("All Data You Have Entered Up to Now Will Be Cleared.\nDo You Want To Proceed?"){ confirm->
                if(confirm) {
                    result.clearSavedData(TEXT_FILE_PATH,myFolder);
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





    private fun showConfirmationDialog(message:String,confirm:(Boolean)->Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage(message)

        builder.setPositiveButton("Yes") { dialog, _ ->
            // Action when YES is clicked
            dialog.dismiss()
            confirm(true)
        }

        builder.setNegativeButton("No") { dialog, _ ->
            // Action when NO is clicked
            dialog.dismiss()
            confirm(false)
        }

        val dialog = builder.create()
        dialog.setCancelable(false);
        dialog.show()
    }





}
