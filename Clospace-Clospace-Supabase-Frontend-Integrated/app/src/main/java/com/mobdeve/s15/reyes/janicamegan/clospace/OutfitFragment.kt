package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.mobdeve.s15.reyes.janicamegan.clospace.adapter.OutfitAdapter
import com.mobdeve.s15.reyes.janicamegan.clospace.util.OutfitPreviewCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OutfitFragment : Fragment() {
    private lateinit var backend: BackendRepository
    private lateinit var recycler: RecyclerView
    private lateinit var tvEmpty: TextView
    private var allOutfits: List<OutfitWithItems> = emptyList()
    private var previews: Map<Int, android.graphics.Bitmap> = emptyMap()

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
        view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.searchOutfitLayout).setStartIconOnClickListener {
            applySearch()
        }
        view.findViewById<TextInputEditText>(R.id.etSearchOutfit).apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s.isNullOrBlank()) applyFilter("")
                }
            })
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                    applySearch()
                    true
                } else false
            }
        }
        loadOutfits()
    }

    private fun applySearch() {
        applyFilter(currentQuery())
    }

    override fun onResume() { super.onResume(); if (::backend.isInitialized) loadOutfits() }

    private fun loadOutfits() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { backend.getOutfits() }.onSuccess { outfits ->
                allOutfits = outfits
                val bitmaps = withContext(Dispatchers.IO) {
                    outfits.mapNotNull { wrapper ->
                        val bitmap = OutfitPreviewCache.render(wrapper.outfit.id, wrapper.placements, 400, 480)
                        if (bitmap != null) wrapper.outfit.id to bitmap else null
                    }.toMap()
                }
                previews = bitmaps
                applyFilter(currentQuery())
            }
        }
    }

    private fun currentQuery(): String {
        val view = view ?: return ""
        return view.findViewById<TextInputEditText>(R.id.etSearchOutfit)?.text?.toString().orEmpty()
    }

    private fun applyFilter(query: String) {
        val q = query.trim()
        val filtered = if (q.isEmpty()) allOutfits else allOutfits.filter { matches(it, q) }
        recycler.adapter = OutfitAdapter(filtered, previews) { wrapper -> openOutfit(wrapper.outfit.id) }
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun matches(wrapper: OutfitWithItems, query: String): Boolean {
        val outfit = wrapper.outfit
        val garmentFields = wrapper.placements.flatMap { p ->
            listOf(p.item.name, p.item.category, p.item.color, p.item.material, p.item.tags)
        }
        return listOf(outfit.caption, outfit.occasion, outfit.tags)
            .plus(garmentFields)
            .any { it != null && it.contains(query, ignoreCase = true) }
    }

    private fun openOutfit(outfitId: Int) {
        startActivity(Intent(requireContext(), OutfitDetailActivity::class.java).putExtra(OutfitDetailActivity.EXTRA_OUTFIT_ID, outfitId))
    }
}
