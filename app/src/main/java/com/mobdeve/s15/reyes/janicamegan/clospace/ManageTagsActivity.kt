package com.mobdeve.s15.reyes.janicamegan.clospace

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ManageTagsActivity : AppCompatActivity() {

    private lateinit var backend: BackendRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_tags)

        backend = BackendRepository(this)

        findViewById<TextView>(R.id.tvToolbarTitle).text = getString(R.string.manage_tags)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.btnAddTag).apply {
            visibility = android.view.View.VISIBLE
            setOnClickListener { promptNewTag() }
        }

        loadTags()
    }

    private fun promptNewTag() {
        ClospaceBottomSheets.showInput(
            this,
            R.string.new_tag,
            getString(R.string.new_tag_hint),
            positiveRes = R.string.add
        ) { raw ->
            val name = raw.trim().takeIf { it.isNotBlank() } ?: return@showInput
            lifecycleScope.launch {
                runCatching { backend.createTag(name) }
                    .onSuccess { loadTags() }
                    .onFailure { Toast.makeText(this@ManageTagsActivity, it.message ?: getString(R.string.tag_add_failed), Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun loadTags() {
        lifecycleScope.launch {
            val tags = runCatching { backend.getTags() }.getOrDefault(emptyList())
            render(tags)
        }
    }

    private fun render(tags: List<Tag>) {
        val list = findViewById<LinearLayout>(R.id.layoutTagsList)
        list.removeAllViews()
        findViewById<TextView>(R.id.tvEmptyTags).visibility =
            if (tags.isEmpty()) TextView.VISIBLE else TextView.GONE

        for (tag in tags.sortedBy { it.name.lowercase() }) {
            list.addView(buildRow(tag))
        }
    }

    private fun buildRow(tag: Tag): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, dp(4), 0, dp(4))
        }

        val name = TextView(this).apply {
            text = tag.name
            textSize = 18f
            setTextColor(getColor(R.color.brown))
        }
        row.addView(name, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val edit = ImageButton(this).apply {
            setImageResource(R.drawable.ic_text_edit)
            background = getDrawable(R.drawable.back_circle_background)
            setColorFilter(getColor(R.color.purple))
            setPadding(dp(6), dp(6), dp(6), dp(6))
            contentDescription = getString(R.string.edit_tag)
            setOnClickListener { promptRename(tag) }
        }
        val delete = ImageButton(this).apply {
            setImageResource(R.drawable.ic_delete)
            background = getDrawable(R.drawable.back_circle_background)
            setColorFilter(getColor(R.color.purple))
            setPadding(dp(6), dp(6), dp(6), dp(6))
            contentDescription = getString(R.string.delete_tag)
            setOnClickListener { promptDelete(tag) }
        }
        val size = dp(36)
        val margin = dp(6)
        row.addView(edit, LinearLayout.LayoutParams(size, size).apply { leftMargin = margin })
        row.addView(delete, LinearLayout.LayoutParams(size, size).apply { leftMargin = margin })
        return row
    }

    private fun promptRename(tag: Tag) {
        ClospaceBottomSheets.showInput(
            this,
            R.string.edit_tag,
            getString(R.string.tag_name_empty),
            tag.name
        ) { raw ->
            val name = raw.trim().takeIf { it.isNotBlank() } ?: return@showInput
            lifecycleScope.launch {
                runCatching { backend.renameTag(tag.id, name) }
                    .onSuccess { loadTags() }
                    .onFailure { Toast.makeText(this@ManageTagsActivity, it.message ?: getString(R.string.tag_rename_failed), Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun promptDelete(tag: Tag) {
        ClospaceBottomSheets.showConfirm(
            this,
            R.string.delete_tag_title,
            R.string.delete_tag_message,
            R.string.delete
        ) {
            lifecycleScope.launch {
                runCatching { backend.deleteTag(tag.id) }
                    .onSuccess { loadTags() }
                    .onFailure { Toast.makeText(this@ManageTagsActivity, it.message ?: getString(R.string.tag_delete_failed), Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}