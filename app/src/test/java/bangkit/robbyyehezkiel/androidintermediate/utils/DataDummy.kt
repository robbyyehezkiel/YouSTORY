package bangkit.robbyyehezkiel.androidintermediate.utils

import bangkit.robbyyehezkiel.androidintermediate.data.model.Story


object DataDummy {
    fun generateDummyNewsEntity(): MutableList<Story> {
        val stories: MutableList<Story> = arrayListOf()
        for (i in 0..100) {
            val story = Story(
                "Id $i",
                "https://cdn.antaranews.com/cache/800x533/2021/07/15/Foto-1-Logo-Bangkit.png",
                "2023/04/09 12:19:31",
                "Bangkit",
                "Intermediate Application Bangkit",
                "103.5199",
                "-1.6146"
            )
            stories.add(story)
        }
        return stories
    }
}