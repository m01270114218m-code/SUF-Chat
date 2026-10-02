package com.pharaohparty.app
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
sealed interface Screen { data object Login:Screen; data object Home:Screen; data class Room(val room:com.pharaohparty.app.Room):Screen; data object Store:Screen; data object Profile:Screen }
class PartyViewModel(app:Application):AndroidViewModel(app){
 private val api=PartyApi(app)
 private val _screen=MutableStateFlow<Screen>(Screen.Login); val screen:StateFlow<Screen> = _screen
 private val _profile=MutableStateFlow<Profile?>(null); val profile:StateFlow<Profile?>=_profile
 private val _rooms=MutableStateFlow<List<Room>>(emptyList()); val rooms:StateFlow<List<Room>>=_rooms
 private val _store=MutableStateFlow<List<StoreItem>>(emptyList()); val store:StateFlow<List<StoreItem>>=_store
 private val _error=MutableStateFlow<String?>(null); val error:StateFlow<String?>=_error
 init{restore()}
 private fun restore()=viewModelScope.launch{try{if(api.restore()){_profile.value=api.profile();refresh();_screen.value=Screen.Home}}catch(_:Exception){}}
 fun login(username:String,password:String,create:Boolean)=viewModelScope.launch{_error.value=null;try{api.auth(AuthRequest(if(create)"register" else "login",username.trim(),password));_profile.value=api.profile();if(_profile.value?.is_banned==true)error("هذا الحساب محظور");refresh();_screen.value=Screen.Home}catch(e:Exception){_error.value=e.message?:"حدث خطأ"}}
 fun quickLogin()=viewModelScope.launch{_error.value=null;try{api.auth(AuthRequest(action="quick_login",device_key=api.deviceKey()));_profile.value=api.profile();refresh();_screen.value=Screen.Home}catch(e:Exception){_error.value=e.message?:"تعذر الدخول السريع"}}
 fun refresh()=viewModelScope.launch{runCatching{_rooms.value=api.rooms();_store.value=api.store()}.onFailure{_error.value=it.message}}
 fun open(room:Room){_screen.value=Screen.Room(room)}
 fun store(){_screen.value=Screen.Store}
 fun profile(){_screen.value=Screen.Profile}
 fun home(){_screen.value=Screen.Home}
 fun clearError(){_error.value=null}
}