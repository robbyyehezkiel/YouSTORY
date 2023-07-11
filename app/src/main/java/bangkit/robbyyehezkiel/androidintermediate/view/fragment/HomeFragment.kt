package bangkit.robbyyehezkiel.androidintermediate.view.fragment

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import bangkit.robbyyehezkiel.androidintermediate.view.activity.MainActivity
import bangkit.robbyyehezkiel.androidintermediate.utils.Helper
import bangkit.robbyyehezkiel.androidintermediate.R
import bangkit.robbyyehezkiel.androidintermediate.databinding.FragmentHomeBinding
import bangkit.robbyyehezkiel.androidintermediate.view.adapter.StoryAdapter
import bangkit.robbyyehezkiel.androidintermediate.view.adapter.StoryLoadingStateAdapter
import bangkit.robbyyehezkiel.androidintermediate.view.activity.FolderActivity
import bangkit.robbyyehezkiel.androidintermediate.view.activity.MapsActivity
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.SettingViewModel
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.ViewModelSettingFactory
import bangkit.robbyyehezkiel.androidintermediate.utils.Constanta
import bangkit.robbyyehezkiel.androidintermediate.utils.UserPreferences
import bangkit.robbyyehezkiel.androidintermediate.utils.dataStore
import java.util.*
import kotlin.concurrent.schedule

class HomeFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private val rvAdapter = StoryAdapter()
    private lateinit var binding: FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        val mainViewModel = (activity as MainActivity).getStoryViewModel()
        mainViewModel.story.observe(viewLifecycleOwner) {
            rvAdapter.submitData(
                lifecycle,
                it
            )
            Helper.updateWidgetData(requireContext())
        }

        (activity as MainActivity)
            .supportActionBar!!.setDisplayShowHomeEnabled(true)

        (activity as MainActivity)
            .supportActionBar?.title = "RySTORY"

        binding.swipeRefresh.setOnRefreshListener {
            onRefresh()
        }
        binding.rvStory.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = false
            adapter =
                rvAdapter.withLoadStateFooter(footer = StoryLoadingStateAdapter { rvAdapter.retry() })
        }
        return binding.root
    }

    override fun onRefresh() {
        binding.swipeRefresh.isRefreshing = true
        rvAdapter.refresh()
        Timer().schedule(2000) {
            binding.swipeRefresh.isRefreshing = false
            binding.rvStory.smoothScrollToPosition(0)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_options, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val pref = UserPreferences.getPreferenceInstance((activity as MainActivity).dataStore)
        val settingViewModel =
            ViewModelProvider(this, ViewModelSettingFactory(pref))[SettingViewModel::class.java]
        settingViewModel.getUserPreferences(Constanta.AuthPreferences.UserToken.name)
            .observe(viewLifecycleOwner) {
                if (it == Constanta.preferenceDefaultValue) {
                    (activity as MainActivity).routeToAuth()
                }
            }
        when (item.itemId) {
            R.id.swipeRefresh -> {
                onRefresh()
            }

            R.id.switchLanguage -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            }

            R.id.allowPermission -> {
                Helper.permissionSetting(requireContext())
            }

            R.id.maps -> {
                startActivity(Intent(activity, MapsActivity::class.java))
            }

            R.id.folder -> {
                if (Helper.permissionGranted(
                        activity as MainActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    startActivity(Intent(activity, FolderActivity::class.java))
                } else {
                    ActivityCompat.requestPermissions(
                        activity as MainActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        Constanta.STORAGE_PERMISSION_CODE
                    )
                }
            }

            R.id.logout -> {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(R.string.alert_warning)
                builder.setMessage(R.string.alert_logout)
                builder.setIcon(R.drawable.baseline_icon_warning_yellow)

                builder.setPositiveButton("Yes") { _, _ ->
                    settingViewModel.clearUserPreferences()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.toast_success),
                        Toast.LENGTH_LONG
                    ).show()
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
        }
        return true
    }

}