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
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font

import java.util.ArrayList

/**
 * [팍스스쿨 소식], [자주 묻는 질문] List Adapter
 */
class ForumListAdapter : RecyclerView.Adapter<ForumListAdapter.ViewHolder?>
{
    private var mDataList : ArrayList<ForumBaseResult> = ArrayList<ForumBaseResult>()
    private var mOnItemViewClickListener : OnItemViewClickListener? = null
    private lateinit var mContext : Context
    private var mForumType : Int

    constructor(context : Context, type : Int)
    {
        mContext = context
        mForumType = type
    }

    fun setData(dataList : ArrayList<ForumBaseResult>)
    {
        mDataList = dataList

        // TODO 김태은 테스트용 나중에 지울 것
        if (mForumType == Common.FORUM_TYPE_FOXSCHOOL_NEWS)
        {
            mDataList[0] = ForumBaseResult("Y", 0, "미확인 신규 컨텐츠 테스트", "2021.08.02 11:00")
            mDataList[1] = ForumBaseResult("Y", 1, "미확인 신규 컨텐츠 테스트", "2021.07.28 16:00")
            mDataList[2] = ForumBaseResult("N", 2, "미확인 신규 컨텐츠 테스트", "2021.07.28 15:00")
            mDataList[3] = ForumBaseResult("Y", 3, "미확인 신규 컨텐츠 테스트", "2021.07.28 00:00")
        }
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
            Common.FORUM_TYPE_FOXSCHOOL_NEWS ->
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
            Common.FORUM_TYPE_FAQ ->
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