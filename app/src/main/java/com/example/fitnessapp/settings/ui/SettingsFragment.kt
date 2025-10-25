package com.example.fitnessapp.settings.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.FragmentSettingsBinding
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import com.example.fitnessapp.utils.DialogManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private val model: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            // Обязательно: стратегия уничтожения композиции
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycleOwner = this@SettingsFragment))

            setContent {
                FitnessAppTheme {
                    SettingsScreen(model)
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        binding.apply {
//            clearDataButton.setOnClickListener {
//                DialogManager.showDialog(
//                    requireContext(),
//                    R.string.reset_days_message, object : DialogManager.Listener {
//                        override fun onClick()  {
//                            model.clearData()
//                        }
//                    })
//            }
//            customTrainingSettingsButton.setOnClickListener {
//                findNavController().navigate(R.id.customDaysListFragment)
//
//            }
//        }
//
//        controlTheme()
    }




}