package com.example.cgpa

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.InputStream
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper


private const val TAG = "MainActivity"
private const val PDF_FILE_PATH = "/storage/emulated/0/Documents/ResultOutput.pdf"
private const val TEXT_FILE_PATH = "/storage/emulated/0/Documents/ResultOutput.txt"


class AdvanceMode : AppCompatActivity() {

    private val result = ResultCalculator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_advance_mode)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //read previously saved data
        result.readSaveFile(TEXT_FILE_PATH);

        val pdfSelector = findViewById<Button>(R.id.pdfSelector)

        PDFBoxResourceLoader.init(applicationContext)

        pdfSelector.setOnClickListener {
            openFilePicker()
        }

        val addName = findViewById<Button>(R.id.addName);
        addName.setOnClickListener {
            val name = findViewById<EditText>(R.id.name).text.toString();
            val regNo = findViewById<EditText>(R.id.regNo).text.toString();

            val info = arrayOf(name,regNo);
            var ok = true
            for (i in info)
            {
                if(i.isBlank() || i. isEmpty())
                    ok=false;
            }
            if(ok) {
                result.addStudent(name, regNo);
                Toast.makeText(this,"Name $name Added For The RegNo $regNo", Toast.LENGTH_SHORT).show();
            }
        }




        //generate result
        val generateResult:Button = findViewById(R.id.generateResult);
        generateResult.setOnClickListener {
            Toast.makeText(this,"Result Successfully Generated At DOCUMENTS Folder", Toast.LENGTH_LONG).show()
            result.show();
            result.writeFinal(PDF_FILE_PATH);
        }


        val clearSavedData:Button = findViewById(R.id.clearSaved);
        clearSavedData.setOnClickListener {
            showConfirmationDialog("All Data You Have Entered Up to Now Will Be Cleared.\nDo You Want To Proceed?"){ confirm->
                if(confirm) {
                    result.clearSavedData(TEXT_FILE_PATH);
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
                readPdfContent(uri)
            }
        }
    }

    private fun readPdfContent(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val document = PDDocument.load(stream)
                val pdfTextStripper = PDFTextStripper()
                val pdfText = pdfTextStripper.getText(document)
                document.close()

                //populate students
                val credit = findViewById<EditText>(R.id.creditA).text.toString().toDouble();
                Log.i(TAG,"$credit");
                Log.i(TAG,"File Selected Successfully!");
                Toast.makeText(this,"Successfully Selected The ${getFileName(uri)} File.",Toast.LENGTH_SHORT).show();

                regGrade(pdfText,credit);
                //pdf file contents
                Log.i(TAG,"PDF Content: $pdfText")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun getFileName(uri: Uri): String {
        var name = "unknown.pdf"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                cursor.moveToFirst()
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }



    private fun regGrade(text: String, credit: Double) {
        // Split text into lines
        val lines = text.split("\n")
//        Log.i(TAG,"now We Have: $lines")

        // Replace spaces or tabs with commas to form a CSV-like structure
        for (line in lines) {
            var validLine = true

            val trimmedLine = line.trim()
            Log.i(TAG,trimmedLine);
            var regNo = "N/A"
            // Validity check
            for (i in trimmedLine.indices) {
                if (trimmedLine[i] == ' ') {
                    regNo = trimmedLine.substring(0, i)
                    if (regNo.length != 10) validLine = false
                    else {
                        for (c in regNo) {
                            if (c in '0'..'9')
                                continue
                            validLine = false
                        }
                    }
                    break
                }
            }
            if (!validLine) continue

            val grade = StringBuilder()
            validLine = true
            var format: Format = Format.REG_GRADE
            for (i in trimmedLine.length - 1 downTo 0) {
                if (trimmedLine[i] == ' ') {
                    grade.append(trimmedLine.substring(i + 1, trimmedLine.length))

                    if (grade.length <= 2 && grade[0] in 'A'..'F') format = Format.REG_GRADE
                    else if (grade.contains('.') && grade[0] <= '4') format = Format.REG_CG
                    else {
                        for (c in grade) {
                            if (c == '.' || c in '0'..'9') validLine = true
                            else {
                                validLine = false
                                break
                            }
                        }
                        if (validLine) format = Format.REG_MARKS
                    }
                    break
                }
            }
            Log.i(TAG,"$regNo, $grade, $validLine")
            if (!validLine) continue


            // Add student now
            when (format) {
                Format.REG_MARKS -> result.addStudent("N/A", regNo, credit, grade.toString().toInt())
                Format.REG_CG -> result.addStudent("N/A", regNo, credit, grade.toString().toDouble())
                Format.REG_GRADE -> result.addStudent("N/A", regNo, credit, grade.toString())
                else -> throw AssertionError()
            }

            Log.i(TAG, "Added Student: $regNo, grade: $grade, credit: $credit")
        }
    }
}
