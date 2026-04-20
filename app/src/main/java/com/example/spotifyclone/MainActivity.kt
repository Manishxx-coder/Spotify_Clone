package com.example.spotifyclone

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat


data class Song(
    val title: String,
    val artist: String,
    val album: String,
    val rawResId: Int
)


class MainActivity : AppCompatActivity() {


    private val GREEN = 0xFF1DB954.toInt()
    private val BG_DARK = 0xFF121212.toInt()
    private val CARD_BG = 0xFF1E1E1E.toInt()
    private val WHITE = 0xFFFFFFFF.toInt()
    private val GREY = 0xFFB3B3B3.toInt()
    private val DIM_GREY = 0xFF535353.toInt()
    private val TOOLBAR_BG = 0xFF1A1A1A.toInt()

    // ── Login credentials (no Firebase) ──────
    private val VALID_EMAIL = "user@sangeet.com"
    private val VALID_PASSWORD = "hindi123"


    private val songs = listOf(
        Song("Tum Hi Ho",          "Arijit Singh",    "Aashiqui 2",           R.raw.tum_hi_ho),
        Song("Kesariya",           "Arijit Singh",    "Brahmastra",           R.raw.kesariya),
        Song("Raataan Lambiyan",   "Jubin Nautiyal",  "Shershaah",            R.raw.raataan_lambiya),
        Song("Tera Ban Jaunga",    "Akhil Sachdeva",  "Kabir Singh",          R.raw.tera_ban_jaunga),
        Song("Channa Mereya",      "Arijit Singh",    "Ae Dil Hai Mushkil",   R.raw.tum_hi_ho),
        Song("Tujhe Kitna Chahne", "Arijit Singh",    "Kabir Singh",          R.raw.tum_hi_ho),
        Song("Hawayein",           "Arijit Singh",    "Jab Harry Met Sejal",  R.raw.tum_hi_ho),
        Song("Ik Vaari Aa",        "Arijit Singh",    "Raazi",                R.raw.tum_hi_ho),
    )

    // ── Player state ─────────────────────────
    private var mediaPlayer: MediaPlayer? = null
    private var currentIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private var seekBar: SeekBar? = null
    private var tvCurrentTime: TextView? = null
    private var tvTotalTime: TextView? = null
    private var btnPlayPause: Button? = null
    private var tvPlayerTitle: TextView? = null
    private var tvPlayerArtist: TextView? = null
    private var tvPlayerAlbum: TextView? = null

    // ── Root container ────────────────────────
    private lateinit var rootContainer: FrameLayout

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            stopPlayer()
            showHomeScreen()
        }
    }

    // ── SeekBar updater ───────────────────────
    private val seekUpdater = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                seekBar?.progress = it.currentPosition
                tvCurrentTime?.text = formatTime(it.currentPosition)
            }
            handler.postDelayed(this, 500)
        }
    }

    // ─────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rootContainer = findViewById(R.id.rootContainer)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
        showLoginScreen()
    }

    // ══════════════════════════════════════════
    // SCREEN 1 — LOGIN
    // ══════════════════════════════════════════
    private fun showLoginScreen() {
        backPressedCallback.isEnabled = false
        rootContainer.removeAllViews()

        val scroll = ScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(BG_DARK)
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(32), dp(60), dp(32), dp(32))
        }

        // Logo icon (music note text used as placeholder)
        val logo = TextView(this).apply {
            text = "♫"
            textSize = 72f
            setTextColor(GREEN)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = dp(8) }
        }

        val appName = TextView(this).apply {
            text = "SangeetApp"
            textSize = 28f
            setTextColor(WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = rowParams(bottomMargin = 4)
        }

        val tagline = TextView(this).apply {
            text = "Hindi Gaano Ki Duniya"
            textSize = 14f
            setTextColor(GREY)
            gravity = Gravity.CENTER
            layoutParams = rowParams(bottomMargin = 40)
        }

        // Email field
        val tvEmailLabel = label("Email")
        val etEmail = editText("user@sangeet.com", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)

        // Password field
        val tvPassLabel = label("Password")
        val etPassword = editText("••••••••", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)

        // Error text
        val tvError = TextView(this).apply {
            text = ""
            textSize = 13f
            setTextColor(0xFFFF4444.toInt())
            gravity = Gravity.CENTER
            layoutParams = rowParams(bottomMargin = 8)
        }

        // Login button
        val btnLogin = Button(this).apply {
            text = "LOGIN"
            textSize = 16f
            setTextColor(0xFF000000.toInt())
            setBackgroundColor(GREEN)
            layoutParams = rowParams(height = dp(52), bottomMargin = 16)
            setPadding(0, 0, 0, 0)
        }

        // Hint text
        val tvHint = TextView(this).apply {
            text = "Demo: user@sangeet.com  /  hindi123"
            textSize = 12f
            setTextColor(DIM_GREY)
            gravity = Gravity.CENTER
            layoutParams = rowParams(bottomMargin = 16)
        }

        val tvSignUp = TextView(this).apply {
            text = "Account nahi hai? Sign Up karein"
            textSize = 14f
            setTextColor(GREEN)
            gravity = Gravity.CENTER
            layoutParams = rowParams()
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            when {
                email.isEmpty() -> tvError.text = "Email daalen"
                pass.isEmpty() -> tvError.text = "Password daalen"
                email == VALID_EMAIL && pass == VALID_PASSWORD -> {
                    tvError.text = ""
                    showHomeScreen()
                }
                else -> tvError.text = "Galat email ya password"
            }
        }

        tvSignUp.setOnClickListener {
            Toast.makeText(this, "Use: user@sangeet.com / hindi123", Toast.LENGTH_LONG).show()
        }

        container.addView(logo)
        container.addView(appName)
        container.addView(tagline)
        container.addView(tvEmailLabel)
        container.addView(etEmail)
        container.addView(tvPassLabel)
        container.addView(etPassword)
        container.addView(tvError)
        container.addView(btnLogin)
        container.addView(tvHint)
        container.addView(tvSignUp)

        scroll.addView(container)
        rootContainer.addView(scroll)
    }

    // ══════════════════════════════════════════
    // SCREEN 2 — HOME (Song List)
    // ══════════════════════════════════════════
    private fun showHomeScreen() {
        backPressedCallback.isEnabled = false
        rootContainer.removeAllViews()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(BG_DARK)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // ── Toolbar ──────────────────────────
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(TOOLBAR_BG)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val tvToolbarTitle = TextView(this).apply {
            text = "SangeetApp 🎵"
            textSize = 22f
            setTextColor(WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        toolbar.addView(tvToolbarTitle)
        root.addView(toolbar)

        // ── Section Title ─────────────────────
        val tvSection = TextView(this).apply {
            text = "Aaj ke Hit Gaane"
            textSize = 18f
            setTextColor(WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(dp(16), dp(16), dp(16), dp(8))
        }
        root.addView(tvSection)

        // ── Scrollable Song List ──────────────
        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), 0, dp(8), dp(8))
        }

        songs.forEachIndexed { index, song ->
            listContainer.addView(buildSongCard(song, index))
        }

        scroll.addView(listContainer)
        root.addView(scroll)
        rootContainer.addView(root)
    }

    private fun buildSongCard(song: Song, index: Int): View {
        val card = CardView(this).apply {
            radius = dp(12).toFloat()
            setCardBackgroundColor(CARD_BG)
            cardElevation = 0f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).also {
                it.topMargin = dp(6)
                it.bottomMargin = dp(6)
            }
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        // Album art placeholder circle
        val albumCircle = TextView(this).apply {
            text = "♪"
            textSize = 22f
            setTextColor(GREEN)
            gravity = Gravity.CENTER
            setBackgroundColor(0xFF2A2A2A.toInt())
            layoutParams = LinearLayout.LayoutParams(dp(60), dp(60)).also {
                it.marginEnd = dp(12)
            }
        }

        val textCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvTitle = TextView(this).apply {
            text = song.title
            textSize = 16f
            setTextColor(WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            maxLines = 1
        }
        val tvArtist = TextView(this).apply {
            text = song.artist
            textSize = 13f
            setTextColor(GREY)
            maxLines = 1
        }
        val tvAlbum = TextView(this).apply {
            text = song.album
            textSize = 12f
            setTextColor(DIM_GREY)
            maxLines = 1
        }

        val playIcon = TextView(this).apply {
            text = "▶"
            textSize = 20f
            setTextColor(GREEN)
            setPadding(dp(8), 0, 0, 0)
        }

        textCol.addView(tvTitle)
        textCol.addView(tvArtist)
        textCol.addView(tvAlbum)

        row.addView(albumCircle)
        row.addView(textCol)
        row.addView(playIcon)

        card.addView(row)

        card.setOnClickListener {
            currentIndex = index
            showPlayerScreen()
        }

        return card
    }

    // ══════════════════════════════════════════
    // SCREEN 3 — PLAYER
    // ══════════════════════════════════════════
    private fun showPlayerScreen() {
        backPressedCallback.isEnabled = true
        rootContainer.removeAllViews()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(BG_DARK)
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // ── Top bar ───────────────────────────
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = dp(24) }
        }

        val btnBack = Button(this).apply {
            text = "←"
            textSize = 20f
            setTextColor(WHITE)
            setBackgroundColor(0x00000000)
            layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
        }

        val tvNowPlaying = TextView(this).apply {
            text = "Ab Chal Raha Hai"
            textSize = 13f
            setTextColor(GREY)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        topBar.addView(btnBack)
        topBar.addView(tvNowPlaying)
        topBar.addView(Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
        })
        root.addView(topBar)

        // ── Album art (big circle) ─────────────
        val albumArt = TextView(this).apply {
            text = "♫"
            textSize = 80f
            setTextColor(GREEN)
            gravity = Gravity.CENTER
            setBackgroundColor(0xFF2A2A2A.toInt())
            layoutParams = LinearLayout.LayoutParams(dp(260), dp(260)).also {
                it.gravity = Gravity.CENTER_HORIZONTAL
                it.bottomMargin = dp(28)
            }
        }
        root.addView(albumArt)

        // ── Song info ─────────────────────────
        tvPlayerTitle = TextView(this).apply {
            textSize = 22f
            setTextColor(WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = rowParams(bottomMargin = 4)
        }
        tvPlayerArtist = TextView(this).apply {
            textSize = 16f
            setTextColor(GREY)
            gravity = Gravity.CENTER
            layoutParams = rowParams(bottomMargin = 4)
        }
        tvPlayerAlbum = TextView(this).apply {
            textSize = 13f
            setTextColor(DIM_GREY)
            gravity = Gravity.CENTER
            layoutParams = rowParams(bottomMargin = 28)
        }
        root.addView(tvPlayerTitle)
        root.addView(tvPlayerArtist)
        root.addView(tvPlayerAlbum)

        // ── SeekBar ───────────────────────────
        seekBar = SeekBar(this).apply {
            progressTintList = android.content.res.ColorStateList.valueOf(GREEN)
            thumbTintList = android.content.res.ColorStateList.valueOf(GREEN)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = dp(4) }
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) mediaPlayer?.seekTo(progress)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }
        root.addView(seekBar)

        // ── Time row ──────────────────────────
        val timeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = dp(28) }
        }
        tvCurrentTime = TextView(this).apply {
            text = "0:00"
            textSize = 12f
            setTextColor(GREY)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        tvTotalTime = TextView(this).apply {
            text = "0:00"
            textSize = 12f
            setTextColor(GREY)
        }
        timeRow.addView(tvCurrentTime)
        timeRow.addView(tvTotalTime)
        root.addView(timeRow)

        // ── Controls ──────────────────────────
        val controlRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).also { it.gravity = Gravity.CENTER_HORIZONTAL }
        }

        val btnPrev = controlButton("⏮", 56)
        btnPlayPause = controlButton("⏸", 72, bgColor = GREEN, textColor = 0xFF000000.toInt())
        val btnNext = controlButton("⏭", 56)

        btnPrev.layoutParams = (btnPrev.layoutParams as LinearLayout.LayoutParams).also {
            it.marginEnd = dp(24)
        }
        btnPlayPause!!.layoutParams = (btnPlayPause!!.layoutParams as LinearLayout.LayoutParams).also {
            it.marginEnd = dp(24)
        }

        controlRow.addView(btnPrev)
        controlRow.addView(btnPlayPause)
        controlRow.addView(btnNext)
        root.addView(controlRow)

        // ── Button listeners ──────────────────
        btnBack.setOnClickListener {
            stopPlayer()
            showHomeScreen()
        }
        btnPrev.setOnClickListener {
            currentIndex = if (currentIndex - 1 < 0) songs.size - 1 else currentIndex - 1
            loadSong()
        }
        btnNext.setOnClickListener {
            currentIndex = (currentIndex + 1) % songs.size
            loadSong()
        }
        btnPlayPause!!.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) pauseSong() else resumeSong()
        }

        rootContainer.addView(root)
        loadSong()
    }

    // ══════════════════════════════════════════
    // MEDIA PLAYER HELPERS
    // ══════════════════════════════════════════
    private fun loadSong() {
        val song = songs[currentIndex]
        tvPlayerTitle?.text = song.title
        tvPlayerArtist?.text = song.artist
        tvPlayerAlbum?.text = song.album

        stopPlayer()

        mediaPlayer = MediaPlayer.create(this, song.rawResId)
        mediaPlayer?.setOnCompletionListener {
            currentIndex = (currentIndex + 1) % songs.size
            loadSong()
        }

        val duration = mediaPlayer?.duration ?: 0
        seekBar?.max = duration
        seekBar?.progress = 0
        tvTotalTime?.text = formatTime(duration)
        tvCurrentTime?.text = "0:00"

        mediaPlayer?.start()
        btnPlayPause?.text = "⏸"
        handler.post(seekUpdater)
    }

    private fun pauseSong() {
        mediaPlayer?.pause()
        btnPlayPause?.text = "▶"
        handler.removeCallbacks(seekUpdater)
    }

    private fun resumeSong() {
        mediaPlayer?.start()
        btnPlayPause?.text = "⏸"
        handler.post(seekUpdater)
    }

    private fun stopPlayer() {
        handler.removeCallbacks(seekUpdater)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun formatTime(ms: Int): String {
        val sec = ms / 1000
        return "%d:%02d".format(sec / 60, sec % 60)
    }

    // ══════════════════════════════════════════
    // UI BUILDER HELPERS
    // ══════════════════════════════════════════
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun rowParams(
        width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        bottomMargin: Int = 0
    ) = LinearLayout.LayoutParams(width, height).also { it.bottomMargin = dp(bottomMargin) }

    private fun label(text: String) = TextView(this).apply {
        this.text = text
        textSize = 13f
        setTextColor(GREY)
        layoutParams = rowParams(bottomMargin = 4)
    }

    private fun editText(hint: String, inputType: Int) = EditText(this).apply {
        this.hint = hint
        this.inputType = inputType
        textSize = 15f
        setTextColor(WHITE)
        setHintTextColor(DIM_GREY)
        setBackgroundColor(CARD_BG)
        setPadding(dp(12), dp(12), dp(12), dp(12))
        layoutParams = rowParams(bottomMargin = 16)
    }

    private fun controlButton(
        label: String,
        sizeDp: Int,
        bgColor: Int = 0x00000000,
        textColor: Int = WHITE
    ) = Button(this).apply {
        text = label
        textSize = if (sizeDp > 60) 26f else 22f
        setTextColor(textColor)
        setBackgroundColor(bgColor)
        layoutParams = LinearLayout.LayoutParams(dp(sizeDp), dp(sizeDp))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayer()
    }
}