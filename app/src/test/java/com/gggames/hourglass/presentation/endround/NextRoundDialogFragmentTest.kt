package com.gggames.hourglass.presentation.endround

import com.gggames.hourglass.presentation.endturn.NextRoundDialogFragment
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NextRoundDialogFragmentTest{

    private val tested = NextRoundDialogFragment()

    @Test
    fun `Given time is less than 1000 Then result is 1`() {
        assertThat(tested.convertTime(900L)).isEqualTo(1)
    }

    @Test
    fun `Given time is more than 1000 And less then 2000 Then result is 2`() {
        assertThat(tested.convertTime(1500L)).isEqualTo(2)
    }

    @Test
    fun `Given time is exactly 2000 Then result is 2`() {
        assertThat(tested.convertTime(2000L)).isEqualTo(2)
    }

    @Test
    fun `Given time is null Then result is 0`() {
        assertThat(tested.convertTime(null)).isEqualTo(0)
    }

    @Test
    fun `Given time is 0 Then result is 0`() {
        assertThat(tested.convertTime(0L)).isEqualTo(0)
    }

    @Test
    fun `Given time is 100000 Then result is 100`() {
        assertThat(tested.convertTime(100000L)).isEqualTo(100)
    }
}