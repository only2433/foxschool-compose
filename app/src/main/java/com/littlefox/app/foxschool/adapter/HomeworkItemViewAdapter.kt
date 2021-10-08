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
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkListItemData
import com.littlefox.app.foxschool.adapter.listener.HomeworkItemListener
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font

/**
 * 숙제현황 숙제리스트 아이템 Adapter
 * - 학생용, 선생님용 아이템 형태 동일해서 같이 사용해도 됨
 * @author 김태은
 */
class HomeworkItemViewAdapter : RecyclerView.Adapter<HomeworkItemViewAdapter.ViewHolder?>
{
    private val mContext : Context
    private var mItemList : ArrayList<HomeworkListItemData> = ArrayList<HomeworkListItemData>() // 숙제 리스트
    private var mHomeworkItemListener : HomeworkItemListener? = null

    constructor(context : Context)
    {
        mContext = context
    }

    fun setItemList(list : ArrayList<HomeworkListItemData>) : HomeworkItemViewAdapter
    {
        mItemList = list
        return this
    }

    override fun getItemCount() : Int
    {
        return mItemList.size
    }

    fun setHomeworkItemListener(homeworkItemListener : HomeworkItemListener) : HomeworkItemViewAdapter
    {
        mHomeworkItemListener = homeworkItemListener
        return this
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
    {
        val view : View
        if (CommonUtils.getInstance(mContext).checkTablet)
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.homework_list_item_tablet, parent, false)
        }
        else
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.homework_list_item, parent, false)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int)
    {
        val item = mItemList[position]

        // 컨텐츠 아이템 세팅
        Glide.with(mContext)
            .load(item.getThumbnailUrl())
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder._HomeworkThumbnailImage)
        holder._HomeworkContentNameText.text = item.getTitle()
        holder._HomeworkContentTypeImage.background = CommonUtils.getInstance(mContext).getContentTypeImage(item.getContentType())
        holder._HomeworkTypeImage.background = CommonUtils.getInstance(mContext).getHomeworkTypeImage(item.getHomeworkType())

        // View 초기화
        holder._HomeworkCompleteDateText.visibility = View.GONE
        holder._HomeworkUnCompleteText.visibility = View.GONE
        holder._HomeworkCheckImage.visibility = View.GONE

        if (item.isComplete)
        {
            // 학습 완료
            holder._HomeworkCheckImage.visibility = View.VISIBLE
            holder._HomeworkCompleteDateText.visibility = View.VISIBLE
            holder._HomeworkCompleteDateText.text = "${mContext.resources.getString(R.string.text_study_date)} ${item.getCompleteDate()}"
        }
        else
        {
            // 학습 미완료
            holder._HomeworkUnCompleteText.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            mHomeworkItemListener?.onClickItem(position)
        }
    }

    inner class ViewHolder : RecyclerView.ViewHolder
    {
        @BindView(R.id._homeworkThumbnailImage)
        lateinit var _HomeworkThumbnailImage : ImageView

        @BindView(R.id._homeworkContentNameText)
        lateinit var _HomeworkContentNameText : TextView

        @BindView(R.id._homeworkContentTypeImage)
        lateinit var _HomeworkContentTypeImage : ImageView

        @BindView(R.id._homeworkTypeImage)
        lateinit var _HomeworkTypeImage : ImageView

        @BindView(R.id._homeworkCheckImage)
        lateinit var _HomeworkCheckImage : ImageView

        @BindView(R.id._homeworkCompleteDateText)
        lateinit var _HomeworkCompleteDateText : TextView

        @BindView(R.id._homeworkUnCompleteText)
        lateinit var _HomeworkUnCompleteText : TextView

        constructor(view : View) : super(view)
        {
            ButterKnife.bind(this, view)
            initFont()
        }

        private fun initFont()
        {
            _HomeworkContentNameText.setTypeface(Font.getInstance(mContext).getRobotoMedium())
            _HomeworkCompleteDateText.setTypeface(Font.getInstance(mContext).getRobotoRegular())
            _HomeworkUnCompleteText.setTypeface(Font.getInstance(mContext).getRobotoRegular())
        }
    }
}