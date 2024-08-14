package base.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import base.ui.base.BaseFragment
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


class CommonUtils {
    companion object {
        fun dpToPx(context: Context, dip: Float): Float {
            val r: Resources = context.resources
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.displayMetrics)
        }

        //time conversion
        fun timeConversion(value: Long): String {
            val songTime: String
            val dur = value.toInt()
            val hrs = dur / 3600000
            val mns = dur / 60000 % 60000
            val scs = dur % 60000 / 1000
            songTime = if (hrs > 0) {
                String.format(Locale.ENGLISH, "%02d:%02d:%02d", hrs, mns, scs)
            } else {
                String.format(Locale.ENGLISH, "%02d:%02d", mns, scs)
            }
            return songTime
        }

        fun isRtl(context: Context): Boolean {
            return TextUtils.getLayoutDirectionFromLocale(getCurrentLocale(context)) == ViewCompat.LAYOUT_DIRECTION_RTL
        }

        private fun getCurrentLocale(context: Context): Locale? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales[0]
            } else {
                context.resources.configuration.locale
            }
        }

        fun getDeviceHeight(context: Context): Int {
            return context.resources.displayMetrics.heightPixels
        }

        fun getDeviceWidth(context: Context): Int {
            return context.resources.displayMetrics.widthPixels
        }

        fun getDeviceSizeInInch(activity: Context): Double {
            val point = Point()
            (activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealSize(point)
            val displayMetrics = activity.resources.displayMetrics
            val width: Int = point.x
            val height: Int = point.y
            val wi = width.toDouble() / displayMetrics.xdpi.toDouble()
            val hi = height.toDouble() / displayMetrics.ydpi.toDouble()
            val x = wi.pow(2.0)
            val y = hi.pow(2.0)
            return (sqrt(x + y) * 10.0).roundToInt() / 10.0
        }

        fun getActualScreenHeight(context: Context): Int {
            return context.resources.displayMetrics.heightPixels// - getStatusBarHeight(context)
        }

        private fun getStatusBarHeight(context: Context): Int {
            var result = 0
            val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = context.resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

        fun Context.share(text: String, subject: String = "", title: String? = null): Boolean {
            return try {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, subject)
                intent.putExtra(Intent.EXTRA_TEXT, text)
                startActivity(Intent.createChooser(intent, title))
                true
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                false
            }
        }

        fun BaseFragment.selector(title: CharSequence? = null, items: Array<String>, onClick: (DialogInterface, Int) -> Unit) {
            val alertBuilder = AlertDialog.Builder(requireContext())
            alertBuilder.setTitle(title)
            alertBuilder.setItems(items) { dialog, index ->
                onClick.invoke(dialog, index)
            }
            alertBuilder.create().show()
        }

    }
}

class ViewChildrenRecursiveSequence(private val view: View) : Sequence<View> {
    override fun iterator(): Iterator<View> {
        if (view !is ViewGroup) return emptyList<View>().iterator()
        return RecursiveViewIterator(view)
    }

    private class RecursiveViewIterator(view: View) : Iterator<View> {
        private val sequences = arrayListOf(ViewChildrenSequence(view))
        private var current = sequences.removeLast().iterator()

        override fun next(): View {
            if (!hasNext()) throw NoSuchElementException()
            val view = current.next()
            if (view is ViewGroup && view.childCount > 0) {
                sequences.add(ViewChildrenSequence(view))
            }
            return view
        }

        override fun hasNext(): Boolean {
            if (!current.hasNext() && sequences.isNotEmpty()) {
                current = sequences.removeLast().iterator()
            }
            return current.hasNext()
        }

        @Suppress("NOTHING_TO_INLINE")
        private inline fun <T : Any> MutableList<T>.removeLast(): T {
            if (isEmpty()) throw NoSuchElementException()
            return removeAt(size - 1)
        }
    }


    private class ViewChildrenSequence(private val view: View) : Sequence<View> {
        override fun iterator(): Iterator<View> {
            if (view !is ViewGroup) return emptyList<View>().iterator()
            return ViewIterator(view)
        }

        private class ViewIterator(private val view: ViewGroup) : Iterator<View> {
            private var index = 0
            private val count = view.childCount

            override fun next(): View {
                if (!hasNext()) throw NoSuchElementException()
                return view.getChildAt(index++)
            }

            override fun hasNext(): Boolean {
                checkCount()
                return index < count
            }

            private fun checkCount() {
                if (count != view.childCount) throw ConcurrentModificationException()
            }
        }
    }

}