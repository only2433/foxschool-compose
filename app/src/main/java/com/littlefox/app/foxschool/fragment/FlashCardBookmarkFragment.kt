package com.littlefox.app.foxschool.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.flashcard.FlashCardDataResult
import com.littlefox.app.foxschool.adapter.FlashcardBookmarkItemAdapter
import com.littlefox.app.foxschool.adapter.listener.BookmarkItemListener
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.view.decoration.GridSpacingItemDecoration
import com.littlefox.app.foxschool.viewmodel.FlashcardBookmarkFragmentObserver
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import java.util.*

/**
 * 플래시카드 북마크 화면
 */
class FlashCardBookmarkFragment : Fragment()
{
    @BindView(R.id._titleImage)
    lateinit var  _TitleImage : ImageView

    @BindView(R.id._bookmarkCountText)
    lateinit var  _BookmarkCountText : TextView

    @BindView(R.id._saveMyBooksButton)
    lateinit var  _SaveMyBooksButton : ImageView

    @BindView(R.id._saveMyBooksText)
    lateinit var  _SaveMyBooksText : TextView

    @BindView(R.id._saveMyBooksIcon)
    lateinit var  _SaveMyBooksIcon : ImageView

    @BindView(R.id._bookmarkItemLayout)
    lateinit var  _BookmarkItemLayout : ScalableLayout

    @BindView(R.id._bookmarkBgImage)
    lateinit var  _BookmarkBgImage : ImageView

    @BindView(R.id._bookmarkItemListView)
    lateinit var  _BookmarkItemListView : RecyclerView

    @BindView(R.id._buttonLayout)
    lateinit var  _ButtonLayout : ScalableLayout

    @BindView(R.id._startWordButton)
    lateinit var  _StartWordButton : ImageView

    @BindView(R.id._startWordMessageText)
    lateinit var  _StartWordMessageText : TextView

    @BindView(R.id._startWordButtonText)
    lateinit var  _StartWordButtonText : TextView

    @BindView(R.id._startMeaningButton)
    lateinit var  _StartMeaningButton : ImageView

    @BindView(R.id._startMeaningMessageText)
    lateinit var  _StartMeaningMessageText : TextView

    @BindView(R.id._startMeaningButtonText)
    lateinit var  _StartMeaningButtonText : TextView

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder
    private var mFlashcardBookmarkFragmentObserver : FlashcardBookmarkFragmentObserver? = null

    private var mDataList : ArrayList<FlashCardDataResult>? = null
    private var mFlashcardBookmarkItemAdapter : FlashcardBookmarkItemAdapter? = null
    private var mVocabularyType : VocabularyType = VocabularyType.VOCABULARY_CONTENTS

    fun getInstance() : FlashCardBookmarkFragment
    {
        return FlashCardBookmarkFragment()
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
        if(Feature.IS_20_9_SUPPORT_RADIO_DISPLAY)
        {
            view = inflater.inflate(R.layout.fragment_flashcard_bookmark_20_9_phone, container, false)
        }
        else
        {
            view = inflater.inflate(R.layout.fragment_flashcard_bookmark, container, false)
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
        if(Feature.IS_4_3_SUPPORT_TABLET_RADIO_DISPLAY)
        {
            _BookmarkItemLayout.setScaleSize(1920f, 810f)
            _BookmarkItemLayout.moveChildView(_BookmarkBgImage, 59f, 0f, 1802f, 810f)
            _BookmarkItemLayout.moveChildView(_BookmarkItemListView, 79f, 20f, 1762f, 790f)
        }

        val viewInformationList = HashMap<Int, String>()
        viewInformationList[R.id._startWordMessageText] = mContext.resources.getString(R.string.text_study_word)
        viewInformationList[R.id._startMeaningMessageText] = mContext.resources.getString(R.string.text_study_meaning)
        CommonUtils.getInstance(mContext).setTextByHtmlType(requireView(), viewInformationList)

        settingButtonText()
        settingSaveMyVocaButton()
        settingBookmarkedCount()
        initRecyclerView()
    }

    private fun initFont()
    {
        _StartWordMessageText.typeface = Font.getInstance(mContext).getRobotoRegular()
        _StartMeaningMessageText.typeface = Font.getInstance(mContext).getRobotoRegular()
        _StartWordButtonText.typeface = Font.getInstance(mContext).getRobotoBold()
        _StartMeaningButtonText.typeface = Font.getInstance(mContext).getRobotoBold()
        _BookmarkCountText.typeface = Font.getInstance(mContext).getRobotoMedium()
        _SaveMyBooksText.typeface = Font.getInstance(mContext).getRobotoMedium()
    }

    private fun initRecyclerView()
    {
        mFlashcardBookmarkItemAdapter = FlashcardBookmarkItemAdapter(mContext, mDataList!!)
        mFlashcardBookmarkItemAdapter!!.setOnBookmarkItemListener(mBookmarkItemListener)
        val gridLayoutManager = GridLayoutManager(mContext, 3)
        gridLayoutManager.spanSizeLookup = object : SpanSizeLookup()
        {
            override fun getSpanSize(position : Int) : Int
            {
                return 1
            }
        }
        _BookmarkItemListView.layoutManager = gridLayoutManager
        _BookmarkItemListView.addItemDecoration(
            GridSpacingItemDecoration(mContext, 3, 0)
        )
        _BookmarkItemListView.adapter = mFlashcardBookmarkItemAdapter
    }
    /** ========== Init ========== */

    /** ViewModel 옵저버 세팅 */
    private fun setupObserverViewModel()
    {
        mFlashcardBookmarkFragmentObserver = ViewModelProviders.of((mContext as AppCompatActivity))[FlashcardBookmarkFragmentObserver::class.java]
    }

    /** 북마크 갯수 화면에 세팅 */
    private fun settingBookmarkedCount()
    {
        _BookmarkCountText.text = getBookmarkedCount().toString()
    }

    /** 디스플레이에 따른 버튼 위치 조정 */
    private fun settingButtonText()
    {
        if(Feature.IS_20_9_SUPPORT_RADIO_DISPLAY)
        {
            _ButtonLayout.moveChildView(_StartWordMessageText, 716f, 95f, 244f, 48f)
            _ButtonLayout.moveChildView(_StartMeaningMessageText, 1232f, 95f, 244f, 48f)
        } else
        {
            _ButtonLayout.moveChildView(_StartWordMessageText, 580f, 50f, 244f, 48f)
            _ButtonLayout.moveChildView(_StartMeaningMessageText, 1096f, 50f, 244f, 48f)
        }
    }

    /**
     * 단어장에 저장하기 버튼 표시/비표시 설정
     *  - 단어장에서 플래시카드를 실행한 경우에는 해당 버튼을 표시하지 않는다.
     */
    private fun settingSaveMyVocaButton()
    {
        when(mVocabularyType)
        {
            VocabularyType.VOCABULARY_SHELF ->
            {
                _SaveMyBooksButton.visibility = View.GONE
                _SaveMyBooksText.visibility = View.GONE
                _SaveMyBooksIcon.visibility = View.GONE
            }
            VocabularyType.VOCABULARY_CONTENTS ->
            {
                _SaveMyBooksButton.visibility = View.VISIBLE
                _SaveMyBooksText.visibility = View.VISIBLE
                _SaveMyBooksIcon.visibility = View.VISIBLE
            }
        }
    }

    /** 리스트에서 북마크 된 항목 수 가져오기 */
    private fun getBookmarkedCount() : Int
    {
        var result = 0
        for(i in mDataList!!.indices)
        {
            if(mDataList!![i].isBookmarked())
            {
                result++
            }
        }
        return result
    }

    /** 데이터 세팅 */
    fun setData(type : VocabularyType, list : ArrayList<FlashCardDataResult>)
    {
        mVocabularyType = type
        mDataList = list
    }

    @OnClick(R.id._saveMyBooksButton, R.id._startWordButton, R.id._startMeaningButton)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._saveMyBooksButton -> mFlashcardBookmarkFragmentObserver?.onClickSaveVocabulary()
            R.id._startWordButton -> mFlashcardBookmarkFragmentObserver?.onClickStartWordStudyData()
            R.id._startMeaningButton -> mFlashcardBookmarkFragmentObserver?.onClickStartMeaningStudyData()
        }
    }

    /** 북마크 Listener */
    private val mBookmarkItemListener : BookmarkItemListener = object : BookmarkItemListener
    {
        override fun onCheckBookmark(position : Int)
        {
            val isBookmarked = mDataList!![position].isBookmarked()
            mDataList!![position].enableBookmark(!isBookmarked)
            settingBookmarkedCount()
            mFlashcardBookmarkFragmentObserver!!.onClickBookmark(
                mDataList!![position].getID(), mDataList!![position].isBookmarked()
            )
            mFlashcardBookmarkItemAdapter!!.notifyDataSetChanged()
        }
    }
}