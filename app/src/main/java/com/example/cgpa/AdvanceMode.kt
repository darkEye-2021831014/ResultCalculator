package com.example.cgpa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File

class AdvanceMode : AppCompatActivity() {
    private lateinit var myFolder:File;

    private val result = ResultCalculator()
    private val pdfManager = PdfManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_advance_mode)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //create folder if it doesn't exist'
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
        result.readSaveFile(myFolder);

        //select pdf file
        val pdfSelector = findViewById<Button>(R.id.pdfSelector)
        pdfSelector.setOnClickListener {
            openFilePicker()
        }


        findViewById<Button>(R.id.normalMode).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java);
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
            confirmationDialog()
        }
    }



    private fun confirmationDialog() {
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








    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        pickPdfLauncher.launch(intent)
    }

    private val pickPdfLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                //File selection Successful
                handleInputFile(uri)
            }
        }
    }

    private fun handleInputFile(uri: Uri)
    {
        Log.i(Helper.TAG,"Selected File: ${Helper.getFileName(this,uri)}")
        Toast.makeText(this,"Selected File: ${Helper.getFileName(this,uri)}",Toast.LENGTH_SHORT).show()

        // Read the PDF content using iText
        val pdfContent = pdfManager.readPdfContent(uri)
        // Get the credit value from the EditText
        val creditText = findViewById<EditText>(R.id.creditA).text.toString()
        var credit = 0.0
        if (creditText.isNotEmpty() && creditText != ".") credit = creditText.toDouble()

        // Process the PDF content and populate the student data
        pdfManager.detectPdfFormatAndPopulate(this.result,pdfContent, credit)
    }
}
