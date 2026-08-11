package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

/** Reusable tag picker: a modal bottom sheet showing existing tags as selectable chips, with a "+ New tag" button. */
object TagPickerDialog {

    fun show(
        activity: AppCompatActivity,
        backend: BackendRepository,
        initial: List<String>,
        onSave: suspend (List<String>) -> Unit
    ) {
        val scope = activity.lifecycleScope
        scope.launch {
            val tags = runCatching { backend.getTags() }.getOrDefault(emptyList()).toMutableList()
            val selected = initial.map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()

            val content = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_add_tags, null)
            val chipGroup = content.findViewById<ChipGroup>(R.id.chipGroupTags)
            val emptyView = content.findViewById<TextView>(R.id.tvNoTags)

            fun rerender() {
                chipGroup.removeAllViews()
                val sorted = tags.sortedBy { it.name.lowercase() }
                emptyView.visibility = if (sorted.isEmpty()) View.VISIBLE else View.GONE
                for (tag in sorted) {
                    chipGroup.addView(buildChip(activity, tag, selected))
                }
            }

            rerender()

            content.findViewById<View>(R.id.btnNewTag).setOnClickListener {
                promptNewTag(activity, backend, scope, tags, selected, ::rerender)
            }

            content.findViewById<View>(R.id.btnClearSelection).setOnClickListener {
                selected.clear()
                chipGroup.clearCheck()
            }

            val sheet = BottomSheetDialog(activity)
            sheet.setContentView(content)

            content.findViewById<View>(R.id.btnDone).setOnClickListener {
                sheet.dismiss()
                scope.launch {
                    runCatching { onSave(selected.toList()) }
                        .onFailure { Toast.makeText(activity, it.message ?: activity.getString(R.string.tag_save_failed), Toast.LENGTH_SHORT).show() }
                }
            }

            sheet.show()
        }
    }

    private fun buildChip(
        activity: AppCompatActivity,
        tag: Tag,
        selected: MutableSet<String>
    ): Chip {
        return Chip(activity).apply {
            text = tag.name
            isCheckable = true
            isChecked = selected.any { it.equals(tag.name, ignoreCase = true) }
            isCheckedIconVisible = false
            chipBackgroundColor = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(
                    activity.getColor(R.color.lavender),
                    activity.getColor(R.color.white)
                )
            )
            chipStrokeColor = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(
                    activity.getColor(R.color.purple),
                    activity.getColor(R.color.purple)
                )
            )
            chipStrokeWidth = dp(activity, 1).toFloat()
            setTextColor(activity.getColor(R.color.purple))
            setOnCheckedChangeListener { _, checked ->
                if (checked) selected += tag.name
                else selected.removeAll { it.equals(tag.name, ignoreCase = true) }
            }
        }
    }

    private fun promptNewTag(
        activity: AppCompatActivity,
        backend: BackendRepository,
        scope: androidx.lifecycle.LifecycleCoroutineScope,
        tags: MutableList<Tag>,
        selected: MutableSet<String>,
        rerender: () -> Unit
    ) {
        ClospaceBottomSheets.showInput(
            activity,
            R.string.new_tag,
            activity.getString(R.string.new_tag_hint),
            positiveRes = R.string.add
        ) { raw ->
            val name = raw.trim().takeIf { it.isNotBlank() } ?: return@showInput
            scope.launch {
                runCatching {
                    val tag = backend.createTag(name)
                    tags.removeAll { it.name.equals(tag.name, ignoreCase = true) }
                    tags.add(tag)
                    selected += tag.name
                }.onFailure {
                    Toast.makeText(activity, it.message ?: activity.getString(R.string.tag_add_failed), Toast.LENGTH_SHORT).show()
                }
                rerender()
            }
        }
    }

    private fun dp(context: android.content.Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()
}