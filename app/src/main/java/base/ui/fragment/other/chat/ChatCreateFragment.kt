package base.ui.fragment.other.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import base.R
import base.application.FansChat
import base.data.api.authentication.LoggedInUserCache
import base.data.api.chat.model.CreateChatGroupRequest
import base.data.api.chat.model.CreateChatGroupResponse
import base.data.api.chat.model.DeleteChatGroupRequest
import base.data.api.chat.model.UpdateChatGroupRequest
import base.data.api.file_upload.UploadFile
import base.data.api.friendrequest.model.GetFriendRequestRequest
import base.data.api.users.model.FansChatUserDetails
import base.data.viewmodelmodule.ViewModelFactory
import base.databinding.FragmentChatCreateBinding
import base.extension.*
import base.socket.SocketDataManager
import base.ui.base.BaseFragment
import base.ui.fragment.other.chat.viewmodel.ChatViewModel
import base.ui.fragment.other.chat.viewmodel.ChatViewState
import base.ui.fragment.other.friends.my.FriendsAdapter
import base.ui.fragment.other.friends.my.FriendsAdapterSelectable
import base.util.*
import base.util.CommonUtils.Companion.selector
import base.views.CustomProgressDialog
import javax.inject.Inject

class ChatCreateFragment(
    val fromEdit: Boolean?,
    private val chatToEdit: CreateChatGroupResponse?,
    private var friendUser: FansChatUserDetails?
) : BaseFragment() {
    private val listFriends: MutableList<FansChatUserDetails> = mutableListOf()
    private lateinit var adapter: FriendsAdapterSelectable
    private lateinit var customProgressDialog: CustomProgressDialog

    private var _binding: FragmentChatCreateBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var socketDataManager: SocketDataManager

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory<ChatViewModel>
    private lateinit var chatViewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FansChat.component.inject(this)
        chatViewModel = getViewModelFromFactory(viewModelFactory)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("StringFormatInvalid")
    override fun onViewCreated(view: View, state: Bundle?) {
        customProgressDialog = CustomProgressDialog()
        adapter = FriendsAdapterSelectable(this, listFriends, loggedInUserCache, listFriends, context = requireContext())
        binding.friendsList.show(adapter)

        getFriends()
        listenToViewModel()

        binding.tvDeleteChat.throttleClicks().subscribeAndObserveOnMainThread {
            showCustomDialog(
                getString(R.string.delete_chat),
                getString(R.string.you_will_delete_chat),
                true
            ) {
                customProgressDialog.show(requireContext())
                deleteChat(chatToEdit!!)
            }
        }.autoDispose()
        binding.submit.onClick {
            binding.submit.isEnabled = false
            when {
                binding.name.text.isNullOrEmpty() -> {
                    binding.submit.isEnabled = true
                    showCustomDialog(
                        resources.getString(R.string.enter_chat_name),
                        resources.getString(R.string.you_need_to_enter_chat_name),
                        false
                    ) {}
                }

                selectedFriend.isEmpty() -> {
                    binding.submit.isEnabled = true
                    showCustomDialog(
                        getString(R.string.no_user_added),
                        getString(R.string.select_users),
                        false
                    ) {}
                }

                (fromEdit == true && chatToEdit!!.avatarUrl == null && selectedFile == null) || (fromEdit == false && selectedFile == null) -> {
                    binding.submit.isEnabled = true
                    showCustomDialog(
                        getString(R.string.select_chat_img),
                        getString(R.string.select_image),
                        false
                    ) {}
                }
                else -> {
                    customProgressDialog.show(requireContext())

                    selectedFile?.let {
                        UploadFile.upload(selectedFile!!).subscribe { it ->
                            customProgressDialog.dialog.dismiss()
                            if (fromEdit == true) {
                                chatToEdit!!.name =
                                    binding.name.get().ifBlank { generateChatName() }
                                chatToEdit.isPublic = binding.isPublic.isChecked
                                chatToEdit.avatarUrl =
                                    if (it == null && fromEdit == true) chatToEdit.avatarUrl else it
                                val arrayOfFriends = arrayListOf<String>()
                                arrayOfFriends.add(loggedInUserCache.getLoggedInUserId().toString())

                                selectedFriend.forEach {
                                    arrayOfFriends.add(it.id.toString())
                                }

                                chatViewModel.updateChatGroup(
                                    UpdateChatGroupRequest(
                                        chatToEdit.id,
                                        binding.name.get().ifBlank { generateChatName() },
                                        chatToEdit.avatarUrl,
                                        binding.isPublic.isChecked,
                                        false,
                                        arrayOfFriends
                                    )
                                )
                            } else {
                                val arrayOfFriends = arrayListOf<String>()
                                selectedFriend.forEach {
                                    arrayOfFriends.add(it.id)
                                }
                                customProgressDialog.show(requireContext())
                                chatViewModel.createChatGroup(
                                    CreateChatGroupRequest(
                                        binding.name.get().ifBlank { generateChatName() },
                                        it,
                                        binding.isPublic.isChecked,
                                        false,
                                        arrayOfFriends
                                    )
                                )
                            }
                        }
                    }

                    if (selectedFile == null && fromEdit == true) {
                        if (chatToEdit != null) {
                            val arrayOfFriends = arrayListOf<String>()
                            arrayOfFriends.add(loggedInUserCache.getLoggedInUserId().toString())

                            selectedFriend.forEach {
                                arrayOfFriends.add(it.id.toString())
                            }

                            chatViewModel.updateChatGroup(
                                UpdateChatGroupRequest(
                                    chatToEdit.id,
                                    binding.name.get().ifBlank { generateChatName() },
                                    chatToEdit.avatarUrl,
                                    binding.isPublic.isChecked,
                                    false,
                                    arrayOfFriends
                                )
                            )
                        }
                    }
                }
            }
        }
        binding.changeImage.onClick {
            selector(
                items = arrayOf(
                    getString(R.string.take_photo),
                    getString(R.string.from_gallery)
                ), onClick = { _, i ->
                    when (i) {
                        0 -> takePhotos {
                            binding.image.visibility = View.VISIBLE
                            binding.image.show(selectedFile)
                        }
                        1 -> selectImages {
                            binding.image.visibility = View.VISIBLE
                            binding.image.show(selectedFile)
                        }
                    }
                })
        }
        binding.name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.tvAddFriendToName.text = getString(
                    R.string.chat_friends_add,
                    binding.name.text.ifEmpty { getString(R.string.unnamed_chat) }
                )
            }
        })
        binding.etSearchFrnd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                adapter.filter(binding.etSearchFrnd.get())
            }
        })

        if (fromEdit == true) {
            binding.name.setText(chatToEdit!!.name)
            binding.image.show(chatToEdit.avatarUrl)
            binding.isPublic.isChecked = chatToEdit.isPublic
            binding.image.visibility = View.VISIBLE
            binding.submit.text = getString(R.string.save)
            binding.linSwitch.visibility = View.GONE
            binding.tvDeleteChat.visibility = View.VISIBLE
        } else {
            binding.tvAddFriendToName.text =
                getString(R.string.chat_friends_add, getString(R.string.unnamed_chat))
        }
    }

    private fun generateChatName(): String {
        return if (selectedFriend.size == 1) {
            selectedFriend[0].displayName.toString()
        } else {
            selectedFriend[0].displayName.toString() + " and " + (selectedFriend.size - 1) + " others"
        }
    }

    fun updateBottomList() {
        binding.rvSelectedFriend.show(
            FriendsAdapter(
                isUserClickable = false,
                isGrid = false,
                loggedInUserCache,
                items = selectedFriend,
                context = requireContext()
            )
        )
        when {
            selectedFriend.isNullOrEmpty() -> {
                binding.tvSelectedFrnd.text = getString(R.string.friends_in_chat)
            }
            selectedFriend.size == 1 -> {
                binding.tvSelectedFrnd.text = getString(R.string.friend_in_chat)
            }
            else -> {
                binding.tvSelectedFrnd.text =
                    "" + selectedFriend.size + " " + getString(R.string.friends_in_chat)
            }
        }
    }

    /* API CALLS */
    private fun deleteChat(item: CreateChatGroupResponse) {
        chatViewModel.deleteChatGroup(DeleteChatGroupRequest(item.id))
    }

    private fun getFriends() {
        chatViewModel.getFriendList(GetFriendRequestRequest(20, 1))

    }

    /* END API CALLS */

    private fun listenToViewModel() {
        chatViewModel.createPostState.subscribeAndObserveOnMainThread {
            when (it) {
                is ChatViewState.ErrorMessage -> {
                    showToast(it.errorMessage)
                }
                is ChatViewState.LoadingState -> {
//                    binding.progressBar.visibility = if (it.isLoading) View.VISIBLE else View.GONE
//                    binding.save.visibility = if (it.isLoading) View.GONE else View.VISIBLE
                }
                is ChatViewState.GetListOfFriends -> {
                    listFriends.clear()
                    listFriends.addAll(it.listOfUser)

                    if (fromEdit == true && chatToEdit != null) {
                        selectedFriend.clear()
                        for (id in chatToEdit.usersIds) {
                            if (id == loggedInUserCache.getLoggedInUserId()) continue
                            val users: List<FansChatUserDetails> =
                                listFriends.filter { it.id == id }
                            if (users.isNotEmpty()) {
                                selectedFriend.add(users.first())
                            }
                        }
                        updateBottomList()
                    }

                    if (friendUser != null) {
                        selectedFriend.clear()
                        val users: List<FansChatUserDetails> =
                            listFriends.filter { it.id == friendUser!!.id }
                        if (users.isNotEmpty()) {
                            selectedFriend.add(users.first())
                        }
                        friendUser = null
                        updateBottomList()
                    }
                    adapter.filter(binding.etSearchFrnd.get())
                }
                is ChatViewState.SuccessMessage -> {
                    customProgressDialog.dialog.dismiss()
                    goBack()
                }
                is ChatViewState.CreateChatGroup -> {
                    customProgressDialog.dialog.dismiss()
                    goBack()
                }
                is ChatViewState.UpdateChatGroup -> {
                    customProgressDialog.dialog.dismiss()
                    goBack()
                }
                else -> {}
            }
        }.autoDispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        selectedFriend.clear()
    }
}
