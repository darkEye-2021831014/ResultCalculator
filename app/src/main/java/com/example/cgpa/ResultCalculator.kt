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

        var watermark = "Language Used - Java"
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
        Log.i(TAG, "Successfully wrote to the $filePath file");
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
//            Toast.makeText(this,"s.getRegNo() s.getName() s.getCg() s.getCredit()",Toast.LENGTH_SHORT).show()
        }
    }

    //format -> reg,name,cg,credit,
    private fun writeTxtFile(filePath:String,fileContent:String){
        try {
            var textFilePath:StringBuilder=StringBuilder();
            for(c in filePath) {
                if (c == '.') break;
                textFilePath.append(c);
            }
            textFilePath.append(".txt");

            BufferedWriter(FileWriter(textFilePath.toString())).use { writer ->
                writer.write(fileContent)
                Log.i(TAG, "Successfully wrote to the $textFilePath file");
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun convertTxtToPdf(txtContent: String, pdfFilePath: String) {
        try {
            // Read the TXT file content
//            val txtFile = File(txtFilePath)
//            val txtContent = txtFile.readText()  // Read the entire content of the TXT file

            // Create a new PdfDocument
            val document = PdfDocument()

            // Set up the page details (width, height, and page number)
            val pageInfo = PdfDocument.PageInfo.Builder(650, 842, 1).create()  // A4 page size (595 x 842)
            var page = document.startPage(pageInfo)

            // Get the canvas to draw the content on the page
            val canvas: Canvas = page.canvas
            val paint = Paint()
            paint.color = Color.BLACK
            paint.textSize = 12f
            paint.isAntiAlias = true
            // Set typeface to a monospaced font (Courier New)
            paint.typeface = Typeface.create("monospace", Typeface.NORMAL)

            // Define margins
            val leftMargin = 40f
            val topMargin = 40f
            var yPosition = topMargin

            // Split the content into lines by newlines
            val lines = txtContent.split("\n")

            // Loop through each line and draw it on the canvas
            for (line in lines) {
                if (yPosition + 20f > pageInfo.pageHeight) {
                    // If we reached the end of the page, create a new page
                    document.finishPage(page)
                    val newPage = document.startPage(pageInfo)
                    yPosition = topMargin
                    canvas.drawText(line, leftMargin, yPosition, paint)
                    yPosition += 20f
                    page = newPage
                } else {
                    canvas.drawText(line, leftMargin, yPosition, paint)
                    yPosition += 20f
                }
            }

            // Finish the page after adding all content
            document.finishPage(page)

            // Write the document to the output file
            val outputStream = FileOutputStream(pdfFilePath)
            document.writeTo(outputStream)

            // Close the document
            document.close()

            // Success message
            println("PDF created successfully at: $pdfFilePath")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error converting TXT to PDF: ${e.message}")
        }
    }
}
