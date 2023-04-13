package com.littlefox.app.foxschool.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.viewmodel.factory.FlashcardFactoryViewModel
import com.littlefox.app.foxschool.`object`.data.flashcard.FlashcardDataObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.DisplayPhoneType
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.library.view.animator.ViewAnimator
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import java.util.*

/**
 * 플래시카드 인트로 화면
 */
class FlashCardIntroFragment : Fragment()
{
    @BindView(R.id._contentLayout)
    lateinit var _ContentLayout : ScalableLayout

    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindView(R.id._subtitleText)
    lateinit var _SubtitleText : TextView

    @BindView(R.id._startWordButton)
    lateinit var _StartWordButton : ImageView

    @BindView(R.id._startWordMessageText)
    lateinit var _StartWordMessageText : TextView

    @BindView(R.id._startWordButtonText)
    lateinit var _StartWordButtonText : TextView

    @BindView(R.id._startMeaningButton)
    lateinit var _StartMeaningButton : ImageView

    @BindView(R.id._startMeaningMessageText)
    lateinit var _StartMeaningMessageText : TextView

    @BindView(R.id._startMeaningButtonText)
    lateinit var _StartMeaningButtonText : TextView

    @BindView(R.id._infoButton)
    lateinit var _InfoButton : ImageView

    @BindView(R.id._helpImageLayout)
    lateinit var _HelpImageLayout : ScalableLayout

    @BindView(R.id._helpImage)
    lateinit var _HelpImage : ImageView

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder

    private val factoryViewModel : FlashcardFactoryViewModel by activityViewModels()

    fun getInstance() : FlashCardIntroFragment
    {
        return FlashCardIntroFragment()
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
        val view : View
        if(CommonUtils.getInstance(mContext).getPhoneDisplayRadio() != DisplayPhoneType.DEFAULT)
        {
            view = inflater.inflate(R.layout.fragment_flashcard_intro_flip_phone, container, false)
        }
        else
        {
            view = inflater.inflate(R.layout.fragment_flashcard_intro, container, false)
        }
        mUnbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initFont()
        setupObserverViewModel()
    }

    override fun onStart()
    {
        super.onStart()
    }

    override fun onResume()
    {
        super.onResume()
        Log.f("")
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
        Log.f("")
    }

    override fun onDestroy()
    {
        super.onDestroy()
    }
    /** ========== LifeCycle ========== */

    /** ========== Init ========== */
    private fun initView()
    {
        val viewInformationList = HashMap<Int, String>()
        viewInformationList[R.id._startWordMessageText] = mContext.resources.getString(R.string.text_study_word)
        viewInformationList[R.id._startMeaningMessageText] = mContext.resources.getString(R.string.text_study_meaning)
        CommonUtils.getInstance(mContext).setTextByHtmlType(requireView(), viewInformationList)
        settingButtonText()
    }

    private fun initFont()
    {
        _TitleText.typeface = Font.getInstance(mContext).getTypefaceBold()
        _SubtitleText.typeface = Font.getInstance(mContext).getTypefaceRegular()
        _StartWordMessageText.typeface = Font.getInstance(mContext).getTypefaceRegular()
        _StartMeaningMessageText.typeface = Font.getInstance(mContext).getTypefaceRegular()
        _StartWordButtonText.typeface = Font.getInstance(mContext).getTypefaceBold()
        _StartMeaningButtonText.typeface = Font.getInstance(mContext).getTypefaceBold()
    }
    /** ========== Init ========== */

    /** 디스플레이에 따른 버튼 텍스트 위치 변경 */
    private fun settingButtonText()
    {
        if(CommonUtils.getInstance(mContext).getPhoneDisplayRadio() != DisplayPhoneType.DEFAULT)
        {
            _ContentLayout.moveChildView(_StartWordMessageText, 821f, 529f, 244f, 48f)
            _ContentLayout.moveChildView(_StartMeaningMessageText, 1337f, 529f, 244f, 48f)
        }
        else
        {
            _ContentLayout.moveChildView(_StartWordMessageText, 580f, 529f, 244f, 48f)
            _ContentLayout.moveChildView(_StartMeaningMessageText, 1096f, 529f, 244f, 48f)
        }
    }

    /** ViewModel 옵저버 세팅 */
    private fun setupObserverViewModel()
    {
        factoryViewModel.introTitle.observe(viewLifecycleOwner){data ->
            setTitle(data)
        }
        factoryViewModel.closeHelpView.observe(viewLifecycleOwner){
            hideHelpView()
        }
    }

    /** 타이틀 세팅 */
    private fun setTitle(data : FlashcardDataObject)
    {
        Log.f("")
        when(data.getVocabularyType())
        {
            VocabularyType.VOCABULARY_CONTENTS ->
            {
                _TitleText.text = data.getTitleName()
                if(data.getTitleSubName() != "")
                {
                    _SubtitleText.text = data.getTitleSubName()
                }
            }
            VocabularyType.VOCABULARY_SHELF ->
            {
                val titleText = "${resources.getText(R.string.text_wordbook)} : ${data.getTitleName()}"
                _TitleText.text = titleText
            }
        }
    }

    /** 도움말 화면 표시 */
    private fun showHelpView()
    {
        Log.f("")
        _InfoButton.visibility = View.GONE
        ViewAnimator.animate(_HelpImageLayout)
            .alpha(0.0f, 1.0f)
            .translationX(CommonUtils.getInstance(mContext).getPixel(1920f), 0.0f)
            .duration(Common.DURATION_NORMAL)
            .andAnimate(_ContentLayout).fadeOut()
            .duration(Common.DURATION_NORMAL)
            .onStart {
                _HelpImageLayout.visibility = View.VISIBLE
            }
            .onStop {
                _ContentLayout.visibility = View.GONE
            }
            .start()
    }

    /** 도움말 화면 닫기 */
    private fun hideHelpView()
    {
        Log.f("")
        _InfoButton.visibility = View.VISIBLE
        ViewAnimator.animate(_HelpImageLayout)
            .alpha(1.0f, 0.0f)
            .translationX(0.0f, CommonUtils.getInstance(mContext).getPixel(1920f))
            .duration(Common.DURATION_NORMAL)
            .andAnimate(_ContentLayout).fadeIn()
            .duration(Common.DURATION_NORMAL)
            .onStart {
                _ContentLayout.visibility = View.VISIBLE
            }
            .onStop {
                _HelpImageLayout.visibility = View.GONE
            }
            .start()
    }

    @OnClick(R.id._startWordButton, R.id._startMeaningButton, R.id._infoButton)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._startWordButton -> factoryViewModel.onClickStartWordStudy()
            R.id._startMeaningButton -> factoryViewModel.onClickStartMeaningStudy()
            R.id._infoButton ->
            {
                factoryViewModel.onClickInformation()
                showHelpView()
            }
        }
    }
}