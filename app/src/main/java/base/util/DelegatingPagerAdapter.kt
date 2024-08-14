package base.util

import android.database.DataSetObserver
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

open class DelegatingPagerAdapter(private val mDelegate: PagerAdapter) : PagerAdapter() {

    init {
        mDelegate.registerDataSetObserver(MyDataSetObserver(this))
    }

    fun getDelegate(): PagerAdapter {
        return mDelegate
    }

    override fun getCount(): Int {
        return mDelegate.count
    }

    override fun startUpdate(container: ViewGroup) {
        mDelegate.startUpdate(container)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return mDelegate.instantiateItem(container, position)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        if (position > -1) mDelegate.destroyItem(container, position, `object`)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        if (position > -1) mDelegate.setPrimaryItem(container, position, `object`)
    }

    override fun finishUpdate(container: ViewGroup) {
        try {
            mDelegate.finishUpdate(container)
        } catch (e: Exception) {
//            e.printStackTrace()
        }
    }

    @Suppress("DEPRECATION")
    override fun startUpdate(container: View) {
        mDelegate.startUpdate(container)
    }

    @Suppress("DEPRECATION")
    override fun instantiateItem(container: View, position: Int): Any {
        return mDelegate.instantiateItem(container, position)
    }

    @Suppress("DEPRECATION")
    override fun destroyItem(container: View, position: Int, `object`: Any) {
        if (position > -1) mDelegate.destroyItem(container, position, `object`)
    }

    @Suppress("DEPRECATION")
    override fun setPrimaryItem(container: View, position: Int, `object`: Any) {
        if (position > -1) mDelegate.setPrimaryItem(container, position, `object`)
    }

    @Suppress("DEPRECATION")
    override fun finishUpdate(container: View) {
        mDelegate.finishUpdate(container)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return mDelegate.isViewFromObject(view, `object`)
    }

    override fun saveState(): Parcelable? {
        return mDelegate.saveState()
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        mDelegate.restoreState(state, loader)
    }

    override fun getItemPosition(`object`: Any): Int {
        return mDelegate.getItemPosition(`object`)
    }

    override fun notifyDataSetChanged() {
        mDelegate.notifyDataSetChanged()
    }

    override fun registerDataSetObserver(observer: DataSetObserver) {
        mDelegate.registerDataSetObserver(observer)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {
        mDelegate.unregisterDataSetObserver(observer)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mDelegate.getPageTitle(position)
    }

    override fun getPageWidth(position: Int): Float {
        return mDelegate.getPageWidth(position)
    }

    private fun superNotifyDataSetChanged() {
        super.notifyDataSetChanged()
    }

    class MyDataSetObserver constructor(private val mParent: DelegatingPagerAdapter?) : DataSetObserver() {

        override fun onChanged() {
            mParent?.superNotifyDataSetChanged()
        }

        override fun onInvalidated() {
            onChanged()
        }

    }

}