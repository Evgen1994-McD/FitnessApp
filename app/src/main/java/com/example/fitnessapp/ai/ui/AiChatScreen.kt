package com.example.fitnessapp.ai.ui

import androidx.compose.foundation.background
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    viewModel: AiViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onCreatePlanClick: () -> Unit = {}
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Автоскролл к последнему сообщению
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Фитнес-тренер") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCreatePlanClick) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Создать план"
                        )
                    }
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Очистить чат"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Сообщения
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = messages,
                    key = { "${it.timestamp}_${it.content.hashCode()}" }
                ) { message ->
                    ChatBubble(
                        message = message,
                        isUser = message.isUser
                    )
                }

                // Индикатор загрузки
                if (isLoading) {
                    item {
                        LoadingBubble()
                    }
                }
                
                // Индикатор набора текста (когда AI печатает)
                if (isLoading && messages.lastOrNull()?.isUser == false) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            // Поле ввода
            ChatInputField(
                value = inputText,
                onValueChange = viewModel::updateInputText,
                onSend = { message ->
                    viewModel.sendMessage(message)
                },
                isLoading = isLoading
            )
        }
    }
}

@Composable
fun ChatBubble(
    message: com.example.fitnessapp.ai.domain.models.ChatMessage,
    isUser: Boolean
) {
    val bubbleColor = if (isUser)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceVariant

    val textColor = if (isUser)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Text(
            text = if (isUser) "Вы" else "AI Тренер",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(
                start = if (isUser) 0.dp else 16.dp,
                end = if (isUser) 16.dp else 0.dp,
                bottom = 2.dp
            )
        )

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message.content,
                color = textColor,
                fontSize = 16.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun LoadingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Dot()
                Dot(delay = 100)
                Dot(delay = 200)
            }
        }
    }
}

@Composable
private fun Dot(delay: Int = 0) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(delay)
        )
    )
    
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                shape = CircleShape
            )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: (String) -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Введите сообщение...") },
            enabled = !isLoading,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = ImeAction.Send
            ),
            shape = RoundedCornerShape(24.dp)
        )

        IconButton(
            onClick = {
                if (value.isNotBlank()) {
                    onSend(value)
                }
            },
            enabled = !isLoading && value.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Отправить"
                )
            }
        }
    }
}
