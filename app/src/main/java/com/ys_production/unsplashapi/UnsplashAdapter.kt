package com.ys_production.unsplashapi

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.core.ImagePipelineFactory
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequest


private const val TAG = "UnsplashAdapter"

class UnsplashAdapter(private val context: Context, private val photos: List<UnsplashPhoto>) :
    RecyclerView.Adapter<UnsplashAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_unsplash_photo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = photos[position]
//        Glide.with(context).load(photo.urls.small).into(holder.imageView)
        val request = ImageRequest.fromUri(photo.urls.small)
        holder.imageView.setImageRequest(request)
//        val requestListener = BaseRequestListener().onRequestSuccess(request!!,"",true)
//        holder.imageView.setImageRequest(request)
//        loadBlur(holder.imageBack)
        val oldPosition = position
        val imagePipeline = ImagePipelineFactory.getInstance().imagePipeline.fetchDecodedImage(request,holder.imageBack.context)
        val suscriber = object : BaseBitmapDataSubscriber(){
            override fun onNewResultImpl(bitmap: Bitmap?) {
                Log.d(TAG, "onNewResultImpl: ${bitmap?.byteCount}")
                val handler = Handler(Looper.getMainLooper())

                Thread {
                    val drawable = loadBlur(holder.imageView, bitmap)
                    drawable?.let {
//                        if (holder.oldPosition == holder.adapterPosition){
                        if (oldPosition == holder.layoutPosition)
                            handler.post {
                                holder.getAnimation()?.let {
                                    holder.imageBack.background = drawable
                                    holder.imageBack.startAnimation(it)
                                    holder.imageBack.visibility = View.VISIBLE
                                }
                            }
//                        }
                    }
                }.start()
            }

            override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {

            }

        }
        imagePipeline.subscribe(suscriber, CallerThreadExecutor.getInstance())
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.imageBack.visibility = View.GONE
    }

    private fun loadBlur(view: View, bitmap: Bitmap?): Drawable? {
        bitmap?.let {bit->
                val blurBitmap = BlurShadowProvider(view.context).getBlurShadow(getSqureBitmap(bit), 40F)
//                val blurBitmap = getBitmapFromView(view)?.let {
//                    BlurShadowProvider(view.context).getBlurShadow(
//                        it, 60F)
//                }
                val d: Drawable = BitmapDrawable(view.resources,blurBitmap)
            return d
        }
        return null
    }
    private fun getSqureBitmap(srcBmp: Bitmap):Bitmap{
        val dstBmp:Bitmap
        if (srcBmp.width >= srcBmp.height){

            dstBmp = Bitmap.createBitmap(
                srcBmp,
                srcBmp.width /2 - srcBmp.height /2,
                0,
                srcBmp.height,
                srcBmp.height
            );

        }else{

            dstBmp = Bitmap.createBitmap(
                srcBmp,
                0,
                srcBmp.height /2 - srcBmp.width /2,
                srcBmp.width,
                srcBmp.width
            );
        }
        return dstBmp
    }
    fun getBitmapFromView(view: View): Bitmap? {
        return try {
            val returnedBitmap =
                Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(returnedBitmap)
            val bgDrawable = view.background
            if (bgDrawable != null) bgDrawable.draw(canvas) else canvas.drawColor(Color.WHITE)
            view.draw(canvas)
            returnedBitmap
        }catch (e: Exception){
            e.printStackTrace()
            null
        }
    }
    override fun getItemCount(): Int {
        return photos.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: SimpleDraweeView = itemView.findViewById(R.id.item_image)
        val imageBack: ConstraintLayout = itemView.findViewById(R.id.mainBack)
        val imageCard: CardView = itemView.findViewById(R.id.image_card)
        fun getAnimation(): Animation? {
            return AnimationUtils.loadAnimation(imageBack.context, R.anim.alpha_0to1)
        }
    }
}
