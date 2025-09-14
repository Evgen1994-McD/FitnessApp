import kotlinx.coroutines.*

/**
 * Функция для дебаунса (ограничения частоты выполнения действий).
 *
 * @param delayMillis Длительность задержки в миллисекундах.
 * @param coroutineScope Корутинный scope для запуска корутин.
 * @param action Действие, которое нужно выполнить после задержки.
 */
fun debounce(delayMillis: Long, coroutineScope: CoroutineScope, action: suspend () -> Unit): () -> Unit {
    var job: Job? = null

    return {
        coroutineScope.launch {
            job?.cancel() // Отменяем старое задание, если оно ещё активно
            job = launch {
                delay(delayMillis) // Ждём установленную задержку
                action() // Выполняем основное действие
            }
        }
    }
}