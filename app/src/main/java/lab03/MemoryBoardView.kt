package lab03
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.Toast
import androidx.gridlayout.widget.GridLayout
import pl.wsei.pam.lab01.R
import java.util.Random
import java.util.Stack

class MemoryBoardView(
    private val gridLayout: GridLayout,
    private val cols: Int,
    private val rows: Int,
    iconList: List<Int>? = null
) {

    private val deckResource: Int = R.drawable.gradient_1
    private var onGameChangeStateListener: (MemoryGameEvent) -> Unit = { _ -> }
    private val tiles: MutableMap<String, Tile> = mutableMapOf()
    private val matchedKeys: MutableSet<String> = mutableSetOf()

    private val icons: List<Int> = listOf(
        R.drawable.baseline_rocket_launch_24,
        R.drawable.icon2,
        R.drawable.icon3,
        R.drawable.icon4,
        R.drawable.icon5,
        R.drawable.icon6,
        R.drawable.icon7,
        R.drawable.icon8,
        R.drawable.icon9,
        R.drawable.icon10,
        R.drawable.icon11,
        R.drawable.icon12,
        R.drawable.icon13,
        R.drawable.icon14,
        R.drawable.icon15,
        R.drawable.icon16,
    )

    private val shuffledIcons: MutableList<Int> = iconList?.toMutableList() ?: mutableListOf<Int>().also {
        it.addAll(icons.subList(0, cols * rows / 2))
        it.addAll(icons.subList(0, cols * rows / 2))
        it.shuffle()
    }

    init {
        for (row in 0 until gridLayout.rowCount) {
            for (col in 0 until gridLayout.columnCount) {
                val btn = ImageButton(gridLayout.context).also {
                    it.tag = "$row $col"
                    val layoutParams = GridLayout.LayoutParams()
                    it.setImageResource(R.drawable.baseline_rocket_launch_24)
                    layoutParams.width = 0
                    layoutParams.height = 0
                    layoutParams.setGravity(Gravity.CENTER)
                    layoutParams.columnSpec = GridLayout.spec(col, 1, 1f)
                    layoutParams.rowSpec = GridLayout.spec(row, 1, 1f)
                    it.layoutParams = layoutParams
                    gridLayout.addView(it)
                }
                addTile(btn, shuffledIcons.removeAt(0))
            }
        }
    }

    private fun onClickTile(v: View) {
        val tile = tiles[v.tag]
        if (tile == null || tile.revealed || matchedKeys.contains(v.tag)) return
        matchedPair.push(tile)
        val matchResult = logic.process { tile.tileResource }
        onGameChangeStateListener(MemoryGameEvent(matchedPair.toList(), matchResult))
        if (matchResult == GameStates.Match) {
            matchedPair.forEach { matchedKeys.add(it.button.tag.toString()) }
        }
        if (matchResult != GameStates.Matching) {
            matchedPair.clear()
        }
    }

    private val matchedPair: Stack<Tile> = Stack()
    private val logic: MemoryGameLogic = MemoryGameLogic(cols * rows / 2)

    fun setOnGameChangeListener(listener: (event: MemoryGameEvent) -> Unit) {
        onGameChangeStateListener = listener
    }


    private fun addTile(button: ImageButton, resourceImage: Int) {
        button.setOnClickListener(::onClickTile)
        val tile = Tile(button, resourceImage, deckResource)
        tiles[button.tag.toString()] = tile
    }

    fun getState(): IntArray {
        return tiles.entries
            .sortedBy { it.key }
            .flatMap { (_, tile) ->
                listOf(tile.tileResource, if (tile.revealed) 1 else 0)
            }.toIntArray()
    }

    fun setState(state: IntArray) {
        val keys = tiles.keys.sorted()
        var index = 0
        for (key in keys) {
            val tile = tiles[key]
            val resource = state[index]
            val revealed = state[index + 1] == 1
            if (tile != null) {
                tile.tileResource = resource
                tile.revealed = revealed
            }
            index += 2
        }
    }

    fun getIconList(): List<Int> =
        tiles.toSortedMap().values.map { it.tileResource }

    fun getMatchedKeys(): List<String> = matchedKeys.toList()

    fun restoreMatchedTiles(keys: List<String>) {
        keys.forEach { key ->
            matchedKeys.add(key)
            tiles[key]?.revealed = true
        }
    }
    fun animatePairedButton(button: ImageButton, action: Runnable) {
        val set = AnimatorSet()

        val rotation = ObjectAnimator.ofFloat(button, "rotation", 3080f)
        val scalingX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 3f)
        val scalingY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 2f)
        val fade = ObjectAnimator.ofFloat(button, "alpha", 1f, 0f)

        set.startDelay = 300
        set.duration = 1000
        set.interpolator = DecelerateInterpolator()
        set.playTogether(rotation, scalingX, scalingY, fade)

        set.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}

            override fun onAnimationEnd(animator: Animator) {
                // Resetowanie karty
                button.rotation = 0f
                button.scaleX = 1f
                button.scaleY = 1f
                button.alpha = 1f
                button.visibility = View.VISIBLE // Pokaż ponownie kartę
                action.run()
            }

            override fun onAnimationCancel(animator: Animator) {}

            override fun onAnimationRepeat(animator: Animator) {}
        })

        set.start()
    }
    fun animateWrongPairedButton(button: ImageButton, action: Runnable ) {
        val set = AnimatorSet()
        val random = Random()
        button.pivotX = random.nextFloat() * 200f

        val rotationToRight = ObjectAnimator.ofFloat(button, "rotation", 4f)
        val rotationToLeft = ObjectAnimator.ofFloat(button, "rotation", -4f)
        val rotationToZero = ObjectAnimator.ofFloat(button, "rotation", 4f)
        set.startDelay = 200
        set.duration = 40
        set.interpolator = DecelerateInterpolator()
        set.playSequentially(rotationToRight,rotationToLeft, rotationToZero)
        set.addListener(object: Animator.AnimatorListener {

            override fun onAnimationStart(animator: Animator) {
            }

            override fun onAnimationEnd(animator: Animator) {
                button.rotation = 0f
                action.run();
            }

            override fun onAnimationCancel(animator: Animator) {
            }

            override fun onAnimationRepeat(animator: Animator) {
            }
        })
        set.start()
    }
}
