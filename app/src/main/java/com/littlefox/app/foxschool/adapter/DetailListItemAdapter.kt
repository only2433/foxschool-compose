package com.littlefox.app.foxschool.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.adapter.listener.DetailItemListener
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

import java.util.*

class DetailListItemAdapter : RecyclerView.Adapter<DetailListItemAdapter.ViewHolder?>
{
    private var mContext : Context
    private lateinit var mDataList : ArrayList<ContentsBaseResult>
    private var mDetailItemListener : DetailItemListener? = null
    private var isSelectDisable = false
    private var isBottomViewDisable = false
    private var mIndexColor = ""
    private var isFullName = false

    constructor(context : Context, indexColor : String, list : ArrayList<ContentsBaseResult>)
    {
        mContext = context
        mIndexColor = indexColor
        mDataList = list
    }

    constructor(context : Context, list : ArrayList<ContentsBaseResult>)
    {
        mContext = context
        mIndexColor = ""
        mDataList = list
    }

    constructor(context : Context)
    {
        mContext = context
    }

    fun setData(list : ArrayList<ContentsBaseResult>) : DetailListItemAdapter
    {
        Log.f("list size : " + list.size)
        mDataList = list
        return this
    }

    fun setIndexColor(indexColor : String) : DetailListItemAdapter
    {
        mIndexColor = indexColor
        return this
    }

    fun setSelectDisable() : DetailListItemAdapter
    {
        isSelectDisable = true
        return this
    }

    fun setFullName() : DetailListItemAdapter
    {
        isFullName = true
        return this
    }

    fun setBottomViewDisable() : DetailListItemAdapter
    {
        isBottomViewDisable = true
        return this
    }

    fun setDetailItemListener(listener : DetailItemListener?) : DetailListItemAdapter
    {
        mDetailItemListener = listener
        return this
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
    {
        val view : View
        if(mIndexColor == "")
        {
            if(CommonUtils.getInstance(mContext).checkTablet)
            {
                view = LayoutInflater.from(mContext).inflate(R.layout.detail_list_not_index_item_tablet, parent, false)
            } else
            {
                view = LayoutInflater.from(mContext).inflate(R.layout.detail_list_not_index_item, parent, false)
            }
        }
        else
        {
            if(CommonUtils.getInstance(mContext).checkTablet)
            {
                view = LayoutInflater.from(mContext).inflate(R.layout.detail_list_item_tablet, parent, false)
            }
            else
            {
                view = LayoutInflater.from(mContext).inflate(R.layout.detail_list_item, parent, false)
            }
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int)
    {
        if(mIndexColor.equals("") == false)
        {
            holder._ContentIndexText.setTextColor(Color.parseColor(mIndexColor))
            holder._ContentIndexText.setText(java.lang.String.valueOf(mDataList[position].index))
        }
        if(isFullName)
        {
            holder._ContentTitleText.setText(mDataList[position].getContentsName())
        }
        else
        {
            Log.f("mDataList[position].getSubName() : "+mDataList[position].sub_name)
            if(mDataList[position].sub_name.equals(""))
            {
                holder._ContentTitleText.setText(mDataList[position].name)
            } else
            {
                holder._ContentTitleText.setText(mDataList[position].sub_name)
            }
        }

        if(mDataList[position].isSelected)
        {
            holder._BackgroundImage.setImageResource(R.drawable.box_list_select)
        }
        else
        {
            holder._BackgroundImage.setImageResource(R.drawable.box_list)
        }

        if(mDataList[position].isStoryViewComplete)
        {
            holder._StudiedCheckIcon.visibility = View.VISIBLE
        }
        else
        {
            holder._StudiedCheckIcon.visibility = View.GONE
        }

        if(mDataList[position].isOptionDisable)
        {
            holder._thumbnailOption.visibility = View.GONE
        } else
        {
            holder._thumbnailOption.visibility = View.VISIBLE
        }

        Glide.with(mContext)
            .load(mDataList[position].thumbnail_url)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder._ThumbnailImage)

        holder._BackgroundImage.setOnClickListener(View.OnClickListener {
            if(isSelectDisable)
            {
                return@OnClickListener
            }
            val isSelected : Boolean = !mDataList!![position].isSelected
            mDataList[position].isSelected = isSelected
            notifyItemChanged(position)
            mDetailItemListener?.onItemSelectCount(getSelectedCount())
        })
        holder._ThumbnailImage.setOnClickListener {
            mDetailItemListener?.onItemClickThumbnail(position)
        }
        holder._thumbnailOption.setOnClickListener {
            mDetailItemListener?.onItemClickOption(position)
        }

        if(CommonUtils.getInstance(mContext).checkTablet == false)
        {
            if(isBottomViewDisable)
            {
                holder._BottomLogoLayout.setVisibility(View.GONE)
            }
            else
            {
                if(position == mDataList.size - 1)
                {
                    holder._BottomLogoLayout.setVisibility(View.VISIBLE)
                }
                else
                {
                    holder._BottomLogoLayout.setVisibility(View.GONE)
                }
            }
        }
    }

    override fun getItemCount() : Int
    {
        return mDataList.size
    }

    fun notifyDataListChanged(list : ArrayList<ContentsBaseResult>)
    {
        mDataList = list
        initSelectedData()
    }

    fun getSelectedList() : ArrayList<ContentsBaseResult>
    {
        val result : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
        for(i in mDataList.indices)
        {
            if(mDataList[i].isSelected) result.add(mDataList[i])
        }
        return result
    }

    fun getSelectedCount() : Int
    {
        var count = 0
        for(i in mDataList.indices)
        {
            if(mDataList[i].isSelected)
            {
                count++
            }
        }
        return count
    }


    fun initSelectedData()
    {
        for(i in mDataList.indices)
        {
            if(mDataList[i].isSelected)
            {
                mDataList[i].isSelected = false
            }
        }
        notifyDataSetChanged()
    }

    fun setSelectedAllData()
    {
        for(i in mDataList.indices)
        {
            mDataList[i].isSelected = true
        }
        notifyDataSetChanged()
        mDetailItemListener?.onItemSelectCount(mDataList.size)
    }

    inner class ViewHolder : RecyclerView.ViewHolder
    {
        @Nullable
        @BindView(R.id._bottomLogoLayout)
        lateinit var _BottomLogoLayout : ScalableLayout

        @BindView(R.id._backgroundImage)
        lateinit var _BackgroundImage : ImageView

        @BindView(R.id._freeIconImage)
        lateinit var _FreeIconImage : ImageView

        @BindView(R.id._thumbnailImage)
        lateinit var _ThumbnailImage : ImageView

        @BindView(R.id._studiedCheckIcon)
        lateinit var _StudiedCheckIcon : ImageView

        @Nullable
        @BindView(R.id._contentIndexText)
        lateinit var _ContentIndexText : TextView

        @BindView(R.id._contentTitleText)
        lateinit var _ContentTitleText : TextView

        @BindView(R.id._thumbnailOption)
        lateinit var _thumbnailOption : ImageView

        @Nullable
        @BindView(R.id._lastPlayedIcon)
        lateinit var _LastPlayedIcon : ImageView

        constructor(view : View) : super(view)
        {
            ButterKnife.bind(this, view)
            initFont()
        }

        private fun initFont()
        {
            if(mIndexColor.equals("") == false)
            {
                _ContentIndexText.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
            }
            _ContentTitleText.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        }

    }
}