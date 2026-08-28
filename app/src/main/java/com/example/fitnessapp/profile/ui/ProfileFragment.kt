package com.example.fitnessapp.profile.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.fitnessapp.R
import com.example.fitnessapp.auth.YandexAuthManager
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    // Внедряем напрямую, а не через ProfileViewModel: registerForActivityResult вызывается
    // до attach фрагмента к FragmentManager, а `by viewModels()` в этот момент ещё
    // недоступен (IllegalStateException: "Can't access ViewModels from detached fragment").
    @Inject
    lateinit var yandexAuthManager: YandexAuthManager

    private val model: ProfileViewModel by viewModels()

    private lateinit var loginLauncher: ActivityResultLauncher<YandexAuthLoginOptions>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Hilt-инъекция полей происходит внутри super.onAttach(), поэтому yandexAuthManager
        // здесь уже проинициализирован. registerForActivityResult безопасно вызывать
        // в любой момент до onCreate() (то есть строго до STARTED).
        loginLauncher = registerForActivityResult(yandexAuthManager.getContract()) { result: YandexAuthResult ->
            model.onAuthResult(result)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycleOwner = this@ProfileFragment))

            setContent {
                FitnessAppTheme {
                    val session by model.userSession.collectAsState()
                    val error by model.errorMessage.collectAsState()

                    ProfileScreen(
                        session = session,
                        errorMessage = error,
                        onLoginClick = { loginLauncher.launch(yandexAuthManager.createLoginOptions()) },
                        onLogoutClick = { model.logout() },
                        onErrorShown = { model.clearError() }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.profile_title)
    }
}
