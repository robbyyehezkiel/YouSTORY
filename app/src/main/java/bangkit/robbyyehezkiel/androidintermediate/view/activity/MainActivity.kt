package bangkit.robbyyehezkiel.androidintermediate.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import bangkit.robbyyehezkiel.androidintermediate.view.fragment.HomeFragment
import bangkit.robbyyehezkiel.androidintermediate.utils.Constanta
import bangkit.robbyyehezkiel.androidintermediate.utils.Helper
import bangkit.robbyyehezkiel.androidintermediate.utils.UserPreferences
import bangkit.robbyyehezkiel.androidintermediate.utils.dataStore
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.SettingViewModel
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.StoryListPagerViewModel
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.ViewModelSettingFactory
import bangkit.robbyyehezkiel.androidintermediate.view.viewmodel.ViewModelStoryFactory
import bangkit.robbyyehezkiel.androidintermediate.R
import bangkit.robbyyehezkiel.androidintermediate.databinding.ActivityMainBinding

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val pref = UserPreferences.getPreferenceInstance(dataStore)
    private val settingViewModel: SettingViewModel by viewModels { ViewModelSettingFactory(pref) }
    private var token = ""
    private var fragmentHome: HomeFragment? = null
    private lateinit var startNewStory: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fragmentHome = HomeFragment()

        startNewStory =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    fragmentHome?.onRefresh()
                }
            }

        settingViewModel.getUserPreferences(Constanta.AuthPreferences.UserToken.name)
            .observe(this) {
                token = "Bearer $it"
                switchFragment(fragmentHome!!)
            }

        binding.fab.setOnClickListener {
            if (Helper.permissionGranted(this, Manifest.permission.CAMERA)) {
                val intent = Intent(this@MainActivity, NewStoryCameraActivity::class.java)
                startNewStory.launch(intent)
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.CAMERA),
                    Constanta.CAMERA_PERMISSION_CODE
                )

            }
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            Constanta.CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Helper.permissionGranted(this, "Give this application permission to access your camera")
                }
            }
            Constanta.LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Helper.askPermissionGranted(
                        this,
                        "Give this application permission to access your location"
                    )
                }
            }
            Constanta.STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Helper.permissionGranted(
                        this,
                        "Give this application permission to access your storage"
                    )
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun getUserToken() = token

    fun getStoryViewModel(): StoryListPagerViewModel {
        val viewModel: StoryListPagerViewModel by viewModels {
            ViewModelStoryFactory(
                this,
                bangkit.robbyyehezkiel.androidintermediate.data.api.ApiConfig.getApiService(),
                getUserToken()
            )
        }
        return viewModel
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameAuth, fragment)
            .commit()
    }

    fun routeToAuth() = startActivity(Intent(this, AuthenticateActivity::class.java))

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

}