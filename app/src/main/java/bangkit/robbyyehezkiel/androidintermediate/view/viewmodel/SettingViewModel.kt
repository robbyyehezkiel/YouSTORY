package bangkit.robbyyehezkiel.androidintermediate.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import bangkit.robbyyehezkiel.androidintermediate.utils.Constanta
import bangkit.robbyyehezkiel.androidintermediate.utils.UserPreferences
import kotlinx.coroutines.launch

class SettingViewModel(private val pref: UserPreferences): ViewModel() {


    fun getUserPreferences(property:String): LiveData<String> {
        return when(property){
            Constanta.AuthPreferences.UserUID.name -> pref.getId().asLiveData()
            Constanta.AuthPreferences.UserToken.name -> pref.getToken().asLiveData()
            Constanta.AuthPreferences.UserName.name -> pref.getName().asLiveData()
            Constanta.AuthPreferences.UserEmail.name -> pref.getEmail().asLiveData()
            else -> pref.getId().asLiveData()
        }
    }

    fun setUserPreferences(userToken: String, userUid: String, userName:String, userEmail: String) {
        viewModelScope.launch {
            pref.saveSession(userToken,userUid,userName,userEmail)
        }
    }

    fun clearUserPreferences() {
        viewModelScope.launch {
            pref.clearSession()
        }
    }


}