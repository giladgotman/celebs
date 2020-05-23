package com.gggames.celebs.presentation

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.gggames.celebs.R
import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.core.di.getAppComponent
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var gameFlow: GameFlow

    override fun onCreate(savedInstanceState: Bundle?) {
        getAppComponent(this).inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
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
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_logout -> {
                finish()
                gameFlow.logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
