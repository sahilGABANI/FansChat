package base.ui.fragment.wall

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import base.R
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.model.wall.ContentItem
import base.databinding.FragmentNewsWallPagerBinding
import base.ui.adapter.wall.NewsWallAdapter
import base.ui.base.BaseFragment
import base.util.GridSpacingItemDecoration
import javax.inject.Inject

class NewsWallPagerFragment : BaseFragment() {
    private lateinit var binding: FragmentNewsWallPagerBinding
    private var groupPosition: Int = 0
    private var list: ArrayList<ContentItem> = arrayListOf()

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    companion object {
        fun newInstance(list: ArrayList<ContentItem>, pos: Int): NewsWallPagerFragment {
            val args = Bundle()
            args.putInt("groupPosition", pos)
            args.putSerializable("list", list)
            val fragment = NewsWallPagerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FansChat.component.inject(this)
        groupPosition = requireArguments().getInt("groupPosition", 0)
        list = requireArguments().getSerializable("list") as ArrayList<ContentItem>
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View? {
        binding = FragmentNewsWallPagerBinding.inflate(inflater, container, false)
        binding.rvNews.adapter = NewsWallAdapter(this, list, loggedInUserCache)
        binding.rvNews.addItemDecoration(
            GridSpacingItemDecoration(
                2,
                resources.getDimensionPixelSize(R.dimen.spacing_wall_item_horizontal),
                false
            )
        )
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }
}
