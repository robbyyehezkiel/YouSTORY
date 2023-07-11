package bangkit.robbyyehezkiel.androidintermediate.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import bangkit.robbyyehezkiel.androidintermediate.R

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        setContentView(R.layout.activity_splash)

        val loginHover = findViewById<Button>(R.id.btn_action)

        loginHover.setOnClickListener {
            startActivity(Intent(this@SplashActivity, AuthenticateActivity::class.java))
        }
    }
}