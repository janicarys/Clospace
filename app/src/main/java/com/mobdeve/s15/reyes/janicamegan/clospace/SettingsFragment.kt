package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val account = view.findViewById<LinearLayout>(R.id.layoutAccount)
        val about = view.findViewById<LinearLayout>(R.id.layoutAbout)
        val support = view.findViewById<LinearLayout>(R.id.layoutSupport)

        account.setOnClickListener {
            startActivity(Intent(requireContext(), AccountActivity::class.java))
        }

        about.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }

        support.setOnClickListener {
            startActivity(Intent(requireContext(), SupportActivity::class.java))
        }
    }
}