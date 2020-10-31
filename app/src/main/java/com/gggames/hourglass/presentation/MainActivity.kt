package com.gggames.hourglass.presentation

import android.app.AlertDialog
import android.content.DialogInterface
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
import com.gggames.hourglass.features.games.data.GameResult
import com.gggames.hourglass.features.games.data.GamesRepository
import com.gggames.hourglass.presentation.common.MainActivityDelegate
import com.gggames.hourglass.presentation.gameon.GameScreenContract.UiEvent.MainUiEvent
import com.gggames.hourglass.utils.showErrorToast
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
    lateinit var authenticator: Authenticator

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
            gamesRepository.getCurrentGame()
                .subscribe { gameResult->
                    if (gameResult is GameResult.Found) {
                        val uriBuilder = Uri.parse("https://gglab.page.link/joinGame/${gameResult.game.id}").buildUpon()
                        val uri = uriBuilder.appendQueryParameter("host", authenticator.me!!.name).build()
                        createDynamicLink(uri).subscribe({ shortUri ->
                            val shareable = shareableFactory.create(gameResult.game.id, gameResult.game.name, shortUri)
                            share(shareable)
                        }, {
                            Timber.e(it, "error sharing link")
                            showErrorToast(this, getString(R.string.error_generic), Toast.LENGTH_LONG)
                        })
                    }

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

    fun setShareVisible(visible: Boolean) {
        button_share.isVisible = visible
    }

    fun setTitle(title: String) {
        toolbar_title.text = title
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val item = menu.findItem(R.id.menu_switch_team)
        item.isVisible = false
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
