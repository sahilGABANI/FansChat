package base.ui

import android.os.Bundle
import base.BaseActivity
import base.R
import base.databinding.ActivityFullScreenBinding
import base.util.onClick
import com.bumptech.glide.Glide

class FullScreenImageActivity : BaseActivity() {

    private var imageToOpen: String = ""
    lateinit var binding: ActivityFullScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imageToOpen = intent.getStringExtra("imageToOpen")!!

        Glide.with(this).load(imageToOpen).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(binding.photoView)

        binding.backButtonMain.onClick { onBackPressed() }
    }
}