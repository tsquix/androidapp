package lab03

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.gridlayout.widget.GridLayout
import pl.wsei.pam.lab01.R
import java.util.Timer
import kotlin.concurrent.schedule

class Lab03Activity : AppCompatActivity() {

    private lateinit var mBoard: GridLayout
    private lateinit var mBoardModel: MemoryBoardView
    lateinit var completionPlayer: MediaPlayer
    lateinit var negativePlayer: MediaPlayer
    private var isSound: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lab03)
        mBoard = findViewById(R.id.gridLayoutId)
        ViewCompat.setOnApplyWindowInsetsListener(mBoard) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mBoard.setPadding(0, 48, 0, 0) // 48px to ok. 24dp

        val size = intent.getIntArrayExtra("size") ?: intArrayOf(3, 3)

        Toast.makeText(this, "columns: ${size[0]} rows: ${size[1]}", Toast.LENGTH_SHORT).show()

        mBoard.columnCount = size[0]
        mBoard.rowCount = size[1]

        val savedState = savedInstanceState?.getIntArray("game_state")
        val savedIcons = savedInstanceState?.getIntegerArrayList("icon_list")
        val savedMatched = savedInstanceState?.getStringArrayList("matched_keys")

        mBoardModel = if (savedIcons != null) {
            MemoryBoardView(mBoard, size[0], size[1], savedIcons)
        } else {
            MemoryBoardView(mBoard, size[0], size[1])
        }

        if (savedState != null) {
            mBoardModel.setState(savedState)
        }
        if (savedMatched != null) {
            mBoardModel.restoreMatchedTiles(savedMatched)
        }

        mBoardModel.setOnGameChangeListener { e ->
            when (e.state) {
                GameStates.Matching -> {
                    // Odkryj pierwszy kafelek – brak animacji
                    e.tiles.forEach { it.revealed = true }
                }

                GameStates.Match -> {
                    // Odkryj drugi kafelek
                    e.tiles.forEach { it.revealed = true }

                    // Poczekaj chwilę i wykonaj animację pary
                    if (isSound) {
                        if (completionPlayer.isPlaying) {
                            completionPlayer.pause()
                            completionPlayer.seekTo(0)
                        }
                        completionPlayer.start()
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        e.tiles.forEach { tile ->
                            mBoardModel.animatePairedButton(tile.button) {
                                tile.button.isEnabled = false
                            }
                        }
                    }, 500)
                }

                GameStates.NoMatch -> {
                    // Odkryj oba na chwilę
                    e.tiles.forEach { it.revealed = true }
                    if (isSound) {
                        negativePlayer.start()
                    }
                    // Animacja "shake" dla obu, a potem zakrycie
                    Handler(Looper.getMainLooper()).postDelayed({
                        e.tiles.forEach { tile ->
                            mBoardModel.animateWrongPairedButton(tile.button) {
                                tile.revealed = false
                            }
                        }
                    }, 500)
                }

                GameStates.Finished -> {
                    e.tiles.forEach { it.revealed = true }
                    if (isSound) {
                        if (completionPlayer.isPlaying) {
                            completionPlayer.pause()
                            completionPlayer.seekTo(0)
                        }
                        completionPlayer.start()
                    }
                    e.tiles.forEach { tile ->
                        mBoardModel.animatePairedButton(tile.button) {
                            tile.button.isEnabled = false
                        }
                    }
                    Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray("game_state", mBoardModel.getState())
        outState.putIntegerArrayList("icon_list", ArrayList(mBoardModel.getIconList()))
        outState.putStringArrayList("matched_keys", ArrayList(mBoardModel.getMatchedKeys()))
    }

    override protected fun onResume() {
        super.onResume()
        completionPlayer = MediaPlayer.create(applicationContext, R.raw.completion)
        negativePlayer = MediaPlayer.create(applicationContext, R.raw.negative_guitar)
    }

    override protected fun onPause() {
        super.onPause();
        completionPlayer.release()
        negativePlayer.release()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //return super.onCreateOptionsMenu(menu)
        val inflater: MenuInflater = getMenuInflater()
        inflater.inflate(R.menu.board_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //return super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.board_activity_sound -> {
                isSound = !isSound

                if (isSound) {
                    item.setIcon(R.drawable.volume_up)
                    Toast.makeText(this, "Sound turned on", Toast.LENGTH_SHORT).show()
                } else {
                    item.setIcon(R.drawable.volume_off)
                    Toast.makeText(this, "Sound turned off", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return true
    }
}