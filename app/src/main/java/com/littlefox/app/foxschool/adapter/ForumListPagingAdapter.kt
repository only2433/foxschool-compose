package com.littlefox.app.foxschool.adapter

import android.content.Context
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
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.forum.ForumBaseResult
import com.littlefox.app.foxschool.`object`.result.forum.paging.ForumBasePagingResult
import com.littlefox.app.foxschool.adapter.listener.ForumItemListener
import com.littlefox.app.foxschool.adapter.listener.base.OnItemViewClickListener
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.ForumType

class ForumListPagingAdapter: PagingDataAdapter<ForumBasePagingResult, ForumListPagingAdapter.ViewHolder>
{
    private var mOnItemViewClickListener : ForumItemListener? = null
    private lateinit var mContext : Context
    private lateinit var mForumType : ForumType

    companion object
    {
        val diffCallback = object : DiffUtil.ItemCallback<ForumBasePagingResult>()
        {
            override fun areItemsTheSame(oldItem: ForumBasePagingResult, newItem: ForumBasePagingResult): Boolean
            {
                return oldItem.getForumId() == newItem.getForumId()
            }

            override fun areContentsTheSame(oldItem: ForumBasePagingResult, newItem: ForumBasePagingResult): Boolean
            {
                return oldItem == newItem
            }
        }
    }

    constructor(context : Context, forumType : ForumType) : super(diffCallback)
    {
        mContext = context
        mForumType = forumType
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
    {
        val view : View
        when(mForumType)
        {
            // [팍스스쿨 뉴스]
            ForumType.FOXSCHOOL_NEWS ->
            {
                if(CommonUtils.getInstance(mContext).checkTablet)
                {
                    view = LayoutInflater.from(mContext).inflate(R.layout.news_list_item_tablet, parent, false)
                }
                else
                {
                    view = LayoutInflater.from(mContext).inflate(R.layout.news_list_item, parent, false)
                }
            }
            // [자주 묻는 질문]
            ForumType.FAQ ->
            {
                if(CommonUtils.getInstance(mContext).checkTablet)
                {
                    view = LayoutInflater.from(mContext).inflate(R.layout.faq_list_item_tablet, parent, false)
                }
                else
                {
                    view = LayoutInflater.from(mContext).inflate(R.layout.faq_list_item, parent, false)
                }
            }
            else -> view = LayoutInflater.from(mContext).inflate(R.layout.news_list_item, parent, false)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int)
    {
        var item = getItem(position) as ForumBasePagingResult
        // [팍스스쿨 소식]에서만 보이는 항목
        if (mForumType == ForumType.FOXSCHOOL_NEWS)
        {
            if (item.isShowNewIcon())
            {
                holder._NewItemImage.visibility = View.VISIBLE
            }
            else
            {
                holder._NewItemImage.visibility = View.GONE
            }

            holder._DateText.setText(item.getRegisterDate())
        }
        holder._TitleText.setText(item.getTitle())
        holder._BackgroundImage.setOnClickListener {
            mOnItemViewClickListener?.onItemClick(item.getForumId())
        }
    }

    fun setOnItemViewClickListener(onItemViewClickListener : ForumItemListener?)
    {
        mOnItemViewClickListener = onItemViewClickListener
    }


    inner class ViewHolder : RecyclerView.ViewHolder
    {
        @BindView(R.id._backgroundImage)
        lateinit var _BackgroundImage : ImageView

        @Nullable
        @BindView(R.id._newItemImage)
        lateinit var _NewItemImage : ImageView

        @BindView(R.id._titleText)
        lateinit var _TitleText : TextView

        @Nullable
        @BindView(R.id._dateText)
        lateinit var _DateText : TextView

        constructor(view : View) : super(view)
        {
            ButterKnife.bind(this, view)
            initFont()
        }

        private fun initFont()
        {
            _TitleText.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
            // [팍스스쿨 소식]에서만 보이는 항목
            if (mForumType == ForumType.FOXSCHOOL_NEWS)
            {
                _DateText.setTypeface(Font.getInstance(mContext).getTypefaceRegular())
            }
        }
    }


}