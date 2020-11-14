package com.gggames.hourglass.presentation.instructions

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.gggames.hourglass.R
import com.gggames.hourglass.presentation.onboarding.WelcomePagerCarouselAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_instructions.*

class InstructionsDialogFragment : BottomSheetDialogFragment() {

    fun show(activity: AppCompatActivity) {
        show(activity.supportFragmentManager, this.javaClass.simpleName)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_instructions, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeCarouselViewPager()
    }

    private fun initializeCarouselViewPager() {
        val carouselItems = listOf(
            WelcomePagerCarouselAdapter.CarouselItem(R.layout.instructions_section1_layout),
            WelcomePagerCarouselAdapter.CarouselItem(R.layout.instructions_section2_layout),
            WelcomePagerCarouselAdapter.CarouselItem(R.layout.instructions_section3_layout)
        )
        instructions_carousel.adapter =
            WelcomePagerCarouselAdapter(requireContext(), carouselItems)
        instructions_carousel.offscreenPageLimit = carouselItems.size - 1
        carousel_indicator.setViewPager(instructions_carousel)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        setupBottomSheet(dialog)
        return dialog

    }

    private fun setupBottomSheet(dialog: Dialog) {
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet!!)
//            behavior.isHideable = false
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
        }
    }
}