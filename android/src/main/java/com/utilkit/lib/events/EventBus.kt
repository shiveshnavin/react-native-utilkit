package com.utilkit.lib.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore

class EventBus : ViewModel() {

  companion object {
    val mViewModelStore: ViewModelStore = ViewModelStore()
  }

  private val _reactToNativeBus = MutableLiveData<Event>()
  val reactToNativeBus: LiveData<Event> get() = _reactToNativeBus
  fun emitReactToNative(event: Event){
    _reactToNativeBus.postValue(event)
  }

  private val _nativeToReactBus = MutableLiveData<Event>()
  val nativeToReactBus: LiveData<Event> get() = _nativeToReactBus
  fun emitNativeToReact(event: Event){
    _nativeToReactBus.postValue(event)
  }

}
