/*
 * MIT License
 *
 * Copyright (c) 2016 Knowledge, education for life.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package video.trimmer.view

import android.content.res.Resources
import android.graphics.Bitmap
import video.trimmer.view.Thumb
import base.R
import android.graphics.BitmapFactory
import java.util.*

class Thumb private constructor() {
    var index = 0
        private set
    var `val` = 0f
    var pos = 0f
    var bitmap: Bitmap? = null
        private set
    var widthBitmap = 0
        private set
    private var heightBitmap = 0
    var lastTouchX = 0f
    private fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        widthBitmap = bitmap.width
        heightBitmap = bitmap.height
    }

    companion object {
        const val LEFT = 0
        const val RIGHT = 1
        fun initThumbs(resources: Resources?): List<Thumb> {
            val thumbs: MutableList<Thumb> = Vector()
            for (i in 0..1) {
                val th = Thumb()
                th.index = i
                if (i == 0) {
                    val resImageLeft = R.drawable.apptheme_text_select_handle_left
                    th.setBitmap(BitmapFactory.decodeResource(resources, resImageLeft))
                } else {
                    val resImageRight = R.drawable.apptheme_text_select_handle_right
                    th.setBitmap(BitmapFactory.decodeResource(resources, resImageRight))
                }
                thumbs.add(th)
            }
            return thumbs
        }

        fun getWidthBitmap(thumbs: List<Thumb>): Int {
            return thumbs[0].widthBitmap
        }

        fun getHeightBitmap(thumbs: List<Thumb>): Int {
            return thumbs[0].heightBitmap
        }
    }
}