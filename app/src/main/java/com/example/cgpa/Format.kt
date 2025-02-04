package com.example.cgpa

enum class Format {
    REG_MARKS,
    REG_NAME_MARKS,
    REG_NAME_GRADE,
    REG_NAME_CREDIT_CG,
    CG_REG_NAME, //this one works for REG_NAME_CG as the order in which pdf reader reads is messed up
    MARKS_REG_NAME, //this one works for REG_NAME_MARKS as the order in which pdf reader reads is messed up
    REG_NAME_CG
}
