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
import com.littlefox.app.foxschool.api.viewmodel.fragment.FlashcardFragmentViewModel
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.DisplayPhoneType
import com.littlefox.app.foxschool.enumerate.DisplayTabletType
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 플래시카드 결과 화면
 */
class FlashCardResultFragment : Fragment()
{
    @BindView(R.id._contentLayout)
    lateinit var _ContentLayout : ScalableLayout

    @BindView(R.id._topTermsLayout)
    lateinit var _TopTermsLayout : ScalableLayout

    @BindView(R.id._effectLayout)
    lateinit var _EffectLayout : ScalableLayout

    @BindView(R.id._effectImage)
    lateinit var _EffectImage : ImageView

    @BindView(R.id._replayButton)
    lateinit var _ReplayButton : ImageView

    @BindView(R.id._replayIcon)
    lateinit var _ReplayIcon : ImageView

    @BindView(R.id._replayText)
    lateinit var _ReplayText : TextView

    @BindView(R.id._bookmarkButton)
    lateinit var _BookmarkButton : ImageView

    @BindView(R.id._bookmarkIcon)
    lateinit var _BookmarkIcon : ImageView

    @BindView(R.id._bookmarkText)
    lateinit var _BookmarkText : TextView

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder
    private val factoryViewModel : FlashcardFactoryViewModel by activityViewModels()
    private val fragmentViewModel : FlashcardFragmentViewModel by activityViewModels()

    fun getInstance() : FlashCardResultFragment
    {
        return FlashCardResultFragment()
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
            view = inflater.inflate(R.layout.fragment_flashcard_result_flip_phone, container, false)
        }
        else
        {
            view = inflater.inflate(R.layout.fragment_flashcard_result, container, false)
        }
        mUnbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initFont()
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
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
        if(CommonUtils.getInstance(mContext).checkTablet
            && CommonUtils.getInstance(mContext).getTabletDisplayRadio() == DisplayTabletType.RADIO_4_3)
        {
            _TopTermsLayout.setScaleSize(1920f, 250f)
            _EffectLayout.setScaleSize(1920f, 1156f)
            _EffectLayout.moveChildView(_EffectImage, 161f, 180f)
        }
    }

    private fun initFont()
    {
        _ReplayText.typeface = Font.getInstance(mContext).getTypefaceBold()
        _BookmarkText.typeface = Font.getInstance(mContext).getTypefaceBold()
    }
    /** ========== Init ========== */

    /** ViewModel 옵저버 세팅 */
    private fun setupObserverViewModel()
    {
        fragmentViewModel.settingBookmarkButton.observe(viewLifecycleOwner){ isBookmark ->
            settingResultView(isBookmark)
        }
    }

    /**
     * 북마크 유/무에 따라 결과 화면 세팅
     *  - 북마크 있을 때 (다시 학습 || 북마크 학습)
     *  - 북마크 없을 때 (다시 학습)
     */
    private fun settingResultView(isBookmarkEnable : Boolean)
    {
        if(isBookmarkEnable)
        {
            if(CommonUtils.getInstance(mContext).getPhoneDisplayRadio() != DisplayPhoneType.DEFAULT)
            {
                _ContentLayout.run {
                    moveChildView(_ReplayButton, 715f, 762f)
                    moveChildView(_ReplayIcon, 803f, 810f)
                    moveChildView(_ReplayText, 865f, 762f)
                }
            }
            else
            {
                _ContentLayout.run {
                    moveChildView(_ReplayButton, 474f, 762f)
                    moveChildView(_ReplayIcon, 562f, 820f)
                    moveChildView(_ReplayText, 624f, 762f)
                }

            }
            _BookmarkButton.visibility = View.VISIBLE
            _BookmarkIcon.visibility = View.VISIBLE
            _BookmarkText.visibility = View.VISIBLE
        }
        else
        {
            if(CommonUtils.getInstance(mContext).getPhoneDisplayRadio() != DisplayPhoneType.DEFAULT)
            {
                _ContentLayout.run {
                    moveChildView(_ReplayButton, 973f, 762f)
                    moveChildView(_ReplayIcon, 1061f, 810f)
                    moveChildView(_ReplayText, 1123f, 762f)
                }

            }
            else
            {
                _ContentLayout.run {
                    moveChildView(_ReplayButton, 732f, 762f)
                    moveChildView(_ReplayIcon, 820f, 820f)
                    moveChildView(_ReplayText, 882f, 762f)
                }
            }
            _BookmarkButton.visibility = View.GONE
            _BookmarkIcon.visibility = View.GONE
            _BookmarkText.visibility = View.GONE
        }
    }

    @OnClick(R.id._replayButton, R.id._bookmarkButton)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._replayButton -> factoryViewModel.onClickReplayStudy()
            R.id._bookmarkButton -> factoryViewModel.onClickBookmarkStudy()
        }
    }
}