package com.littlefox.app.foxschool.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.listener.ForumItemListener
import com.littlefox.app.foxschool.`object`.result.forum.ForumBaseResult
import com.littlefox.app.foxschool.adapter.listener.base.OnItemViewClickListener
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.ForumType

import java.util.ArrayList

/**
 * [팍스스쿨 소식], [자주 묻는 질문] List Adapter
 */
class ForumListAdapter : RecyclerView.Adapter<ForumListAdapter.ViewHolder?>
{
    private var mDataList : ArrayList<ForumBaseResult> = ArrayList<ForumBaseResult>()
    private var mOnItemViewClickListener : ForumItemListener? = null
    private lateinit var mContext : Context
    private var mForumType : ForumType

    constructor(context : Context, type : ForumType)
    {
        mContext = context
        mForumType = type
    }

    fun setData(dataList : ArrayList<ForumBaseResult>)
    {
        mDataList = dataList
    }

    fun setOnItemViewClickListener(onItemViewClickListener : ForumItemListener?)
    {
        mOnItemViewClickListener = onItemViewClickListener
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
        // [팍스스쿨 소식]에서만 보이는 항목
        if (mForumType == ForumType.FOXSCHOOL_NEWS)
        {
            if (mDataList[position].isShowNewIcon())
            {
                holder._NewItemImage?.visibility = View.VISIBLE
            }
            else
            {
                holder._NewItemImage?.visibility = View.GONE
            }

            holder._DateText?.setText(mDataList[position].getRegisterDate())
        }
        holder._TitleText.setText(mDataList[position].getTitle())
        holder._BackgroundImage.setOnClickListener {
            mOnItemViewClickListener?.onItemClick(mDataList[position].getForumId())
        }
    }

    override fun getItemCount() : Int
    {
        return mDataList.size
    }

    inner class ViewHolder : RecyclerView.ViewHolder
    {
        @BindView(R.id._backgroundImage)
        lateinit var _BackgroundImage : ImageView

        @JvmField
        @BindView(R.id._newItemImage)
        var _NewItemImage : ImageView? = null

        @BindView(R.id._titleText)
        lateinit var _TitleText : TextView

        @JvmField
        @BindView(R.id._dateText)
        var _DateText : TextView? = null

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
                _DateText?.setTypeface(Font.getInstance(mContext).getTypefaceRegular())
            }
        }
    }
}