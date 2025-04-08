package pl.wsei.pam.lab01

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import lab06.MainActivity

class Main2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }

    }
    fun onClickMainBtnRunLab01(v: View){
        val intent = Intent(this, Lab01Activity::class.java)
        startActivity(intent)
    }
    fun onClickMainBtnRunLab02(v: View){
        val intent = Intent(this, Lab02Activity::class.java)
        startActivity(intent)
    }
    fun onClickMainBtnRunLab06(v: View){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}