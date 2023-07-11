package bangkit.robbyyehezkiel.androidintermediate.data.remotemediator

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import bangkit.robbyyehezkiel.androidintermediate.data.database.StoryListDatabase
import bangkit.robbyyehezkiel.androidintermediate.data.model.Story
import bangkit.robbyyehezkiel.androidintermediate.data.api.ApiService

class StoryRepository(
    private val storyListDatabase: StoryListDatabase,
    private val apiService: ApiService,
    private val token:String
) {
    fun getStory(): LiveData<PagingData<Story>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(pageSize = 5),
            remoteMediator = StoryRemoteMediator(storyListDatabase, apiService,token),
            pagingSourceFactory = { storyListDatabase.storyDao().getAllStory() }
        ).liveData
    }
}