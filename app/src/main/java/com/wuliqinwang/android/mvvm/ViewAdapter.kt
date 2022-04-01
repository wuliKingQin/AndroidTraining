package com.wuliqinwang.android.mvvm

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition

object ViewAdapter {

    @JvmStatic
    @BindingAdapter("bgIcon")
    fun setBackground(view: View, imageRes: Any?) {
        when (imageRes) {
            is Int -> {
                try {
                    view.setBackgroundResource(imageRes)
                } catch (e: Exception) {
                    try {
                        view.setBackgroundColor(imageRes)
                    } catch (colorEx: java.lang.Exception) {
                    }
                }
            }
            is Drawable -> {
                view.background = imageRes
            }
            is String, is Bitmap -> {
                Glide.with(view)
                    .load(imageRes)
                    .into(object : CustomViewTarget<View, Drawable>(view) {
                        override fun onLoadFailed(errorDrawable: Drawable?) {
                        }
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            view.background = resource
                        }
                        override fun onResourceCleared(placeholder: Drawable?) {
                        }
                    })
            }
        }
    }

    @JvmStatic
    @BindingAdapter("imageSrc")
    fun setImageViewSrc(targetView: ImageView, imageRes: Any?) {
        Glide.with(targetView)
            .load(imageRes)
            .into(targetView)
    }
}