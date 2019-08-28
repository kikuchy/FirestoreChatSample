package com.diverseinc.firestorechat.ui.talk

import androidx.lifecycle.*
import com.diverseinc.firestorechat.data.Repository
import com.diverseinc.firestorechat.data.RoomId
import com.diverseinc.firestorechat.data.Transcript
import com.diverseinc.firestorechat.data.UserId
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@InternalCoroutinesApi
class TalkViewModel(private val repository: Repository, private val roomId: RoomId): ViewModel() {
    private val _transcripts: MutableLiveData<List<Transcript>> = MutableLiveData()
    val transcripts: LiveData<List<Transcript>>
        get() = _transcripts
    private val _fromUserId: MutableLiveData<UserId> = MutableLiveData()
    val fromUserId: LiveData<UserId>
        get() = _fromUserId

    init {
        viewModelScope.launch {
            _fromUserId.value = repository.userId()
        }
        viewModelScope.launch {
            repository.talks(roomId)
                .onEach {
                    _transcripts.value = it
                }
                .collect(object : FlowCollector<List<Transcript>> {
                    override suspend fun emit(value: List<Transcript>) {

                    }
                })
        }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            repository.postTranscript(roomId, message)
        }
    }

    class Factory(private val repository: Repository, private val roomId: RoomId): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            // FIXME: いい加減な実装なのであとで直す
            return TalkViewModel(repository, roomId) as T
        }
    }
}