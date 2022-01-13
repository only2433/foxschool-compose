package com.littlefox.app.foxschool.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
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
import com.littlefox.app.foxschool.`object`.data.quiz.QuizPictureData
import com.littlefox.app.foxschool.`object`.data.quiz.QuizTextData
import com.littlefox.app.foxschool.`object`.data.quiz.QuizUserInteractionData
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.DisplayPhoneType
import com.littlefox.app.foxschool.viewmodel.QuizFragmentDataObserver
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

class QuizPlayFragment : Fragment()
{
    @BindView(R.id._baseLayout)
    lateinit var _BaseLayout : RelativeLayout

    @BindView(R.id._questionIndexText)
    lateinit var _QuestionIndexText : TextView

    @BindView(R.id._questionTitleText)
    lateinit var _QuestionTitleText : TextView

    @BindView(R.id._questionImageLayout)
    lateinit var _PictureQuestionTypeLayout : ScalableLayout

    @BindView(R.id._questionTextLayout)
    lateinit var _TextQuestionTypeLayout : ScalableLayout

    @BindView(R.id._questionPlayButton)
    lateinit var _PlaySoundButton : ImageView

    @BindView(R.id._imageIndexFirstImage)
    lateinit var _FirstPictureImage : ImageView

    @BindView(R.id._imageIndexFirstSelectImage)
    lateinit var _FirstSelectImage : ImageView

    @BindView(R.id._imageIndexSecondImage)
    lateinit var _SecondPictureImage : ImageView

    @BindView(R.id._imageIndexSecondSelectImage)
    lateinit var _SecondSelectImage : ImageView

    @BindView(R.id._questionBackgroundLayout)
    lateinit var _QuestionBackgroundLayout : ScalableLayout

    @BindView(R.id._questionNextButtonLayout)
    lateinit var _QuestionNextButtonLayout : ScalableLayout

    @BindView(R.id._questionNextButton)
    lateinit var _NextPlayButton : TextView

    companion object
    {
        // Quiz 의 정답 위치를 나타내는 변수
        private const val QUIZ_CORRECT_PICTURE_LEFT : Int   = 0
        private const val QUIZ_CORRECT_PICTURE_RIGHT : Int  = 1
        private const val TEXT_TAG_CHECK : String           = "check"
        private const val TEXT_TAG_ANSWER : String          = "answer"
    }

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder
    private lateinit var mQuizFragmentDataObserver : QuizFragmentDataObserver

    private lateinit var mQuizPictureData : QuizPictureData
    private lateinit var mQuizTextData : QuizTextData

    private var mCurrentQuestionType : String               = ""
    private var isQuestionEnd : Boolean                     = false

    fun getInstance() : QuizPlayFragment
    {
        return QuizPlayFragment()
    }

    /** ========== LifeCycle ========== */
    override fun onAttach(context : Context)
    {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState : Bundle?)
    {
        Log.i("")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View?
    {
        var view : View


        if(CommonUtils.getInstance(mContext).getPhoneDisplayRadio() != DisplayPhoneType.DEFAULT)
        {
            view = inflater.inflate(R.layout.fragment_quiz_play_20_9_phone, container, false)
        }
        else
        {
           view = inflater.inflate(R.layout.fragment_quiz_play, container, false)
        }

        mUnbinder = ButterKnife.bind(this, view)
        initFont()
        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        initView()
        settingNextPlayLayout()
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

    private fun initFont()
    {
        _QuestionIndexText.typeface = Font.getInstance(mContext).getRobotoBold()
        _QuestionTitleText.typeface = Font.getInstance(mContext).getRobotoMedium()
        _NextPlayButton.typeface = Font.getInstance(mContext).getRobotoBold()
    }

    private fun initView()
    {
        mQuizFragmentDataObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(QuizFragmentDataObserver::class.java)
        when(mCurrentQuestionType)
        {
            // 이미지형 퀴즈
            Common.QUIZ_CODE_PICTURE ->
            {
                val index = "${mQuizPictureData.getQuizIndex()}."
                _QuestionIndexText.text = index
                _QuestionTitleText.text = mQuizPictureData.getTitle()
                _PictureQuestionTypeLayout.visibility = View.VISIBLE
                _TextQuestionTypeLayout.visibility = View.GONE
                setQuestionImage()
            }
            // 단어형 퀴즈
            Common.QUIZ_CODE_TEXT,
            Common.QUIZ_CODE_SOUND_TEXT ->
            {
                val index = "${mQuizTextData.getQuizIndex()}."
                _QuestionIndexText.text = index
                _QuestionTitleText.text = mQuizTextData.getTitle()
                _PictureQuestionTypeLayout.visibility = View.GONE
                _TextQuestionTypeLayout.visibility = View.VISIBLE
                setQuestionText(Common.QUIZ_CODE_TEXT)
            }
            // 문장형 퀴즈
            else ->
            {
                val index = "${mQuizTextData.getQuizIndex()}."
                _QuestionIndexText.text = index
                _QuestionTitleText.text = resources.getString(R.string.message_sound_text_title)
                _PictureQuestionTypeLayout.visibility = View.GONE
                _TextQuestionTypeLayout.visibility = View.VISIBLE
                setQuestionText(Common.QUIZ_CODE_PHONICS_SOUND_TEXT)
            }
        }

        // 사운드 없는 단어형 퀴즈의 경우 스피커 버튼 숨김
        if(mCurrentQuestionType == Common.QUIZ_CODE_TEXT)
        {
            _PlaySoundButton.visibility = View.GONE
        }

        // Next 버튼 비활성화 된 것 처럼 흐리게 표시하기 위함
        _NextPlayButton.isEnabled = false
        _NextPlayButton.alpha = 0.3f
    }
    /** ========== Init ========== */

    /** 다음 퀴즈화면 레이아웃 설정 */
    private fun settingNextPlayLayout()
    {
        val params = _QuestionNextButtonLayout.layoutParams as RelativeLayout.LayoutParams
        params.addRule(RelativeLayout.BELOW, R.id._questionBackgroundLayout)
        params.topMargin = CommonUtils.getInstance(mContext).getPixel(50)
        _QuestionNextButtonLayout.layoutParams = params
    }

    /** 이미지형 퀴즈화면 세팅 */
    private fun setQuestionImage()
    {
        mQuizPictureData.shuffle()
        _FirstPictureImage.setImageBitmap(mQuizPictureData.getImageInformationList()[0]!!.getImage())
        _SecondPictureImage.setImageBitmap(mQuizPictureData.getImageInformationList()[1]!!.getImage())
    }

    /** 단어, 문장형 퀴즈화면 세팅 */
    private fun setQuestionText(type : String)
    {
        Log.f("type : $type")
        val indexResource = intArrayOf(
            R.drawable.icon_index_1,
            R.drawable.icon_index_2,
            R.drawable.icon_index_3,
            R.drawable.icon_index_4
        )

        var exampleMarginTop : Int = 0
        if(mQuizTextData.getExampleList().size == 3)
        {
            exampleMarginTop = 50
        }
        else
        {
            exampleMarginTop = 30
        }

        Log.f("list Size : " + mQuizTextData.getExampleList().size)

        for(i in 0 until mQuizTextData.getExampleList().size)
        {
            // 보기 영역
            val exampleBaseLayout = ScalableLayout(mContext, 1640f, 110f)
            exampleBaseLayout.tag = i

            // 체크박스
            val checkImage = ImageView(mContext).apply {
                tag = TEXT_TAG_CHECK
                setImageResource(R.drawable.icon_check_off)
            }
            exampleBaseLayout.addView(checkImage, 119f, 30f, 50f, 50f)

            // 인덱스
            val indexImage = ImageView(mContext)
            indexImage.setImageResource(indexResource[i])
            exampleBaseLayout.addView(indexImage, 203f, 30f, 50f, 50f)

            // 텍스트
            val examText = TextView(mContext).apply {
                typeface = Font.getInstance(mContext).getRobotoRegular()
                text = mQuizTextData.getExampleList()[i]!!.getExampleText()
                setTextColor(mContext.resources.getColor(R.color.color_444444))
                gravity = Gravity.CENTER_VERTICAL
            }
            exampleBaseLayout.addView(examText, 270f, 0f, 1400f, 110f)
            exampleBaseLayout.setScale_TextSize(examText, 40f)

            // 클릭이벤트
            exampleBaseLayout.setOnClickListener(View.OnClickListener {v ->
                if(isQuestionEnd)
                {
                    return@OnClickListener
                }
                isQuestionEnd = true

                // 전체 보기에 대한 체크박스 반영을 위해 반복문 사용 (선택, 정답)
                for (i in 0 until mQuizTextData.getExampleList().size)
                {
                    val layout = _TextQuestionTypeLayout.getChildAt(i) as ScalableLayout
                    var check = ""
                    if (i == v.tag) check = TEXT_TAG_CHECK
                    else if (mQuizTextData.getExampleList()[i]!!.isAnswer()) check = TEXT_TAG_ANSWER

                    // 체크박스 이미지 변경
                    if((layout as ViewGroup).getChildAt(0).tag == TEXT_TAG_CHECK)
                    {
                        val view = layout.getChildAt(0) as ImageView
                        if (check == TEXT_TAG_CHECK) view.setImageResource(R.drawable.icon_check_on)
                        else if (check == TEXT_TAG_ANSWER) view.setImageResource(R.drawable.icon_check_answer)
                    }
                }

                val selectIndex = v.tag as Int
                Log.f("User TEXT Select Item : $selectIndex")
                if(type == Common.QUIZ_CODE_TEXT || type == Common.QUIZ_CODE_SOUND_TEXT)
                {
                    if(mQuizTextData.getExampleList()[selectIndex]!!.isAnswer())
                    {
                        sendUserSelectTextInformation(true, selectIndex)
                    } else
                    {
                        sendUserSelectTextInformation(false, selectIndex)
                    }
                } else
                {
                    if(mQuizTextData.getExampleList()[selectIndex]!!.isAnswer())
                    {
                        sendUserSelectSoundTextInformation(true, selectIndex)
                    } else
                    {
                        sendUserSelectSoundTextInformation(false, selectIndex)
                    }
                }
                _NextPlayButton.isEnabled = true
                _NextPlayButton.alpha = 1.0f
            })

            _TextQuestionTypeLayout.addView(
                exampleBaseLayout,
                140f,
                (exampleMarginTop + i * 118).toFloat(),
                1640f,
                118f
            )
        }
    }

    /**
     * 선택한 이미지 테두리 표시, 선택안한 이미지 불투명하게 표시
     * @param type
     */
    private fun visibleSelectImage(type : Int)
    {
        if(type == QUIZ_CORRECT_PICTURE_LEFT)
        {
            _FirstSelectImage.visibility = View.VISIBLE
            _SecondPictureImage.alpha = 0.3f
        } else if(type == QUIZ_CORRECT_PICTURE_RIGHT)
        {
            _SecondSelectImage.visibility = View.VISIBLE
            _FirstPictureImage.alpha = 0.3f
        }
    }

    @OnClick(
        R.id._imageIndexFirstImage,
        R.id._imageIndexSecondImage,
        R.id._questionNextButton,
        R.id._questionPlayButton
    )
    fun onSelectImage(view : View)
    {
        when(view.id)
        {
            R.id._imageIndexFirstImage ->
            {
                // 이미지 퀴즈 - 왼쪽 탭
                if(isQuestionEnd)
                {
                    return
                }
                isQuestionEnd = true

                Log.f("User IMAGE Select Item  LEFT ")
                if(mQuizPictureData.getImageInformationList()[QUIZ_CORRECT_PICTURE_LEFT]!!.isAnswer())
                {
                    sendUserSelectPictureInformation(true)
                } else
                {
                    sendUserSelectPictureInformation(false)
                }
                visibleSelectImage(QUIZ_CORRECT_PICTURE_LEFT)
                _NextPlayButton.isEnabled = true
                _NextPlayButton.alpha = 1.0f
            }
            R.id._imageIndexSecondImage ->
            {
                // 이미지 퀴즈 - 오른쪽 탭
                if(isQuestionEnd)
                {
                    return
                }
                isQuestionEnd = true

                Log.f("User IMAGE Select Item  RIGHT ")
                if(mQuizPictureData.getImageInformationList()[QUIZ_CORRECT_PICTURE_RIGHT]!!.isAnswer())
                {
                    sendUserSelectPictureInformation(true)
                } else
                {
                    sendUserSelectPictureInformation(false)
                }
                visibleSelectImage(QUIZ_CORRECT_PICTURE_RIGHT)
                _NextPlayButton.isEnabled = true
                _NextPlayButton.alpha = 1.0f
            }
            R.id._questionNextButton ->
            {
                // Next 버튼 탭
                if(!isQuestionEnd)
                {
                    return
                }
                mQuizFragmentDataObserver.onGoNext()
            }
            R.id._questionPlayButton ->
            {
                // 문제 사운드 재생 버튼 탭
                mQuizFragmentDataObserver.onPlaySound()
            }
        }
    }

    /**
     * 선택한 텍스트 문제 정보를 전달
     * @param isCorrect
     * @param selectIndex
     */
    private fun sendUserSelectSoundTextInformation(isCorrect : Boolean, selectIndex : Int)
    {
        var questionSequence = ""
        for(i in 0 until mQuizTextData.getExampleList().size)
        {
            if(i == mQuizTextData.getExampleList().size - 1)
            {
                questionSequence += mQuizTextData.getExampleList()[i]!!.getExampleIndex().toString()
            }
            else
            {
                questionSequence += mQuizTextData.getExampleList()[i]!!.getExampleIndex().toString() + ","
            }
        }

        mQuizFragmentDataObserver.onChoiceItem(
            QuizUserInteractionData(
                isCorrect,
                questionSequence,
                mQuizTextData.getAnswerDataIndex(),
                mQuizTextData.getExampleList()[selectIndex]!!.getExampleIndex().toString()
            )
        )
    }

    /**
     * 선택한 텍스트 문제 정보를 전달
     * @param isCorrect
     * @param selectIndex
     */
    private fun sendUserSelectTextInformation(isCorrect : Boolean, selectIndex : Int)
    {
        val questionSequence = mQuizTextData.getRecordQuizIndex().toString()
        mQuizFragmentDataObserver.onChoiceItem(
            QuizUserInteractionData(
                isCorrect,
                questionSequence,
                mQuizTextData.getRecordCorrectIndex(),
                mQuizTextData.getExampleList()[selectIndex]!!.getExampleIndex().toString()
            )
        )
    }

    /**
     * 선택한 이미지 문제 정보를 전달
     * @param isCorrect
     */
    private fun sendUserSelectPictureInformation(isCorrect : Boolean)
    {
        var questionSequence = ""
        var incorrectIndexString = ""

        for(i in 0 until mQuizPictureData.getImageInformationList().size)
        {
            if(i == mQuizPictureData.getImageInformationList().size - 1)
            {
                questionSequence += mQuizPictureData.getImageInformationList()[i]!!.getIndex().toString()
            }
            else
            {
                questionSequence += mQuizPictureData.getImageInformationList()[i]!!.getIndex().toString() + ","
            }
        }

        if(isCorrect)
        {
            mQuizFragmentDataObserver.onChoiceItem(
                QuizUserInteractionData(
                    true,
                    questionSequence,
                    mQuizPictureData.getRecordQuizCorrectIndex(),
                    mQuizPictureData.getRecordQuizCorrectIndex().toString()
                )
            )
        }
        else
        {
            if (mQuizPictureData.getRecordQuizCorrectIndex() == mQuizPictureData.getRecordQuizInCorrectIndex())
            {
                incorrectIndexString = mQuizPictureData.getRecordQuizInCorrectIndex().toString() + "r"
            }
            else
            {
                incorrectIndexString = mQuizPictureData.getRecordQuizInCorrectIndex().toString()
            }

            mQuizFragmentDataObserver.onChoiceItem(
                QuizUserInteractionData(
                    false,
                    questionSequence,
                    mQuizPictureData.getRecordQuizCorrectIndex(),
                    incorrectIndexString
                )
            )
        }
    }

    fun setQuestionItemObject(type : String, mObject : Any)
    {
        mCurrentQuestionType = type
        Log.i("mCurrentQuestionType : $mCurrentQuestionType")

        when(mCurrentQuestionType)
        {
            Common.QUIZ_CODE_PICTURE ->
                mQuizPictureData = mObject as QuizPictureData
            Common.QUIZ_CODE_TEXT,
            Common.QUIZ_CODE_PHONICS_SOUND_TEXT,
            Common.QUIZ_CODE_SOUND_TEXT ->
                mQuizTextData = mObject as QuizTextData
        }
    }
}