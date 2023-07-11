package bangkit.robbyyehezkiel.androidintermediate.data.remotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import bangkit.robbyyehezkiel.androidintermediate.data.database.StoryListDatabase
import bangkit.robbyyehezkiel.androidintermediate.data.model.Story
import bangkit.robbyyehezkiel.androidintermediate.data.model.StoryRemoteKeys
import bangkit.robbyyehezkiel.androidintermediate.data.api.ApiService

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val storyListDatabase: StoryListDatabase,
    private val apiService: ApiService,
    private val token: String
) : RemoteMediator<Int, Story>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Story>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {

                INITIAL_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }
        return try {
            val responseData =
                apiService.getStoryList(token, page, state.config.pageSize).listStory
            val endOfPaginationReached = responseData.isEmpty()
            storyListDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    storyListDatabase.storyRemoteKeysDao().deleteRemoteKeys()
                    storyListDatabase.storyDao().deleteAll()
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = responseData.map {
                    StoryRemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                storyListDatabase.storyRemoteKeysDao().insertAll(keys)
                storyListDatabase.storyDao().insertStory(responseData)
            }
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: Exception) {
            MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Story>): StoryRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            storyListDatabase.storyRemoteKeysDao().getRemoteKeysId(data.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Story>): StoryRemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            storyListDatabase.storyRemoteKeysDao().getRemoteKeysId(data.id)
        }
    }
}