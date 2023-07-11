package bangkit.robbyyehezkiel.androidintermediate.view.widget

import android.content.Intent
import android.widget.RemoteViewsService

class RecentStoryService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        StackRemoteViewsFactory(this.applicationContext)
}