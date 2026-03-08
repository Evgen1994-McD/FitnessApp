package com.example.fitnessapp.settings.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnessapp.R
import com.example.fitnessapp.exercises.domain.models.ThemeMode


@Composable
fun SettingsScreen(viewModel: SettingsViewModel,
                   onClearedDataClick: () -> Unit,
                   onOpenAllTrainingsClick: () -> Unit) {
    val themeMode by viewModel.themeMode.collectAsState()
    val voiceTipsEnabled by viewModel.voiceTipsEnabled.collectAsState()


    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),

    ) {
        Spacer(
            modifier = Modifier
                .height(100.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth(),

        ) {
            Text(
                text = stringResource(R.string.dark_theme),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = themeMode==ThemeMode.DARK,
                onCheckedChange = {isChecked ->
                    val theme = if (isChecked) ThemeMode.DARK else ThemeMode.LIGHT
                    viewModel.switchTheme(theme)

                                  },
                colors = SwitchDefaults.colors(
                    checkedIconColor = Color.Blue,
                    checkedThumbColor = Color.Blue,

                )
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),

        ) {
            Text(
                text = "Включить голосовые советы",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = voiceTipsEnabled,
                onCheckedChange = { enabled ->
                    viewModel.toggleVoiceTips(enabled)
                },
                colors = SwitchDefaults.colors(
                    checkedIconColor = Color.Blue,
                    checkedThumbColor = Color.Blue,
                )
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clickable {
                    onOpenAllTrainingsClick()
                }
        ) {
            Text(
                text = "Открыть все тренировки",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                painter = painterResource(R.drawable.ic_check_bx_im_24),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clickable {
                    onClearedDataClick()
                }
        ) {
            Text(
                text = stringResource(R.string.clean_data),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                painter = painterResource(R.drawable.ic_clear_24),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}



