package com.diverseinc.firestorechat.ui.rooms

import androidx.lifecycle.*
import com.diverseinc.firestorechat.data.Repository
import com.diverseinc.firestorechat.data.Room
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@InternalCoroutinesApi
class RoomsViewModel(private val repository: Repository) : ViewModel(), LifecycleObserver {
    private val _roomList: MutableLiveData<List<Room>> = MutableLiveData()
    val roomList: LiveData<List<Room>>
        get() = _roomList
    private val _addRoomResult: MutableLiveData<Result<Unit>> = MutableLiveData()
    val addRoomResult: LiveData<Result<Unit>>
        get() = _addRoomResult

    init {
        viewModelScope.launch {
            repository
                .rooms()
                .onEach {
                    _roomList.value = it
                }
                .collect(object : FlowCollector<List<Room>> {
                    override suspend fun emit(value: List<Room>) {
                        _roomList.value = value
                    }
                })
        }
    }

    fun addRoom(targetUserId: String) {
        viewModelScope.launch {
            _addRoomResult.value = try {
                repository.addRoom(targetUserId)
                Result.success(Unit)
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
    }

    class Factory(private val repository: Repository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            // FIXME: いい加減な実装なのであとで直す
            return RoomsViewModel(repository) as T
        }
    }
}