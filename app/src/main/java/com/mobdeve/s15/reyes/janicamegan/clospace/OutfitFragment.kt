package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mobdeve.s15.reyes.janicamegan.clospace.adapter.OutfitAdapter
import com.mobdeve.s15.reyes.janicamegan.clospace.util.OutfitPreviewCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OutfitFragment : Fragment() {
    private lateinit var backend: BackendRepository
    private lateinit var recycler: RecyclerView
    private lateinit var tvEmpty: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_outfit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backend = BackendRepository(requireContext())
        recycler = view.findViewById(R.id.rvOutfits)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        view.findViewById<FloatingActionButton>(R.id.fabAddOutfit).setOnClickListener {
            startActivity(Intent(requireContext(), SelectGarmentsActivity::class.java))
        }
        loadOutfits()
    }

    override fun onResume() { super.onResume(); if (::backend.isInitialized) loadOutfits() }

    private fun loadOutfits() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { backend.getOutfits() }.onSuccess { outfits ->
                val previews = withContext(Dispatchers.IO) {
                    outfits.mapNotNull { wrapper ->
                        val bitmap = OutfitPreviewCache.render(wrapper.outfit.id, wrapper.placements, 400, 480)
                        if (bitmap != null) wrapper.outfit.id to bitmap else null
                    }.toMap()
                }
                recycler.adapter = OutfitAdapter(outfits, previews) { wrapper -> openOutfit(wrapper.outfit.id) }
                tvEmpty.visibility = if (outfits.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun openOutfit(outfitId: Int) {
        startActivity(Intent(requireContext(), OutfitDetailActivity::class.java).putExtra(OutfitDetailActivity.EXTRA_OUTFIT_ID, outfitId))
    }
}
