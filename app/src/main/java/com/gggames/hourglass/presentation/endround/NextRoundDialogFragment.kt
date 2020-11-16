package com.gggames.hourglass.presentation.endturn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gggames.hourglass.R
import com.gggames.hourglass.model.Round

class NextRoundDialogFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_next_round, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val nextRound = it.getParcelable<Round>(KEY_NEXT_ROUND)

        }
    }
}
