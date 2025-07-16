package com.example.fitnessapp.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.WeightDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DialogManager {   // Сначала сделал как класс, но он работает только если обджект. Как и фрагмент менеджер. Если мы укажем обжект - сможем добраться без инициализации класса. Если как класс - сначала надо его инициализировать.

    fun showDialog(
        context: Context,
        mId: Int,
        listener: Listener,
    ) {  // передаём контекст, mId - messageId ( это сообщение) - Так как ресурсы у нас это ИНТ!!!
        val builder = MaterialAlertDialogBuilder(
            ContextThemeWrapper(
                context,
                R.style.AlertDialog_AppCompat_
            )
        ) // мы делаем Диалоговое окно при попытке сбросить. ПОзитив баттон - согласиться, негатив - отменить
        var dialog: Dialog? =
            null // типа инициализировали диалог, изначально он равен null, а ниже мы используем его
        builder.setTitle(R.string.alert)
        builder.setMessage(mId)
        builder.setPositiveButton(R.string.reset) { _, _ ->
            // суть - _,_ ->   - нижние подчёркивания используются для того, если мы не используем переданные переменные. Тут передаются определенные значения. Если нам они не нужны, используем подчёркивания. А так это ОнКликЛистенер типа.
            listener.onClick()
            dialog?.dismiss()

        }
        builder.setNegativeButton(R.string.back) { _, _ ->
            dialog?.dismiss()  // Просто отменяем диалог если не согласны
        }
        dialog = builder.create()
        dialog.show() // показываем диалог, иначе его не будет видно

    }

    fun showWeightDialog(
        context: Context,
        listener: WeightListener,
        weight : String = ""
    ) {  // передаём контекст, mId - messageId ( это сообщение) - Так как ресурсы у нас это ИНТ!!!
        val builder =
            MaterialAlertDialogBuilder(context) // мы делаем Диалоговое окно при попытке сбросить. ПОзитив баттон - согласиться, негатив - отменить
        val dialog = builder.create()
        val binding = WeightDialogBinding.inflate(LayoutInflater.from(context))
        dialog.setView(binding.root)

        binding.apply {
            edWeight.setText(weight)
            bCancel.setOnClickListener {
                dialog.dismiss()
            }

            bSave.setOnClickListener {
                listener.onClick(edWeight.text.toString())

                dialog.dismiss()
            }
        }
        dialog.show() // показываем диалог, иначе его не будет видн


    }


    interface Listener {
        fun onClick()  // мы создали Интерфейс с функцией Он клик, это будет наш кликер :D
    }

    interface WeightListener {
        fun onClick(weight: String)  // мы создали Интерфейс с функцией Он клик, это будет наш кликер :D
    }

}