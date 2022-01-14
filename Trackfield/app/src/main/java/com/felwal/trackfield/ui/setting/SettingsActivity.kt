package com.felwal.trackfield.ui.setting

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.LinearLayout
import com.felwal.android.ui.AbsSettingsActivity
import com.felwal.android.widget.dialog.AlertDialog
import com.felwal.android.widget.dialog.DecimalDialog
import com.felwal.android.widget.dialog.SingleChoiceDialog
import com.felwal.trackfield.R
import com.felwal.trackfield.data.db.DbWriter
import com.felwal.trackfield.data.prefs.Prefs
import com.felwal.trackfield.databinding.ActivitySettingsBinding
import com.felwal.trackfield.ui.main.MainActivity
import com.felwal.trackfield.ui.onboarding.OnboardingActivity
import com.felwal.trackfield.utils.AppConsts
import com.felwal.trackfield.utils.FileUtils
import com.felwal.trackfield.utils.LayoutUtils
import com.felwal.trackfield.utils.ScreenUtils
import java.time.LocalDate

private const val DIALOG_THEME = "themeDialog"
private const val DIALOG_COLOR = "colorDialog"
private const val DIALOG_MASS = "massDialog"
private const val DIALOG_EXPORT = "exportDialog"
private const val DIALOG_IMPORT = "importDialog"
private const val DIALOG_RECREATE_DB = "recreateDbDialog"

class SettingsActivity :
    AbsSettingsActivity(dividerMode = DividerMode.IN_SECTION, indentEverything = false),
    AlertDialog.DialogListener,
    DecimalDialog.DialogListener,
    SingleChoiceDialog.DialogListener {

    // view
    private lateinit var binding: ActivitySettingsBinding
    override val llItemContainer: LinearLayout get() = binding.llSettings

    // lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        ScreenUtils.updateTheme(this);
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setToolbar()
        inflateSettingItems()
    }

    // menu

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    // view

    private fun setToolbar() {
        setSupportActionBar(findViewById(R.id.tb_settings))
        supportActionBar?.apply {
            title = resources.getString(R.string.fragment_title_settings)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun inflateSettingItems() {
        inflateSections(
            ItemSection(
                title = getString(R.string.tv_text_settings_header_display),
                BooleanItem(
                    title = getString(R.string.tv_text_settings_title_week_headers),
                    value = Prefs.isWeekHeadersShown(),
                    onSwitch = { Prefs.showWeekHeaders(it) },
                    iconRes = R.drawable.ic_title
                ),
                BooleanItem(
                    title = getString(R.string.tv_text_settings_title_singletion_routes),
                    value = Prefs.areSingletonRoutesHidden(),
                    onSwitch = { Prefs.hideSingletonRoutes(it) },
                    iconRes = R.drawable.ic_route
                )
            ),
            ItemSection(
                title = getString(R.string.tv_text_settings_header_look),
                SingleSelectionItem(
                    title = getString(R.string.tv_text_settings_title_theme),
                    values = AppConsts.themeNames.toTypedArray(),
                    selectedIndex = Prefs.getTheme(),
                    tag = DIALOG_THEME,
                    iconRes = R.drawable.ic_theme
                ),
                SingleSelectionItem(
                    title = getString(R.string.tv_text_settings_title_color),
                    values = AppConsts.colorNames.toTypedArray(),
                    selectedIndex = Prefs.getColor(),
                    tag = DIALOG_COLOR,
                    iconRes = R.drawable.ic_color
                )
            ),
            ItemSection(
                title = getString(R.string.tv_text_settings_header_services),
                ActionItem(
                    title = getString(R.string.tv_text_settings_title_strava),
                    onClick = { StravaSettingsActivity.startActivity(this) },
                    iconRes = R.drawable.ic_logo_strava
                )
            ),
            ItemSection(
                title = getString(R.string.tv_text_settings_header_file),
                ConfirmationItem(
                    title = getString(R.string.tv_text_settings_title_export),
                    msg = getString(R.string.dialog_msg_export),
                    dialogPosBtnRes = R.string.dialog_btn_export,
                    tag = DIALOG_EXPORT,
                    iconRes = R.drawable.ic_export
                ),
                ConfirmationItem(
                    title = getString(R.string.tv_text_settings_title_import),
                    msg = getString(R.string.dialog_msg_import),
                    dialogPosBtnRes = R.string.dialog_btn_import,
                    tag = DIALOG_IMPORT,
                    iconRes = R.drawable.ic_import
                )
            ),
            ItemSection(
                title = getString(R.string.tv_text_settings_header_profile),
                FloatItem(
                    title = getString(R.string.tv_text_settings_title_mass),
                    desc = Prefs.getMass().toString() + " kg",
                    value = Prefs.getMass(),
                    hint = getString(R.string.tv_text_settings_hint_mass),
                    tag = DIALOG_MASS,
                    iconRes = R.drawable.ic_weight
                ),
                ActionItem(
                    title = getString(R.string.tv_text_settings_title_birthday),
                    desc = Prefs.getBirthday()?.format(AppConsts.FORMATTER_CAPTION) ?: "",
                    onClick = {
                        val bd = Prefs.getBirthday()

                        val yearSelect = bd?.year ?: 1970
                        val monthSelect = (bd?.monthValue ?: 1) - 1
                        val daySelect = bd?.dayOfMonth ?: 1

                        val picker =
                            DatePickerDialog(this, { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                                Prefs.setBirthday(LocalDate.of(year, month + 1, dayOfMonth))
                                recreate()
                            }, yearSelect, monthSelect, daySelect)

                        picker.datePicker.maxDate = System.currentTimeMillis()
                        picker.show()
                    },
                    iconRes = R.drawable.ic_date
                )
            ),
            ItemSection(
                title = getString(R.string.tv_text_settings_header_developer),
                ActionItem(
                    title = getString(R.string.tv_text_settings_title_reboard),
                    onClick = {
                        Prefs.setFirstLogin(true)
                        OnboardingActivity.startActivity(this)
                    }
                ),
                ActionItem(
                    title = getString(R.string.tv_text_settings_title_regenerate_places),
                    onClick = {
                        DbWriter.get(this).regeneratePlaces(this)
                    }
                ),
                ConfirmationItem(
                    title = getString(R.string.tv_text_settings_title_recreate),
                    msg = "Recreate database?",
                    dialogPosBtnRes = R.string.fw_dialog_btn_ok,
                    tag = DIALOG_RECREATE_DB
                )
            ).takeIf { Prefs.isDeveloper() }
        )
    }

    // dialog

    override fun onAlertDialogPositiveClick(tag: String, passValue: String?) {
        when (tag) {
            DIALOG_EXPORT -> {
                LayoutUtils.toast(R.string.toast_json_exporting, this)
                Thread {
                    val success = FileUtils.exportJson(this)
                    runOnUiThread {
                        LayoutUtils.toast(
                            if (success) R.string.toast_json_export_successful
                            else R.string.toast_json_export_err,
                            this
                        )
                    }
                }.start()
            }
            DIALOG_IMPORT -> {
                LayoutUtils.toast(R.string.toast_json_importing, this)
                Thread {
                    val success = FileUtils.importJson(this)
                    runOnUiThread {
                        LayoutUtils.toast(
                            if (success) R.string.toast_json_import_successful
                            else R.string.toast_json_import_err,
                            this
                        )
                    }
                }.start()
            }
            DIALOG_RECREATE_DB -> {
                DbWriter.get(this).recreate()
            }
        }
    }

    override fun onDecimalDialogPositiveClick(input: Float, tag: String, passValue: String?) {
        when (tag) {
            DIALOG_MASS -> {
                Prefs.setMass(input)
                reflateViews()
            }
        }
    }

    override fun onSingleChoiceDialogItemSelected(selectedIndex: Int, tag: String, passValue: String?) {
        when (tag) {
            DIALOG_THEME -> {
                Prefs.setTheme(selectedIndex)
                MainActivity.recreateOnRestart = true
                recreate()
            }
            DIALOG_COLOR -> {
                Prefs.setColor(selectedIndex)
                MainActivity.recreateOnRestart = true
                recreate()
            }
        }
    }

    // object

    companion object {
        @JvmStatic
        fun startActivity(c: Context) {
            val intent = Intent(c, SettingsActivity::class.java)
            c.startActivity(intent)
        }
    }
}
