package base.ui.base

import android.app.Dialog
import android.view.View
import androidx.fragment.app.Fragment
import base.R
import base.databinding.DialogConfirmationBinding
import base.extension.showToast
import base.util.AudioPlayer
import base.util.hideKeyboard
import base.util.selectedFile
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseFragment : Fragment() {

    val compositeDisposable = CompositeDisposable()

    fun Disposable.autoDispose() {
        compositeDisposable.add(this)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    fun clearObservers() {
        compositeDisposable.clear()
    }
/*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
    }*/

    fun showCustomDialog(title: String, text: String, showCancel: Boolean, callback: () -> Unit?) {
        val dialog = Dialog(requireContext())
        dialog.window?.setWindowAnimations(R.style.DialogAnimation)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val binding: DialogConfirmationBinding = DialogConfirmationBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)

        binding.ivDismiss.setOnClickListener {
            dialog.dismiss()
        }
        binding.ivCancel.setOnClickListener {
            dialog.dismiss()
        }
        binding.ivOkay.setOnClickListener {
            dialog.dismiss()
            callback.invoke()
        }

        binding.tvTitle.text = title
        binding.tvDesc.text = text

        binding.ivCancel.visibility = if (showCancel) View.VISIBLE else View.GONE
        binding.ivDismiss.visibility = if (showCancel) View.VISIBLE else View.GONE

        dialog.show()
    }

    fun showCustomWithEditTextDialog(
        title: String,
        text: String,
        showCancel: Boolean,
        editText: Boolean = false,
        callback: (String?) -> Unit?
    ) {
        val dialog = Dialog(requireContext())
        dialog.window?.setWindowAnimations(R.style.DialogAnimation)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val binding: DialogConfirmationBinding = DialogConfirmationBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)

        binding.reportAppCompatEditText.visibility = if (editText) View.VISIBLE else View.GONE

        binding.ivDismiss.setOnClickListener {
            dialog.dismiss()
        }
        binding.ivCancel.setOnClickListener {
            dialog.dismiss()
        }
        binding.ivOkay.setOnClickListener {
            if (binding.reportAppCompatEditText.text.toString().isEmpty()
            ) showToast(resources.getString(R.string.enter_report_message))
            else {
                dialog.dismiss()
                if (editText) callback.invoke(binding.reportAppCompatEditText.text.toString())
            }
        }

        binding.tvTitle.text = title
        binding.tvDesc.text = text

        binding.ivCancel.visibility = if (showCancel) View.VISIBLE else View.GONE
        binding.ivDismiss.visibility = if (showCancel) View.VISIBLE else View.GONE

        dialog.show()
    }

    fun viewAudio(audioUrl: String) {
        AudioPlayer(
            requireActivity(),
            requireActivity().isFinishing,
            childFragmentManager,
            audioUrl
        ).playAudio()
    }

    protected open fun isSafe(): Boolean {
        return !(this.isRemoving || this.activity == null || this.isDetached || !this.isAdded || this.view == null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
        selectedFile = null
    }
}