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
import com.littlefox.app.foxschool.`object`.result.homework.detail.HomeworkDetailItemData
import com.littlefox.app.foxschool.adapter.listener.base.OnItemViewClickListener
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.HomeworkType
import com.littlefox.library.view.listener.OnSingleClickListner
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 숙제현황 숙제리스트 아이템 Adapter
 * - 학생용, 선생님용 같이 사용
 * @author 김태은
 */
class HomeworkItemViewAdapter : RecyclerView.Adapter<HomeworkItemViewAdapter.ViewHolder?>
{
    private val mContext : Context
    private var mItemDetail : ArrayList<HomeworkDetailItemData> = ArrayList<HomeworkDetailItemData>() // 숙제 리스트
    private var mHomeworkItemListener : OnItemViewClickListener? = null
    private var isButtonEnable : Boolean = false // 숙제버튼 활성/비활성
    private var isTeacher : Boolean = false

    constructor(context : Context, isTeacher : Boolean)
    {
        mContext = context
        this.isTeacher = isTeacher
    }

    fun setItemList(detail : ArrayList<HomeworkDetailItemData>) : HomeworkItemViewAdapter
    {
        mItemDetail = detail
        return this
    }

    override fun getItemCount() : Int
    {
        return mItemDetail.size
    }

    fun setHomeworkItemListener(homeworkItemListener : OnItemViewClickListener) : HomeworkItemViewAdapter
    {
        mHomeworkItemListener = homeworkItemListener
        return this
    }

    fun setButtonEnable(isEnable : Boolean)
    {
        isButtonEnable = isEnable
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
        val item = mItemDetail[position]

        // 컨텐츠 아이템 세팅
        Glide.with(mContext)
            .load(item.getThumbnailUrl())
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder._HomeworkThumbnailImage)
        holder._HomeworkContentNameText.text = item.getFullTitle()
        holder._HomeworkContentTypeImage.background = CommonUtils.getInstance(mContext).getContentTypeImage(item.getContentType())
        holder._HomeworkTypeImage.background = CommonUtils.getInstance(mContext).getHomeworkTypeImage(item.getHomeworkType(), isButtonEnable)

        // View 초기화
        holder._HomeworkTypeImage.visibility = View.VISIBLE
        holder._HomeworkCompleteDateText.visibility = View.GONE
        holder._HomeworkUnCompleteText.visibility = View.GONE
        holder._HomeworkCheckImage.visibility = View.GONE
        holder._HomeworkRecordLayout.visibility = View.GONE

        if (item.isComplete)
        {
            // 학습 완료
            if (isTeacher && item.getHomeworkType() == HomeworkType.RECORDER)
            {
                // 녹음기 컨텐츠인 경우
                holder._HomeworkTypeImage.visibility = View.GONE
                holder._HomeworkRecordLayout.visibility = View.VISIBLE

                if (item.getExpired() == 0)
                {
                    // 기간만료
                    holder._HomeworkRecordImage.background = mContext.resources.getDrawable(R.drawable.icon_recorder_play_off)
                    holder._HomeworkRecordDate.text = "${mContext.resources.getString(R.string.text_record_expired)}"
                }
                else
                {
                    // 남은 기간
                    holder._HomeworkRecordImage.background = mContext.resources.getDrawable(R.drawable.icon_recorder_play_on)
                    holder._HomeworkRecordDate.text = "${item.getExpired()}${mContext.resources.getString(R.string.text_record_remain_date)}"
                }
            }

            val date = CommonUtils.getInstance(mContext).getStudyCompleteDateText(item.getCompleteDate())
            holder._HomeworkCompleteDateText.text = "${mContext.resources.getString(R.string.text_study_date)} : $date"
            holder._HomeworkCompleteDateText.visibility = View.VISIBLE
            holder._HomeworkCheckImage.visibility = View.VISIBLE
        }
        else
        {
            // 학습 미완료
            if (isTeacher)
            {
                holder._HomeworkUnCompleteText.text = mContext.resources.getString(R.string.text_study_date)
            }
            else
            {
                holder._HomeworkUnCompleteText.text = mContext.resources.getString(R.string.message_homework_start)
            }
            holder._HomeworkUnCompleteText.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener(object : OnSingleClickListner()
        {
            override fun onSingleClick(v : View?)
            {
                mHomeworkItemListener?.onItemClick(position)
            }
        })
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

        @BindView(R.id._homeworkRecordLayout)
        lateinit var _HomeworkRecordLayout : ScalableLayout

        @BindView(R.id._homeworkRecordImage)
        lateinit var _HomeworkRecordImage : ImageView

        @BindView(R.id._homeworkRecordDate)
        lateinit var _HomeworkRecordDate : TextView

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
            _HomeworkRecordDate.setTypeface(Font.getInstance(mContext).getRobotoRegular())
        }
    }
}