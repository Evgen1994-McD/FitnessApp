package com.example.fitnessapp.utils

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.core.view.isVisible
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.AfterTrainingDialogueBinding
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



    interface OnDifficultySelectedListener {
        fun onDifficultySelected(difficultyLevel: Int)
    }

    fun showAfterTrainingDialog(
        context: Context,
        listener: OnDifficultySelectedListener
    ) {
        val builder = MaterialAlertDialogBuilder(context)
        val dialog = builder.create()
        val binding = AfterTrainingDialogueBinding.inflate(LayoutInflater.from(context))
        dialog.setView(binding.root)



        binding.apply {
            // Установим обработку кликов отдельно для каждой кнопки
            btSoEasy.setOnClickListener {
                lottieView.setAnimation(R.raw.down_arrow)
                lottieView.rotationX = 180f
                lottieView.repeatCount = ValueAnimator.INFINITE
                lottieView.playAnimation()
                btSoHard.isVisible = false
                tvTitle.setText("Усложнить тренировку?")
                btSoEasy.setText("Да, усложнить вызов!")
                btSoEasy.setOnClickListener {
                    listener.onDifficultySelected(DIFFICULTY_UP)
                    tvTitle.setText("Работаем над вашим запросом!\n" +
                            "Пожалуйста, подождите...")
                    btSoEasy.isVisible = false
                    btIsNothing.isVisible = false
                    btIsNothing.setText("Отлично!")

                    lottieView.setAnimation(R.raw.place_holder_question)
                    lottieView.speed = 0.75F
                    lottieView.repeatCount = 0
                    lottieView.playAnimation()

                    lottieView.addAnimatorListener(object : Animator.AnimatorListener{
                        override fun onAnimationStart(animation: Animator) {

                        }

                        override fun onAnimationEnd(animation: Animator) {
                            tvTitle.setText("Тренер Закончил настройку!\n" +
                                    "Нагрузка увеличена!")
                            btIsNothing.isVisible = true

                        }

                        override fun onAnimationCancel(animation: Animator) {

                        }

                        override fun onAnimationRepeat(animation: Animator) {

                        }
                    })



                }


            }

            btSoHard.setOnClickListener {
                lottieView.setAnimation(R.raw.down_arrow)
                lottieView.repeatCount = ValueAnimator.INFINITE
                lottieView.playAnimation()
                btSoEasy.isVisible = false
                tvTitle.setText("Упростить тренировку?")
                btSoHard.setText("Да, сделать проще!")
                btSoHard.setOnClickListener {
                    listener.onDifficultySelected(DIFFICULTY_DOWN)
                    tvTitle.setText("Работаем над вашим запросом!\n" +
                            "Пожалуйста, подождите...")
                    btSoHard.isVisible = false
                    btIsNothing.isVisible = false
                    btIsNothing.setText("Отлично!")

                    lottieView.setAnimation(R.raw.place_holder_question)
                    lottieView.speed = 0.75F
                    lottieView.repeatCount = 0
                    lottieView.playAnimation()

                    lottieView.addAnimatorListener(object : Animator.AnimatorListener{
                        override fun onAnimationStart(animation: Animator) {

                        }

                        override fun onAnimationEnd(animation: Animator) {
                            tvTitle.setText("Тренер Закончил настройку!\n" +
                                    "Нагрузка сокращена!")
                            btIsNothing.isVisible = true

                        }

                        override fun onAnimationCancel(animation: Animator) {

                        }

                        override fun onAnimationRepeat(animation: Animator) {

                        }
                    })

                }
            }
            btIsNothing.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
        binding.lottieView.isVisible = true
        binding.lottieView.playAnimation()
    }

    // Константы уровней сложности
    const val DIFFICULTY_UP = 1
    const val DIFFICULTY_DOWN = 2







}