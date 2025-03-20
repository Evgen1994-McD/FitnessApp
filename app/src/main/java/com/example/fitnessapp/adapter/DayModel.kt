package com.example.fitnessapp.adapter

import android.os.Bundle

data class DayModel(
    var exercises : String,
    var isDone : Boolean,
    var dayNumber : Int // будем передавать день по счетчику
)
