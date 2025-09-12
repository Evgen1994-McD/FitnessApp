package com.example.fitnessapp.settings.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.FragmentSettingsBinding
import com.example.fitnessapp.utils.DialogManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null //ЭТО сам байндинг Налл
    private val binding get() = _binding!! // а здесь мы получаем байндинг
    private val model: SettingsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSettingsBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).
            supportActionBar?.title = "Настройки"
        binding.apply {
            clearDataButton.setOnClickListener {
                DialogManager.showDialog(
                    requireContext(),
                    R.string.reset_days_message, object : DialogManager.Listener {
                        override fun onClick()  {
                            model.clearData()
                        }
                    })
            }
            customTrainingSettingsButton.setOnClickListener {
                findNavController().navigate(R.id.customDaysListFragment)

            }
        }

        controlTheme()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*
    В onDestroyView наш байндинг приравниваем обратно к null
    Данная фича помогает избежать некоторых ошибок когда вью уже разрушено
    но доступ к байдингу всё ещё есть
     */


    private fun controlTheme(){
        binding.darkTheme.setOnCheckedChangeListener {_, isChecked ->
            model.switchTheme(isChecked)

        }
    }
}