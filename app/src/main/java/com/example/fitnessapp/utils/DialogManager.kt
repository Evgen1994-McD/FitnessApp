package com.example.fitnessapp.utils

import android.app.AlertDialog
import android.content.Context
import androidx.compose.runtime.Composable
import com.example.fitnessapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogManager {

    @Composable
    fun showDialog(context : Context, mId : Int, listener : Listener) {  // передаём контекст, mId - messageId ( это сообщение) - Так как ресурсы у нас это ИНТ!!!
    val builder = MaterialAlertDialogBuilder(context) // мы делаем Диалоговое окно при попытке сбросить. ПОзитив баттон - согласиться, негатив - отменить
        var dialog: AlertDialog? = null // типа инициализировали диалог, изначально он равен null, а ниже мы используем его
        builder.setTitle(R.string.alert)
        builder.setMessage(mId)
       builder.setPositiveButton(R.string.reset){ _,_ -> {   }                                 // суть - _,_ ->   - нижние подчёркивания используются для того, если мы не используем переданные переменные. Тут передаются определенные значения. Если нам они не нужны, используем подчёркивания. А так это ОнКликЛистенер типа.
dialog = builder.create()  // Урок 29 минута 7.17 - продолжить


       }

    }

    interface Listener {
        fun onClick()  // мы создали Интерфейс с функцией Он клик, это будет наш кликер :D
    }


}