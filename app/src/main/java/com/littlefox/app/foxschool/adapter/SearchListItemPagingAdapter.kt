package com.littlefox.app.foxschool.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.listener.DetailItemListener
import com.littlefox.app.foxschool.adapter.listener.SearchItemListener
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.search.paging.ContentBasePagingResult
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import java.util.ArrayList

class SearchListItemPagingAdapter: PagingDataAdapter<ContentBasePagingResult, SearchListItemPagingAdapter.ViewHolder>
{
    private var mContext : Context
    private var mDetailItemListener : SearchItemListener? = null

    companion object
    {
        val diffCallback = object : DiffUtil.ItemCallback<ContentBasePagingResult>()
        {
            override fun areItemsTheSame(oldItem : ContentBasePagingResult, newItem : ContentBasePagingResult) : Boolean
            {
                return oldItem.id == newItem.id
            }


            override fun areContentsTheSame(oldItem : ContentBasePagingResult, newItem : ContentBasePagingResult) : Boolean
            {
                return oldItem == newItem
            }
        }
    }

    constructor(context : Context) : super(diffCallback)
    {
        mContext = context
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int)
    {
        var item = getItem(position) as ContentBasePagingResult

        holder._ContentTitleText.setText(item.getContentsName())

        if(item.isStoryViewComplete)
        {
            holder._StudiedCheckIcon.visibility = View.VISIBLE
        }
        else
        {
            holder._StudiedCheckIcon.visibility = View.GONE
        }

        Glide.with(mContext)
            .load(item.thumbnail_url)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder._ThumbnailImage)


        holder._ThumbnailImage.setOnClickListener {
            mDetailItemListener?.onItemClickThumbnail(ContentsBaseResult(item))
        }
        holder._thumbnailOption.setOnClickListener {
            mDetailItemListener?.onItemClickOption(ContentsBaseResult(item))
        }

        if(CommonUtils.getInstance(mContext).checkTablet == false)
        {
            holder._BottomLogoLayout?.setVisibility(View.GONE)
        }
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
    {
        val view = if(CommonUtils.getInstance(mContext).checkTablet)
        {
            LayoutInflater.from(mContext).inflate(R.layout.detail_list_not_index_item_tablet, parent, false)
        }
        else
        {
            LayoutInflater.from(mContext).inflate(R.layout.detail_list_not_index_item, parent, false)
        }
        return ViewHolder(view)
    }



    fun setDetailItemListener(listener : SearchItemListener?) : SearchListItemPagingAdapter
    {
        mDetailItemListener = listener
        return this
    }


    inner class ViewHolder : RecyclerView.ViewHolder
    {
        @JvmField
        @BindView(R.id._bottomLogoLayout)
        var _BottomLogoLayout : ScalableLayout? = null

        @BindView(R.id._backgroundImage)
        lateinit var _BackgroundImage : ImageView

        @BindView(R.id._freeIconImage)
        lateinit var _FreeIconImage : ImageView

        @BindView(R.id._thumbnailImage)
        lateinit var _ThumbnailImage : ImageView

        @BindView(R.id._studiedCheckIcon)
        lateinit var _StudiedCheckIcon : ImageView

        @JvmField
        @BindView(R.id._contentIndexText)
        var _ContentIndexText : TextView? = null

        @BindView(R.id._contentTitleText)
        lateinit var _ContentTitleText : TextView

        @BindView(R.id._thumbnailOption)
        lateinit var _thumbnailOption : ImageView

        @JvmField
        @BindView(R.id._lastPlayedIcon)
        var _LastPlayedIcon : ImageView? = null

        constructor(view : View) : super(view)
        {
            ButterKnife.bind(this, view)
            initFont()
        }

        private fun initFont()
        {
            _ContentTitleText.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        }
    }
}