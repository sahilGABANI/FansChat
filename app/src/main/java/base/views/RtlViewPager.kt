package base.views


import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import base.util.DelegatingPagerAdapter

open class RtlViewPager : ViewPager {

    private val mPageChangeListeners: HashMap<OnPageChangeListener, ReversingOnPageChangeListener> =
        hashMapOf()
    var mLayoutDirection = ViewCompat.LAYOUT_DIRECTION_LTR

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        super.onRtlPropertiesChanged(layoutDirection)
        val viewCompatLayoutDirection =
            if (layoutDirection == LAYOUT_DIRECTION_RTL) ViewCompat.LAYOUT_DIRECTION_RTL else ViewCompat.LAYOUT_DIRECTION_LTR
        if (viewCompatLayoutDirection != mLayoutDirection) {
            val adapter = super.getAdapter()
            var position = 0
            if (adapter != null) {
                position = currentItem
            }
            mLayoutDirection = viewCompatLayoutDirection
            if (adapter != null) {
                adapter.notifyDataSetChanged()
                currentItem = position
            }
        }
    }

    override fun setAdapter(adapter: PagerAdapter?) {
        var mAdapter = adapter
        if (mAdapter != null) {
            mAdapter = ReversingAdapter(mAdapter)
        }
        super.setAdapter(mAdapter)
//        setCurrentItem(0);
    }

    override fun getAdapter(): PagerAdapter? {
        return (super.getAdapter() as ReversingAdapter?)?.getDelegate()
    }

    fun isRtl(): Boolean {
        return mLayoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL
    }

    override fun getCurrentItem(): Int {
        var item = super.getCurrentItem()
        val adapter = super.getAdapter()
        if (adapter != null && isRtl()) {
            item = adapter.count - item - 1
        }
        return item
    }

    override fun setCurrentItem(position: Int, smoothScroll: Boolean) {
        var mPosition = position
        val adapter = super.getAdapter()
        if (adapter != null && isRtl()) {
            mPosition = adapter.count - mPosition - 1
        }
        super.setCurrentItem(mPosition, smoothScroll)
    }

    override fun setCurrentItem(position: Int) {
        var mPosition = position
        val adapter = super.getAdapter()
        if (adapter != null && isRtl()) {
            mPosition = adapter.count - mPosition - 1
        }
        super.setCurrentItem(mPosition)
    }

    @Suppress("DEPRECATION")
    override fun setOnPageChangeListener(listener: OnPageChangeListener) {
        super.setOnPageChangeListener(ReversingOnPageChangeListener(listener))
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return SavedState(superState, mLayoutDirection)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        val ss = state
        mLayoutDirection = ss.mLayoutDirection
        super.onRestoreInstanceState(ss.mViewPagerSavedState)
    }

    class SavedState : Parcelable {
        val mViewPagerSavedState: Parcelable?
        val mLayoutDirection: Int

        constructor(viewPagerSavedState: Parcelable?, layoutDirection: Int) {
            mViewPagerSavedState = viewPagerSavedState
            mLayoutDirection = layoutDirection
        }

        constructor(parcel: Parcel, mLoader: ClassLoader?) {
            var loader: ClassLoader? = mLoader
            if (loader == null) {
                loader = javaClass.classLoader
            }
            mViewPagerSavedState = parcel.readParcelable(loader)
            mLayoutDirection = parcel.readInt()
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            out.writeParcelable(mViewPagerSavedState, flags)
            out.writeInt(mLayoutDirection)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel, null)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun addOnPageChangeListener(listener: OnPageChangeListener) {
        val reversingListener = ReversingOnPageChangeListener(listener)
        mPageChangeListeners[listener] = reversingListener
        super.addOnPageChangeListener(reversingListener)
    }

    override fun removeOnPageChangeListener(listener: OnPageChangeListener) {
        val reverseListener: ReversingOnPageChangeListener? = mPageChangeListeners.remove(listener)
        if (reverseListener != null) {
            super.removeOnPageChangeListener(reverseListener)
        }
    }

    override fun clearOnPageChangeListeners() {
        super.clearOnPageChangeListeners()
        mPageChangeListeners.clear()
    }

    override fun onMeasure(widthMeasureSpec: Int, mHeightMeasureSpec: Int) {
        var heightMeasureSpec = mHeightMeasureSpec
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            var height = 0
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                child.measure(
                    widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
                val h = child.measuredHeight
                if (h > height) {
                    height = h
                }
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    inner class ReversingOnPageChangeListener(private val mListener: OnPageChangeListener) :
        OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            var mPosition: Int = position
            var mPositionOffset: Float = positionOffset
            var mPositionOffsetPixels: Int = positionOffsetPixels
            val width = width
            val adapter = super@RtlViewPager.getAdapter()
            if (isRtl() && adapter != null) {
                val count = adapter.count
                var remainingWidth =
                    (width * (1 - adapter.getPageWidth(mPosition))).toInt() + mPositionOffsetPixels
                while (mPosition < count && remainingWidth > 0) {
                    mPosition += 1
                    remainingWidth -= (width * adapter.getPageWidth(mPosition)).toInt()
                }
                mPosition = count - mPosition - 1
                mPositionOffsetPixels = -remainingWidth
                mPositionOffset = mPositionOffsetPixels / (width * adapter.getPageWidth(mPosition))
            }
            mListener.onPageScrolled(mPosition, mPositionOffset, mPositionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
            var mPosition: Int = position
            val adapter = super@RtlViewPager.getAdapter()
            if (isRtl() && adapter != null) {
                mPosition = adapter.count - mPosition - 1
            }
            mListener.onPageSelected(mPosition)
        }

        override fun onPageScrollStateChanged(state: Int) {
            mListener.onPageScrollStateChanged(state)
        }
    }

    inner class ReversingAdapter(adapter: PagerAdapter) : DelegatingPagerAdapter(adapter) {
        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            var mPosition = position
            if (isRtl()) {
                mPosition = count - mPosition - 1
            }
            super.destroyItem(container, mPosition, `object`)
        }

        override fun destroyItem(container: View, position: Int, `object`: Any) {
            var mPosition = position
            if (isRtl()) {
                mPosition = count - mPosition - 1
            }
            super.destroyItem(container, mPosition, `object`)
        }

        override fun getItemPosition(`object`: Any): Int {
            var position = super.getItemPosition(`object`)
            if (isRtl()) {
                position = if (position == POSITION_UNCHANGED || position == POSITION_NONE) {
                    // We can't accept POSITION_UNCHANGED when in RTL mode because adding items to the end of the collection adds them to the beginning of the
                    // ViewPager.  Items whose positions do not change from the perspective of the wrapped adapter actually do change from the perspective of
                    // the ViewPager.
                    POSITION_NONE
                } else {
                    count - position - 1
                }
            }
            return position
        }

        override fun getPageTitle(position: Int): CharSequence? {
            var mPosition = position
            if (isRtl()) {
                mPosition = count - mPosition - 1
            }
            return super.getPageTitle(mPosition)
        }

        override fun getPageWidth(position: Int): Float {
            var mPosition = position
            if (isRtl()) {
                mPosition = count - mPosition - 1
            }
            return super.getPageWidth(mPosition)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var mPosition = position
            if (isRtl()) {
                mPosition = count - mPosition - 1
            }
            return super.instantiateItem(container, mPosition)
        }

        @Suppress("DEPRECATION")
        override fun instantiateItem(container: View, position: Int): Any {
            var mPosition = position
            if (isRtl()) {
                mPosition = count - mPosition - 1
            }
            return super.instantiateItem(container, mPosition)
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            var mPosition = position
            if (isRtl()) {
                mPosition = count - mPosition - 1
            }
            super.setPrimaryItem(container, mPosition, `object`)
        }

        @Suppress("DEPRECATION")
        override fun setPrimaryItem(container: View, position: Int, `object`: Any) {
            var mPosition = position
            if (isRtl()) {
                mPosition = count - mPosition - 1
            }
            super.setPrimaryItem(container, mPosition, `object`)
        }
    }
}