package com.example.fitnessapp.utils

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.TextView
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
        val builder = MaterialAlertDialogBuilder(context) // Возвращаем к стандартной теме
        var dialog: Dialog? =
            null // типа инициализировали диалог, изначально он равен null, а ниже мы используем его
        builder.setTitle(R.string.alert)
        builder.setMessage(mId)
        builder.setPositiveButton(R.string.reset) { _, _ ->
            // суть - _,_ ->   - нижние подчёркивания используются для того, если мы не используем переданные переменные. Тут передаются определенные значения. Если нам они не нужны, используем подчёркивания. А так это ОнКликЛистенер типа.
            listener.onClick()
            dialog?.dismiss()

        }
        builder.setNegativeButton(R.string.backoff) { _, _ ->
            dialog?.dismiss()  // Просто отменяем диалог если не согласны
        }
        dialog = builder.create()
        dialog.show()
        
        // Устанавливаем черный фон диалога
        dialog.window?.setBackgroundDrawableResource(android.R.color.black)
        
        // Получаем цвета из темы приложения
        val typedValue = android.util.TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        val backgroundColor = typedValue.data
        
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
        val textColor = typedValue.data
        
        // Применяем цвета к тексту
        dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(textColor)
        dialog.findViewById<TextView>(android.R.id.title)?.setTextColor(textColor)
        
    }

    fun showWeightDialog(
        context: Context,
        listener: WeightListener,
        weight : String = ""
    ) {  // передаём контекст, mId - messageId ( это сообщение) - Так как ресурсы у нас это ИНТ!!!
        val builder = MaterialAlertDialogBuilder(context) // Используем стандартную тему
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
        fun onDifficultySelected(difficultyLevel: Int, zone: String? = null)
    }

    fun showAfterTrainingDialog(
        context: Context,
        listener: OnDifficultySelectedListener,
        zone: String? = null
    ) {
        val builder = MaterialAlertDialogBuilder(context) // Используем стандартную тему
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
                    listener.onDifficultySelected(DIFFICULTY_UP, zone)
                    tvTitle.setText("Работаем...\n" +
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
                            btIsNothing.backgroundTintList = ColorStateList.valueOf(android.graphics.Color.BLUE)
                            btIsNothing.setTextColor(android.graphics.Color.WHITE)

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
                    listener.onDifficultySelected(DIFFICULTY_DOWN, zone)
                    tvTitle.setText("Работаем...\n" +
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
                            btIsNothing.setTextColor(android.graphics.Color.WHITE)

                            btIsNothing.backgroundTintList = ColorStateList.valueOf(android.graphics.Color.BLUE)
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