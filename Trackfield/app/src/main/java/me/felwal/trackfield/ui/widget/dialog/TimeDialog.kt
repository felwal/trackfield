package me.felwal.trackfield.ui.widget.dialog

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import me.felwal.android.fragment.dialog.BaseDialog
import me.felwal.android.fragment.dialog.NO_INT_TEXT
import me.felwal.android.util.toast
import me.felwal.android.widget.control.DialogOption
import me.felwal.trackfield.R

private const val ARG_TEXT1 = "text1"
private const val ARG_TEXT2 = "text2"
private const val ARG_HINT1 = "hint1"
private const val ARG_HINT2 = "hint2"
private const val ARG_NEUTRAL_BUTTON_RES = "neutralButtonTextId"

class TimeDialog : BaseDialog<TimeDialog.DialogListener>() {

    // args
    private var text1 = 0
    private var text2 = 0
    private lateinit var hint1: String
    private lateinit var hint2: String
    private var neuBtnTxtRes: Int? = null

    // BaseDialog

    override fun unpackBundle(bundle: Bundle?) {
        bundle?.apply {
            text1 = getInt(ARG_TEXT1, 0)
            text2 = getInt(ARG_TEXT2, 0)
            hint1 = getString(ARG_HINT1, "")
            hint2 = getString(ARG_HINT2, "")
            neuBtnTxtRes = getInt(ARG_NEUTRAL_BUTTON_RES)
        }
    }

    override fun buildDialog(): AlertDialog {
        val root = inflater.inflate(R.layout.dialog_time, null)
        val et1 = root.findViewById<EditText>(R.id.et_timedialog_minutes)
        val et2 = root.findViewById<EditText>(R.id.et_timedialog_seconds)

        et1.hint = hint1
        et2.hint = hint2
        if (text1 != NO_INT_TEXT) et1.setText(text1.toString())
        if (text2 != NO_INT_TEXT) et2.setText(text2.toString())

        return builder.run {
            setView(root)
            setDialogOptions(option, {
                listener?.onTimeDialogNeutralClick(option.tag)
            }, {
                listener ?: return@setDialogOptions
                try {
                    val input1 = et1.text.toString().toInt()
                    val input2 = et2.text.toString().toInt()
                    listener?.onTimeDialogPositiveClick(input1, input2, option.tag)
                }
                catch (e: NumberFormatException) {
                    activity?.toast(R.string.fw_toast_e_input)
                }
            })

            show()
        }
    }

    //

    interface DialogListener : BaseDialog.DialogListener {
        fun onTimeDialogPositiveClick(input1: Int, input2: Int, tag: String)

        fun onTimeDialogNeutralClick(tag: String)
    }

    //

    companion object {
        @JvmStatic
        fun newInstance(
            option: DialogOption,
            text1: Int = NO_INT_TEXT,
            text2: Int = NO_INT_TEXT,
            hint1: String = "",
            hint2: String = "",
        ): TimeDialog = TimeDialog().apply {
            arguments = putBaseBundle(option).apply {
                putInt(ARG_TEXT1, text1)
                putInt(ARG_TEXT2, text2)
                putString(ARG_HINT1, hint1)
                putString(ARG_HINT2, hint2)
            }
        }
    }
}