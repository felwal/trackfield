package com.felwal.trackfield.ui.widget.dialog

import android.os.Bundle
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.felwal.android.util.toast
import com.felwal.android.widget.dialog.BaseDialog
import com.felwal.android.widget.dialog.NO_INT_TEXT
import com.felwal.trackfield.R

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
        val et1 = root.findViewById<EditText>(R.id.et_dialog_time_minutes)
        val et2 = root.findViewById<EditText>(R.id.et_dialog_time_seconds)

        et1.hint = hint1
        et2.hint = hint2
        if (text1 != NO_INT_TEXT) et1.setText(text1.toString())
        if (text2 != NO_INT_TEXT) et2.setText(text2.toString())

        return builder.run {
            setView(root)
            setTitle(title)
            if (message != "") setMessage(message)

            setPositiveButton(posBtnTxtRes) { _, _ ->
                listener ?: return@setPositiveButton
                try {
                    val input1 = et1.text.toString().toInt()
                    val input2 = et2.text.toString().toInt()
                    listener?.onTimeDialogPositiveClick(input1, input2, dialogTag)
                }
                catch (e: NumberFormatException) {
                    activity?.toast(R.string.fw_toast_e_input)
                }
            }
            setNeutralButton(neuBtnTxtRes!!) { _, _ ->
                listener?.onTimeDialogNeutralClick(dialogTag)
            }
            setCancelButton(negBtnTxtRes)

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
            title: String,
            message: String = "",
            text1: Int = NO_INT_TEXT,
            text2: Int = NO_INT_TEXT,
            hint1: String = "",
            hint2: String = "",
            @StringRes neuBtnTxtRes: Int,
            @StringRes posBtnTxtRes: Int = R.string.fw_dialog_btn_ok,
            @StringRes negBtnTxtRes: Int = R.string.fw_dialog_btn_cancel,
            tag: String,
            passValue: String? = null
        ): TimeDialog = TimeDialog().apply {
            arguments = putBaseBundle(title, message, posBtnTxtRes, negBtnTxtRes, tag, passValue).apply {
                putInt(ARG_TEXT1, text1)
                putInt(ARG_TEXT2, text2)
                putString(ARG_HINT1, hint1)
                putString(ARG_HINT2, hint2)
                putInt(ARG_NEUTRAL_BUTTON_RES, neuBtnTxtRes)
            }
        }
    }
}