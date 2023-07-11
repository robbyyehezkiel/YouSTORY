package bangkit.robbyyehezkiel.androidintermediate.view.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import bangkit.robbyyehezkiel.androidintermediate.utils.UserPreferences

class   ViewModelSettingFactory(private val pref: UserPreferences) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
            return SettingViewModel(pref) as T
        }
        throw IllegalArgumentException("ViewModel not found: " + modelClass.name)
    }
}
