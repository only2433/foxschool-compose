package com.littlefox.app.foxschool.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView


class PlayerSpeedListAdapter(context : Context?, currentSpeedIndex : Int) : RecyclerView.Adapter<net.littlefox.lf_app_fragment.adapter.PlayerSpeedListAdapter.ViewHolder?>()
{
    private var mContext : Context? = null
    private var mPlayerEventListener : PlayerEventListener? = null
    private var mSpeedTextList : Array<String>? = null
    private var mCurrentSpeedIndex = 0
    fun setPlayerEventListener(listener : PlayerEventListener?)
    {
        mPlayerEventListener = listener
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
    {
        var view : View? = null
        view = LayoutInflater.from(mContext).inflate(R.layout.player_speed_item, parent, false)
        return ViewHolder(view)
    }

    fun onBindViewHolder(holder : ViewHolder, position : Int)
    {
        if(mCurrentSpeedIndex == position)
        {
            holder._SelectIcon!!.setImageResource(R.drawable.player__speed_select)
        }
        else
        {
            holder._SelectIcon!!.setImageResource(R.drawable.player__speed_select_default)
        }
        holder._SelectSpeedText.setText(mSpeedTextList!![position])
        holder._ItemBaseLayout.setOnClickListener(View.OnClickListener {
            mCurrentSpeedIndex = position
            notifyDataSetChanged()
            mPlayerEventListener.onSelectSpeed(position)
        })
    }

    override fun getItemCount() : Int
    {
        return mSpeedTextList!!.size
    }

    private fun getSpeedTextList() : Array<String>
    {
        return mContext!!.resources.getStringArray(R.array.text_list_speed)
    }

    inner class ViewHolder(view : View?) : RecyclerView.ViewHolder(view)
    {
        @BindView(R.id._itemBaseLayout)
        var _ItemBaseLayout : ScalableLayout? = null

        @BindView(R.id._selectIcon)
        var _SelectIcon : ImageView? = null

        @BindView(R.id._selectSpeedText)
        var _SelectSpeedText : TextView? = null
        private fun initFont()
        {
            _SelectSpeedText.setTypeface(Font.getInstance(mContext).getRobotoRegular())
        }

        init
        {
            ButterKnife.bind(this, view)
            initFont()
        }
    }

    init
    {
        mContext = context
        mSpeedTextList = getSpeedTextList()
        mCurrentSpeedIndex = currentSpeedIndex
    }
}