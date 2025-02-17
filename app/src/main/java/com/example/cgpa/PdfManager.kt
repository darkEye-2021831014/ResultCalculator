package com.example.cgpa

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.itextpdf.kernel.colors.Color
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

private const val TAG="MainActivity"
class PdfManager(private val context:Context) {

    companion object{
        fun createPdfFile(context:Context,myFolder:File,fileContent:String,identifier:Char,column:Int) {
            // Define the file path for the PDF
            val file = File(myFolder,Helper.PDF_FILE)
            val outputStream = FileOutputStream(file)

            // Initialize PDF writer and document
            val writer = PdfWriter(outputStream)
            val pdfDocument = PdfDocument(writer)
            val document = Document(pdfDocument)



            // Create a caption for the table
            var caption = Paragraph("Author: Ashraful Islam\n")
                .setFontSize(20f)
                .setFontColor(DeviceRgb(34, 139, 34))
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
            document.add(caption)

            caption = Paragraph("This Pdf File Was Automatically Generated\nBased On The Input Files Provided By The User\n\n")
                .setFontSize(18f)
                .setFontColor(DeviceRgb(75, 0, 130))
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
            document.add(caption)


            // Create a table with 3 columns
            val table = Table(column) // 3 columns

            // Set the table width to 100% of the available page width (minus margins)
            table.setWidth(UnitValue.createPercentValue(100f))


            //write file content to the pdf file
            val lines = fileContent.split("\n");

            // Add header cells
            var header = true;
            for (line in lines)
            {
                if(line.isEmpty() || line.isBlank())break;
//                Log.i(Helper.TAG,"New Line: $line")
                val elements = line.split(identifier)
                for(element in elements) {
                    if(header)
                        table.addCell(
                            Cell().add(Paragraph(element)).setBold().setTextAlignment(TextAlignment.CENTER))
                    else
                        table.addCell(
                            Cell().add(Paragraph(element)).setTextAlignment(TextAlignment.CENTER))
                }
                header = false;
            }

            // Add the table to the document
            document.add(table)

            caption = Paragraph("N.B: Star(*) After Total Credit Means Drop In One or More Courses.")
                .setFontSize(14f)
                .setBold()
                .setTextAlignment(TextAlignment.LEFT)

            // Add the caption to the document
            document.add(caption)

            caption = Paragraph("Language Used - Kotlin")
                .setFontSize(14f)
                .setItalic()
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
            document.add(caption)

            caption = Paragraph("Prepared By - darkEye")
                .setFontSize(14f)
                .setItalic()
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
            document.add(caption)

            // Close the document
            document.close()

            // Notify the user that the PDF has been created
            Log.i(TAG,"PDF created at: ${file.absolutePath}")
            //instantly provide option to open the pdf file
            openPdfFile(context,file.absolutePath)
        }


        //provide options to select an available pdf viewer to view the pdf file
        private fun openPdfFile(context:Context,filePath: String) {
            val file = File(filePath)

            if (file.exists()) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.applicationContext.packageName}.fileprovider",
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
                    context.startActivity(chooser) // Show chooser dialog
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No PDF viewer found!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
            }
        }
    }





    fun readPdfContent(uri: Uri): List<String> {
        val contentResolver = context.applicationContext.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        val pdfReader = PdfReader(inputStream)
        val pdfDocument = PdfDocument(pdfReader)
        val text = StringBuilder()

        for (i in 1..pdfDocument.numberOfPages) {
            val page = pdfDocument.getPage(i)
            val strategy = LocationTextExtractionStrategy()
            text.append(PdfTextExtractor.getTextFromPage(page, strategy))
        }


        pdfDocument.close()
        pdfReader.close()
        return fixMergedRows(text.toString())
    }


    private fun fixMergedRows(text: String): List<String> {
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val fixedRows = mutableListOf<String>()
        val digitRegex = Regex("\\b\\d{10}\\b")  // Match exactly one 10-digit number

        for (line in lines) {
            val matches = digitRegex.findAll(line).toList()

            when (matches.size) {
                1 -> {
                    // Line has exactly one 10-digit number → valid row
                    fixedRows.add(line)
                }
                else -> {
                    // Multiple numbers → Split at the **second** 10-digit number
                    var startIndex = 0
                    for ((index, match) in matches.withIndex()) {
                        val endIndex = match.range.first
                        if (index == 1) {  // Found second 10-digit number
                            fixedRows.add(line.substring(0, endIndex).trim())
                            fixedRows.add(line.substring(endIndex).trim())
                            break
                        }
                    }
                }
            }
        }

        return fixedRows
    }






    //detect pdf format then extract info and populate
    fun detectPdfFormatAndPopulate(result:ResultCalculator,lines: List<String>,credit:Double) {
//        Log.i(Helper.TAG,"$lines")
        Log.i(TAG,"Inside Extractor")
        val patterns = mapOf(
            Format.REG to Regex("""(?<!\S)(\d{10})(?!\S)"""),
            Format.NAME to Regex("""\d{10}.+?([A-Za-z. ]+?)(?=\s+[A-CF][+-]?(?:\s+|$)|\s+\d|$)"""),
            Format.GRADE to Regex("""\d{10}.+?\s+([A-CF][+/-]?)(?!\S)"""),
            Format.CG to Regex("""\d{10}.+?\s+(\d(?:\.\d{0,2})?)(?!\S)"""),
            Format.MARKS to Regex("""\d{10}.+?(\d{1,3}(?:\.\d{0,2})?)"""),
            Format.CREDIT to Regex("""\d{10}.+?(\d{1,3}(?:\.\d{0,2})?)(?!\S)\s+\d(?:\.\d{0,2})?\s+[A-CF][+-]?$"""),
        )

        val students:MutableList<MutableList<String?>> = mutableListOf(mutableListOf());
        var isMarks = false;
        var isNull = false;
        for(line in lines) {
            val totalElements = line.split(" ").size
            val studentInfo:MutableList<String?> = mutableListOf();
            if(totalElements > 13)continue

//            Log.i(Helper.TAG,"Line: $line");
            for((format,pattern) in patterns)
                studentInfo.add(pattern.find(line)?.groupValues?.get(1))

            val reg = studentInfo.getOrNull(0)?:continue;
            val marks:Int? = studentInfo.getOrNull(4)?.toDoubleOrNull()?.toInt()
            val totalCredit:Double? = studentInfo.getOrNull(5)?.toDoubleOrNull();

            students.add(studentInfo);
            if(marks!=null && marks > 4) {
                isMarks = true;
            }
            if(marks== null || totalCredit!=null)
                isNull = true;
        }
        if(isNull) isMarks = false;
        Log.i(Helper.TAG,"$isMarks");

        var found = false;
        for(studentInfo in students)
        {
            val reg:String = studentInfo.getOrNull(0)?:continue;
            val marks:Int = studentInfo.getOrNull(4)?.toDoubleOrNull()?.toInt()?:0
            val name:String = studentInfo.getOrNull(1)?:"N/A"
            val grade:String = studentInfo.getOrNull(2)?:"F"
            val cg:Double?  = studentInfo.getOrNull(3)?.toDoubleOrNull()
            val totalCredit:Double = studentInfo.getOrNull(5)?.toDoubleOrNull()?:credit;

            Log.i(Helper.TAG,"$reg $name $totalCredit $grade $cg $marks")
            found=true;
            if(isMarks)
                result.addStudent(name,reg,totalCredit,if(marks>100)marks/10 else marks);
            else {
                if(cg!=null)
                    result.addStudent(name, reg, totalCredit, cg);
                else
                    result.addStudent(name, reg, totalCredit, grade);
            }
        }

        if(!found)
        {
            Log.i(Helper.TAG,"Invalid Format!")
            Toast.makeText(context,"Failed To Read Pdf File\nInvalid Format!", Toast.LENGTH_LONG).show()
        }
    }

}
