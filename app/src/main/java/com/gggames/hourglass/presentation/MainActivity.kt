package com.gggames.hourglass.presentation

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import com.gggames.hourglass.BuildConfig
import com.gggames.hourglass.R
import com.gggames.hourglass.core.Authenticator
import com.gggames.hourglass.core.di.getAppComponent
import com.gggames.hourglass.features.games.data.GamesRepository
import com.gggames.hourglass.presentation.common.MainActivityDelegate
import com.gggames.hourglass.presentation.feedback.FeedbackActivity
import com.gggames.hourglass.presentation.gameon.GameScreenContract.UiEvent.MainUiEvent
import com.gggames.hourglass.presentation.instructions.InstructionsDialogFragment
import com.gggames.hourglass.utils.showErrorToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.idagio.app.core.utils.share.Shareable
import com.idagio.app.core.utils.share.createDynamicLink
import com.idagio.app.core.utils.share.getPendingDeepLink
import com.idagio.app.core.utils.share.share
import io.reactivex.Completable
import io.reactivex.Completable.complete
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var authenticator: Authenticator

    @Inject
    lateinit var gamesRepository: GamesRepository

    @Inject
    lateinit var shareableFactory: Shareable.Factory

    private val disposables = CompositeDisposable()

    private var showUpdateVersionPopup = true

    val events = PublishSubject.create<MainUiEvent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        getAppComponent(this).inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_share.setOnClickListener {
            shareGame()
        }
        setSupportActionBar(toolbar)

        getPendingDeepLink().subscribe({
            Timber.w("found uri from deeplink: $it")
            // TODO: 23.05.20 handle deeplink when needed. for example navigate to a game after install
        }, {
            Timber.e(it, "error sharing link")
        }).let {
            disposables.add(it)
        }

        setRemoteConfigSettings(60 * 15)
    }

    override fun onResume() {
        super.onResume()
        checkVersionUpdate()
    }

    private fun checkVersionUpdate() {
        fetchRemoteConfig().andThen {
            val minimumVersionCode = Firebase.remoteConfig.getLong("version_code_minimum")
            val showMustUpdate = minimumVersionCode > BuildConfig.VERSION_CODE
            Timber.v("ccc minimumVersion: $minimumVersionCode")
            if (showMustUpdate) {
                showNewVersionMustUpdateView()
            }
            val versionCodeUpdate = Firebase.remoteConfig.getLong("version_code_update")
            val showCanUpdate = versionCodeUpdate > BuildConfig.VERSION_CODE
            Timber.v("ccc versionCodeUpdate: $versionCodeUpdate")
            if (!showMustUpdate && showCanUpdate && showUpdateVersionPopup) {
                showNewVersionAvailableView()
            }
            Firebase.remoteConfig.getLong("version_code_latest").let { latestVersionCode ->
                Timber.v("ccc latestVersionCode: $latestVersionCode")
            }

            complete()
        }.subscribe {
        }.let { disposables.add(it) }
    }

    fun shareGame() {
        try {
            gamesRepository.getCurrentGame().toObservable().take(1)
                .subscribe { game ->
                    val uriBuilder = Uri.parse("https://gglab.page.link/joinGame/${game.id}").buildUpon()
                    val uri = uriBuilder.appendQueryParameter("host", authenticator.me!!.name).build()
                    createDynamicLink(uri).subscribe({ shortUri ->
                        val shareable = shareableFactory.create(game.id, game.name, shortUri)
                        share(shareable)
                    }, {
                        Timber.e(it, "error sharing link")
                        showErrorToast(this, getString(R.string.error_generic), Toast.LENGTH_LONG)
                    })
                }.let { disposables.add(it) }
        } catch (e: Exception) {
            Timber.e(e, "Exception while checking deep link")
        }

    }

    fun setShareVisible(visible: Boolean) {
        button_share.isVisible = visible
    }

    fun onShare() {
        shareGame()
    }

    fun setTitle(title: String) {
        toolbar_title.text = title
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val itemSwitchTeam = menu.findItem(R.id.menu_switch_team)
        val itemEndTurn = menu.findItem(R.id.menu_end_turn)
        itemSwitchTeam.isVisible = false
        itemEndTurn.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                val currentFragment = getDelegateFragment()
                val onLogoutAction = currentFragment?.onLogout() ?: Completable.complete()
                onLogoutAction
                    .doAfterTerminate {
                        finish()
                        authenticator.logout()
                    }
                    .subscribe {}
                true
            }
            R.id.menu_about -> {
                showAbout()
                true
            }
            R.id.menu_switch_team -> {
                // handled in gameOnFragment
                false
            }

            R.id.menu_help -> {
                InstructionsDialogFragment().show(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setRemoteConfigSettings(fetchInterval: Long) {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = fetchInterval
        }
        Firebase.remoteConfig.setConfigSettingsAsync(configSettings)

    }

    private fun fetchRemoteConfig(): Completable {
        return Completable.fromCallable {
            Firebase.remoteConfig.fetchAndActivate()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Timber.d("Config params updated: ${task.result}")
                        complete()
                    } else {
                        error("Fetch config failed")
                    }
                }
        }
    }

    private fun showNewVersionAvailableView() {
        Timber.w("ccc showNewverisionView")
        val goToPlayStore = DialogInterface.OnClickListener { dialog, _ ->
            showUpdateVersionPopup = false
            dialog.dismiss()
            val url = "http://play.google.com/store/apps/details?id=com.gggames.hourglass"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }

        val dialogClickListener = DialogInterface.OnClickListener { _, _ ->
            showUpdateVersionPopup = false
        }
        val builder = MaterialAlertDialogBuilder(this, R.style.celebs_MaterialAlertDialog)
        builder
            .setTitle(getString(R.string.new_version_available_title))
            .setMessage(getString(R.string.new_version_available_body))
            .setPositiveButton(getString(R.string.new_version_available_positive), goToPlayStore)
            .setNegativeButton(getString(R.string.new_version_available_negative), dialogClickListener)
            .show()
    }

    private fun showNewVersionMustUpdateView() {
        Timber.w("ccc showNewVersionMustUpdateView")
        val goToPlayStore = DialogInterface.OnClickListener { dialog, _ ->
            val url = "http://play.google.com/store/apps/details?id=com.gggames.hourglass"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }

        val builder = MaterialAlertDialogBuilder(this, R.style.celebs_MaterialAlertDialog)
        builder
            .setTitle(getString(R.string.new_version_must_title))
            .setMessage(getString(R.string.new_version_must_body))
            .setPositiveButton(getString(R.string.new_version_must_positive), goToPlayStore)
            .setOnDismissListener {}
            .setCancelable(false)
            .show()
    }


    private fun showAbout() {
        val sb = StringBuilder()
        sb.append(getString(R.string.about_dialog_message_first_part))
        sb.append("\n\n")
        sb.append(getString(R.string.about_dialog_version_part, BuildConfig.VERSION_NAME))
        val dialogClickListener = DialogInterface.OnClickListener { _, _ ->
        }
        val feedbackClickListener = DialogInterface.OnClickListener { _, _ ->

            openWeb("https://i6j1aat88q8.typeform.com/to/OxJapaQX")
        }
        val builder = MaterialAlertDialogBuilder(this, R.style.celebs_MaterialAlertDialog)
        builder
            .setTitle(getString(R.string.about_dialog_title))
            .setMessage(sb.toString())
            .setPositiveButton(getString(R.string.about_dialog_later), dialogClickListener)
            .setNegativeButton(getString(R.string.about_dialog_send_feedback), feedbackClickListener)
            .show()
    }

    private fun openWeb(url: String) {
        FeedbackActivity.start(this)
    }

    override fun onBackPressed() {
        val currentFragment = getDelegateFragment()
        currentFragment?.onBackPressed()?.let {
            if (!it) {
                super.onBackPressed()
            }
        } ?: super.onBackPressed()
    }

    private fun getDelegateFragment(): MainActivityDelegate? {
        val fragment =
            this.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        return fragment?.childFragmentManager?.fragments?.get(0) as? MainActivityDelegate
    }

    override fun onDestroy() {
        Timber.w("onDestroyed")
        super.onDestroy()
        disposables.clear()
    }
}
