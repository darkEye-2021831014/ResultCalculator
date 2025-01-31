package com.example.cgpa

import java.util.Locale


object Converter {
    var convertLatter: MutableMap<String, Int> = HashMap()
    var convertMarks: MutableMap<Int, Double> = HashMap()
    var convertCg: MutableMap<Double, String> = HashMap()

    init {
        grades()
    }

    fun grades() {
        convertLatter["A+"] = 80
        convertLatter["A"] = 75
        convertLatter["A-"] = 70
        convertLatter["B+"] = 65
        convertLatter["B"] = 60
        convertLatter["B-"] = 55
        convertLatter["C+"] = 50
        convertLatter["C"] = 45
        convertLatter["C-"] = 40
        convertLatter["F"] = 0

        // convert marks
        convertMarks[80] = 4.0
        convertMarks[75] = 3.75
        convertMarks[70] = 3.5
        convertMarks[65] = 3.25
        convertMarks[60] = 3.0
        convertMarks[55] = 2.75
        convertMarks[50] = 2.5
        convertMarks[45] = 2.25
        convertMarks[40] = 2.0
        convertMarks[0] = 0.0

        // convert cg
        convertCg[4.0] = "A+"
        convertCg[3.75] = "A"
        convertCg[3.5] = "A-"
        convertCg[3.25] = "B+"
        convertCg[3.0] = "B"
        convertCg[2.75] = "B-"
        convertCg[2.5] = "C+"
        convertCg[2.25] = "C"
        convertCg[2.0] = "C-"
        convertCg[0.0] = "F"
    }

    fun gradeToCg(grade: String): Double {
        val latter = grade.substring(0, 1)
            .uppercase(Locale.getDefault()) + grade.substring(1)
        return marksToCg(convertLatter[latter]!!)
    }

    fun marksToCg(marks: Int): Double {
        var varify = marks
        if (varify < 40) varify = 0
        else if (varify >= 80) varify = 80
        else varify -= (varify % 5)
        // System.out.println(varify + " " + convertMarks.get(varify));
        return convertMarks[varify]!!
    }

    fun cgToLatter(cg: Double): String? {
        var varify = ((cg * 100) % 100).toInt()
        var cur = StringBuilder()
        cur.append((cg.toInt()) % 10).append(".")
        varify -= (varify % 25)
        cur.append(varify)
        if ((cg.toInt()) % 10 < 2) cur = StringBuilder("0.0")

        return convertCg[cur.toString().toDouble()]
    }
}
