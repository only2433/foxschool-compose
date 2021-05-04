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
import com.littlefox.app.foxschool.adapter.listener.PlayerEventListener
import com.littlefox.app.foxschool.common.Font
import com.ssomai.android.scalablelayout.ScalableLayout


class PlayerSpeedListAdapter : RecyclerView.Adapter<PlayerSpeedListAdapter.ViewHolder?>
{
    private val mContext : Context
    private lateinit var mPlayerEventListener : PlayerEventListener
    private val mSpeedTextList : Array<String>
    private var mCurrentSpeedIndex = 0


    constructor(context : Context, currentSpeedIndex : Int)
    {
        mContext = context
        mSpeedTextList = getSpeedTextList()
        mCurrentSpeedIndex = currentSpeedIndex
    }


    fun setPlayerEventListener(listener : PlayerEventListener)
    {
        mPlayerEventListener = listener
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
    {
        var view : View? = null
        view = LayoutInflater.from(mContext).inflate(R.layout.player_speed_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int)
    {
        if(mCurrentSpeedIndex == position)
        {
            holder._SelectIcon.setImageResource(R.drawable.player__speed_select)
        }
        else
        {
            holder._SelectIcon.setImageResource(R.drawable.player__speed_select_default)
        }
        holder._SelectSpeedText.setText(mSpeedTextList[position])
        holder._ItemBaseLayout.setOnClickListener(View.OnClickListener {
            mCurrentSpeedIndex = position
            notifyDataSetChanged()
            mPlayerEventListener.onSelectSpeed(position)
        })
    }

    override fun getItemCount() : Int
    {
        return mSpeedTextList.size
    }

    private fun getSpeedTextList() : Array<String>
    {
        return mContext.resources.getStringArray(R.array.text_list_speed)
    }

    inner class ViewHolder : RecyclerView.ViewHolder
    {
        @BindView(R.id._itemBaseLayout)
        lateinit var _ItemBaseLayout : ScalableLayout

        @BindView(R.id._selectIcon)
        lateinit var _SelectIcon : ImageView

        @BindView(R.id._selectSpeedText)
        lateinit var _SelectSpeedText : TextView
        private fun initFont()
        {
            _SelectSpeedText.setTypeface(Font.getInstance(mContext).getRobotoRegular())
        }

        constructor(view : View) : super(view)
        {
            ButterKnife.bind(this, view)
            initFont()
        }
    }
}