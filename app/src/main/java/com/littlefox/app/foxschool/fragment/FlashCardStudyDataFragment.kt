package com.littlefox.app.foxschool.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.viewmodel.factory.FlashcardFactoryViewModel
import com.littlefox.app.foxschool.`object`.result.flashcard.FlashCardDataResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.DisplayPhoneType
import com.littlefox.app.foxschool.enumerate.DisplayTabletType
import com.littlefox.app.foxschool.enumerate.FlashcardStudyType
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import java.util.*

/**
 * 플래시카드 학습 화면
 */
class FlashCardStudyDataFragment : Fragment()
{
    @BindView(R.id._topTermsLayout)
    lateinit var _TopTermsLayout : ScalableLayout

    @BindView(R.id._itemViewFlipper)
    lateinit var _ItemViewFlipper : ViewFlipper

    @BindView(R.id._studyControllerLayout)
    lateinit var _StudyControllerLayout : ScalableLayout

    @BindView(R.id._prevButton)
    lateinit var _PrevButton : ImageView

    @BindView(R.id._nextButton)
    lateinit var _NextButton : ImageView

    @BindView(R.id._prevButtonRect)
    lateinit var _PrevButtonRect : ImageView

    @BindView(R.id._nextButtonRect)
    lateinit var _NextButtonRect : ImageView

    /** 플래시카드 ViewPager (카드 2장) */
    lateinit var _Item1ContainerLayout : FrameLayout
    lateinit var _Item2ContainerLayout : FrameLayout

    lateinit var _Item1BackLayout : ScalableLayout
    lateinit var _Item1MeaningTitleText : TextView
    lateinit var _Item1BackStatusText : TextView
    lateinit var _Item1BackBookmarkButton : ImageView
    lateinit var _Item1BackBookmarkButtonRect : ImageView
    lateinit var _Item1BackMeaningRect : ImageView
    lateinit var _Item1FrontLayout : ScalableLayout
    lateinit var _Item1FrontStatusText : TextView
    lateinit var _Item1FrontBookmarkButton : ImageView
    lateinit var _Item1FrontBookmarkButtonRect : ImageView
    lateinit var _Item1WordTitleText : TextView
    lateinit var _Item1WordExampleText : TextView
    lateinit var _Item1FrontWordRect : ImageView
    lateinit var _Item2BackLayout : ScalableLayout
    lateinit var _Item2MeaningTitleText : TextView
    lateinit var _Item2BackStatusText : TextView
    lateinit var _Item2BackBookmarkButton : ImageView
    lateinit var _Item2BackBookmarkButtonRect : ImageView
    lateinit var _Item2BackMeaningRect : ImageView
    lateinit var _Item2FrontLayout : ScalableLayout
    lateinit var _Item2FrontStatusText : TextView
    lateinit var _Item2FrontBookmarkButton : ImageView
    lateinit var _Item2FrontBookmarkButtonRect : ImageView
    lateinit var _Item2WordTitleText : TextView
    lateinit var _Item2WordExampleText : TextView
    lateinit var _Item2FrontWordRect : ImageView

    lateinit var card1FrontView : View
    lateinit var card2FrontView : View
    lateinit var card1BackView : View
    lateinit var card2BackView : View

    companion object
    {
        // 최대 글자수
        private const val MAX_TEXT_WORD_COUNT : Int     = 20
        private const val MAX_TEXT_MEANING_COUNT : Int  = 30

        // ViewPager TAG
        private const val ITEM_1_TAG : String           = "item_1_tag"
        private const val ITEM_2_TAG : String           = "item_2_tag"

        private const val MESSAGE_SOUND_PLAY : Int      = 100
        private const val MESSAGE_INIT_FLIP : Int       = 101
    }

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder

    private lateinit var mTopInAnimatorSet : AnimatorSet
    private lateinit var mBottomOutAnimatorSet : AnimatorSet
    private var mSlideInLeftAnimation : Animation?      = null
    private var mSlideOutRightAnimation : Animation?    = null
    private var mSlideInRightAnimation : Animation?     = null
    private var mSlideOutLeftAnimation : Animation?     = null

    private lateinit var mDataList : ArrayList<FlashCardDataResult>
    private var mCurrentFlashcardStudyType : FlashcardStudyType = FlashcardStudyType.WORD_START
    private var isInitDataSettingComplete : Boolean = false
    private var isAutoPlayStopPossible : Boolean = false
    private var mCurrentCardIndex : Int = 0
    private var mBeforeCardIndex : Int = 0

    private val factoryViewModel : FlashcardFactoryViewModel by activityViewModels()

    var mMainHandler : Handler = object : Handler()
    {
        override fun handleMessage(msg : Message)
        {
            when(msg.what)
            {
                MESSAGE_SOUND_PLAY ->
                {
                    // 사운드 재생
                    enableControllerButton(true)
                    factoryViewModel.onActionAutoSound(mDataList[mCurrentCardIndex].getID())
                }
                MESSAGE_INIT_FLIP ->
                {
                    // 카드 플립
                    if(mDataList.size <= 1)
                    {
                        initFlipSingleView()
                    }
                    else
                    {
                        initFlipView()
                    }
                }
            }
        }
    }

    fun getInstance() : FlashCardStudyDataFragment
    {
        return FlashCardStudyDataFragment()
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
            view = inflater.inflate(R.layout.fragment_flashcard_study_data_flip_phone, container, false)
        }
        else
        {
            view = inflater.inflate(R.layout.fragment_flashcard_study_data, container, false)
        }
        mUnbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?)
    {
        Log.f("")
        super.onViewCreated(view, savedInstanceState)
        initView()
        initFont()
        initLoadAnimation()
        Log.f("")
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
            _TopTermsLayout.setScaleSize(1920f, 300f)
            _StudyControllerLayout.run {
                setScaleSize(1920f, 730f)
                moveChildView(_PrevButton, 120f, 624f)
                moveChildView(_NextButton, 1746f, 624f)
                moveChildView(_PrevButtonRect, 105f, 609f)
                moveChildView(_NextButtonRect, 1731f, 609f)
            }

        }
        initCardLayout()
    }

    private fun initFont()
    {
        _Item1FrontStatusText.typeface = Font.getInstance(mContext).getTypefaceRegular()
        _Item1BackStatusText.typeface = Font.getInstance(mContext).getTypefaceRegular()
        _Item1WordTitleText.typeface = Font.getInstance(mContext).getTypefaceBold()
        _Item1WordExampleText.typeface = Font.getInstance(mContext).getTypefaceRegular()
        _Item1MeaningTitleText.typeface = Font.getInstance(mContext).getTypefaceBold()
        _Item2FrontStatusText.typeface = Font.getInstance(mContext).getTypefaceRegular()
        _Item2BackStatusText.typeface = Font.getInstance(mContext).getTypefaceRegular()
        _Item2WordTitleText.typeface = Font.getInstance(mContext).getTypefaceBold()
        _Item2WordExampleText.typeface = Font.getInstance(mContext).getTypefaceRegular()
        _Item2MeaningTitleText.typeface = Font.getInstance(mContext).getTypefaceBold()
    }

    private fun initLoadAnimation()
    {
        mSlideInLeftAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_left)
        mSlideInRightAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right)
        mSlideOutLeftAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_left)
        mSlideOutRightAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_right)
    }

    /** 카드 레이아웃 생성 */
    private fun initCardLayout()
    {
        if(_ItemViewFlipper.childCount > 0)
        {
            _ItemViewFlipper.removeAllViews()
        }

        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        _Item1ContainerLayout = FrameLayout(mContext)
        _Item2ContainerLayout = FrameLayout(mContext)

        // 플래시카드 앞면 세팅
        if(CommonUtils.getInstance(mContext).getPhoneDisplayRadio() != DisplayPhoneType.DEFAULT)
        {
            card1FrontView = LayoutInflater.from(mContext).inflate(R.layout.include_flashcard_card_front_flip_phone, null, false)
            card2FrontView = LayoutInflater.from(mContext).inflate(R.layout.include_flashcard_card_front_flip_phone, null, false)
        }
        else
        {
            card1FrontView = LayoutInflater.from(mContext).inflate(R.layout.include_flashcard_card_front, null, false)
            card2FrontView = LayoutInflater.from(mContext).inflate(R.layout.include_flashcard_card_front, null, false)
        }

        _Item1FrontLayout = card1FrontView.findViewById<View>(R.id._itemFrontLayout) as ScalableLayout
        _Item1FrontStatusText = card1FrontView.findViewById<View>(R.id._itemFrontStatusText) as TextView
        _Item1WordTitleText = card1FrontView.findViewById<View>(R.id._itemWordTitleText) as TextView
        _Item1WordExampleText = card1FrontView.findViewById<View>(R.id._itemWordExampleText) as TextView
        _Item1FrontWordRect = card1FrontView.findViewById<View>(R.id._itemFrontWordRect) as ImageView
        _Item1FrontBookmarkButton = card1FrontView.findViewById<View>(R.id._itemFrontBookmarkButton) as ImageView
        _Item1FrontBookmarkButtonRect = card1FrontView.findViewById<View>(R.id._itemFrontBookmarkButtonRect) as ImageView
        _Item1FrontWordRect.setOnClickListener(mOnClickListener)
        _Item1FrontBookmarkButtonRect.setOnClickListener(mOnClickListener)

        _Item2FrontLayout = card2FrontView.findViewById<View>(R.id._itemFrontLayout) as ScalableLayout
        _Item2FrontStatusText = card2FrontView.findViewById<View>(R.id._itemFrontStatusText) as TextView
        _Item2WordTitleText = card2FrontView.findViewById<View>(R.id._itemWordTitleText) as TextView
        _Item2WordExampleText = card2FrontView.findViewById<View>(R.id._itemWordExampleText) as TextView
        _Item2FrontWordRect = card2FrontView.findViewById<View>(R.id._itemFrontWordRect) as ImageView
        _Item2FrontBookmarkButton = card2FrontView.findViewById<View>(R.id._itemFrontBookmarkButton) as ImageView
        _Item2FrontBookmarkButtonRect = card2FrontView.findViewById<View>(R.id._itemFrontBookmarkButtonRect) as ImageView
        _Item2FrontWordRect.setOnClickListener(mOnClickListener)
        _Item2FrontBookmarkButtonRect.setOnClickListener(mOnClickListener)

        // 플래시카드 뒷면 세팅
        if(CommonUtils.getInstance(mContext).getPhoneDisplayRadio() != DisplayPhoneType.DEFAULT)
        {
            card1BackView = LayoutInflater.from(mContext).inflate(R.layout.include_flashcard_card_back_flip_phone, null, false)
            card2BackView = LayoutInflater.from(mContext).inflate(R.layout.include_flashcard_card_back_flip_phone, null, false)
        }
        else
        {
            card1BackView = LayoutInflater.from(mContext).inflate(R.layout.include_flashcard_card_back, null, false)
            card2BackView = LayoutInflater.from(mContext).inflate(R.layout.include_flashcard_card_back, null, false)
        }

        _Item1BackLayout = card1BackView.findViewById<View>(R.id._itemBackLayout) as ScalableLayout
        _Item1MeaningTitleText = card1BackView.findViewById<View>(R.id._itemMeaningTitleText) as TextView
        _Item1BackStatusText = card1BackView.findViewById<View>(R.id._itemBackStatusText) as TextView
        _Item1BackMeaningRect = card1BackView.findViewById<View>(R.id._itemBackMeaningRect) as ImageView
        _Item1BackBookmarkButton = card1BackView.findViewById<View>(R.id._itemBackBookmarkButton) as ImageView
        _Item1BackBookmarkButtonRect = card1BackView.findViewById<View>(R.id._itemBackBookmarkButtonRect) as ImageView
        _Item1BackMeaningRect.setOnClickListener(mOnClickListener)
        _Item1BackBookmarkButtonRect.setOnClickListener(mOnClickListener)

        _Item2BackLayout = card2BackView.findViewById<View>(R.id._itemBackLayout) as ScalableLayout
        _Item2MeaningTitleText = card2BackView.findViewById<View>(R.id._itemMeaningTitleText) as TextView
        _Item2BackStatusText = card2BackView.findViewById<View>(R.id._itemBackStatusText) as TextView
        _Item2BackMeaningRect = card2BackView.findViewById<View>(R.id._itemBackMeaningRect) as ImageView
        _Item2BackBookmarkButton = card2BackView.findViewById<View>(R.id._itemBackBookmarkButton) as ImageView
        _Item2BackBookmarkButtonRect = card2BackView.findViewById<View>(R.id._itemBackBookmarkButtonRect) as ImageView
        _Item2BackMeaningRect.setOnClickListener(mOnClickListener)
        _Item2BackBookmarkButtonRect.setOnClickListener(mOnClickListener)

        _Item1ContainerLayout.tag = ITEM_1_TAG
        _Item2ContainerLayout.tag = ITEM_2_TAG

        _ItemViewFlipper.addView(_Item1ContainerLayout, params)
        _ItemViewFlipper.addView(_Item2ContainerLayout, params)

        settingStudyControllerView()
        changeCameraDistance()
    }

    /** 플래시카드 1장일 때 Flip */
    private fun initFlipSingleView()
    {
        if(mDataList[mBeforeCardIndex].isBackVisible())
        {
            when(mCurrentFlashcardStudyType)
            {
                FlashcardStudyType.WORD_START ->
                {
                    // 단어로 학습하기 (뜻->단어)
                    mTopInAnimatorSet = getRotationInAnimatorSet(_Item1FrontLayout, isAnimation = true, isReverse = true)
                    mBottomOutAnimatorSet = getRotationOutAnimatorSet(_Item1BackLayout, isAnimation = true, isReverse = true)
                }
                FlashcardStudyType.MEANING_START ->
                {
                    // 뜻으로 학습하기 (단어->뜻)
                    mTopInAnimatorSet = getRotationInAnimatorSet(_Item1BackLayout, isAnimation = true, isReverse = true)
                    mBottomOutAnimatorSet = getRotationOutAnimatorSet(_Item1FrontLayout, isAnimation = true, isReverse = true)
                }
            }
            mTopInAnimatorSet.start()
            mBottomOutAnimatorSet.start()
            mDataList[mBeforeCardIndex].setBackVisible(false)
        }
    }

    private fun initFlipView()
    {
        Log.f("beforeIndex : " + mBeforeCardIndex + ", isBackVisible() : " + mDataList[mBeforeCardIndex].isBackVisible())
        if(mDataList[mBeforeCardIndex].isBackVisible())
        {
            when(mCurrentFlashcardStudyType)
            {
                FlashcardStudyType.WORD_START ->
                {
                    // 단어로 학습하기
                    if(_ItemViewFlipper.currentView.tag == ITEM_1_TAG)
                    {
                        mTopInAnimatorSet = getRotationInAnimatorSet(_Item2FrontLayout, isAnimation = true, isReverse = true)
                        mBottomOutAnimatorSet = getRotationOutAnimatorSet(_Item2BackLayout, isAnimation = true, isReverse = true)
                    }
                    else
                    {
                        mTopInAnimatorSet = getRotationInAnimatorSet(_Item1FrontLayout, isAnimation = true, isReverse = true)
                        mBottomOutAnimatorSet = getRotationOutAnimatorSet(_Item1BackLayout, isAnimation = true, isReverse = true)
                    }
                }

                FlashcardStudyType.MEANING_START ->
                {
                    // 뜻으로 학습하기
                    if(_ItemViewFlipper.currentView.tag == ITEM_1_TAG)
                    {
                        mTopInAnimatorSet = getRotationInAnimatorSet(_Item2BackLayout, isAnimation = true, isReverse = true)
                        mBottomOutAnimatorSet = getRotationOutAnimatorSet(_Item2FrontLayout, isAnimation = true, isReverse = true)
                    }
                    else
                    {
                        mTopInAnimatorSet = getRotationInAnimatorSet(_Item1BackLayout, isAnimation = true, isReverse = true)
                        mBottomOutAnimatorSet = getRotationOutAnimatorSet(_Item1FrontLayout, isAnimation = true, isReverse = true)
                    }
                }
            }
            mTopInAnimatorSet.start()
            mBottomOutAnimatorSet.start()
            mDataList[mBeforeCardIndex].setBackVisible(false)
        }
    }
    /** ========== Init ========== */

    /** ViewModel 옵저버 세팅 */
    private fun setupObserverViewModel()
    {
        factoryViewModel.notifyListUpdate.observe(viewLifecycleOwner){data ->
            setData(data)
        }

        factoryViewModel.initStudySetting.observe(viewLifecycleOwner){type ->
            Log.f("LifeCycle : " + viewLifecycleOwner.lifecycle.currentState)
            Log.f("mCurrentFlashcardStudyType : $type")
            if(viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.STARTED)
            {
                isInitDataSettingComplete = false
                mCurrentCardIndex = 0
                mBeforeCardIndex = 0
                mCurrentFlashcardStudyType = type
                initCardLayout()
                initFont()
                settingContainerLayoutByStudyType()
                settingCardView(ITEM_1_TAG)
                mMainHandler.sendEmptyMessageDelayed(MESSAGE_SOUND_PLAY, Common.DURATION_NORMAL)
            }
        }

        factoryViewModel.nextCardData.observe(viewLifecycleOwner){
            Log.f("LifeCycle : " + viewLifecycleOwner.lifecycle.currentState)
            if(viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED)
            {
                Log.f("AUTO PLAY NEXT CARD : " + (mCurrentCardIndex + 1))
                showNextStudyCard()
            }
        }
    }

    /** 학습모드에 따른 카드 세팅 (앞/뒤) */
    private fun settingContainerLayoutByStudyType()
    {
        _Item1ContainerLayout.removeAllViews()
        _Item2ContainerLayout.removeAllViews()
        when(mCurrentFlashcardStudyType)
        {
            FlashcardStudyType.WORD_START ->
            {
                _Item1ContainerLayout.addView(card1BackView)
                _Item1ContainerLayout.addView(card1FrontView)
                _Item2ContainerLayout.addView(card2BackView)
                _Item2ContainerLayout.addView(card2FrontView)
            }
            FlashcardStudyType.MEANING_START ->
            {
                _Item1ContainerLayout.addView(card1FrontView)
                _Item1ContainerLayout.addView(card1BackView)
                _Item2ContainerLayout.addView(card2FrontView)
                _Item2ContainerLayout.addView(card2BackView)
            }
        }
    }

    /**
     * 해당 태그에 맞는 뷰의 정보 세팅 을 하는 메소드
     * @param tag 해당 뷰의 TAG
     */
    private fun settingCardView(tag : String)
    {
        Log.f("currentGetTag : " + _ItemViewFlipper.currentView.tag)
        Log.f("beforeIndex : $mBeforeCardIndex, currentIndex : $mCurrentCardIndex")
        Log.f("isBackVisible : " + mDataList[mCurrentCardIndex].isBackVisible())

        if(tag == ITEM_1_TAG)
        {
            setCurrentItemIndexStatus(_Item1BackStatusText, mDataList[mCurrentCardIndex].getCardNumber(), mDataList.size)
            setCurrentItemIndexStatus(_Item1FrontStatusText, mDataList[mCurrentCardIndex].getCardNumber(), mDataList.size)
            measureWordTextSize(_Item1FrontLayout, _Item1WordTitleText)
            measureMeaningTextSize(_Item1FrontLayout, _Item1MeaningTitleText)

            val isBookmarkedItem = mDataList[mCurrentCardIndex].isBookmarked()
            checkBookmark(_Item1FrontBookmarkButton, _Item1BackBookmarkButton, isBookmarkedItem)

            _Item1WordTitleText.text = mDataList[mCurrentCardIndex].getWordText()
            _Item1MeaningTitleText.text = mDataList[mCurrentCardIndex].getMeaningText()

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                _Item1WordExampleText.text = Html.fromHtml(mDataList[mCurrentCardIndex].getExampleText(), Html.FROM_HTML_MODE_LEGACY)
            }
            else
            {
                _Item1WordExampleText.text = Html.fromHtml(mDataList[mCurrentCardIndex].getExampleText())
            }
        }
        else
        {
            setCurrentItemIndexStatus(_Item2BackStatusText, mDataList[mCurrentCardIndex].getCardNumber(), mDataList.size)
            setCurrentItemIndexStatus(_Item2FrontStatusText, mDataList[mCurrentCardIndex].getCardNumber(), mDataList.size)
            measureWordTextSize(_Item2FrontLayout, _Item2WordTitleText)
            measureMeaningTextSize(_Item2FrontLayout, _Item2MeaningTitleText)

            val isBookmarkedItem = mDataList[mCurrentCardIndex].isBookmarked()
            checkBookmark(_Item2FrontBookmarkButton, _Item2BackBookmarkButton, isBookmarkedItem)

            _Item2WordTitleText.text = mDataList[mCurrentCardIndex].getWordText()
            _Item2MeaningTitleText.text = mDataList[mCurrentCardIndex].getMeaningText()

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                _Item2WordExampleText.text = Html.fromHtml(mDataList[mCurrentCardIndex].getExampleText(), Html.FROM_HTML_MODE_LEGACY)
            }
            else
            {
                _Item2WordExampleText.text = Html.fromHtml(mDataList[mCurrentCardIndex].getExampleText())
            }
        }
    }

    /** 
     * 플래시카드 이전 버튼 표시 세팅
     * 첫번째 카드를 표시하고 있을 때에는 이전 버튼을 표시하지 않는다.
     */
    private fun settingStudyControllerView()
    {
        if(mCurrentCardIndex == 0)
        {
            _PrevButton.visibility = View.INVISIBLE
        } else
        {
            _PrevButton.visibility = View.VISIBLE
        }
    }

    /** 플래시카드 넘어가는 모션 */
    private fun changeCameraDistance()
    {
        val distance = 8000
        val scale = resources.displayMetrics.density * distance
        _Item1FrontLayout.cameraDistance = scale
        _Item1BackLayout.cameraDistance = scale
        _Item2FrontLayout.cameraDistance = scale
        _Item2BackLayout.cameraDistance = scale
    }

    /** 플래시카드 단어 글씨 크기 설정 */
    private fun measureWordTextSize(baseLayout : ScalableLayout, textView : TextView)
    {
        if(mDataList[mCurrentCardIndex].getWordText().length < MAX_TEXT_WORD_COUNT)
        {
            baseLayout.setScale_TextSize(textView, 100f)
        }
        else
        {
            baseLayout.setScale_TextSize(textView, 76f)
        }
    }

    /** 플래시카드 뜻 글씨 크기 설정 */
    private fun measureMeaningTextSize(baseLayout : ScalableLayout, textView : TextView)
    {
        if(mDataList[mCurrentCardIndex].getMeaningText().length < MAX_TEXT_MEANING_COUNT)
        {
            baseLayout.setScale_TextSize(textView, 86f)
        }
        else
        {
            baseLayout.setScale_TextSize(textView, 66f)
        }
    }

    /** 플래시카드 좌우 이동 애니메이션 (IN) */
    private fun getRotationInAnimatorSet(view : View, isAnimation : Boolean, isReverse : Boolean) : AnimatorSet
    {
        val animatorSet = AnimatorSet()
        val alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f)
        alphaAnimator.duration = 0

        val rotateAnimator : ObjectAnimator
        if(isReverse)
        {
            rotateAnimator = ObjectAnimator.ofFloat(view, "rotationX", -180f, 0f)
        }
        else
        {
            rotateAnimator = ObjectAnimator.ofFloat(view, "rotationX", 180f, 0f)
        }

        if(isAnimation)
        {
            rotateAnimator.duration = Common.DURATION_LONG
        }
        else
        {
            rotateAnimator.duration = 0
        }

        val alphaDelayAnimator = ObjectAnimator.ofFloat(view, "alpha", 0.0f, 1.0f)
        if(isAnimation)
        {
            alphaDelayAnimator.startDelay = Common.DURATION_NORMAL
        }
        else
        {
            alphaDelayAnimator.startDelay = 0
        }

        alphaDelayAnimator.duration = 0
        animatorSet.playTogether(alphaAnimator, rotateAnimator, alphaDelayAnimator)
        return animatorSet
    }

    /** 플래시카드 좌우 이동 애니메이션 (OUT) */
    private fun getRotationOutAnimatorSet(view : View, isAnimation : Boolean, isReverse : Boolean) : AnimatorSet
    {
        val animatorSet = AnimatorSet()

        val rotateAnimator : ObjectAnimator
        if(isReverse)
        {
            rotateAnimator = ObjectAnimator.ofFloat(view, "rotationX", 0f, 180f)
        }
        else
        {
            rotateAnimator = ObjectAnimator.ofFloat(view, "rotationX", 0f, -180f)
        }

        if(isAnimation)
        {
            rotateAnimator.duration = Common.DURATION_LONG
        }
        else
        {
            rotateAnimator.duration = 0
        }

        val alphaDelayAnimator = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f)
        if(isAnimation)
        {
            alphaDelayAnimator.startDelay = Common.DURATION_NORMAL
        }
        else
        {
            alphaDelayAnimator.startDelay = 0
        }

        alphaDelayAnimator.duration = 0
        animatorSet.playTogether(rotateAnimator, alphaDelayAnimator)
        return animatorSet
    }

    /** 플래시카드 플립 애니메이션 (IN) */
    private fun flipCardInAnimation()
    {
        when(mCurrentFlashcardStudyType)
        {
            FlashcardStudyType.WORD_START ->
            {
                if(_ItemViewFlipper.currentView.tag == ITEM_1_TAG)
                {
                    mTopInAnimatorSet = getRotationInAnimatorSet(_Item1BackLayout, isAnimation = true, isReverse = false)
                    mBottomOutAnimatorSet = getRotationOutAnimatorSet(_Item1FrontLayout, isAnimation = true, isReverse = false)
                }
                else
                {
                    mTopInAnimatorSet = getRotationInAnimatorSet(_Item2BackLayout, isAnimation = true, isReverse = false)
                    mBottomOutAnimatorSet = getRotationOutAnimatorSet(_Item2FrontLayout, isAnimation = true, isReverse = false)
                }
            }
            FlashcardStudyType.MEANING_START ->
            {
                if(_ItemViewFlipper.currentView.tag == ITEM_1_TAG)
                {
                    mTopInAnimatorSet = getRotationInAnimatorSet(_Item1FrontLayout, isAnimation = true, isReverse = false)
                    mBottomOutAnimatorSet = getRotationOutAnimatorSet(_Item1BackLayout, isAnimation = true, isReverse = false)
                }
                else
                {
                    mTopInAnimatorSet = getRotationInAnimatorSet(_Item2FrontLayout, isAnimation = true, isReverse = false)
                    mBottomOutAnimatorSet = getRotationOutAnimatorSet(_Item2BackLayout, isAnimation = true, isReverse = false)
                }
            }
        }
        mTopInAnimatorSet.start()
        mBottomOutAnimatorSet.start()
        mDataList[mCurrentCardIndex].setBackVisible(true)
    }

    /** 플래시카드 플립 애니메이션 (OUT) */
    private fun flipCardOutAnimation()
    {
        when(mCurrentFlashcardStudyType)
        {
            FlashcardStudyType.WORD_START ->
            {
                if(_ItemViewFlipper.currentView.tag == ITEM_1_TAG)
                {
                    mTopInAnimatorSet = getRotationInAnimatorSet(_Item1FrontLayout, isAnimation = true, isReverse = true)
                    mBottomOutAnimatorSet = getRotationOutAnimatorSet(_Item1BackLayout, isAnimation = true, isReverse = true)
                }
                else
                {
                    mTopInAnimatorSet = getRotationInAnimatorSet(_Item2FrontLayout, isAnimation = true, isReverse = true)
                    mBottomOutAnimatorSet = getRotationOutAnimatorSet(_Item2BackLayout, isAnimation = true, isReverse = true)
                }
            }
            FlashcardStudyType.MEANING_START ->
            {
                if(_ItemViewFlipper.currentView.tag == ITEM_1_TAG)
                {
                    mTopInAnimatorSet = getRotationInAnimatorSet(_Item1BackLayout, isAnimation = true, isReverse = true)
                    mBottomOutAnimatorSet = getRotationOutAnimatorSet(_Item1FrontLayout, isAnimation = true, isReverse = true)
                }
                else
                {
                    mTopInAnimatorSet = getRotationInAnimatorSet(_Item2BackLayout, isAnimation = true, isReverse = true)
                    mBottomOutAnimatorSet = getRotationOutAnimatorSet(_Item2FrontLayout, isAnimation = true, isReverse = true)
                }
            }
        }
        mTopInAnimatorSet.start()
        mBottomOutAnimatorSet.start()
        mDataList[mCurrentCardIndex].setBackVisible(false)
    }

    /** 화살표 버튼 활성/비활성 */
    private fun enableControllerButton(isEnable : Boolean)
    {
        isAutoPlayStopPossible = isEnable
        if(isEnable)
        {
            _PrevButton.alpha = 1.0f
            _NextButton.alpha = 1.0f
            _PrevButton.isEnabled = true
            _NextButton.isEnabled = true
        } 
        else
        {
            _PrevButton.alpha = 0.5f
            _NextButton.alpha = 0.5f
            _PrevButton.isEnabled = false
            _NextButton.isEnabled = false
        }
    }

    /** 화면에 인덱스 세팅 (현재 인덱스/전체 수) */
    private fun setCurrentItemIndexStatus(view : TextView, currentIndex : Int, maxCount : Int)
    {
        val countText = String.format(resources.getString(R.string.text_item_count_question), currentIndex, maxCount)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            view.text = Html.fromHtml(countText, Html.FROM_HTML_MODE_LEGACY)
        } else
        {
            view.text = Html.fromHtml(countText)
        }
    }

    /** 북마크 ON/OFF 이미지 세팅 */
    private fun checkBookmark(frontBookmark : ImageView, backBookmark : ImageView, isEnable : Boolean)
    {
        if(isEnable)
        {
            frontBookmark.setImageResource(R.drawable.flashcard_bookmark_on)
            backBookmark.setImageResource(R.drawable.flashcard_bookmark_on)
        } 
        else
        {
            frontBookmark.setImageResource(R.drawable.flashcard_bookmark_off)
            backBookmark.setImageResource(R.drawable.flashcard_bookmark_off)
        }
    }

    /** 이전 카드 표시 */
    private fun showPrevStudyCard()
    {
        mBeforeCardIndex = mCurrentCardIndex
        enableControllerButton(false)
        if(mCurrentCardIndex <= 0)
        {
            mCurrentCardIndex = 0
            return
        } 
        else
        {
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_INIT_FLIP, Common.DURATION_NORMAL)
            mCurrentCardIndex--
        }
        settingStudyControllerView()

        // 현재의 뷰가 아닌 다음 뷰 화면을 세팅해줘야한다.
        if(_ItemViewFlipper.currentView.tag == ITEM_1_TAG)
        {
            settingCardView(ITEM_2_TAG)
        } 
        else
        { 
            settingCardView(ITEM_1_TAG)
        }

        _ItemViewFlipper.inAnimation = mSlideInLeftAnimation
        _ItemViewFlipper.outAnimation = mSlideOutRightAnimation
        _ItemViewFlipper.showPrevious()
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_SOUND_PLAY, Common.DURATION_NORMAL)
    }

    /** 다음 카드 표시 */
    private fun showNextStudyCard()
    {
        mBeforeCardIndex = mCurrentCardIndex
        Log.f("mBeforeCardIndex : " + mBeforeCardIndex + ", nextCard Index : " + (mCurrentCardIndex + 1) + ", mDataList.size() : " + mDataList.size)
        if(mCurrentCardIndex >= mDataList.size - 1)
        {
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_INIT_FLIP, Common.DURATION_NORMAL)
            factoryViewModel.onEndStudyFlashCard()
            return
        } 
        else
        {
            enableControllerButton(false)
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_INIT_FLIP, Common.DURATION_NORMAL)
            mCurrentCardIndex++
        }
        settingStudyControllerView()

        // 현재의 뷰가 아닌 다음 뷰 화면을 세팅해줘야한다.
        if(_ItemViewFlipper.currentView.tag == ITEM_1_TAG)
        {
            settingCardView(ITEM_2_TAG)
        } 
        else
        {
            settingCardView(ITEM_1_TAG)
        }

        _ItemViewFlipper.inAnimation = mSlideInRightAnimation
        _ItemViewFlipper.outAnimation = mSlideOutLeftAnimation
        _ItemViewFlipper.showNext()
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_SOUND_PLAY, Common.DURATION_LONG)
    }

    /**
     * 음성 재생해도 되는 조건인지 체크
     */
    private fun checkPlaySound()
    { 
        // 단어가 보여지는 상태에서만 음성을 재생해야한다.
        if((mCurrentFlashcardStudyType == FlashcardStudyType.WORD_START && mDataList[mCurrentCardIndex].isBackVisible() == false) || 
            (mCurrentFlashcardStudyType == FlashcardStudyType.MEANING_START && mDataList[mCurrentCardIndex].isBackVisible() == true))
        {
            factoryViewModel.onClickSound(mDataList[mCurrentCardIndex].getID())
        }
    }

    /** 플래시카드 리스트 데이터 세팅 */
    fun setData(dataList : ArrayList<FlashCardDataResult>)
    {
        if(isInitDataSettingComplete)
        {
            return
        }
        mDataList = dataList
        isInitDataSettingComplete = true
        Log.f("mDataList size : " + mDataList.size)
    }

    @OnClick(R.id._flipButton, R.id._prevButtonRect, R.id._nextButtonRect)
    fun onClickView(view : View)
    {
        if(isAutoPlayStopPossible == false)
        {
            return
        }

        factoryViewModel.onActionStudyCard()
        when(view.id)
        {
            R.id._flipButton ->
            {
                if(mDataList[mCurrentCardIndex].isBackVisible())
                {
                    flipCardOutAnimation()
                }
                else
                {
                    flipCardInAnimation()
                }
            }
            R.id._prevButtonRect -> showPrevStudyCard()
            R.id._nextButtonRect -> showNextStudyCard()
        }
    }

    /** onClick Listener */
    private val mOnClickListener = View.OnClickListener {v ->
        if(isAutoPlayStopPossible == false)
        {
            return@OnClickListener
        }

        factoryViewModel.onActionStudyCard()
        when(v.id)
        {
            R.id._itemFrontBookmarkButtonRect,
            R.id._itemBackBookmarkButtonRect ->
            {
                val isBookmarkedItem = mDataList[mCurrentCardIndex].isBookmarked()
                mDataList[mCurrentCardIndex].enableBookmark(!isBookmarkedItem)

                if(_ItemViewFlipper.currentView.tag == ITEM_1_TAG)
                {
                    checkBookmark(
                        _Item1FrontBookmarkButton,
                        _Item1BackBookmarkButton,
                        mDataList[mCurrentCardIndex].isBookmarked()
                    )
                }
                else
                {
                    checkBookmark(
                        _Item2FrontBookmarkButton,
                        _Item2BackBookmarkButton,
                        mDataList[mCurrentCardIndex].isBookmarked()
                    )
                }

                factoryViewModel.onClickBookmark(
                    mDataList[mCurrentCardIndex].getID(),
                    mDataList[mCurrentCardIndex].isBookmarked()
                )
            }

            R.id._itemBackMeaningRect,
            R.id._itemFrontWordRect ->
                checkPlaySound()
        }
    }
}