package com.example.cgpa

import com.example.cgpa.Converter.cgToLatter
import com.example.cgpa.Converter.gradeToCg
import com.example.cgpa.Converter.marksToCg

class Student : Comparable<Student> {
    // setters
    private var name: String? = null
    private var regNo: String
    private var credit: Double
    private var cg: Double

    constructor(name: String?, regNo: String) {
        this.name = name
        this.regNo = regNo
        this.credit = 0.0
        this.cg = 0.0
    }

    constructor(regNo: String) {
        this.regNo = regNo
        this.cg = 0.0
        this.credit = 0.0
    }

    fun result(cg: Double, credit: Double) {
        if (cg > 0.0) {
            this.cg += (cg * credit)
            this.credit += credit
        }
    }

    fun result(letterGrade: String?, credit: Double) {
        if (gradeToCg(letterGrade!!) > 0.0) {
            this.cg += (gradeToCg(letterGrade) * credit)
            this.credit += credit
        }
    }

    fun result(marks: Int, credit: Double) {
        if (marksToCg(marks) > 0.0) {
            this.cg += (marksToCg(marks) * credit)
            this.credit += credit
        }
    }


    // getters
    fun getCg(): Double {
        return this.cg / this.credit
    }

    val latterGrade: String?
        get() = cgToLatter(getCg())

    override fun compareTo(other: Student): Int {
        // this is for descending order
        var compare = java.lang.Double.compare(other.credit, this.credit)
        if (compare == 0) compare = java.lang.Double.compare(other.getCg(), this.getCg())
        return compare
        // this is for ascending order
        // return Double.compare(this.getCg(), that.getCg());
    }

    fun setCg(cg: Double) {
        this.cg = cg
    }
    fun setName(name: String) {
        this.name = name
    }
    fun getCredit():Double {
        return this.credit;
    }
    fun getRegNo():String {
        return this.regNo;
    }
    fun getName():String? {
        return this.name;
    }


}
