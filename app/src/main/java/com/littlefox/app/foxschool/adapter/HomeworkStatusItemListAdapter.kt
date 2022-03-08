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
import com.littlefox.app.foxschool.`object`.result.homework.status.HomeworkStatusItemData
import com.littlefox.app.foxschool.adapter.listener.HomeworkStatusItemListener
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.library.view.listener.OnSingleClickListner
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 숙제관리 학생리스트 아이템 Adapter
 * - 선생님용
 * @author 김태은
 */
class HomeworkStatusItemListAdapter: RecyclerView.Adapter<HomeworkStatusItemListAdapter.ViewHolder?>
{
    private val mContext : Context
    private var mStatusList : ArrayList<HomeworkStatusItemData> = ArrayList<HomeworkStatusItemData>() // 숙제현황 리스트
    private var mHomeworkStatusItemListener : HomeworkStatusItemListener? = null

    constructor(context : Context)
    {
        mContext = context
    }

    fun setItemList(detail : ArrayList<HomeworkStatusItemData>) : HomeworkStatusItemListAdapter
    {
        mStatusList = detail
        return this
    }

    override fun getItemCount() : Int
    {
        return mStatusList.size
    }

    fun setHomeworkItemListener(homeworkItemListener : HomeworkStatusItemListener) : HomeworkStatusItemListAdapter
    {
        mHomeworkStatusItemListener = homeworkItemListener
        return this
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
    {
        val view : View
        if (CommonUtils.getInstance(mContext).checkTablet)
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.homework_status_list_item_tablet, parent, false)
        }
        else
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.homework_status_list_item, parent, false)
        }
        return ViewHolder(view)
    }
    private final lateinit var mCurrentViewHolder : ViewHolder
    final var itemPosition : Int = 0
    override fun onBindViewHolder(holder : ViewHolder, position : Int)
    {
        val item = mStatusList[position]
        itemPosition = position
        mCurrentViewHolder = holder

        // 체크박스
        if (item.isSelected())
        {
            holder._CheckIcon.setImageResource(R.drawable.radio_on)
        }
        else
        {
            holder._CheckIcon.setImageResource(R.drawable.radio_off)
        }

        // 학생 이름, 아이디
        if (CommonUtils.getInstance(mContext).checkTablet)
        {
            // 태블릿 (줄바꿈)
            holder._StudentNameText.text = "${item.getUserName()}\n${item.getLoginID()}"
        }
        else
        {
            // 스마트폰 (한줄)
            holder._StudentNameText.text = "${item.getUserName()}(${item.getLoginID()})"
        }

        // 숙제 진행현황
        holder._HomeworkCompleteText.text = "숙제 ${item.getHomeworkCompleteCount()}/${item.getHomeworkCount()}개 완료"
        if (item.isHomeworkAllComplete)
        {
            // 숙제 전부 완료한 경우 텍스트 붉은색으로
            holder._HomeworkCompleteText.setTextColor(mContext.resources.getColor(R.color.color_fa4959))
        }
        else
        {
            // 숙제 남아있는 경우 텍스트 검은색으로
            holder._HomeworkCompleteText.setTextColor(mContext.resources.getColor(R.color.color_666666))
        }

        // 학생 코멘트 유/무 이미지
        if (item.isHaveStudentComment)
        {
            holder._HomeworkStudentCommentImage.visibility = View.VISIBLE
        }
        else
        {
            holder._HomeworkStudentCommentImage.visibility = View.GONE
        }

        // 평가여부
        if (item.getEvaluationState() == "N")
        {
            // 숙제검사
            holder._HomeworkEvalImage.visibility = View.GONE
            holder._HomeworkEvalText.text = mContext.getString(R.string.text_homework_check)
            holder._HomeworkEvalText.background = mContext.resources.getDrawable(R.drawable.round_box_green_60)
        }
        else
        {
            // 검사 수정
            holder._HomeworkEvalText.text = mContext.getString(R.string.text_homework_eval_change)
            holder._HomeworkEvalText.background = mContext.resources.getDrawable(R.drawable.round_box_gray_60)

            // 선생님 코멘트
            if (item.isHaveTeacherComment)
            {
                if (CommonUtils.getInstance(mContext).checkTablet)
                {
                    // 태블릿 이미지 2개
                    holder._HomeworkStatusLayout.moveChildView(holder._HomeworkEvalImage, 1337f, 35f)
                }
                holder._HomeworkTeacherCommentImage.visibility = View.VISIBLE
            }
            else
            {
                if (CommonUtils.getInstance(mContext).checkTablet)
                {
                    // 태블릿 이미지 1개
                    holder._HomeworkStatusLayout.moveChildView(holder._HomeworkEvalImage, 1384f, 35f)
                }
                holder._HomeworkTeacherCommentImage.visibility = View.GONE
            }

            // 평가 이미지
            val image = CommonUtils.getInstance(mContext).getHomeworkEvalImage(item.getEvaluationState())
            if (image != null)
            {
                holder._HomeworkEvalImage.visibility = View.VISIBLE
                holder._HomeworkEvalImage.background = image
            }
        }

        // [체크박스] 클릭 이벤트
        holder._CheckIcon.setOnClickListener {
            val isChecked = item.isSelected()
            if (isChecked)
            {
                item.setSelected(false)
            }
            else
            {
                item.setSelected(true)
            }
            notifyDataSetChanged()
            mHomeworkStatusItemListener!!.onClickCheck(getListCheckCount())
        }
    }

    private fun getListCheckCount() : Int
    {
        val count : Int = mStatusList.filter {it.isSelected() == true}.count()
        return count
    }

    inner class ViewHolder : RecyclerView.ViewHolder
    {
        @BindView(R.id._homeworkStatusLayout)
        lateinit var _HomeworkStatusLayout : ScalableLayout

        @BindView(R.id._checkIcon)
        lateinit var _CheckIcon : ImageView

        @BindView(R.id._studentNameText)
        lateinit var _StudentNameText : TextView

        @BindView(R.id._homeworkCompleteText)
        lateinit var _HomeworkCompleteText : TextView

        @BindView(R.id._homeworkDetailText)
        lateinit var _HomeworkDetailText : TextView

        @BindView(R.id._homeworkEvalText)
        lateinit var _HomeworkEvalText : TextView

        @BindView(R.id._homeworkEvalImage)
        lateinit var _HomeworkEvalImage : ImageView

        @BindView(R.id._homeworkTeacherCommentImage)
        lateinit var _HomeworkTeacherCommentImage : ImageView

        @BindView(R.id._homeworkStudentCommentImage)
        lateinit var _HomeworkStudentCommentImage : ImageView

        constructor(view : View) : super(view)
        {
            ButterKnife.bind(this, view)

            this._HomeworkDetailText.setOnClickListener(mOnSingleClickListener)
            this._HomeworkEvalText.setOnClickListener(mOnSingleClickListener)
            initFont()
        }

        private fun initFont()
        {
            _StudentNameText.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
            _HomeworkCompleteText.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
            _HomeworkDetailText.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
            _HomeworkEvalText.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        }

        private val mOnSingleClickListener : OnSingleClickListner = object : OnSingleClickListner()
        {
            override fun onSingleClick(v : View)
            {
                when(v.id)
                {
                    R.id._homeworkDetailText ->
                    {
                        // [숙제 현황 상세 보기] 클릭 이벤트
                        mHomeworkStatusItemListener!!.onClickShowDetail(bindingAdapterPosition)
                    }
                    R.id._homeworkEvalText ->
                    {
                        // [숙제 검사] [검사 수정] 클릭 이벤트
                        mHomeworkStatusItemListener!!.onClickHomeworkChecking(bindingAdapterPosition)
                    }
                }
            }
        }
    }
}