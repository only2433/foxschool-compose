package com.littlefox.app.foxschool.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.viewmodel.QuizFragmentDataObserver
import com.littlefox.library.view.dialog.ProgressWheel
import com.littlefox.library.view.text.SeparateTextView
import com.littlefox.logmonitor.Log

class QuizIntroFragment : Fragment()
{
    @BindView(R.id._quizMainTitleText)
    lateinit var _MainTitleText : SeparateTextView

    @BindView(R.id._quizIntroLoadingLayout)
    lateinit var _LoadingLayout : ProgressWheel

    @BindView(R.id._quizIntroPlayButton)
    lateinit var _QuizPlayButton : TextView

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder
    private lateinit var mQuizFragmentDataObserver : QuizFragmentDataObserver

    fun getInstance() : QuizIntroFragment
    {
        return QuizIntroFragment()
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
        val view = inflater.inflate(R.layout.fragment_quiz_intro, container, false)
        mUnbinder = ButterKnife.bind(this, view)
        initFont()
        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onStart()
    {
        super.onStart()
    }

    override fun onResume()
    {
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
    }

    private fun initFont()
    {
        _MainTitleText.typeface = (Font.getInstance(mContext).getRobotoMedium())
        _QuizPlayButton.typeface = (Font.getInstance(mContext).getRobotoMedium())
    }
    /** ========== Init ========== */

    /** 타이틀 설정 */
    fun setTitle(title : String, subtitle : String)
    {
        Log.i("title : $title, subtitle : $subtitle")
        if(subtitle == "")
        {
            _MainTitleText.text = title
        }
        else
        {
            _MainTitleText.setSeparateText(title, "\n$subtitle")
                .setSeparateColor(resources.getColor(R.color.color_000000), resources.getColor(R.color.color_444444))
                .setSeparateTextSize(CommonUtils.getInstance(mContext).getPixel(74), CommonUtils.getInstance(mContext).getPixel(48))
                .setSeparateTextStyle((Font.getInstance(mContext).getRobotoBold()), (Font.getInstance(mContext).getRobotoMedium()))
                .showView()
        }
    }

    /** 로딩완료 */
    fun loadingComplete()
    {
        try
        {
            _LoadingLayout.visibility = View.GONE
            _QuizPlayButton.visibility = View.VISIBLE
        } catch(e : NullPointerException)
        {
            e.printStackTrace()
        }
    }

    @OnClick(R.id._quizIntroPlayButton)
    fun onClickView(view : View?)
    {
        mQuizFragmentDataObserver.onGoNext()
    }
}