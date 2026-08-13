package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.mobdeve.s15.reyes.janicamegan.clospace.adapter.GarmentPickerAdapter
import com.mobdeve.s15.reyes.janicamegan.clospace.adapter.SelectedGarmentAdapter

import kotlinx.coroutines.launch

class SelectGarmentsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CLOTHING_IDS = "clothingIds"
        const val EXTRA_RETURN_SELECTION = "returnSelection"
        const val EXTRA_DATE = "selectedDate"

        private const val REQ_OPEN_CANVAS = 2001
    }

    private lateinit var backend: BackendRepository

    private val selected = mutableListOf<ClothingItem>()

    private val selectedIds = mutableSetOf<Int>()

    private lateinit var rvGarments: RecyclerView

    private lateinit var gridAdapter: GarmentPickerAdapter

    private lateinit var selectedAdapter: SelectedGarmentAdapter

    private lateinit var tvCount: TextView

    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_garments)

        findViewById<TextView>(R.id.tvToolbarTitle).text =
            getString(R.string.select_garments_title)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        backend = BackendRepository(this)

        rvGarments = findViewById(R.id.rvGarments)
        rvGarments.layoutManager = GridLayoutManager(this, 2)

        val rvSelected = findViewById<RecyclerView>(R.id.rvSelected)
        rvSelected.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        tvCount = findViewById(R.id.tvSelectedCount)

        btnNext = findViewById(R.id.btnNext)

        gridAdapter = GarmentPickerAdapter(emptyList(), selectedIds) { item ->
            toggle(item)
        }
        rvGarments.adapter = gridAdapter

        selectedAdapter = SelectedGarmentAdapter(selected) { item ->
            toggle(item)
        }
        rvSelected.adapter = selectedAdapter

        updateCount()

        btnNext.setOnClickListener {

            if (selected.isEmpty()) {

                Toast.makeText(
                    this,
                    R.string.select_at_least_one,
                    Toast.LENGTH_SHORT
                ).show()

            } else if (intent.getBooleanExtra(EXTRA_RETURN_SELECTION, false)) {

                val result = Intent()
                    .putExtra(
                        EXTRA_CLOTHING_IDS,
                        selected.map { it.id }.toIntArray()
                    )
                setResult(RESULT_OK, result)
                finish()

            } else {

                startActivityForResult(
                    Intent(this, OutfitCanvasActivity::class.java)
                        .putExtra(
                            OutfitCanvasActivity.EXTRA_CLOTHING_IDS,
                            selected.map { it.id }.toIntArray()
                        )
                        .putExtra(
                            OutfitCanvasActivity.EXTRA_DATE,
                            intent.getStringExtra(EXTRA_DATE)
                        ),
                    REQ_OPEN_CANVAS
                )
            }
        }

        loadGarments()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != REQ_OPEN_CANVAS || resultCode != RESULT_OK) return

        val survivors = data?.getIntArrayExtra(EXTRA_CLOTHING_IDS)?.toSet() ?: return

        selected.removeAll { it.id !in survivors }
        selectedIds.clear()
        selectedIds.addAll(selected.map { it.id })

        updateCount()
        gridAdapter.notifyDataSetChanged()
        selectedAdapter.notifyDataSetChanged()
    }

    private fun loadGarments() {
        lifecycleScope.launch {
            runCatching { backend.getClothing() }.onSuccess { items ->
                gridAdapter = GarmentPickerAdapter(items, selectedIds) { item -> toggle(item) }
                rvGarments.adapter = gridAdapter
            }.onFailure {
                Toast.makeText(this@SelectGarmentsActivity, it.message ?: "Unable to load garments", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggle(item: ClothingItem) {

        if (selected.contains(item)) {

            selected.remove(item)
            selectedIds.remove(item.id)

        } else {

            selected.add(item)
            selectedIds.add(item.id)
        }

        updateCount()

        gridAdapter.notifyDataSetChanged()
        selectedAdapter.notifyDataSetChanged()
    }

    private fun updateCount() {

        tvCount.text = getString(R.string.selected_count, selected.size)

        btnNext.isEnabled = selected.isNotEmpty()
    }
}
