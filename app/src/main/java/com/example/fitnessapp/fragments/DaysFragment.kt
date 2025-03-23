package com.example.fitnessapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.R
import com.example.fitnessapp.adapter.DayModel
import com.example.fitnessapp.adapter.DaysAdapter
import com.example.fitnessapp.adapter.ExerciseModel
import com.example.fitnessapp.databinding.FragmentDaysBinding
import com.example.fitnessapp.utils.DialogManager
import com.example.fitnessapp.utils.FragmentManager
import com.example.fitnessapp.utils.MainViewModel

@Suppress("DEPRECATION")
class DaysFragment : Fragment(), DaysAdapter.Listener { // Подключили интерфейс из который создали в DaysAdapter
    private lateinit var adapter : DaysAdapter  // мы вынесли адаптер сюда, чтобы ОБНОВИТЬ шаред префс по факту как только пользователь нажал очистить
    private lateinit var binding: FragmentDaysBinding
    private val model: MainViewModel by activityViewModels() // Добавили зависимость. Для добавления надо указать зависимость от фрагмент в Gradle !
    private var ab: ActionBar? = null // добавили переменную для ActionBar, будем показывать счетчик упражнений

    override fun onCreate(savedInstanceState: Bundle?) {  // мы включили меню, его надо включить!
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)  // deprecated too

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDaysBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ab = (activity as AppCompatActivity).supportActionBar
        ab?.title = getString(R.string.trainingDays)
model.currentDay = 0     // мы обнуляем currentDay для того, чтобы функция fillDaysArray всегда начинала с 0 и перебирала все дни и правильно отображались чек боксы
        super.onViewCreated(view, savedInstanceState)
   initRcView()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) { // это deprecated - узнай чем заменить
    return inflater.inflate(R.menu.main_menu, menu)  // Сделали Меню - теперь есть 3 точки чтобы сбросить

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // Deprecated !!!!!
        if (item.itemId == R.id.reset) { // Мы проверили, если мы НАЖАЛИ именно на ВАРИАНТ "СБРОСИТЬ" - то выполняем действия в скобках
            DialogManager.showDialog(
                activity as AppCompatActivity,
                R.string.reset_days_message,
                object : DialogManager.Listener{
                    override fun onClick() {
                        model.pref?.edit()?.clear()?.apply()  // стираем всю таблицу - всё сбрасываем и делаем заново ( если пользователь решил начать с начала)
                        adapter.submitList(fillDaysArray())  // мы специально ВЫНЕСЛИ адатер в Лейт Инит Вар чтобы использовать сдесь и обновить адаптер если пользователь нажмет СБРОС
                    }

                })


        }
        return super.onOptionsItemSelected(item)
    }



    private fun initRcView() = with(binding){
        adapter = DaysAdapter(this@DaysFragment)
        rcviewdays.layoutManager = LinearLayoutManager(activity as AppCompatActivity)
        rcviewdays.adapter = adapter
        adapter.submitList(fillDaysArray())
    }


    private fun fillDaysArray() : ArrayList<DayModel>{
        val tArray = ArrayList<DayModel>() // создаём класс для заполнения из массивов с упражнениями. получается массив с упражнениями который состоит из DayModel
        var daysDoneCounter = 0
    resources.getStringArray(R.array.day_exercise).forEach {
  model.currentDay++
 val exCounter = it.split(",").size  // мы делаем чек бокс на экране с упражнениями. Он будет заполняться только когда ВСЕ упражнения данного дня - выполнены. Для этого возьмем массив, разделим по запятокй у узнаем размер, чтобы сравнить со счетчиком
    tArray.add(DayModel(it, model.getExerciseCount() == exCounter, 0)) // вот здесь я передал пока что 0 потому что добавленное поле DayModel требует указать все поля в конструкторе
    }
        binding.progressbar.max = tArray.size // размер массива это максимум проресс бара
        tArray.forEach{
            if (it.isDone) daysDoneCounter++ // Делаю прогресс бар ( сколько выполнено дней) - для этого сделал переменную restDaysCounter, которая считает по циклу количество isDone
        }
        updateRestDaysUI(tArray.size- daysDoneCounter, tArray.size) // - считаем оставшиеся дни + Обновляем прогрессбар
    return tArray
    }

    private fun updateRestDaysUI(restDays : Int, days: Int) = with(binding){
        val rDays = getString(R.string.rest)+ " $restDays "+ getString(R.string.rest_days) // собрали строку, сколько осталось дней ) на главном экране
        tvRestDays.text = rDays
        progressbar.progress = days - restDays // покажем прогресс в прогресс баре (DaysFragment)
    }

    private fun fillExerciseList(day: DayModel) {
        val templist = ArrayList<ExerciseModel>()
        day.exercises.split(",").forEach {//Мы вызвали опять массив с упражнениями, разбили его по запятой. Далее - с помощью фор ич получаем конкретное число которое далее используем для получения упражнения
           val exerciseList = resources.getStringArray(R.array.exercise)  // Внутри цикла forEach получаем упражнения из массива, которое далее тоже разделим на 3 части
           val exercise = exerciseList[it.toInt()] // здесь уже переводим It который получили выше в ИНТ и получаем элемент из массива с упражнениями. Далее мы тоже разделим его по палочке и получим элементы упражнения
        val exerciseArray = exercise.split("|")    // теперь элементы упражнения будут по порядку по позициям. Название, время. Картинка
        templist.add(ExerciseModel(exerciseArray[0], exerciseArray[1], false,exerciseArray[2])) // мы заполнили ExerciseModel.
        }// В итоге когда пройдёт весь список, у нас будут заполнены все упражнения в templist !

    model.mutableListExercise.value = templist // Везде где будет подключен "Обсервер", где подключен ViewModel, будет передаваться список из наших упражнений

    }



    companion object {

        @JvmStatic
        fun newInstance() = DaysFragment()


    }

    override fun onClick(day: DayModel) {  // функция интерфейса  //Фунцкия  перехода на фрагмент с упражнениями
        if (!day.isDone) {
        fillExerciseList(day)
        model.currentDay = day.dayNumber // с помощью этой переменной можем получить доступ к Модел с любого фрагмента и знать что записано, а так же записываются всё упражнения в разные дни!!!
        FragmentManager.setFragment(ExListFragment.newInstance(),
            activity as AppCompatActivity)
        }else {
            DialogManager.showDialog(
                activity as AppCompatActivity,
                R.string.reset_day_message,
                object : DialogManager.Listener{
                    override fun onClick() {
                        model.savePref(day.dayNumber.toString(), 0) // стираем всю таблицу - всё сбрасываем и делаем заново ( если пользователь решил начать с начала)
                        fillExerciseList(day)
                        model.currentDay = day.dayNumber // с помощью этой переменной можем получить доступ к Модел с любого фрагмента и знать что записано, а так же записываются всё упражнения в разные дни!!!
                        FragmentManager.setFragment(ExListFragment.newInstance(),
                            activity as AppCompatActivity)
                    } // с помощью Диалог менеджера сделал стирание только определенного дня, а не всех сразу. Получается, переиспользование кода выше, только с заменой ресурса
// мы передаем количество выполненных упражнений в 0, поэтому день обнуляется при нажатии на диалог
                })


        }
    }
}