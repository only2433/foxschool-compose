package com.littlefox.app.foxschool.adapter

import android.content.Context
import android.content.res.Configuration
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
import com.littlefox.app.foxschool.`object`.result.common.ContentsBaseResult
import com.littlefox.app.foxschool.adapter.listener.PlayerEventListener
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import java.util.*

/**
 * Created by only340 on 2018-03-21.
 */
class PlayerListAdapter : RecyclerView.Adapter<PlayerListAdapter.ViewHolder?>
{
    private val mContext : Context
    private val mCurrentOrientation : Int
    private val mPlayInformationList : ArrayList<ContentsBaseResult>
    private lateinit var mPlayerEventListener : PlayerEventListener
    private var mCurrentPlayIndex = 0

    constructor(context : Context, orientation : Int, currentPlayIndex : Int, list : ArrayList<ContentsBaseResult>)
    {
        Log.f("orientation : $orientation, currentPlayIndex : $currentPlayIndex")
        mContext = context
        mCurrentOrientation = orientation
        mPlayInformationList = list
        mCurrentPlayIndex = currentPlayIndex
    }


    fun setPlayerEventListener(listener : PlayerEventListener)
    {
        mPlayerEventListener = listener
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
    {
        var view : View? = null
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.player_list_item_portrait, parent, false)
        }
        else if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.player_list_item_landscape, parent, false)
        }
        return ViewHolder(view!!)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int)
    {
        Glide.with(mContext)
                .load(mPlayInformationList[position].getThumbnailUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder._ItemTitleImage)
        holder._ItemTitleText.setText(CommonUtils.getInstance(mContext).getContentsName(mPlayInformationList[position]))
        if(mCurrentPlayIndex == position)
        {
            Log.f("yellow : $mCurrentPlayIndex")
            holder._ItemBackground.setImageResource(R.drawable.box_yellow)
        }
        else
        {
            holder._ItemBackground.setImageResource(R.drawable.box)
        }
        holder._ItemBaseLayout.setOnClickListener(View.OnClickListener {
            mCurrentPlayIndex = position
            Log.f("mCurrentPlayIndex : $mCurrentPlayIndex")
            mPlayerEventListener.onItemClick(position)
            notifyDataSetChanged()
        })
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
        {
            if(mPlayInformationList[position].isOptionDisable)
            {
                holder._ItemTitleOption.visibility = View.GONE
            }
            else
            {
                holder._ItemTitleOption.visibility = View.VISIBLE
            }
            holder._ItemTitleOption.setOnClickListener {mPlayerEventListener.onClickOption(position)}
        }
    }

    fun setCurrentPlayIndex(index : Int)
    {
        mCurrentPlayIndex = index
        notifyDataSetChanged()
    }

    override fun getItemCount() : Int
    {
        return mPlayInformationList!!.size
    }


    inner class ViewHolder : RecyclerView.ViewHolder
    {
        @BindView(R.id._itemBaseLayout)
        lateinit var _ItemBaseLayout : ScalableLayout

        @BindView(R.id._itemBackground)
        lateinit var _ItemBackground : ImageView

        @BindView(R.id._itemTitleImage)
        lateinit var _ItemTitleImage : ImageView

        @BindView(R.id._itemTitleText)
        lateinit var _ItemTitleText : TextView

        @BindView(R.id._itemTitleOption)
        lateinit var _ItemTitleOption : ImageView

        private fun initFont()
        {
            _ItemTitleText.setTypeface(Font.getInstance(mContext).getRobotoRegular())
        }

        constructor(view : View) : super(view)
        {
            ButterKnife.bind(this, view)
            initFont()
        }
    }
}