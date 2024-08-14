package base.extension

import android.util.Patterns
import android.widget.EditText

fun EditText.isFieldBlank(): Boolean {
    if (this.text.toString().isEmpty()) {
        this.requestFocus()
        return true
    }
    return false
}

fun EditText.isNotValidLength(): Boolean {
    if (this.text.toString().trim().length < 3 || this.text.toString().trim().length > 12) {
        this.requestFocus()
        return true
    }
    return false
}

fun EditText.isNotValidPhoneLength(): Boolean {
    if (this.text.toString().trim().length < 8 || this.text.toString().trim().length > 15) {
        this.requestFocus()
        return true
    }
    return false
}

fun EditText.isNotValidEmail(): Boolean {
    if (!Patterns.EMAIL_ADDRESS.matcher(this.text.toString().trim()).matches()) {
        this.requestFocus()
        return true
    }
    return false
}

fun EditText.isNotValidPasswordLength(): Boolean {
    if (this.text.toString().trim().length < 6) {
        this.requestFocus()
        return true
    }
    return false
}