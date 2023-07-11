package bangkit.robbyyehezkiel.androidintermediate.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import bangkit.robbyyehezkiel.androidintermediate.databinding.ActivityFolderBinding
import bangkit.robbyyehezkiel.androidintermediate.view.adapter.FolderAdapter
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.FolderViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FolderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFolderBinding
    private val viewModel: FolderViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Image Folder"
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewModel.let { vm ->
            vm.loadingDownload.observe(this) {
                binding.loadingDownload.isVisible = it
            }

            vm.assetImageDownload.observe(this) { data ->
                binding.rvDownload.let {
                    binding.nullDownload.isVisible = data.isEmpty()
                    it.setHasFixedSize(true)
                    it.layoutManager = GridLayoutManager(this@FolderActivity, 3)
                    it.isNestedScrollingEnabled = false
                    it.adapter = FolderAdapter(data)
                }
            }
        }
        binding.root
    }

    override fun onResume() {
        super.onResume()
        loadFolderData()
    }
    @OptIn(DelicateCoroutinesApi::class)
    private fun loadFolderData() {
        GlobalScope.launch {
            viewModel.loadImage(this@FolderActivity)
        }
    }
}