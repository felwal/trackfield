package com.felwal.trackfield.ui.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import com.felwal.android.ui.AbsSettingsActivity
import com.felwal.android.util.toIndicesOfTruths
import com.felwal.android.widget.dialog.AlertDialog
import com.felwal.android.widget.dialog.MultiChoiceDialog
import com.felwal.android.widget.dialog.TextDialog
import com.felwal.trackfield.R
import com.felwal.trackfield.data.network.StravaApi
import com.felwal.trackfield.data.prefs.Prefs
import com.felwal.trackfield.databinding.ActivitySettingsBinding
import com.felwal.trackfield.utils.LayoutUtils
import com.felwal.trackfield.utils.ScreenUtils

private const val DIALOG_DEVICE = "deviceDialog"
private const val DIALOG_RECORDING_METHOD = "methodDialog"
private const val DIALOG_REQUEST_ALL = "requestAllDialog"
private const val DIALOG_PULL_ALL = "pullAllDialog"
private const val DIALOG_PULL_POLICY = "pullPolicyDialog"

class StravaSettingsActivity :
    AbsSettingsActivity(dividerMode = DividerMode.IN_SECTION, indentEverything = false),
    AlertDialog.DialogListener,
    TextDialog.DialogListener,
    MultiChoiceDialog.DialogListener {

    // view
    private lateinit var binding: ActivitySettingsBinding
    override val llItemContainer: LinearLayout get() = binding.llSettings

    private lateinit var strava: StravaApi

    // lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        ScreenUtils.updateTheme(this);
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        strava = StravaApi(this)
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
                ActionItem(
                    title = getString(R.string.tv_text_settings_title_auth),
                    onClick = { strava.authorizeStrava() },
                    iconRes = R.drawable.ic_key
                )
            ),
            ItemSection(
                title = getString(R.string.tv_text_settings_header_requests),
                ActionItem(
                    title = getString(R.string.tv_text_settings_title_request_new),
                    onClick = {
                        strava.requestNewActivities { successCount, errorCount ->
                            StravaApi.toastResponse(successCount, errorCount, this)
                        }
                    },
                    iconRes = R.drawable.ic_send
                ),
                ConfirmationItem(
                    title = getString(R.string.tv_text_settings_title_request_all),
                    msg = "Request all exercises?",
                    dialogPosBtnRes = R.string.fw_dialog_btn_ok,
                    tag = DIALOG_REQUEST_ALL,
                    iconRes = R.drawable.ic_send
                ),
                ConfirmationItem(
                    title = getString(R.string.tv_text_settings_title_pull_all),
                    msg = "Pull all exercises?",
                    dialogPosBtnRes = R.string.fw_dialog_btn_ok,
                    tag = DIALOG_PULL_ALL,
                    iconRes = R.drawable.fw_ic_arrow_down_24
                )//.takeIf { Prefs.isDeveloper() } TODO
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
                    title = getString(R.string.tv_text_settings_title_method),
                    value = Prefs.getDefaultRecordingMethod(),
                    msg = getString(R.string.dialog_msg_recording_method),
                    hint =  getString(R.string.tv_text_settings_hint_method),
                    tag = DIALOG_RECORDING_METHOD,
                    iconRes = R.drawable.ic_gps
                ),
                MultiSelectionItem(
                    title = getString(R.string.tv_text_settings_title_policy),
                    values = Prefs.getPullPolicy().texts,
                    selectedIndices = Prefs.getPullPolicy().checked!!.toIndicesOfTruths(),
                    tag = DIALOG_PULL_POLICY,
                    iconRes = R.drawable.ic_tune
                )
            ),
        )
    }

    // dialog

    override fun onAlertDialogPositiveClick(passValue: String?, tag: String) {
        when (tag) {
            DIALOG_REQUEST_ALL -> {
                strava.requestAllActivities { successCount, errorCount ->
                    StravaApi.toastResponse(successCount, errorCount, this)
                }
            }
            DIALOG_PULL_ALL -> {
                strava.pullAllActivities { success ->
                    // we dont want to toast for every successfully requested activity
                    if (!success) LayoutUtils.toast(R.string.toast_strava_pull_activity_err, this)
                }
            }
        }
    }

    override fun onTextDialogPositiveClick(input: String, tag: String) {
        when (tag) {
            DIALOG_DEVICE -> {
                Prefs.setDefaultDevice(input)
                reflateViews()
            }
            DIALOG_RECORDING_METHOD -> {
                Prefs.setDefaultRecordingMethod(input)
                reflateViews()
            }
        }
    }

    override fun onMultiChoiceDialogItemsSelected(itemStates: BooleanArray, tag: String) {
        when (tag) {
            DIALOG_PULL_POLICY -> {
                Prefs.setPullSettings(itemStates)
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
