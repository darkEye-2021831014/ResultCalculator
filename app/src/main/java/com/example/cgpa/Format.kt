package com.example.cgpa

enum class Format {
    REG_MARKS,
    REG_NAME_MARKS,
    REG_NAME_GRADE,
    REG_NAME_CREDIT_CG,
    CG_REG_NAME, //this one works for REG_NAME_CG as the order in which pdf reader reads is messed up
    MARKS_REG_NAME, //this one works for REG_NAME_MARKS as the order in which pdf reader reads is messed up
    REG_NAME_CG,
    NAME,
    REG,
    GRADE,
    MARKS,
    CG,
    CREDIT,
    THIS_MONTH,
    LAST_MONTH,
    THIS_YEAR,
    LAST_YEAR,
    DAILY_REPORT,
    MONTHLY_REPORT,
    YEARLY_REPORT,
    EXPENSE,
    INCOME
}
