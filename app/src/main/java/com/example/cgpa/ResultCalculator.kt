package com.example.cgpa

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.util.Log
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import kotlin.math.max


private const val TAG = "MainActivity"
class ResultCalculator() {
    private var students: ArrayList<Student>
    init {
        students = ArrayList()
    }

    private var maxCell = 0

    fun clearSavedData(filePath: String) {
        students.clear()
        writeTxtFile(filePath,"");
    }


    // file reader for reg,name,cg,credit;
    fun readSaveFile(filePath:String) {
        try {
            BufferedReader(FileReader(filePath)).use { reader ->
                var line: String?
                var credit = 0.0
                while ((reader.readLine().also { line = it }) != null) {
                    // now we have a single line of the file
                    // do operation on the line
                    reader(line.toString());
                    Log.i(TAG,"Successfully read The File $filePath")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun reader(line:String){
        var name: String="N/A"
        var regNo: String="N/A"
        var credit = 0.0
        var cg = 0.0

        var block = 0
        var pre = 0
        for (i in line.indices) {
            if (line[i] == ',') {
                val cur = line.substring(pre, i)

                when (block) {
                    0 -> regNo = cur
                    1 -> name = cur
                    2 -> cg = cur.toDouble()
                    3 -> credit = cur.toDouble()
                    else -> throw AssertionError()
                }
                block++
                pre = i + 1
            }
        }

        addStudent(name,regNo,credit,cg);
    }







    fun addStudent(name:String,regNo:String,credit:Double,marks:Int)
    {
        var student:Student? = isPresent(regNo);
        var newStudent = false
        if(student==null) {
            student = Student(name, regNo);
            newStudent = true;
        }
        else
            student.setName(name);

        student.result(marks,credit)
        if(newStudent)
            students.add(student);
    }

    fun addStudent(name:String,regNo:String,credit:Double,cg:Double)
    {
        var student:Student? = isPresent(regNo);
        var newStudent = false
        if(student==null) {
            student = Student(name, regNo);
            newStudent = true;
        }
        else
            student.setName(name);

        student.result(cg,credit)
        if(newStudent)
            students.add(student);
    }

    fun addStudent(name:String,regNo:String,credit:Double,grade:String)
    {
        var student:Student? = isPresent(regNo);
        var newStudent = false
        if(student==null) {
            student = Student(name, regNo);
            newStudent = true;
        }
        else
            student.setName(name);

        student.result(grade,credit)
        if(newStudent)
            students.add(student);
    }

    fun addStudent(name:String,regNo:String)
    {
        var student:Student? = isPresent(regNo);
        var newStudent = false
        if(student==null) {
            student = Student(name, regNo);
            newStudent = true;
        }
        else {
            student.setName(name);
            student.setRegNo(regNo);
        }

        if(newStudent)
            students.add(student);
    }


    private fun isPresent(regNo:String):Student?
    {
        for(student in students)
            if(student.getRegNo().equals(regNo,true))
                return student;
        return null
    }

    private fun totalCredit():Double
    {
        var credit = 0.0
        for(student in students)
            credit = max(credit,student.getCredit());
        return credit;
    }



    // write the final file that will be converted to pdf
    fun writeFinal(filePath: String) {
        val tmp = ArrayList<Student>()
        for (s: Student in students) {
            if (s.getCredit() > 0.0) tmp.add(s)
        }
        students = tmp

        students.sort()
        val fileContent = StringBuilder()
        val textFileContent = StringBuilder()
        var line = StringBuilder()
        var column = 0
        var limit = 0
        val rnk = 6
        val reg = 14
        val name = 29
        val cg = 8
        val grade = 9
        val credit = 8
        maxCell = rnk + reg + name + cg + grade + credit + 6

        block("Rank", limit.also { column = it }, rnk.let { limit += it; limit }, fileContent)
        block("RegNo", limit.also { column = it }, reg.let { limit += it; limit }, fileContent)
        block("Name", limit.also { column = it }, name.let { limit += it; limit }, fileContent)
        block("CGPA", limit.also { column = it }, cg.let { limit += it; limit }, fileContent)
        block("Grade", limit.also { column = it }, grade.let { limit += it; limit }, fileContent)
        block("Credit", limit.also { column = it }, limit + credit, fileContent)

        fileContent.append(line.append("\n"))
        line(fileContent)

        var rank = 1
        for (student: Student in students) {
            //textFile contents
            textFileContent.append("${student.getRegNo()},${student.getName()},${student.getCg()},${student.getCredit()},\n");


            //pdf file contents
            limit = 0
            // create a line using the student info
            line = StringBuilder()
            block(rank.toString(), limit.also { column = it }, rnk.let { limit += it; limit }, line)
            block(student.getRegNo(), limit.also { column = it }, reg.let { limit += it; limit }, line)
            block(student.getName(), limit.also { column = it }, name.let { limit += it; limit }, line)
            block(
                String.format("%.2f", student.getCg()),
                limit.also { column = it },
                cg.let { limit += it; limit },
                line
            )
            block(
                student.latterGrade,
                limit.also { column = it },
                grade.let { limit += it; limit },
                line
            )
            // System.out.println(this.totalCredit + " " + student.getCredit());
            if (this.totalCredit() != student.getCredit()) block(
                student.getCredit().toString() + "*",
                limit.also {
                    column = it
                },
                limit + credit,
                line
            )
            else block(student.getCredit().toString(), limit.also { column = it }, limit + credit, line)

            // append line to the file
            fileContent.append(line.append("\n"))
            line(fileContent)
            rank++
        }

        var watermark = "Language Used - Kotlin"
        val message = "N.B: Star(*) After Total Credit Means Drop In One Or More Courses."
        line = StringBuilder()
        line.append(message).append("\n")

        line.append(" ".repeat(maxCell)).append("\n")
        line.append(" ".repeat(maxCell - watermark.length - 1)).append(watermark).append("\n")
        watermark = "Prepared By - darkEye"
        line.append(" ".repeat(maxCell - watermark.length)).append(watermark).append("\n")
        fileContent.append(line)


        //write to text file
        writeTxtFile(filePath,textFileContent.toString());

        //write pdf file
        convertTxtToPdf(fileContent.toString(),filePath);
        Log.i(TAG, "Successfully Wrote To The $fileContent File");
    }

    private fun line(stringBuilder: StringBuilder) {
        stringBuilder.append("-".repeat(maxCell)).append("\n")
    }

    private fun block(value: String?, column: Int, limit: Int, line: StringBuilder) {
        var value = value
        var column = column
        if (value == null) value = "N/A"
        column += value.length
        line.append(" ".repeat((limit - column) / 2)).append(value)
            .append(" ".repeat((limit - column) / 2))
        column += ((limit - column) / 2) * 2
        line.append(" ".repeat(limit - column)).append("|")
    }

    fun show() {
        for (s: Student in students) {
            Log.i(TAG,
                s.getRegNo() + " " + s.getName() + " " + s.getCg() + " " +
                    s.getCredit()
            )
        }
    }

    //format -> reg,name,cg,credit,
    private fun writeTxtFile(filePath:String,fileContent:String){
        val textFilePath:StringBuilder=StringBuilder();
        try {
            for(c in filePath) {
                if (c == '.') break;
                textFilePath.append(c);
            }
            textFilePath.append(".txt");

            BufferedWriter(FileWriter(textFilePath.toString())).use { writer ->
                writer.write(fileContent)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Log.i(TAG, "Successfully wrote to the $textFilePath file");
    }


    private fun convertTxtToPdf(txtContent: String, pdfFilePath: String) {
        try {
            val document = PdfDocument()

            // Set page dimensions (A4 size)
            val pageWidth = 650
            val pageHeight = 842

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            val paint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                isAntiAlias = true
                typeface = Typeface.create("monospace", Typeface.NORMAL)
            }

            val leftMargin = 40f
            val topMargin = 40f
            var yPosition = topMargin
            val lineSpacing = 20f
            val maxHeight = pageHeight - 40f // Keep margin at the bottom

            val lines = txtContent.split("\n")

            for (line in lines) {
                if (yPosition + lineSpacing > maxHeight) {
                    // Finish the current page
                    document.finishPage(page)

                    // Start a new page with incremented page number
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas // Update canvas reference

                    // Reset yPosition for new page
                    yPosition = topMargin
                }

                // Draw text on the current page
                canvas.drawText(line, leftMargin, yPosition, paint)
                yPosition += lineSpacing
            }

            // Finish the last page
            document.finishPage(page)

            // Write PDF to file
            val outputStream = FileOutputStream(pdfFilePath)
            document.writeTo(outputStream)

            document.close()

            println("PDF created successfully at: $pdfFilePath")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error converting TXT to PDF: ${e.message}")
        }
    }

}
