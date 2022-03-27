package me.felwal.trackfield.ui.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import me.felwal.android.fragment.app.AbsSettingsActivity
import me.felwal.android.fragment.dialog.AlertDialog
import me.felwal.android.fragment.dialog.InputDialog
import me.felwal.android.fragment.dialog.MultiChoiceDialog
import me.felwal.android.util.toIndicesOfTruths
import me.felwal.trackfield.R
import me.felwal.trackfield.data.network.StravaService
import me.felwal.trackfield.data.prefs.Prefs
import me.felwal.trackfield.databinding.ActivitySettingsBinding
import me.felwal.trackfield.ui.main.MainActivity
import me.felwal.trackfield.utils.LayoutUtils
import me.felwal.trackfield.utils.ScreenUtils

private const val DIALOG_DEVICE = "deviceDialog"
private const val DIALOG_RECORDING_METHOD = "methodDialog"
private const val DIALOG_REQUEST_SPECIFIC = "requestSpecificDialog"
private const val DIALOG_REQUEST_ALL = "requestAllDialog"
private const val DIALOG_PULL_ALL = "pullAllDialog"
private const val DIALOG_REQUEST_OPTIONS = "requestOptionsDialog"

class StravaSettingsActivity :
    AbsSettingsActivity(dividerMode = DividerMode.IN_SECTION, indentEverything = false),
    AlertDialog.DialogListener,
    InputDialog.DialogListener,
    MultiChoiceDialog.DialogListener {

    // view
    private lateinit var binding: ActivitySettingsBinding
    override val llItemContainer: LinearLayout get() = binding.llSettings

    private lateinit var strava: StravaService

    // lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        ScreenUtils.updateTheme(this);
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        strava = StravaService(this)
        strava.handleIntent(intent)

        setToolbar()
        inflateSettingItems()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        strava.handleIntent(getIntent())
    }

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
            title = resources.getString(R.string.fragment_title_settings_strava)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun inflateSettingItems() {
        inflateSections(
            ItemSection(
                title = getString(R.string.tv_text_settings_header_connection),
                LinkItem(
                    title = getString(R.string.tv_text_settings_title_auth),
                    onClick = { strava.authorizeStrava() },
                    iconRes = R.drawable.ic_key
                )
            ),
            ItemSection(
                title = getString(R.string.tv_text_settings_header_requests),
                NumberItem(
                    title = getString(R.string.tv_text_settings_title_request_specific),
                    desc = "",
                    value = 0,
                    hint = getString(R.string.tv_text_settings_hint_request_specific),
                    iconRes = R.drawable.ic_send,
                    tag = DIALOG_REQUEST_SPECIFIC
                ),
                ActionItem(
                    title = getString(R.string.tv_text_settings_title_request_new),
                    onClick = {
                        strava.requestNewActivities { successCount, errorCount ->
                            StravaService.toastResponse(successCount, errorCount, this)
                        }
                    },
                    iconRes = R.drawable.ic_send
                ),
                ConfirmationItem(
                    title = getString(R.string.tv_text_settings_title_request_all),
                    dialogPosBtnRes = R.string.fw_dialog_btn_ok,
                    tag = DIALOG_REQUEST_ALL,
                    iconRes = R.drawable.ic_send
                ),
                MultiSelectionItem(
                    title = getString(R.string.tv_text_settings_title_pull_all),
                    values = Prefs.getPullOptions().texts,
                    selectedIndices = Prefs.getPullOptions().checked.toIndicesOfTruths(),
                    //dialogPosBtnRes = R.string.dialog_btn_pull,
                    tag = DIALOG_PULL_ALL,
                    iconRes = R.drawable.fw_ic_arrow_down_24
                ).takeIf { Prefs.isDeveloper() }
            ),
            ItemSection(
                title = getString(R.string.tv_text_settings_header_request_options),
                StringItem(
                    title = getString(R.string.tv_text_settings_title_device),
                    value = Prefs.getDefaultDevice(),
                    msg = getString(R.string.dialog_msg_device),
                    hint =  getString(R.string.tv_text_settings_hint_device),
                    tag = DIALOG_DEVICE,
                    iconRes = R.drawable.ic_device
                ),
                StringItem(
                    title = getString(R.string.tv_text_settings_title_recordingmethod),
                    value = Prefs.getDefaultRecordingMethod(),
                    msg = getString(R.string.dialog_msg_recording_method),
                    hint =  getString(R.string.tv_text_settings_hint_recordingmethod),
                    tag = DIALOG_RECORDING_METHOD,
                    iconRes = R.drawable.ic_gps
                ),
                MultiSelectionItem(
                    title = getString(R.string.tv_text_settings_title_request_options),
                    desc = Prefs.getRequestOptions().checkedTexts.contentToString().replace("[", "").replace("]", ""),
                    values = Prefs.getRequestOptions().texts,
                    selectedIndices = Prefs.getRequestOptions().checked!!.toIndicesOfTruths(),
                    tag = DIALOG_REQUEST_OPTIONS,
                    iconRes = R.drawable.ic_tune
                )
            ),
        )
    }

    // dialog

    override fun onAlertDialogPositiveClick(tag: String, passValue: String?) {
        when (tag) {
            DIALOG_REQUEST_ALL -> {
                strava.requestAllActivities { successCount, errorCount ->
                    StravaService.toastResponse(successCount, errorCount, this)
                }
            }
        }
    }

    override fun onInputDialogPositiveClick(input: String, tag: String, passValue: String?) {
        when (tag) {
            DIALOG_DEVICE -> {
                Prefs.setDefaultDevice(input)
                reflateViews()
            }
            DIALOG_RECORDING_METHOD -> {
                Prefs.setDefaultRecordingMethod(input)
                reflateViews()
            }
            DIALOG_REQUEST_SPECIFIC -> strava.requestActivity(input.toLong()) {
                MainActivity.updateFragmentOnRestart = true
                LayoutUtils.toast(R.string.toast_strava_req_activity_successful, this)
            }
        }
    }

    override fun onMultiChoiceDialogItemsSelected(itemStates: BooleanArray, tag: String, passValue: String?) {
        when (tag) {
            DIALOG_PULL_ALL -> {
                Prefs.setPullOptions(itemStates)
                strava.pullAllActivities { success ->
                    // we dont want to toast for every successfully requested activity
                    if (!success) LayoutUtils.toast(R.string.toast_strava_pull_activity_err, this)
                }
            }
            DIALOG_REQUEST_OPTIONS -> {
                Prefs.setRequestOptions(itemStates)
                reflateViews()
            }
        }
    }

    // object

    companion object {
        @JvmStatic
        fun startActivity(c: Context) {
            val intent = Intent(c, StravaSettingsActivity::class.java)
            c.startActivity(intent)
        }
    }
}
