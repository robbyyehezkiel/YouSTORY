package bangkit.robbyyehezkiel.androidintermediate.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import bangkit.robbyyehezkiel.androidintermediate.data.model.Story
import bangkit.robbyyehezkiel.androidintermediate.data.remotemediator.StoryRepository

class StoryListPagerViewModel(storyRepository: StoryRepository) : ViewModel() {
    val story: LiveData<PagingData<Story>> =
        storyRepository.getStory().cachedIn(viewModelScope)
}
