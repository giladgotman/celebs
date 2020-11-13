package com.gggames.hourglass.utils.transitions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.transition.Fade
import android.transition.Fade.IN
import android.transition.Fade.OUT
import android.transition.Slide
import android.transition.Transition
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.GravityCompat
import com.gggames.hourglass.R
import java.util.*

/**
 * Helper class for creating content transitions used with [android.app.ActivityOptions].
 */
object TransitionHelper {

    /**
     * Create the transition participants required during a activity transition while
     * avoiding glitches with the system UI.
     *
     * @param activity The activity used as start for the transition.
     * @param includeStatusBar If false, the status bar will not be added as the transition
     * participant.
     * @return All transition participants.
     */



    fun createSafeTransitionParticipants(
        activity: Activity,
        includeStatusBar: Boolean,
        otherParticipants: ArrayList<Pair<View, String>?>? = null
    ): Array<Pair<View, String>> {
        // Avoid system UI glitches as described here:
        // https://plus.google.com/+AlexLockwood/posts/RPtwZ5nNebb
        val decor = activity.window.decorView
        var statusBar: View? = null
        if (includeStatusBar) {
            statusBar = decor.findViewById(android.R.id.statusBarBackground)
        }
        val navBar = decor.findViewById<View?>(android.R.id.navigationBarBackground)

        // Create pair of transition participants.
        val participants = ArrayList<Pair<View, String>>(3)
        statusBar?.let {
            participants.add(Pair(it, it.transitionName))
        }
        navBar?.let {
            participants.add(Pair(it, it.transitionName))
        }
        // only add transition participants if there's at least one none-null element
        otherParticipants?.let {
            participants.addAll(it.filterNotNull())
        }
        return participants.toTypedArray()
    }

    fun buildSlideTransition(context: Context, gravity: Int): Transition {
        val slideTransition = Slide(gravity)
        slideTransition.duration =
            context.resources.getInteger(R.integer.activity_transition_duration).toLong()
        slideTransition.excludeTarget(android.R.id.statusBarBackground, true)
        slideTransition.excludeTarget(android.R.id.navigationBarBackground, true)
        val interpolator =
            AnimationUtils.loadInterpolator(context, android.R.interpolator.fast_out_slow_in)
        slideTransition.interpolator = interpolator
        return slideTransition
    }

    fun buildFadeTransition(context: Context, fadingMode: Int): Transition {
        val fadeTransition = Fade(fadingMode)
        fadeTransition.duration =
            context.resources.getInteger(R.integer.activity_transition_duration).toLong()
        fadeTransition.excludeTarget(android.R.id.statusBarBackground, true)
        fadeTransition.excludeTarget(android.R.id.navigationBarBackground, true)
        val interpolator = AnimationUtils.loadInterpolator(context, android.R.interpolator.linear)
        fadeTransition.interpolator = interpolator!!
        return fadeTransition
    }
}

fun Activity.startActivityAnimated(
    intent: Intent,
    transition: Int = TRANSITION_SLIDE,
    includeStatusBar: Boolean = false
) {
    val pairs =
        TransitionHelper.createSafeTransitionParticipants(
            this,
            includeStatusBar
        )
    val transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this, *pairs)
    intent.putExtra(ACTIVITY_TRANSITION_KEY, transition)
    startActivity(intent, transitionActivityOptions.toBundle())
}

fun Activity.setupWindowSlideAnimations() {
    val compatEndGravity =
        GravityCompat.getAbsoluteGravity(GravityCompat.END, resources.configuration.layoutDirection)
    val slideFromEndTransition =
        TransitionHelper.buildSlideTransition(
            this,
            compatEndGravity
        )
    window.enterTransition = slideFromEndTransition
}

fun Activity.setupWindowFadeAnimations() {
    val fadeIndTransition =
        TransitionHelper.buildFadeTransition(this, IN)
    val fadeOutTransition =
        TransitionHelper.buildFadeTransition(this, OUT)
    window.enterTransition = fadeIndTransition
    window.returnTransition = fadeOutTransition
}

fun Activity.setupWindowAnimations(intent: Intent?) {
    when (intent?.getIntExtra(ACTIVITY_TRANSITION_KEY, NO_TRANSITION)) {
        TRANSITION_SLIDE -> setupWindowSlideAnimations()
        TRANSITION_FADE -> setupWindowFadeAnimations()
        else -> {
            // do nothing
        }
    }
}

val ACTIVITY_TRANSITION_KEY = "ACTIVITY_TRANSITION"
val NO_TRANSITION = -1
val TRANSITION_SLIDE = 1
val TRANSITION_FADE = 2
