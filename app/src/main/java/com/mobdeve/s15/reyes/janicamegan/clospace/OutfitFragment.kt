package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.mobdeve.s15.reyes.janicamegan.clospace.adapter.OutfitAdapter

import kotlinx.coroutines.launch

class OutfitFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    private lateinit var outfitDao: OutfitDao

    private lateinit var recycler: RecyclerView

    private lateinit var tvEmpty: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.fragment_outfit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        outfitDao = ClospaceDatabase.getDatabase(requireContext()).outfitDao()

        recycler = view.findViewById(R.id.rvOutfits)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)

        tvEmpty = view.findViewById(R.id.tvEmpty)

        view.findViewById<FloatingActionButton>(R.id.fabAddOutfit).setOnClickListener {
            startActivity(Intent(requireContext(), SelectGarmentsActivity::class.java))
        }

        loadOutfits()
    }

    override fun onResume() {
        super.onResume()
        loadOutfits()
    }

    private fun loadOutfits() {

        val ownerId = sessionManager.getUserId()

        if (ownerId == -1) {
            return
        }

        lifecycleScope.launch {

            val outfits = outfitDao.getAll(ownerId)

            if (outfits.isEmpty()) {

                recycler.adapter = OutfitAdapter(emptyList(), onOpen = {})
                tvEmpty.visibility = View.VISIBLE

            } else {

                val full = outfits.map { outfit ->

                    val placements =
                        outfitDao.getOutfitItemsWithClothing(outfit.id)
                            .map { joined ->

                                OutfitPlacement(
                                    item = joined.clothing,
                                    x = joined.placement.x,
                                    y = joined.placement.y,
                                    scale = joined.placement.scale,
                                    layer = joined.placement.layer
                                )
                            }

                    OutfitWithItems(
                        outfit = outfit,
                        placements = placements
                    )
                }

                recycler.adapter = OutfitAdapter(full) { wrapper ->
                    openOutfit(wrapper.outfit.id)
                }

                tvEmpty.visibility = View.GONE
            }
        }
    }

    private fun openOutfit(outfitId: Int) {

        if (outfitId <= 0) {
            return
        }

        startActivity(
            Intent(requireContext(), OutfitDetailActivity::class.java)
                .putExtra(OutfitDetailActivity.EXTRA_OUTFIT_ID, outfitId)
        )
    }
}