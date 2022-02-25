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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.record.RecordHistoryResult
import com.littlefox.app.foxschool.adapter.listener.base.OnItemViewClickListener
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font

/**
 * 녹음기록 아이템 Adapter
 * @author 김태은
 */
class RecordHistoryListAdapter : RecyclerView.Adapter<RecordHistoryListAdapter.ViewHolder?>
{
    private val mContext : Context
    private var mRecordHistoryList : ArrayList<RecordHistoryResult> = ArrayList<RecordHistoryResult>() // 녹음기록 리스트
    private var mRecordItemListener : OnItemViewClickListener? = null

    constructor(context : Context)
    {
        mContext = context
    }

    fun setItemList(list : ArrayList<RecordHistoryResult>) : RecordHistoryListAdapter
    {
        mRecordHistoryList = list
        return this
    }

    override fun getItemCount() : Int
    {
        return mRecordHistoryList.size
    }

    fun setHomeworkItemListener(recordItemListener : OnItemViewClickListener) : RecordHistoryListAdapter
    {
        mRecordItemListener = recordItemListener
        return this
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
    {
        val view : View
        if (CommonUtils.getInstance(mContext).checkTablet)
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.record_history_list_item_tablet, parent, false)
        }
        else
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.record_history_list_item, parent, false)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int)
    {
        val item = mRecordHistoryList[position]

        // 컨텐츠 아이템 세팅
        Glide.with(mContext)
            .load(item.getThumbnailUrl())
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder._RecordThumbnailImage)
        holder._RecordContentNameText.text = item.getTitle()
        if (item.getExpire() == 0)
        {
            holder._RecordPlayButton.background = mContext.resources.getDrawable(R.drawable.icon_recorder_play_off)
            holder._RecordRemainDateText.text = "${mContext.resources.getString(R.string.text_record_expired)}"
        }
        else
        {
            holder._RecordPlayButton.background = mContext.resources.getDrawable(R.drawable.icon_recorder_play_on)
            holder._RecordRemainDateText.text = "${item.getExpire()}${mContext.resources.getString(R.string.text_record_remain_date)}"
        }
        holder._RecordDateText.text = "${mContext.resources.getString(R.string.text_study_date)}: ${item.getDate()}"

        holder.itemView.setOnClickListener {
            mRecordItemListener?.onItemClick(position)
        }
    }

    inner class ViewHolder : RecyclerView.ViewHolder
    {
        @BindView(R.id._recordThumbnailImage)
        lateinit var _RecordThumbnailImage : ImageView

        @BindView(R.id._recordContentNameText)
        lateinit var _RecordContentNameText : TextView

        @BindView(R.id._recordPlayButton)
        lateinit var _RecordPlayButton : ImageView

        @BindView(R.id._recordRemainDateText)
        lateinit var _RecordRemainDateText : TextView

        @BindView(R.id._recordDateText)
        lateinit var _RecordDateText : TextView

        constructor(view : View) : super(view)
        {
            ButterKnife.bind(this, view)
            initFont()
        }

        private fun initFont()
        {
            _RecordContentNameText.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
            _RecordRemainDateText.setTypeface(Font.getInstance(mContext).getTypefaceRegular())
            _RecordDateText.setTypeface(Font.getInstance(mContext).getTypefaceRegular())
        }
    }
}