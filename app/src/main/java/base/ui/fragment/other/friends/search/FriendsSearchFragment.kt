package base.ui.fragment.other.friends.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import base.R
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.viewmodelmodule.ViewModelFactory
import base.databinding.FragmentFriendsSearchBinding
import base.extension.*
import base.ui.base.BaseFragment
import base.ui.fragment.other.friends.my.FriendRequestAdapter
import base.ui.fragment.other.friends.search.viewmodel.SearchFriendViewModel
import base.ui.fragment.other.friends.search.viewmodel.SearchFriendViewState
import base.util.LinearLayoutManagerWrapper
import base.util.hideKeyboards
import base.util.open
import com.jakewharton.rxbinding3.widget.editorActions
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FriendsSearchFragment : BaseFragment() {

    private var _binding: FragmentFriendsSearchBinding? = null
    private val binding get() = _binding!!

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<SearchFriendViewModel>
    private lateinit var searchFriendViewModel: SearchFriendViewModel

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    private lateinit var friendRequestAdapter: FriendRequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FansChat.component.inject(this)
        searchFriendViewModel = getViewModelFromFactory(viewModelFactory)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFriendsSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        listenToViewModel()
        listenToViewEvents()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun listenToViewModel() {
        searchFriendViewModel.searchFriendState.subscribeAndObserveOnMainThread { it ->
            when (it) {
                is SearchFriendViewState.ErrorMessage -> {
                    if (it.errorCode == 401) {
                        goBack()
                        open(R.id.authDecideFragment)
                    } else showToast(it.errorMessage)
                }
                is SearchFriendViewState.LoadingState -> {
//                    binding.progressBar.visibility = if (it.isLoading) View.VISIBLE else View.GONE
                }
                is SearchFriendViewState.ListOfUser -> {
                    friendRequestAdapter.updateAdapter(it.listOfSearchUser)
                }
            }
        }.autoDispose()
    }

    private fun listenToViewEvents() {
        binding.etSearch.editorActions().filter { action -> action == EditorInfo.IME_ACTION_SEARCH }
            .subscribeAndObserveOnMainThread {
                hideKeyboards(binding.etSearch)
            }.autoDispose()

        binding.etSearch.textChanges().doOnNext {
            manageClearIconVisibility(it.isNotEmpty())
        }.debounce(500, TimeUnit.MILLISECONDS, Schedulers.io()).subscribeOnIoAndObserveOnMainThread({
            Timber.i("SearchString %s", it.toString())
            if (it.length > 1) {
                searchFriendViewModel.searchFriendRequest(binding.etSearch.text.toString())
            } else if (it.isEmpty()) {
                searchFriendViewModel.searchFriendRequest()
            }
        }, {
            Timber.e(it)
        }).autoDispose()

        binding.ivClearText.throttleClicks().subscribeAndObserveOnMainThread {
            binding.etSearch.text.clear()
            showUsers()
            view?.let { it1 -> hideKeyboards(it1) }
        }.autoDispose()

        friendRequestAdapter =
            FriendRequestAdapter(requireContext(), isGrid = true, userId = loggedInUserCache.getLoggedInUserId())
        binding.list.apply {
            layoutManager = LinearLayoutManagerWrapper(context, 3)
            adapter = friendRequestAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!recyclerView.canScrollVertically(1)) { //1 for down
                        if (binding.etSearch.text.toString().length > 1) {
                            searchFriendViewModel.loadMore(binding.etSearch.text.toString())
                        } else {
                            searchFriendViewModel.loadMore()
                        }
                    }
                }
            })
        }
    }

    private fun manageClearIconVisibility(isVisible: Boolean) {
        binding.ivClearText.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun showUsers() {
        searchFriendViewModel.resetLoading()
        if (binding.etSearch.text.toString().length > 1) {
            searchFriendViewModel.searchFriendRequest(binding.etSearch.text.toString())
        } else {
            searchFriendViewModel.searchFriendRequest()
        }
    }

    override fun onResume() {
        super.onResume()
        searchFriendViewModel.resetLoading()
        searchFriendViewModel.searchFriendRequest()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}