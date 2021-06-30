package com.littlefox.app.foxschool.adapter

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.vocabulary.VocabularySelectData
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyDataResult
import com.littlefox.app.foxschool.adapter.listener.VocabularyItemListener
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font
import com.littlefox.library.view.animator.AnimationListener
import com.littlefox.library.view.animator.ViewAnimator
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import java.util.*

class VocabularyItemListAdapter : RecyclerView.Adapter<VocabularyItemListAdapter.ViewHolder>
{
    companion object
    {
        private val BASE_LAYOUT_WIDTH = if(Feature.IS_TABLET) 960 else 1080
        private val BASE_LAYOUT_LEFT_MARGIN = if(Feature.IS_TABLET) 0 else 30
        private val BASE_LAYOUT_BOTTOM_MARGIN = if(Feature.IS_TABLET) 10 else 30
        private val BACKGROUND_WIDTH = if(Feature.IS_TABLET) 960 else 1020
        private val TITLE_HEIGHT = if(Feature.IS_TABLET) 84 else 134
        private val CONTENTS_LEFT_MARGIN = if(Feature.IS_TABLET) 60 else 70
        private val CONTENTS_WIDTH = if(Feature.IS_TABLET) 864 else 940
    }

    private lateinit var mVocabularyItemList : ArrayList<VocabularyDataResult>
    private var mVocabularyItemListener : VocabularyItemListener? = null
    private lateinit var mVocabularySelectData : VocabularySelectData
    private var mContentsText = ""
    private var mTitleText = ""
    private var mCurrentPlayIndex = -1
    private var isTitleDataChanged = false
    private var isContentsDataChanged = false
    private var isPlaying = false
    private val mContext : Context;


    constructor(context : Context)
    {
        mContext = context;
        mVocabularySelectData = VocabularySelectData()
    }

    fun setData(list : ArrayList<VocabularyDataResult>)
    {
        mVocabularyItemList = list
    }

    /**
     * 리스트를 정보를 받아서 새로 리스트를 갱신하며 초기화 한다.
     * @param list 보여줄 리스트
     * @param isPlaying 플레이 여부에 따라 화면에 플레이 아이콘을 보여야할지 여부를 결정
     */
    fun notifyDataListChanged(list : ArrayList<VocabularyDataResult>, isPlaying : Boolean)
    {
        mCurrentPlayIndex = -1
        mVocabularyItemList = list
        this.isPlaying = isPlaying
        notifyDataSetChanged()
    }

    fun notifyPlayItem(position : Int)
    {
        mCurrentPlayIndex = position
        notifyItemRangeChanged(0, mVocabularyItemList.size, "check")
    }

    fun setOnVocabularyListener(vocabularyItemListener : VocabularyItemListener)
    {
        mVocabularyItemListener = vocabularyItemListener
    }

    /**
     * 전체, 단어, 뜻, 예문을 선택에 따라 해당부분을 체크하여 아답터를 갱신하여 안보이고 보이게 하는 메소드
     *
     * @param vocabularySelectData
     * 선택된 정보
     */
    fun notifySelectContents(vocabularySelectData : VocabularySelectData)
    {
        if(mVocabularySelectData.isSelectedWord() != vocabularySelectData.isSelectedWord())
        {
            isTitleDataChanged = true
        }
        else
        {
            isTitleDataChanged = false
        }
        if(((mVocabularySelectData.isSelectedMeaning() != vocabularySelectData.isSelectedMeaning())
                        || (mVocabularySelectData.isSelectedExample() != vocabularySelectData.isSelectedExample())))
        {
            isContentsDataChanged = true
        }
        else
        {
            isContentsDataChanged = false
        }
        mVocabularySelectData.setData(vocabularySelectData)
        notifyDataSetChanged()
    }

    val selectedList : ArrayList<VocabularyDataResult>
        get()
        {
            val result : ArrayList<VocabularyDataResult> = ArrayList<VocabularyDataResult>()
            for(i in mVocabularyItemList.indices)
            {
                if(mVocabularyItemList[i].isSelected())
                {
                    result.add(mVocabularyItemList[i])
                }
            }
            return result
        }

    val selectedCount : Int
        get()
        {
            var count = 0
            for(i in mVocabularyItemList.indices)
            {
                if(mVocabularyItemList[i].isSelected())
                {
                    count++
                }
            }
            return count
        }

    fun initSelectedData()
    {
        isTitleDataChanged = false
        isContentsDataChanged = false
        for(i in mVocabularyItemList.indices)
        {
            if(mVocabularyItemList[i].isSelected())
            {
                mVocabularyItemList[i].setSelected(false)
            }
        }
        notifyItemRangeChanged(0, mVocabularyItemList.size, "check")
    }

    fun setSelectedAllData()
    {
        isTitleDataChanged = false
        isContentsDataChanged = false
        for(i in mVocabularyItemList.indices)
        {
            if(mVocabularyItemList[i].isSelected() == false)
            {
                mVocabularyItemList[i].setSelected(true)
            }
        }
        notifyItemRangeChanged(0, mVocabularyItemList.size, "check")
    }

    fun initChangedDataValue()
    {
        isTitleDataChanged = false
        isContentsDataChanged = false
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
    {
        val view : View
        if(Feature.IS_TABLET)
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.vocabulary_list_item_tablet, parent, false)
        }
        else
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.vocabulary_list_item, parent, false)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int)
    {
        holder._ItemLayout.setScaleSize(BASE_LAYOUT_WIDTH.toFloat(),
                (TITLE_HEIGHT + BASE_LAYOUT_BOTTOM_MARGIN + mVocabularyItemList[position].getContentViewSize()).toFloat())
        holder._ItemLayout.moveChildView(holder._ItemBackground,
                BASE_LAYOUT_LEFT_MARGIN.toFloat(),
                0f,
                BACKGROUND_WIDTH.toFloat(),
                (TITLE_HEIGHT + mVocabularyItemList[position].getContentViewSize()).toFloat())
        holder._ItemLayout.moveChildView(holder._ItemContentsText,
                CONTENTS_LEFT_MARGIN.toFloat(),
                TITLE_HEIGHT.toFloat(),
                CONTENTS_WIDTH.toFloat(),
                mVocabularyItemList[position].getContentViewSize().toFloat())
        mTitleText = ""
        if(mVocabularySelectData.isSelectedWord())
        {
            mTitleText = mVocabularyItemList[position].getWordText()
        }
        else
        {
            mTitleText = ""
        }
        mContentsText = ""
        if(mVocabularySelectData.isSelectedMeaning())
        {
            mContentsText = mVocabularyItemList[position].getMeaningText()
        }
        if(mVocabularySelectData.isSelectedExample())
        {
            if((mContentsText == "") == false)
            {
                mContentsText += "<br>" + mVocabularyItemList[position].getExampleText()
            }
            else
            {
                mContentsText = mVocabularyItemList[position].getExampleText()
            }
        }
        if(isTitleDataChanged)
        {
            ViewAnimator.animate(holder._ItemTitleText).standUp().duration(Common.DURATION_SHORT).onStart(AnimationListener.Start {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        {
                            holder._ItemTitleText.setText(Html.fromHtml(mTitleText, Html.FROM_HTML_MODE_LEGACY))
                        }
                        else
                        {
                            holder._ItemTitleText.setText(Html.fromHtml(mTitleText))
                        }
                    }).start()
        }
        else
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                holder._ItemTitleText.setText(Html.fromHtml(mTitleText, Html.FROM_HTML_MODE_LEGACY))
            }
            else
            {
                holder._ItemTitleText.setText(Html.fromHtml(mTitleText))
            }
        }
        if(isContentsDataChanged)
        {
            ViewAnimator.animate(holder._ItemContentsText).standUp().duration(Common.DURATION_SHORT).onStart(object : AnimationListener.Start
                    {
                        override fun onStart()
                        {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            {
                                holder._ItemContentsText.setText(Html.fromHtml(mContentsText, Html.FROM_HTML_MODE_LEGACY))
                            }
                            else
                            {
                                holder._ItemContentsText.setText(Html.fromHtml(mContentsText))
                            }
                        }
                    }).start()
        }
        else
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                holder._ItemContentsText.setText(Html.fromHtml(mContentsText, Html.FROM_HTML_MODE_LEGACY))
            }
            else
            {
                holder._ItemContentsText.setText(Html.fromHtml(mContentsText))
            }
        }
        holder._ItemPlayIcon.setOnClickListener(object : View.OnClickListener
        {
            override fun onClick(view : View)
            {
                mVocabularyItemListener?.onClickSoundPlay(position)
            }
        })
        holder._ItemLayout.setOnClickListener(object : View.OnClickListener
        {
            override fun onClick(view : View)
            {
                if(isPlaying)
                {
                    return
                }
                Log.f("select position : $position")
                val isSelected : Boolean = !mVocabularyItemList!![position].isSelected()
                mVocabularyItemList[position].setSelected(isSelected)
                notifyItemChanged(position, "check")
                mVocabularyItemListener?.onItemSelectCount(selectedCount)
            }
        })
        if(isPlaying)
        {
            holder._ItemPlayIcon.visibility = View.GONE
        }
        else
        {
            holder._ItemPlayIcon.visibility = View.VISIBLE
        }
        checkBackground(holder, position)
    }

    /**
     * 선택 됬을때 체크하는 부분의 음영처리 변경을 위해 사용. payload를 사용하면 기존 뷰를 다시 그리지 않아서 뷰가 흔들리는 현상을 방지한다.
     * @param holder holder
     * @param position 해당 포지션
     * @param payloads payload
     */
    override fun onBindViewHolder(holder : ViewHolder, position : Int, payloads : List<Any>)
    {
        if(payloads.isEmpty())
        {
            super.onBindViewHolder(holder, position, payloads)
        }
        else
        {
            for(payload : Any? in payloads)
            {
                if(payload is String)
                {
                    if(TextUtils.equals(payload, "check") && holder is ViewHolder)
                    {
                        checkBackground(holder, position)
                    }
                }
            }
        }
    }

    override fun getItemCount() : Int
    {
        return mVocabularyItemList.size
    }

    inner class ViewHolder : RecyclerView.ViewHolder
    {
        @BindView(R.id._itemLayout)
        lateinit var _ItemLayout : ScalableLayout

        @BindView(R.id._itemBackground)
        lateinit var _ItemBackground : ImageView

        @BindView(R.id._itemTitleText)
        lateinit var _ItemTitleText : TextView

        @BindView(R.id._itemPlayIcon)
        lateinit var _ItemPlayIcon : ImageView

        @BindView(R.id._itemDivideLine)
        lateinit var _ItemDivideLine : ImageView

        @BindView(R.id._itemContentsText)
        lateinit var _ItemContentsText : TextView

        constructor(view : View) : super(view)
        {
            ButterKnife.bind(this, view)
            initFont()
        }

        private fun initFont()
        {
            _ItemTitleText.setTypeface(Font.getInstance(mContext).getRobotoMedium())
            _ItemContentsText.setTypeface(Font.getInstance(mContext).getRobotoRegular())
        }
    }

    private fun checkBackground(holder : ViewHolder, position : Int)
    {
        if(mCurrentPlayIndex == position)
        {
            holder._ItemBackground.setImageResource(R.drawable.box_list_select)
        }
        else
        {
            if(mVocabularyItemList[position].isSelected())
            {
                holder._ItemBackground.setImageResource(R.drawable.voca_select)
            }
            else
            {
                holder._ItemBackground.setImageResource(R.drawable.box_list)
            }
        }
    }
}