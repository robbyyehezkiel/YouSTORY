package bangkit.robbyyehezkiel.androidintermediate.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import bangkit.robbyyehezkiel.androidintermediate.data.model.Story
import bangkit.robbyyehezkiel.androidintermediate.data.model.StoryRemoteKeys

@Database(
    entities = [Story::class, StoryRemoteKeys::class],
    version = 2,
    exportSchema = false
)
abstract class StoryListDatabase : RoomDatabase() {

    abstract fun storyDao(): StoryListDao
    abstract fun storyRemoteKeysDao(): StoryRemoteKeysDao

    companion object {
        @Volatile
        private var INSTANCE: StoryListDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): StoryListDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StoryListDatabase::class.java, "story_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}