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
package video.trimmer.utils

import com.googlecode.mp4parser.authoring.Track
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator
import com.googlecode.mp4parser.authoring.tracks.AppendTrack
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack
import timber.log.Timber
import video.trimmer.interfaces.OnTrimVideoListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object TrimVideoUtils {
    private val TAG = TrimVideoUtils::class.java.simpleName
    @Throws(IOException::class)
    fun startTrim(src: File, dst: String, startMs: Long, endMs: Long, callback: OnTrimVideoListener) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "MP4_$timeStamp.mp4"
        val filePath = dst + fileName
        val file = File(filePath)
        if (file.parentFile != null && !file.parentFile.exists()) if (!file.parentFile.mkdirs()) {
            Timber.d("Fail to create dir")
        }
        Timber.tag(TAG).d("Generated file path $filePath")
        genVideoUsingMp4Parser(src, file, startMs, endMs, callback)
    }

    @Throws(IOException::class)
    private fun genVideoUsingMp4Parser(src: File, dst: File, startMs: Long, endMs: Long, callback: OnTrimVideoListener) {
        // NOTE: Switched to using FileDataSourceViaHeapImpl since it does not use memory mapping (VM).
        // Otherwise we get OOM with large movie files.
        val movie = MovieCreator.build(src.absolutePath /*new FileDataSourceViaHeapImpl(src.getAbsolutePath())*/)
        val tracks = movie.tracks
        movie.tracks = LinkedList()
        // remove all tracks we will create new tracks from the old
        var startTime1 = startMs / 1000.0
        var endTime1 = endMs / 1000.0
        var timeCorrected = false

        // Here we try to find a track that has sync samples. Since we can only start decoding
        // at such a sample we SHOULD make sure that the start of the new fragment is exactly
        // such a frame
        for (track in tracks) {
            if (track.syncSamples != null && track.syncSamples.size > 0) {
                if (timeCorrected) {
                    // This exception here could be a false positive in case we have multiple tracks
                    // with sync samples at exactly the same positions. E.g. a single movie containing
                    // multiple qualities of the same video (Microsoft Smooth Streaming file)
                    throw RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.")
                }
                startTime1 = correctTimeToSyncSample(track, startTime1, false)
                endTime1 = correctTimeToSyncSample(track, endTime1, true)
                timeCorrected = true
            }
        }
        for (track in tracks) {
            var currentSample: Long = 0
            var currentTime = 0.0
            var lastTime = -1.0
            var startSample1: Long = -1
            var endSample1: Long = -1
            for (i in track.sampleDurations.indices) {
                val delta = track.sampleDurations[i]
                if (currentTime > lastTime && currentTime <= startTime1) {
                    // current sample is still before the new starttime
                    startSample1 = currentSample
                }
                if (currentTime > lastTime && currentTime <= endTime1) {
                    // current sample is after the new start time and still before the new endtime
                    endSample1 = currentSample
                }
                lastTime = currentTime
                currentTime += delta.toDouble() / track.trackMetaData.timescale.toDouble()
                currentSample++
            }
            movie.addTrack(AppendTrack(CroppedTrack(track, startSample1, endSample1)))
        }
        if (dst.parentFile != null && !dst.parentFile.exists()) if (!dst.parentFile.mkdirs()) {
            Timber.d("Fail to create dir")
        }
        if (!dst.exists()) {
            dst.createNewFile()
        }
        val out = DefaultMp4Builder().build(movie)
        val fos = FileOutputStream(dst)
        val fc = fos.channel
        out.writeContainer(fc)
        fc.close()
        fos.close()
        if (callback != null) callback.getResult(dst.absolutePath)
    }

    private fun correctTimeToSyncSample(track: Track, cutHere: Double, next: Boolean): Double {
        val timeOfSyncSamples = DoubleArray(track.syncSamples.size)
        var currentSample: Long = 0
        var currentTime = 0.0
        for (i in track.sampleDurations.indices) {
            val delta = track.sampleDurations[i]
            if (Arrays.binarySearch(track.syncSamples, currentSample + 1) >= 0) {
                // samples always start with 1 but we start with zero therefore +1
                timeOfSyncSamples[Arrays.binarySearch(track.syncSamples, currentSample + 1)] = currentTime
            }
            currentTime += delta.toDouble() / track.trackMetaData.timescale.toDouble()
            currentSample++
        }
        var previous = 0.0
        for (timeOfSyncSample in timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                return if (next) {
                    timeOfSyncSample
                } else {
                    previous
                }
            }
            previous = timeOfSyncSample
        }
        return timeOfSyncSamples[timeOfSyncSamples.size - 1]
    }

    fun stringForTime(timeMs: Int): String {
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        val mFormatter = Formatter()
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }
}