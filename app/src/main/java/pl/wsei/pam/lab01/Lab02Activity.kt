package pl.wsei.pam.lab01

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.Debug.getState
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import lab03.Lab03Activity
import lab03.MemoryBoardView

class Lab02Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lab02)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }
    fun onBoardClick(view: View) {
        val button = view as Button
        val tokens: List<String>? = button.text?.split(" ")
        val rows = tokens?.get(0)?.toIntOrNull() ?: 0
        val columns = tokens?.get(2)?.toIntOrNull() ?: 0
        val size = intArrayOf(rows, columns)




        val intent = Intent(this, Lab03Activity::class.java)
        Toast.makeText(this, "Selected: ${button.text}", Toast.LENGTH_SHORT).show()
        intent.putExtra("size", size)
        startActivity(intent)
    }
}