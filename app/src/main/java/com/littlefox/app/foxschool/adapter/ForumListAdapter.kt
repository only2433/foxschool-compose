package com.littlefox.app.foxschool.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.littlefox.app.foxschool.R
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
    private var mOnItemViewClickListener : OnItemViewClickListener? = null
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

    fun setOnItemViewClickListener(onItemViewClickListener : OnItemViewClickListener?)
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
        if (mDataList[position].isShowNewIcon())
        {
            holder._NewItemImage.visibility = View.VISIBLE
        }
        else
        {
            holder._NewItemImage.visibility = View.GONE
        }
        holder._TitleText.setText(mDataList[position].getTitle())
        holder._DateText.setText(mDataList[position].getRegisterDate())
        holder._BackgroundImage.setOnClickListener {
            mOnItemViewClickListener?.onItemClick(position)
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

        @BindView(R.id._newItemImage)
        lateinit var _NewItemImage : ImageView

        @BindView(R.id._titleText)
        lateinit var _TitleText : TextView

        @BindView(R.id._dateText)
        lateinit var _DateText : TextView

        constructor(view : View) : super(view)
        {
            ButterKnife.bind(this, view)
            initFont()
        }

        private fun initFont()
        {
            _TitleText.setTypeface(Font.getInstance(mContext).getRobotoMedium())
            _DateText.setTypeface(Font.getInstance(mContext).getRobotoRegular())
        }
    }
}