package bangkit.robbyyehezkiel.androidintermediate.view.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Handler
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.widget.Toast
import androidx.core.os.bundleOf
import bangkit.robbyyehezkiel.androidintermediate.data.model.Story
import bangkit.robbyyehezkiel.androidintermediate.utils.Constanta
import bangkit.robbyyehezkiel.androidintermediate.utils.Helper
import bangkit.robbyyehezkiel.androidintermediate.R
import kotlinx.coroutines.runBlocking
import java.lang.StringBuilder

internal class StackRemoteViewsFactory(private val mContext: Context) :
    RemoteViewsService.RemoteViewsFactory {

    private val widgetBitmap = ArrayList<Bitmap>()
    private val storyItem = ArrayList<Story>()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        runBlocking {
            try {
                val service = bangkit.robbyyehezkiel.androidintermediate.data.api.ApiConfig.getApiService()

                val response = service.getStoryListWidget(Constanta.tempToken, 10).body()
                val stories = response?.listStory
                if (stories != null) {
                    widgetBitmap.clear()
                    storyItem.clear()
                    for (story in stories) {
                        val bitmap = Helper.bitmapFromURL(mContext, story.photoUrl)
                        val newBitmap = Helper.resizeBitmap(bitmap, 500, 500)
                        widgetBitmap.add(newBitmap)
                        storyItem.add(story)
                    }
                } else {
                    Log.i(Constanta.TAG_WIDGET, "Empty Stories")
                }
            } catch (exception: Exception) {
                Handler(mContext.mainLooper).post {
                    Toast.makeText(
                        mContext,
                        StringBuilder(mContext.getString(R.string.failed_fetch))
                            .append(" : ")
                            .append(exception.message),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(Constanta.TAG_WIDGET, "Failed fetch data : ${exception.message}")
                    exception.printStackTrace()
                }
            }
        }
    }


    override fun onDestroy() {
        Handler(mContext.mainLooper).post {
            Toast.makeText(
                mContext,
                mContext.getString(R.string.removed_widget),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun getCount(): Int = widgetBitmap.size

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(mContext.packageName, R.layout.widget_item)
        rv.setImageViewBitmap(R.id.imageView, widgetBitmap[position])
        val extras = bundleOf(
            Constanta.DetailStory.UserName.name to storyItem[position].name,
            Constanta.DetailStory.ImageURL.name to storyItem[position].photoUrl,
            Constanta.DetailStory.Longitude.name to storyItem[position].lon,
            Constanta.DetailStory.Latitude.name to storyItem[position].lat,
            Constanta.DetailStory.ContentDescription.name to storyItem[position].description,
        )
        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)
        rv.setOnClickFillInIntent(R.id.imageView, fillInIntent)
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(i: Int): Long = 0

    override fun hasStableIds(): Boolean = false
}