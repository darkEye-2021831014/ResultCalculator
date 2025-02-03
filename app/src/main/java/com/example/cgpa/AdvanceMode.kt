package com.example.cgpa

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.InputStream
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import java.io.FileOutputStream


private const val TAG = "MainActivity"
private const val PDF_FILE_PATH = "/storage/emulated/0/Documents/ResultOutput.pdf"
private const val TEXT_FILE_PATH = "ResultOutput.txt"
private const val FOLDER_NAME = "MyFolder"


class AdvanceMode : AppCompatActivity() {
    private lateinit var myFolder:File;

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

        //create folder if it doesn't exist'
        myFolder = File(getExternalFilesDir(null), FOLDER_NAME)

        if (!myFolder.exists()) {
            val success = myFolder.mkdirs()
            val file = File(myFolder, TEXT_FILE_PATH);
            if(!file.exists()) file.createNewFile()
            Log.i(TAG, "Directory created:  at ${myFolder.absolutePath}")
        }

        //read previously saved data
        result.readSaveFile(TEXT_FILE_PATH,myFolder);

        val pdfSelector = findViewById<Button>(R.id.pdfSelector)

        PDFBoxResourceLoader.init(applicationContext)

        pdfSelector.setOnClickListener {
            openFilePicker()
        }

        val regNameFile= findViewById<Button>(R.id.regNameFile);
        regNameFile.setOnClickListener {
            openFilePicker();
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
            result.writeFinal(PDF_FILE_PATH, TEXT_FILE_PATH,myFolder);

            //show the pdf file instantly by providing viewing options
            openPdfFile(PDF_FILE_PATH);
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



    private fun openPdfFile(filePath: String) {
        val file = File(filePath)

        if (file.exists()) {
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf") // Force correct MIME type
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Make sure a chooser is displayed (like when clicking a file manually)
            val chooser = Intent.createChooser(intent, "Open PDF with")

            try {
                startActivity(chooser) // Show chooser dialog
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "No PDF viewer found!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
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
                val creditText = findViewById<EditText>(R.id.creditA).text.toString();
                var credit = 0.0;
                if(creditText.isNotEmpty() && creditText.isNotBlank() && creditText[0]!='.')
                    credit = creditText.toDouble();

                Log.i(TAG,"$credit");
                Log.i(TAG,"File Selected Successfully!");
                Toast.makeText(this,"Successfully Selected The ${getFileName(uri)} File.",Toast.LENGTH_SHORT).show();

//                regGrade(pdfText,credit);
                Log.i(TAG,"PDF Content: $pdfText")
                extractor(pdfText,credit);
                //pdf file contents
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





    private fun extractor(text: String,credit:Double)
    {
        Log.i(TAG,"Inside Extractor")
        val lines = text.split("\n")

        val formatMap:Map<Format, Regex> = mapOf(
            Format.REG_NAME_MARKS to Regex("""^(\d{10})\s+([\w. ]+?)\s+(\d{1,3}(?:\.\d{0,2})?)$"""),
            Format.REG_NAME_GRADE to Regex("""^(\d{10})\s+([\w. ]+?)\s+([A-D][+-]?|F)$"""),
            Format.REG_MARKS to Regex("""^(\d{10})\s+(\d{1,3}(?:\.\d{0,2})?)$"""),
            Format.REG_NAME_CREDIT_CG to
                Regex("""^(\d{10})\s+[\d-]+\s+([\w. ]+?)\s+(\d{1,3}(?:\.\d{0,2})?)\s+(\d(?:\.\d{0,2})?)\s+\S+$""")
        )

        for((format,pattern) in formatMap) {
            var found = false
            Log.i(TAG,"pattern: $format")
            for(line in lines) {
                val matches = pattern.findAll(line);
                for (match in matches) {
                    found=true
                    //populate student
                    when(format) {
                        Format.REG_NAME_MARKS -> {
                            val (reg, name, marks) = match.destructured
                            result.addStudent(name, reg, credit, marks.toInt())
                        }
                        Format.REG_NAME_GRADE -> {
                            val (reg, name, grade) = match.destructured
                            result.addStudent(name, reg, credit, grade)
                        }
                        Format.REG_MARKS -> {
                            val (reg, marks) = match.destructured
                            result.addStudent(reg,credit,marks.toInt());
                        }
                        Format.REG_NAME_CREDIT_CG -> {
                            val (reg,name, totalCredit,cg) = match.destructured
                            result.addStudent(name,reg,totalCredit.toDouble(),cg.toDouble());
                            Log.i(TAG,"$reg $name $totalCredit $cg")
                        }
                        else -> Log.i(TAG,"Invalid format!")
                    }
                }
            }

            if(found) break;
        }

    }

























    private fun regGrade(text: String, credit: Double) {
        // Split text into lines
        val lines = text.split("\n")
//        Log.i(TAG,"now We Have: $lines")

        // Replace spaces or tabs with commas to form a CSV-like structure
        for (line in lines) {
            var validLine = true

            val trimmedLine = line.trim()
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

            //user entered the reg/name file. populate names;
            if(credit==5.0)
            {
                val name:String = line.substring(11,line.length);
                result.addStudent(regNo,name);
                Log.i(TAG,"$regNo..$name");
                continue;
            }

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

//    private fun regName(text:String)
//    {
//        val lines = text.split("\n");
//        for(line in lines)
//        {
//
//        }
//    }
}
