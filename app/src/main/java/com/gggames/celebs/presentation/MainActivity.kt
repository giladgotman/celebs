package com.gggames.celebs.presentation

import android.app.AlertDialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import com.gggames.celebs.BuildConfig
import com.gggames.celebs.R
import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.core.di.getAppComponent
import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.presentation.common.MainActivityDelegate
import com.gggames.celebs.presentation.gameon.GameScreenContract.UiEvent.MainUiEvent
import com.gggames.celebs.utils.showErrorToast
import com.google.android.material.snackbar.Snackbar
import com.idagio.app.core.utils.share.Shareable
import com.idagio.app.core.utils.share.createDynamicLink
import com.idagio.app.core.utils.share.getPendingDeepLink
import com.idagio.app.core.utils.share.share
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var gameFlow: GameFlow

    @Inject
    lateinit var gamesRepository: GamesRepository

    @Inject
    lateinit var shareableFactory: Shareable.Factory

    private val disposables = CompositeDisposable()

    val events = PublishSubject.create<MainUiEvent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        getAppComponent(this).inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button_share.setOnClickListener {
            gamesRepository.currentGame?.let { game ->
                val uri = Uri.parse("https://gglab.page.link/joinGame/${game.id}")
                createDynamicLink(uri).subscribe({ shortUri ->
                    val shareable = shareableFactory.create(game.id, game.name, shortUri)
                    share(shareable)
                }, {
                    Timber.e(it, "error sharing link")
                    showErrorToast(this, getString(R.string.error_generic), Toast.LENGTH_LONG)
                })

            }
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
    }

    private fun createSnackbar(view: View) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
    }

    fun setShareVisible(visible: Boolean) {
        button_share.isVisible = visible
    }

    fun setTitle(title: String) {
        toolbar_title.text = title
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
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
                        gameFlow.logout()
                    }
                    .subscribe {}
                true
            }
            R.id.menu_about -> {
                showAbout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAbout() {
        val sb = StringBuilder()
        sb.append("This game was made with love")
        sb.append("\n\n\n")
        sb.append("Version: ${BuildConfig.VERSION_NAME}")
        val dialogClickListener = DialogInterface.OnClickListener { _, _ ->
        }
        val builder = AlertDialog.Builder(this)
        builder
            .setTitle("About")
            .setMessage(sb.toString())
            .setPositiveButton(getString(R.string.ok), dialogClickListener)
            .show()

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

