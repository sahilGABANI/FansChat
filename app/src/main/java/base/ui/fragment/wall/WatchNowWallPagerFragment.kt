package base.ui.fragment.wall

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import base.data.model.wall.ContentItem
import base.databinding.FragmentWatchNowWallPagerBinding
import base.ui.base.BaseCarouselFragment
import com.google.android.exoplayer2.ui.StyledPlayerView

class WatchNowWallPagerFragment : BaseCarouselFragment() {
    private lateinit var binding: FragmentWatchNowWallPagerBinding

    companion object {
        fun newInstance(
            contentItem: ContentItem,
            pos: Int,
            groupPosition: Int/*,
            type: String*/
        ): WatchNowWallPagerFragment {
            val args = Bundle()
            args.putInt("pos", pos)
            args.putInt("groupPosition", groupPosition)
            args.putSerializable("contentItem", contentItem)
//            args.putString("type", type)
            val fragment = WatchNowWallPagerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWatchNowWallPagerBinding.inflate(inflater, container, false)
        setUpViews()
        return binding.root
    }

    override fun getImageViewRef(): ImageView {
        return binding.ivImage
    }

    override fun getVideoRootRef(): FrameLayout {
        return binding.flVideo
    }

    override fun getClickableViewRef(): LinearLayout {
        return binding.llClickable
    }

    override fun getProgressBarRef(): ProgressBar {
        return binding.progressBar
    }

    override fun getPlayPauseViewRef(): AppCompatImageView {
        return binding.ivPlayPause
    }

    override fun getPlayerViewRef(): StyledPlayerView {
        return binding.videoView
    }

    override fun getRootViewRef(): CardView {
        return binding.cardMain
    }


}
