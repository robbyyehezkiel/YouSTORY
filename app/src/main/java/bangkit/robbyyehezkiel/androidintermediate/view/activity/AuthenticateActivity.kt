package bangkit.robbyyehezkiel.androidintermediate.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import bangkit.robbyyehezkiel.androidintermediate.R
import bangkit.robbyyehezkiel.androidintermediate.databinding.ActivityAuthenticateBinding
import bangkit.robbyyehezkiel.androidintermediate.view.fragment.LoginFragment

class AuthenticateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticateBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.frameAuth, LoginFragment.newInstance())
                .commit()
        }
    }

    fun routeToMainActivity() {
        startActivity(Intent(this@AuthenticateActivity, MainActivity::class.java))
    }
}