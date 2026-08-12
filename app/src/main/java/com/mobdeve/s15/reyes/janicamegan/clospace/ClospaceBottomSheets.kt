package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Context
import android.graphics.Bitmap
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.reyes.janicamegan.clospace.adapter.DayOutfitAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import java.time.LocalDate

/** Material-style modal bottom sheets matching the Clospace design language (white sheet, purple/lavender palette). */
object ClospaceBottomSheets {

    // Single, authoritative day-outfit sheet so returning from the create flow
    // refreshes (or replaces) the same dialog instead of stacking stale ones.
    private var dayOutfitSheet: BottomSheetDialog? = null
    private var dayOutfitAdapter: DayOutfitAdapter? = null
    private var dayOutfitDate: LocalDate? = null

    fun dismissDaySheet() {
        dayOutfitSheet?.let { if (it.isShowing) it.dismiss() }
        dayOutfitSheet = null
        dayOutfitAdapter = null
        dayOutfitDate = null
    }

    /** Single-line text input with Save/Cancel actions. */
    fun showInput(
        context: Context,
        @StringRes titleRes: Int,
        hint: String,
        initial: String = "",
        @StringRes positiveRes: Int = R.string.save,
        onPositive: (String) -> Unit
    ) {
        val content = base(context)
        content.title().setText(titleRes)

        val wrap = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val input = EditText(context).apply {
            setText(initial)
            this.hint = hint
            textSize = 16f
            setTextColor(context.getColor(R.color.purple))
            setHintTextColor(context.getColor(R.color.brown))
            typeface = ResourcesCompat.getFont(context, R.font.rokkitt_regular)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setPadding(0, dp(context, 8), 0, dp(context, 8))
        }
        val underline = View(context).apply { setBackgroundColor(context.getColor(R.color.lavender)) }
        wrap.addView(input, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        wrap.addView(underline, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 2)))
        content.container().addView(wrap, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val sheet = BottomSheetDialog(context)
        sheet.setContentView(content)
        showWithActions(content, sheet, positiveRes) { onPositive(input.text?.toString() ?: "") }
        sheet.show()
    }

    /** List of selectable options; tapping one dismisses and returns its index. */
    fun showChoice(
        context: Context,
        @StringRes titleRes: Int,
        items: Array<String>,
        selectedIndex: Int = -1,
        onSelect: (Int) -> Unit
    ) {
        val content = base(context)
        content.title().setText(titleRes)

        val list = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val rows = mutableListOf<View>()
        items.forEachIndexed { index, label ->
            val row = choiceRow(context, label, selected = index == selectedIndex)
            rows.add(row)
            list.addView(
                row,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    .apply { bottomMargin = dp(context, 10) }
            )
        }
        val scroll = ScrollView(context).apply {
            addView(list)
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        content.container().addView(scroll, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val sheet = BottomSheetDialog(context)
        sheet.setContentView(content)
        rows.forEachIndexed { index, row ->
            row.setOnClickListener {
                sheet.dismiss()
                onSelect(index)
            }
        }
        sheet.show()
    }

    /** Confirmation sheet with a message and Cancel/positive actions. */
    fun showConfirm(
        context: Context,
        @StringRes titleRes: Int,
        @StringRes messageRes: Int,
        @StringRes positiveRes: Int,
        onConfirm: () -> Unit
    ) {
        val content = base(context)
        content.title().setText(titleRes)

        val message = TextView(context).apply {
            setText(messageRes)
            textSize = 16f
            setTextColor(context.getColor(R.color.brown))
            typeface = ResourcesCompat.getFont(context, R.font.rokkitt_regular)
        }
        content.container().addView(message, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val sheet = BottomSheetDialog(context)
        sheet.setContentView(content)
        showWithActions(content, sheet, positiveRes, onPositive = { onConfirm() })
        sheet.show()
    }

    /** Non-dismissable progress indicator; dismiss the returned sheet when done. */
    fun showProgress(context: Context, @StringRes messageRes: Int): BottomSheetDialog {
        val content = base(context)
        content.title().visibility = View.GONE

        val box = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, dp(context, 8), 0, dp(context, 8))
        }
        val spinner = ProgressBar(context).apply {
            isIndeterminate = true
            indeterminateTintList = android.content.res.ColorStateList.valueOf(context.getColor(R.color.purple))
        }
        box.addView(spinner, LinearLayout.LayoutParams(dp(context, 40), dp(context, 40)))
        val message = TextView(context).apply {
            setText(messageRes)
            textSize = 16f
            setTextColor(context.getColor(R.color.brown))
            typeface = ResourcesCompat.getFont(context, R.font.rokkitt_regular)
            gravity = Gravity.CENTER
        }
        box.addView(message, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(context, 16) })
        content.container().addView(box, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val sheet = BottomSheetDialog(context)
        sheet.setCancelable(false)
        sheet.setContentView(content)
        sheet.show()
        return sheet
    }

    /** Add-garment source chooser (camera / gallery). */
    fun showAddSource(
        context: Context,
        onCamera: () -> Unit,
        onGallery: () -> Unit
    ) {
        val content = base(context)
        content.title().setText(R.string.add_source_title)

        val list = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val cameraRow = sourceRow(context, R.string.take_photo, R.drawable.ic_camera)
        val galleryRow = sourceRow(context, R.string.choose_from_library, R.drawable.ic_gallery)
        list.addView(cameraRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 56)).apply { bottomMargin = dp(context, 12) })
        list.addView(galleryRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 56)))
        content.container().addView(list, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val sheet = BottomSheetDialog(context)
        sheet.setContentView(content)
        cameraRow.setOnClickListener { sheet.dismiss(); onCamera() }
        galleryRow.setOnClickListener { sheet.dismiss(); onGallery() }
        sheet.show()
    }

    /** Multi-outfit panel for a calendar day: 2-column grid of outfit cards with delete
     *  buttons plus an "Add another outfit" action, in a dimmed, nearly full-width sheet. */
    fun showDayOutfits(
        context: Context,
        date: LocalDate,
        outfits: List<OutfitWithItems>,
        previews: Map<Int, Bitmap>,
        onCardClick: (OutfitWithItems) -> Unit = {},
        onDelete: (OutfitWithItems) -> Unit,
        onAddAnother: () -> Unit
    ) {
        if (dayOutfitSheet?.isShowing == true && dayOutfitDate == date && dayOutfitAdapter != null) {
            dayOutfitAdapter?.update(outfits, previews)
            dayOutfitSheet?.findViewById<MaterialButton>(R.id.btnAddAnotherOutfit)?.setText(
                if (outfits.isEmpty()) R.string.create_outfit else R.string.add_another_outfit
            )
            return
        }

        dismissDaySheet()

        val content = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_day_outfits, null)

        val grid = content.findViewById<RecyclerView>(R.id.rvDayOutfits)
        grid.layoutManager = GridLayoutManager(context, 2)
        grid.adapter = DayOutfitAdapter(
            outfits,
            previews,
            onClick = { wrapper ->
                dismissDaySheet()
                onCardClick(wrapper)
            },
            onDelete = { wrapper -> onDelete(wrapper) }
        )
        dayOutfitAdapter = grid.adapter as DayOutfitAdapter

        val addButton = content.findViewById<MaterialButton>(R.id.btnAddAnotherOutfit)
        addButton.setText(if (outfits.isEmpty()) R.string.create_outfit else R.string.add_another_outfit)
        addButton.setOnClickListener { onAddAnother() }

        val sheet = BottomSheetDialog(context)
        sheet.window?.setDimAmount(0.4f)
        sheet.setContentView(content)
        dayOutfitDate = date
        dayOutfitSheet = sheet
        sheet.show()
    }

    private fun base(context: Context): View =
        LayoutInflater.from(context).inflate(R.layout.bottom_sheet_base, null)

    private fun View.title(): TextView = findViewById(R.id.tvSheetTitle)

    private fun View.container(): FrameLayout = findViewById(R.id.sheetContainer)

    private fun showWithActions(content: View, sheet: BottomSheetDialog, @StringRes positiveRes: Int, onPositive: () -> Unit) {
        content.findViewById<View>(R.id.sheetDivider).visibility = View.VISIBLE
        content.findViewById<View>(R.id.sheetActionRow).visibility = View.VISIBLE
        content.findViewById<MaterialButton>(R.id.btnSheetSecondary).setOnClickListener { sheet.dismiss() }
        content.findViewById<MaterialButton>(R.id.btnSheetPrimary).apply {
            setText(positiveRes)
            setOnClickListener {
                onPositive()
                sheet.dismiss()
            }
        }
    }

    private fun choiceRow(context: Context, label: String, selected: Boolean): View {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 18), 0, dp(context, 18), 0)
            minimumHeight = dp(context, 48)
            background = context.getDrawable(R.drawable.bg_choice_row)
            isSelected = selected
        }
        val text = TextView(context).apply {
            text = label
            textSize = 17f
            setTextColor(context.getColor(R.color.purple))
            typeface = ResourcesCompat.getFont(context, R.font.rokkitt_regular)
        }
        val check = AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_check)
            setColorFilter(context.getColor(R.color.purple))
            visibility = if (selected) View.VISIBLE else View.INVISIBLE
        }
        row.addView(text, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(check, LinearLayout.LayoutParams(dp(context, 22), dp(context, 22)))
        return row
    }

    private fun sourceRow(context: Context, @StringRes labelRes: Int, @DrawableRes iconRes: Int): View {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 20), 0, dp(context, 20), 0)
            background = context.getDrawable(R.drawable.bg_choice_row)
            isClickable = true
            isFocusable = true
        }
        val icon = AppCompatImageView(context).apply {
            setImageResource(iconRes)
            setColorFilter(context.getColor(R.color.purple))
        }
        val label = TextView(context).apply {
            setText(labelRes)
            textSize = 18f
            setTextColor(context.getColor(R.color.brown))
            typeface = ResourcesCompat.getFont(context, R.font.rokkitt_regular)
        }
        row.addView(icon, LinearLayout.LayoutParams(dp(context, 24), dp(context, 24)))
        row.addView(label, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(context, 16) })
        return row
    }

    private fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()
}
