package com.example.pgproto01.data.model

enum class PunchType {
    IN, BREAK_OUT, BREAK_IN, OUT;

    fun displayName(): String = when (this) {
        IN -> "出勤"
        BREAK_OUT -> "外出"
        BREAK_IN -> "戻り"
        OUT -> "退勤"
    }
}
