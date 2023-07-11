package bangkit.robbyyehezkiel.androidintermediate.view.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import bangkit.robbyyehezkiel.androidintermediate.view.activity.DetailStoryActivity
import com.bumptech.glide.Glide
import bangkit.robbyyehezkiel.androidintermediate.data.model.Story
import bangkit.robbyyehezkiel.androidintermediate.utils.Constanta
import bangkit.robbyyehezkiel.androidintermediate.databinding.RvStoryBinding

class StoryAdapter : PagingDataAdapter<Story, StoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

    inner class ViewHolder(private val binding: RvStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(story: Story) {
            with(binding) {
                storyName.text = story.name
                Glide.with(itemView)
                    .load(story.photoUrl)
                    .into(storyImage)
                itemView.setOnClickListener {
                    val intent = Intent(itemView.context, DetailStoryActivity::class.java)
                    intent.putExtra(Constanta.DetailStory.UserName.name, story.name)
                    intent.putExtra(Constanta.DetailStory.ImageURL.name, story.photoUrl)
                    try {
                        intent.putExtra(Constanta.DetailStory.Latitude.name, story.lat.toString())
                        intent.putExtra(Constanta.DetailStory.Longitude.name, story.lon.toString())
                    } catch (e: Exception) {
                        /* if story don't have location (lat, lon is null) -> skip put extra*/
                        Log.e(Constanta.TAG_STORY, e.toString())
                    }
                    intent.putExtra(
                        Constanta.DetailStory.ContentDescription.name,
                        story.description
                    )
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    val optionsCompat: ActivityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            itemView.context as Activity,
                            androidx.core.util.Pair(storyImage, "story_image"),
                            androidx.core.util.Pair(storyName, "user_name"),
                            androidx.core.util.Pair(defaultAvatar, "user_avatar"),
                        )
                    itemView.context.startActivity(intent, optionsCompat.toBundle())
                }
            }
        }
    }
}