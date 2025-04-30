package com.tutorapp.data



object Converters {


    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(separator = ",") ?: ""
    }


    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList()
        else value.split(",")
    }
}
