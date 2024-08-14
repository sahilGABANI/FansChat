package base.views


import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import base.R


class CustomProgressDialog {

    lateinit var dialog: CustomDialog

    fun show(context: Context): Dialog {
        val view: View = LayoutInflater.from(context).inflate(
            R.layout.dialog_custom, null
        )
        dialog = CustomDialog(context)
        dialog.setContentView(view)
        dialog.show()
        return dialog
    }

    class CustomDialog(context: Context) : Dialog(context) {
        init {
            window?.decorView?.rootView?.setBackgroundResource(android.R.color.transparent)
            window?.decorView?.setOnApplyWindowInsetsListener { _, insets ->
                insets.consumeSystemWindowInsets()
            }
        }
    }

    /*fun CustomProgressDialog(context: Context?) {
        val wlmp = window!!.attributes
        wlmp.gravity = Gravity.CENTER_HORIZONTAL
        window!!.attributes = wlmp
        setTitle(null)
        setCancelable(false)
        setOnCancelListener(null)
        val view: View = LayoutInflater.from(context).inflate(
            R.layout.dialog_custom, null
        )
        setContentView(view)
    }

    fun showDialog() {
        show()
    }

    fun hideDialog() {
        if (isShowing) dismiss()
    }*/
}