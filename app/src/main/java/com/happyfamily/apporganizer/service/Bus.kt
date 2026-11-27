package com.happyfamily.apporganizer.service

import com.happyfamily.apporganizer.data.RunRecord
import com.happyfamily.apporganizer.data.ScanResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

/** 앱 화면 <-> 접근성 서비스 연결 통로 (같은 프로세스) */
object Bus {
    data class Progress(val title: String, val detail: String, val done: Int, val total: Int)

    sealed class Event {
        data class ScanDone(val result: ScanResult) : Event()
        data class ApplyDone(val record: RunRecord) : Event()
        data class DumpDone(val text: String) : Event()
        data class Failed(val message: String) : Event()
    }

    val progress = MutableStateFlow<Progress?>(null)
    val events = MutableSharedFlow<Event>(replay = 0, extraBufferCapacity = 8)
    val serviceOn = MutableStateFlow(false)

    @Volatile var service: OrganizerService? = null
}
