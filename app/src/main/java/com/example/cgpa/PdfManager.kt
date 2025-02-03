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
                Log.i(Helper.TAG,"New Line: ${line}")
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

            caption = Paragraph("N.B: Star(*) After Total Credit Means Drop In One Or More Courses.")
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





    fun readPdfContent(uri: Uri): String {
        val contentResolver = context.applicationContext.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        val pdfReader = PdfReader(inputStream)
        val pdfDocument = PdfDocument(pdfReader)
        val text = StringBuilder()

        for (i in 1..pdfDocument.numberOfPages) {
            val page = pdfDocument.getPage(i)
            val strategy = SimpleTextExtractionStrategy()
            text.append(PdfTextExtractor.getTextFromPage(page, strategy))
        }

        pdfDocument.close()
        pdfReader.close()
        return text.toString()
    }



    //detect pdf format then extract info and populate
    fun detectPdfFormatAndPopulate(result:ResultCalculator,text: String,credit:Double) {
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
//                            Log.i(TAG,"$reg $name $totalCredit $cg")
                        }
                        else -> Log.i(TAG,"Invalid format!")
                    }
                }
            }

            if(found) break;
        }
    }









}
