package bangkit.robbyyehezkiel.androidintermediate.view.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import bangkit.robbyyehezkiel.androidintermediate.data.model.Story
import bangkit.robbyyehezkiel.androidintermediate.data.remotemediator.StoryRepository
import bangkit.robbyyehezkiel.androidintermediate.utils.DataDummy
import bangkit.robbyyehezkiel.androidintermediate.utils.MainDispatcherRule
import bangkit.robbyyehezkiel.androidintermediate.utils.getOrAwaitValue
import bangkit.robbyyehezkiel.androidintermediate.view.adapter.StoryAdapter
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StoryListPagerViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var storyPagingRepository: StoryRepository

    @Test
    fun `when Get Story List Should Not Null and Return Success`() = runTest {
        val dataDummyStory = DataDummy.generateDummyNewsEntity()
        val data: PagingData<Story> = StoryListPaging.snapshot(dataDummyStory)
        val expectedQuote = MutableLiveData<PagingData<Story>>()
        expectedQuote.value = data

        Mockito.`when`(storyPagingRepository.getStory()).thenReturn(expectedQuote)
        val mainViewModel = StoryListPagerViewModel(storyPagingRepository)
        val actualQuote: PagingData<Story> = mainViewModel.story.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)

        assertNotNull(differ.snapshot())
        assertEquals(dataDummyStory, differ.snapshot())
        assertEquals(dataDummyStory.size, differ.snapshot().size)
        assertEquals(dataDummyStory[0], differ.snapshot()[0])
        Mockito.verify(storyPagingRepository).getStory()
    }

    @Test
    fun `when Get Story List Should Be Empty axnd Return Success`() = runTest {
        val dataDummyStory = mutableListOf<Story>()
        val data: PagingData<Story> = StoryListPaging.snapshot(dataDummyStory)
        val expectedQuote = MutableLiveData<PagingData<Story>>()
        expectedQuote.value = data

        Mockito.`when`(storyPagingRepository.getStory()).thenReturn(expectedQuote)
        val mainViewModel = StoryListPagerViewModel(storyPagingRepository)
        val actualQuote: PagingData<Story> = mainViewModel.story.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)

        assertNotNull(differ.snapshot())
        assertEquals(dataDummyStory, differ.snapshot())
        assertEquals(dataDummyStory.size, differ.snapshot().size)
        Mockito.verify(storyPagingRepository).getStory()
    }
}


class StoryListPaging : PagingSource<Int, LiveData<List<Story>>>() {
    companion object {
        fun snapshot(items: MutableList<Story>): PagingData<Story> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, LiveData<List<Story>>>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<Story>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}