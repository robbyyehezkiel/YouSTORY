@file:Suppress("UNCHECKED_CAST")

package bangkit.robbyyehezkiel.androidintermediate.view.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import bangkit.robbyyehezkiel.androidintermediate.data.database.StoryListDatabase
import bangkit.robbyyehezkiel.androidintermediate.data.api.ApiService
import bangkit.robbyyehezkiel.androidintermediate.data.remotemediator.StoryRepository

class ViewModelStoryFactory(val context: Context, private val apiService: ApiService, val token:String) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryListPagerViewModel::class.java)) {
            val database = StoryListDatabase.getDatabase(context)
            return StoryListPagerViewModel(
                StoryRepository(
                    database,
                    apiService, token

                )
            ) as T
        }
        throw IllegalArgumentException("ViewModel not found")
    }
}