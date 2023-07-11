package bangkit.robbyyehezkiel.androidintermediate.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import bangkit.robbyyehezkiel.androidintermediate.data.model.Story

@Dao
interface StoryListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: List<Story>)

    @Query("SELECT * FROM story order by createdAt DESC")
    fun getAllStory(): PagingSource<Int, Story>

    @Query("DELETE FROM story")
    suspend fun deleteAll()
}