package com.littlefox.app.foxschool.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.quiz.QuizResultViewData
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.DisplayPhoneType
import com.littlefox.app.foxschool.enumerate.Grade
import com.littlefox.app.foxschool.viewmodel.QuizFragmentDataObserver
import com.littlefox.app.foxschool.viewmodel.QuizPresenterDataObserver
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

class QuizResultFragment : Fragment()
{
    @BindView(R.id._quizContentsLayout)
    lateinit var _QuizContentsLayout : ScalableLayout

    @BindView(R.id._quizResultImage)
    lateinit var _QuizResultImage : ImageView

    @BindView(R.id._quizResultLayout)
    lateinit var _QuizResultLayout : ScalableLayout

    @BindView(R.id._quizCorrectLayout)
    lateinit var _QuizCorrectLayout : ImageView

    @BindView(R.id._quizTitleCorrectImage)
    lateinit var _QuizCorrectIconImage : ImageView

    @BindView(R.id._quizTitleCorrectText)
    lateinit var _QuizCorrectTitleText : TextView

    @BindView(R.id._quizResultCorrectText)
    lateinit var _CorrectCountText : TextView

    @BindView(R.id._quizIncorrectLayout)
    lateinit var _QuizIncorrectLayout : ImageView

    @BindView(R.id._quizTitleIncorrectImage)
    lateinit var _QuizIncorrectIconImage : ImageView

    @BindView(R.id._quizTitleIncorrectText)
    lateinit var _QuizInCorrectTitleText : TextView

    @BindView(R.id._quizResultIncorrectText)
    lateinit var _InCorrectCountText : TextView

    @BindView(R.id._quizResultButtonLayout)
    lateinit var _QuizResultButtonLayout : ScalableLayout

    @BindView(R.id._quizSaveButton)
    lateinit var _QuizSaveButton : TextView

    @BindView(R.id._quizReplayButton)
    lateinit var _QuizReplayButton : TextView

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder
    private lateinit var mQuizPresenterDataObserver : QuizPresenterDataObserver
    private lateinit var mQuizFragmentDataObserver : QuizFragmentDataObserver

    private var mQuizTotalCount : Int   = -1
    private var mQuizCorrectCount : Int = -1

    fun getInstance() : QuizResultFragment
    {
        return QuizResultFragment()
    }

    /** ========== LifeCycle ========== */
    override fun onAttach(context : Context)
    {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View?
    {
        Log.i("checkTablet : "+CommonUtils.getInstance(mContext).checkTablet+", radio : "+CommonUtils.getInstance(mContext).getPhoneDisplayRadio())
        var view : View
        if(CommonUtils.getInstance(mContext).getPhoneDisplayRadio() != DisplayPhoneType.DEFAULT)
        {
            view = inflater.inflate(R.layout.fragment_quiz_result_20_9_phone, container, false)
        }
        else
        {
            view = inflater.inflate(R.layout.fragment_quiz_result, container, false)
        }

        mUnbinder = ButterKnife.bind(this, view)
        initFont()
        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        initView()
        settingResultView()
    }

    override fun onStart()
    {
        Log.i("")
        super.onStart()
    }

    override fun onResume()
    {
        Log.i("")
        super.onResume()
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onStop()
    {
        super.onStop()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        mUnbinder.unbind()
    }

    override fun onDestroy()
    {
        super.onDestroy()
    }
    /** ========== LifeCycle ========== */

    /** ========== Init ========== */
    private fun initView()
    {
        mQuizFragmentDataObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(QuizFragmentDataObserver::class.java)
        mQuizPresenterDataObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(QuizPresenterDataObserver::class.java)
        mQuizPresenterDataObserver.resultData.observe(mContext as AppCompatActivity, {quizResultViewData ->
            Log.f("getViewLifecycleOwner().getLifecycle().getCurrentState() : ${this.lifecycle.currentState}")
            setResultInformation(quizResultViewData)
        })
    }

    private fun initFont()
    {
        _CorrectCountText.typeface = Font.getInstance(mContext).getRobotoBold()
        _InCorrectCountText.typeface = Font.getInstance(mContext).getRobotoBold()
        _QuizCorrectTitleText.typeface = Font.getInstance(mContext).getRobotoMedium()
        _QuizInCorrectTitleText.typeface = Font.getInstance(mContext).getRobotoMedium()
        _QuizSaveButton.typeface = Font.getInstance(mContext).getRobotoBold()
        _QuizReplayButton.typeface = Font.getInstance(mContext).getRobotoBold()
    }
    /** ========== Init ========== */

    /** 결과 화면 위치조정 */
    private fun settingResultView()
    {
        /*if(CommonUtils.getInstance(mContext).checkTablet == false)
        {
            if(CommonUtils.getInstance(mContext).getPhoneDisplayRadio() != DisplayPhoneType.RADIO_FLIP)
            {
                _QuizContentsLayout.moveChildView(_QuizResultLayout, 500f, 345f, 919f, 382f);
            }
            val params = _QuizResultButtonLayout.layoutParams as RelativeLayout.LayoutParams
            params.removeRule(RelativeLayout.BELOW)
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        }*/
    }

    /** 결과 */
    private fun setResultInformation(quizResultViewData : QuizResultViewData)
    {
        Log.f("onChanged : quizPlayingCount : ${quizResultViewData.getQuizPlayingCount()}, AnswerCount : ${quizResultViewData.getQuizCorrectAnswerCount()}")

        mQuizTotalCount = quizResultViewData.getQuizPlayingCount() // 문제 수
        mQuizCorrectCount = quizResultViewData.getQuizCorrectAnswerCount() // 정답 수
        try {
            setResult()
        } catch(e : Exception) {
            e.printStackTrace()
        }
    }

    /** 화면에 결과 세팅 */
    private fun setResult()
    {
        Log.f("CorrectCount : $mQuizCorrectCount, InCorrectCount : ${(mQuizTotalCount - mQuizCorrectCount)}")

        setResultTitleText(mQuizTotalCount, mQuizCorrectCount)
        _CorrectCountText.text = mQuizCorrectCount.toString()
        _InCorrectCountText.text = (mQuizTotalCount - mQuizCorrectCount).toString()
    }

    /** 결과 상단 타이틀 이미지 */
    fun setResultTitleText(quizCount : Int, correctCount : Int)
    {
        when(CommonUtils.getInstance(mContext).getMyGrade(quizCount, correctCount))
        {
            Grade.EXCELLENT -> _QuizResultImage.setImageResource(R.drawable.img_excellent)
            Grade.VERYGOOD -> _QuizResultImage.setImageResource(R.drawable.img_very_good)
            Grade.GOODS -> _QuizResultImage.setImageResource(R.drawable.img_good)
            Grade.POOL -> _QuizResultImage.setImageResource(R.drawable.img_try_again)
        }
    }

    /** 저장 버튼 활성화 */
    fun enableSaveButton()
    {
        _QuizSaveButton.isEnabled = true
    }

    @OnClick(R.id._quizSaveButton, R.id._quizReplayButton)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._quizSaveButton ->
            {
                // 결과 저장 버튼
                mQuizFragmentDataObserver.onSaveStudyInformation()
                _QuizSaveButton.alpha = 0.5f
                _QuizSaveButton.isEnabled = false
            }
            R.id._quizReplayButton ->
            {
                // 퀴즈 재시작 버튼
                mQuizFragmentDataObserver.onGoReplay()
                _QuizReplayButton.alpha = 0.5f
                _QuizReplayButton.isEnabled = false
            }
        }
    }
}