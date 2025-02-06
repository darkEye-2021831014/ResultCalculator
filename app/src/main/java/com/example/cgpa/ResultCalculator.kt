package com.example.cgpa

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.util.Log
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.Locale
import kotlin.math.max


private const val TAG = "MainActivity"
class ResultCalculator() {
    private var students: ArrayList<Student>
    init {
        students = ArrayList()
    }

    fun clearSavedData(myFolder:File) {
        students.clear()
        Helper.writeTextFile(myFolder,"")
    }


    // file reader for reg,name,cg,credit;
    fun readSaveFile(myFolder:File) {
        val textFile = File(myFolder,Helper.TEXT_FILE);
        try {
            BufferedReader(FileReader(textFile)).use { reader ->
                var line: String?
                var credit = 0.0
                while ((reader.readLine().also { line = it }) != null) {
                    // now we have a single line of the file
                    // do operation on the line
                    reader(line.toString());
                }
            }
            Log.i(TAG,"Successfully read The File ${Helper.TEXT_FILE}")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun reader(line:String){
        var name="N/A"
        var regNo="N/A"
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
        else if(name !="N/A")
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
        else if(name !="N/A")
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
        else if(name !="N/A")
            student.setName(name);

        student.result(grade,credit)
        if(newStudent)
            students.add(student);
    }

    fun addStudent(regNo:String,credit:Double,marks:Int)
    {
        var student:Student? = isPresent(regNo);
        var newStudent = false
        if(student==null) {
            student = Student(regNo);
            newStudent = true;
        }

        student.result(marks,credit)
        if(newStudent)
            students.add(student);
    }

    fun addStudent(regNo:String,credit:Double,cg:Double)
    {
        var student:Student? = isPresent(regNo);
        var newStudent = false
        if(student==null) {
            student = Student(regNo);
            newStudent = true;
        }

        student.result(cg,credit)
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


    fun generateResult(context: Context,myFolder:File)
    {
        val tmp = ArrayList<Student>()
        for (s: Student in students) {
            if (s.getCredit() > 0.0) tmp.add(s)
        }
        students = tmp

        students.sort()
        val pdfFileContent = StringBuilder()
        val textFileContent = StringBuilder()
        val identifier = '^';
        pdfFileContent.append("Rank${identifier}RegNo${identifier}Name${identifier}CGPA${identifier}Grade${identifier}Total Credit\n")

        var rank = 1;
        for(student in students)
        {
            //pdf file
            pdfFileContent.append("${rank++}${identifier}${student.getRegNo()}${identifier}${student.getName()}${identifier}${String.format(
                Locale.US,"%.2f",student.getCg())}${identifier}${student.latterGrade}${identifier}${String.format(Locale.US,"%.1f",student.getCredit())}")
            if(this.totalCredit() != student.getCredit())
                pdfFileContent.append(" *");
            pdfFileContent.append("\n");
            //text file
            textFileContent.append("${student.getRegNo()},${student.getName()},${student.getCg()},${student.getCredit()},\n")
        }

        //write pdf file
        val pdfColumn = 6;
        PdfManager.createPdfFile(context,myFolder,pdfFileContent.toString(),identifier,pdfColumn);
        //write text File
        Helper.writeTextFile(myFolder,textFileContent.toString());
    }



    fun show() {
        for (s: Student in students) {
            Log.i(TAG,
                s.getRegNo() + " " + s.getName() + " " + s.getCg() + " " +
                    s.getCredit()
            )
        }
    }

}
