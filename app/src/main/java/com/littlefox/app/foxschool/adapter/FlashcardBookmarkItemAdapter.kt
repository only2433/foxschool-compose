package com.littlefox.app.foxschool.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.flashcard.FlashCardDataResult
import com.littlefox.app.foxschool.adapter.listener.BookmarkItemListener
import com.littlefox.app.foxschool.common.Font

/**
 * 플래시카드 북마크 Adapter
 */
class FlashcardBookmarkItemAdapter : RecyclerView.Adapter<FlashcardBookmarkItemAdapter.ViewHolder>
{
    private lateinit var mContext : Context
    private lateinit var mBookmarkItemListener : BookmarkItemListener
    private var mDataList : ArrayList<FlashCardDataResult> = ArrayList()

    constructor(context : Context, list : ArrayList<FlashCardDataResult>)
    {
        this.mContext = context
        this.mDataList = list
    }

    fun setOnBookmarkItemListener(bookmarkItemListener : BookmarkItemListener)
    {
        mBookmarkItemListener = bookmarkItemListener
    }

    @NonNull
    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.flashcard_bookmark_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int)
    {
        holder._WordText.text = mDataList[position].getWordText()
        holder._BookmarkRect.setOnClickListener {
            mBookmarkItemListener.onCheckBookmark(position)
        }

        if (mDataList[position].isBookmarked())
        {
            holder._BookmarkButton.setImageResource(R.drawable.flashcard_bookmark_on)
        }
        else
        {
            holder._BookmarkButton.setImageResource(R.drawable.flashcard_bookmark_off)
        }
    }

    override fun getItemCount() : Int
    {
        return mDataList.size
    }

    inner class ViewHolder : RecyclerView.ViewHolder
    {
        @BindView(R.id._wordText)
        lateinit var _WordText : TextView

        @BindView(R.id._bookmarkButton)
        lateinit var _BookmarkButton : ImageView

        @BindView(R.id._bookmarkRect)
        lateinit var _BookmarkRect : ImageView
        
        constructor(itemView : View) : super(itemView)
        {
            ButterKnife.bind(this, itemView)
            initFont()
        }

        private fun initFont()
        {
            _WordText.typeface = Font.getInstance(mContext).getTypefaceBold()
        }
    }
}