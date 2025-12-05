package com.tecsup.eventplanner_flores.data.model

data class Event(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val date: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)