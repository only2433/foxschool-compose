package com.littlefox.app.foxschool.main.presenter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Message
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.flashcard.FlashcardDataObject
import com.littlefox.app.foxschool.`object`.data.player.PlayerIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.quiz.QuizIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.record.RecordIntentParamsObject
import com.littlefox.app.foxschool.`object`.result.BookshelfBaseObject
import com.littlefox.app.foxschool.`object`.result.DetailItemInformationBaseObject
import com.littlefox.app.foxschool.`object`.result.IntroduceSeriesBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.content.DetailItemInformationResult
import com.littlefox.app.foxschool.`object`.result.introduceSeries.IntroduceSeriesInformationResult
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesBaseResult
import com.littlefox.app.foxschool.adapter.DetailListItemAdapter
import com.littlefox.app.foxschool.adapter.listener.DetailItemListener
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.coroutine.BookshelfContentAddCoroutine
import com.littlefox.app.foxschool.coroutine.IntroduceSeriesCoroutine
import com.littlefox.app.foxschool.coroutine.SeriesContentsListInformationCoroutine
import com.littlefox.app.foxschool.dialog.BottomBookAddDialog
import com.littlefox.app.foxschool.dialog.BottomContentItemOptionDialog
import com.littlefox.app.foxschool.dialog.IntroduceSeriesTabletDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.BookAddListener
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.dialog.listener.ItemOptionListener
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.main.contract.SeriesContentsListContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log
import java.util.*


class SeriesContentsListPresenter : SeriesContentsListContract.Presenter
{
    companion object
    {
        private const val MESSAGE_REQUEST_CONTENTS_DETAIL_LIST : Int    = 100
        private const val MESSAGE_REQUEST_CONTENTS_ADD : Int            = 101
        private const val MESSAGE_COMPLETE_CONTENTS_ADD : Int           = 102
        private const val MESSAGE_SET_STORY_DETAIL_LIST : Int           = 103
        private const val MESSAGE_SHOW_BOOKSHELF_ADD_ITEM_DIALOG : Int  = 104
        private const val MESSAGE_START_QUIZ : Int                      = 105
        private const val MESSAGE_START_TRANSLATE : Int                 = 106
        private const val MESSAGE_START_EBOOK : Int                     = 107
        private const val MESSAGE_START_VOCABULARY : Int                = 108
        private const val MESSAGE_START_GAME_STARWORDS : Int            = 109
        private const val MESSAGE_START_GAME_CROSSWORD : Int            = 110
        private const val MESSAGE_START_FLASHCARD : Int                 = 111
        private const val MESSAGE_START_RECORD_PLAYER : Int             = 112
    }

    private lateinit var mContext : Context
    private lateinit var mStoryDetailListContractView : SeriesContentsListContract.View
    private lateinit var mMainHandler : WeakReferenceHandler
    private lateinit var mMainInformationResult : MainInformationResult
    private lateinit var mDetailItemInformationResult : DetailItemInformationResult
    private lateinit var mStoryDetailItemAdapter : DetailListItemAdapter
    private lateinit var mBottomContentItemOptionDialog : BottomContentItemOptionDialog
    private lateinit var mBottomBookAddDialog : BottomBookAddDialog
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog
    private var mCurrentSeriesBaseResult : SeriesBaseResult
    private var mSeriesContentsListInformationCoroutine : SeriesContentsListInformationCoroutine? = null
    private var mIntroduceSeriesCoroutine : IntroduceSeriesCoroutine? = null
    private var mBookshelfContentAddCoroutine : BookshelfContentAddCoroutine? = null
    private var mCurrentBookshelfAddResult : MyBookshelfResult? = null
    private var mIntroduceSeriesInformationResult : IntroduceSeriesInformationResult? = null
    private var mIntroduceSeriesTabletDialog : IntroduceSeriesTabletDialog? = null
    private var mSendBookshelfAddList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var mCurrentContentsItemList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var mCurrentPlayIndex : Int     = 0
    private var mCurrentOptionIndex : Int   = 0
    private var isStillOnSeries : Boolean   = false

    constructor(context : Context)
    {
        Log.f("onCreate")
        mContext = context
        mCurrentSeriesBaseResult = (mContext as AppCompatActivity).getIntent().getParcelableExtra(Common.INTENT_STORY_SERIES_DATA)!!
        mMainHandler = WeakReferenceHandler(mContext as MessageHandlerCallback)
        mStoryDetailListContractView = mContext as SeriesContentsListContract.View
        mStoryDetailListContractView.initView()
        mStoryDetailListContractView.initFont()
        mStoryDetailListContractView.initTransition(mCurrentSeriesBaseResult.getTransitionType())
        mStoryDetailListContractView.setStatusBar(mCurrentSeriesBaseResult.statusBarColor)
        if(CommonUtils.getInstance(mContext).checkTablet)
        {
            mStoryDetailListContractView.settingTitleViewTablet(mCurrentSeriesBaseResult.getSeriesName())
            mStoryDetailListContractView.settingBackgroundViewTablet(
                mCurrentSeriesBaseResult.getThumbnailUrl(),
                mCurrentSeriesBaseResult.titleColor,
                mCurrentSeriesBaseResult.getTransitionType()
            )
        } else
        {
            mStoryDetailListContractView.settingTitleView(mCurrentSeriesBaseResult.getSeriesName())
            mStoryDetailListContractView.settingBackgroundView(
                mCurrentSeriesBaseResult.getThumbnailUrl(),
                mCurrentSeriesBaseResult.titleColor
            )
        }
        mStoryDetailListContractView.showContentListLoading()
        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_REQUEST_CONTENTS_DETAIL_LIST, Common.DURATION_LONG)
    }

    override fun resume()
    {
        Log.f("")
    }

    override fun pause()
    {
        Log.f("")
    }

    override fun destroy()
    {
        Log.f("")
        mSeriesContentsListInformationCoroutine?.cancel()
        mSeriesContentsListInformationCoroutine = null
        mIntroduceSeriesCoroutine?.cancel()
        mIntroduceSeriesCoroutine = null
        mBookshelfContentAddCoroutine?.cancel()
        mBookshelfContentAddCoroutine = null
        mMainHandler.removeCallbacksAndMessages(null)
        releaseDialog()
    }

    override fun activityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        Log.f("requestCode : $requestCode, resultCode : $resultCode")
    }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_REQUEST_CONTENTS_DETAIL_LIST -> requestContentsDetailInformationAsync()
            MESSAGE_REQUEST_CONTENTS_ADD ->
            {
                mStoryDetailListContractView.showLoading()
                requestBookshelfContentsAddAsync(mSendBookshelfAddList)
            }
            MESSAGE_COMPLETE_CONTENTS_ADD ->
            if(msg.arg1 == Activity.RESULT_OK)
            {
                mStoryDetailListContractView.showSuccessMessage(msg.obj as String)
            } else
            {
                mStoryDetailListContractView.showErrorMessage(msg.obj as String)
            }
            MESSAGE_SHOW_BOOKSHELF_ADD_ITEM_DIALOG -> showBottomBookAddDialog()
            MESSAGE_SET_STORY_DETAIL_LIST ->
            {
                if(mDetailItemInformationResult.seriesID.equals("") === false)
                {
                    mStoryDetailListContractView.showSeriesInformationView()
                }
                if(CommonUtils.getInstance(mContext).checkTablet)
                {
                    mStoryDetailListContractView.showSeriesInformationIntroduceTablet(
                        mCurrentSeriesBaseResult.introduction
                    )
                    mStoryDetailListContractView.showSeriesDataViewTablet(
                        mCurrentSeriesBaseResult.getSeriesType(),
                        mDetailItemInformationResult.seriesLevel,
                        mDetailItemInformationResult.getContentsList().size,
                        mCurrentSeriesBaseResult.categoryData,
                        mDetailItemInformationResult.isSingleSeries,
                        mDetailItemInformationResult.seriesARLevel
                    )
                } else
                {
                    mStoryDetailListContractView.showSeriesDataView(
                        mCurrentSeriesBaseResult.getSeriesType(),
                        mDetailItemInformationResult.seriesLevel,
                        mDetailItemInformationResult.getContentsList().size,
                        mDetailItemInformationResult.isSingleSeries,
                        mDetailItemInformationResult.seriesARLevel
                    )
                }
                if(Feature.IS_FREE_USER === false)
                {
                    if(mDetailItemInformationResult.seriesID.equals("") === false)
                    {
                        if(mDetailItemInformationResult.isSingleSeries === false && mDetailItemInformationResult.isStillOnSeries)
                        {
                            isStillOnSeries = true
                        }
                    }
                }
                initContentItemList()
            }
            MESSAGE_START_QUIZ -> startQuizAcitiviy()
            MESSAGE_START_TRANSLATE -> startOriginTranslateActivity()
            MESSAGE_START_EBOOK -> startEbookActivity()
            MESSAGE_START_VOCABULARY -> startVocabularyActivity()
            MESSAGE_START_GAME_STARWORDS -> startGameStarwordsActivity()
            MESSAGE_START_GAME_CROSSWORD -> startGameCrosswordActivity()
            MESSAGE_START_FLASHCARD -> startFlashcardActivity()
            MESSAGE_START_RECORD_PLAYER -> startRecordPlayerActivity()
        }
    }

    override fun onClickSeriesInformation()
    {
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.INTRODUCE_SERIES)
            .setData(mDetailItemInformationResult.seriesID)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    override fun onClickNextMovieAfterLastMovie(playNumber : Int)
    {
        Log.f("playNumber : " + playNumber + ", isStillOnSeries : " + mDetailItemInformationResult.isStillOnSeries)
        var playPosition = 0
        if(mDetailItemInformationResult.isStillOnSeries)
        {
            playPosition = mDetailItemInformationResult.getContentsList().size - (playNumber + 1)
        } else
        {
            playPosition = playNumber
        }
        startCurrentSelectMovieActivity(playPosition)
    }

    override fun onClickSelectAll()
    {
        Log.f("")
        mStoryDetailItemAdapter.setSelectedAllData()
    }

    override fun onClickSelectPlay()
    {
        Log.f("")

        if(mStoryDetailItemAdapter.getSelectedList().size > 0)
        {
            val sendItemList : ArrayList<ContentsBaseResult> = mStoryDetailItemAdapter.getSelectedList()
            val playerIntentParamsObject = PlayerIntentParamsObject(sendItemList)
            if(isStillOnSeries)
            {
                Log.f("onClickSelectPlay List isStillOnSeries : " + mDetailItemInformationResult.seriesID)
                Collections.reverse(sendItemList)
            }
            IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.PLAYER)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .setData(playerIntentParamsObject)
                .startActivity()
        }
        else
        {
            mStoryDetailListContractView.showErrorMessage(mContext.resources.getString(R.string.message_not_seleted_contents_list))
        }
    }

    override fun onClickAddBookshelf()
    {
        Log.f("add list size : " + mStoryDetailItemAdapter.getSelectedList().size)

        if(mStoryDetailItemAdapter.getSelectedList().size > 0)
        {
            mSendBookshelfAddList.clear()
            mSendBookshelfAddList = mStoryDetailItemAdapter.getSelectedList()
            if(isStillOnSeries)
            {
                Log.f("Add List isStillOnSeries : " + mDetailItemInformationResult.seriesID)
                Collections.reverse(mSendBookshelfAddList)
            }
            showBottomBookAddDialog()
        } else
        {
            mStoryDetailListContractView.showErrorMessage(mContext!!.resources.getString(R.string.message_not_add_selected_contents_bookshelf))
        }
    }

    override fun onClickCancel()
    {
        Log.f("")
        mStoryDetailItemAdapter.initSelectedData()
    }

    private fun requestContentsDetailInformationAsync()
    {
        Log.f("seriesType : "+mCurrentSeriesBaseResult.getSeriesType()+", displayID : "+ mCurrentSeriesBaseResult.getDisplayID())
        mSeriesContentsListInformationCoroutine = SeriesContentsListInformationCoroutine(mContext)
        mSeriesContentsListInformationCoroutine!!.setData(mCurrentSeriesBaseResult.getSeriesType(), mCurrentSeriesBaseResult.getDisplayID())
        mSeriesContentsListInformationCoroutine!!.asyncListener = mAsyncListener
        mSeriesContentsListInformationCoroutine!!.execute()
    }

    private fun requestIntroduceSeriesAsync()
    {
        Log.f("")
        mIntroduceSeriesCoroutine = IntroduceSeriesCoroutine(mContext)
        mIntroduceSeriesCoroutine!!.setData(mDetailItemInformationResult.seriesID)
        mIntroduceSeriesCoroutine!!.asyncListener = mAsyncListener
        mIntroduceSeriesCoroutine!!.execute()
    }

    private fun requestBookshelfContentsAddAsync(data : ArrayList<ContentsBaseResult>)
    {
        Log.f("")
        mBookshelfContentAddCoroutine = BookshelfContentAddCoroutine(mContext)
        mBookshelfContentAddCoroutine!!.setData(mCurrentBookshelfAddResult!!.getID(), data)
        mBookshelfContentAddCoroutine!!.asyncListener = mAsyncListener
        mBookshelfContentAddCoroutine!!.execute()
    }

    private fun initContentItemList()
    {
        mCurrentContentsItemList.clear()
        mCurrentContentsItemList = mDetailItemInformationResult.getContentsList()
        for(i in mCurrentContentsItemList.indices)
        {
            val index = i + 1
            mCurrentContentsItemList[i].setIndex(index)
        }

        if(isStillOnSeries)
        {
            Log.f("Is Still on Series ID : " + mDetailItemInformationResult.seriesID)
            Collections.reverse(mCurrentContentsItemList)
        }

        mStoryDetailItemAdapter = DetailListItemAdapter(mContext, seriesColor, mDetailItemInformationResult.getContentsList())
        mStoryDetailItemAdapter.setDetailItemListener(mDetailItemListener)
        mStoryDetailListContractView.showStoryDetailListView(mStoryDetailItemAdapter)
        if(Feature.IS_FREE_USER === false)
        {
            if(mDetailItemInformationResult.lastStudyContentID.equals("") === false)
            {
                val mLoginInformationResult : LoginInformationResult =
                    CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult
                val resultIndex = lastStudyMovieIndex
                mStoryDetailListContractView.showLastWatchSeriesInformation(
                    mCurrentSeriesBaseResult.getSeriesName(),
                    mLoginInformationResult.getUserInformation().getName(),
                    resultIndex,
                    if(resultIndex == mDetailItemInformationResult.getContentsList().size) true else false
                )
            }
        }
    }

    private val lastStudyMovieIndex : Int
        private get()
        {
            var result = 0
            for(i in 0 until mDetailItemInformationResult.getContentsList().size)
            {
                if(mDetailItemInformationResult.lastStudyContentID.equals(mDetailItemInformationResult.getContentsList().get(i).getID()))
                {
                    if(mDetailItemInformationResult.isStillOnSeries)
                    {
                        result = mDetailItemInformationResult.getContentsList().size - i
                    }
                    else
                    {
                        result = i + 1
                    }
                }
            }
            return result
        }

    private fun startCurrentSelectMovieActivity(index : Int)
    {
        Log.f("index : $index")
        mCurrentPlayIndex = index

        val sendItemList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
        sendItemList.add(mDetailItemInformationResult.getContentsList().get(mCurrentPlayIndex))
        val playerIntentParamsObject = PlayerIntentParamsObject(sendItemList)
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.PLAYER)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setData(playerIntentParamsObject)
            .startActivity();
    }

    private fun startQuizAcitiviy()
    {
        Log.f("")
        var quizIntentParamsObject : QuizIntentParamsObject = QuizIntentParamsObject(mDetailItemInformationResult.getContentsList().get(mCurrentOptionIndex).getID())
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.QUIZ)
            .setData(quizIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startOriginTranslateActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_ORIGIN_TRANSLATE)
            .setData(mDetailItemInformationResult.getContentsList().get(mCurrentOptionIndex).getID())
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startEbookActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_EBOOK)
            .setData(mDetailItemInformationResult.getContentsList().get(mCurrentOptionIndex).getID())
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startGameStarwordsActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_STARWORDS)
            .setData(mDetailItemInformationResult.getContentsList().get(mCurrentOptionIndex).getID())
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startGameCrosswordActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_CROSSWORD)
            .setData(mDetailItemInformationResult.getContentsList().get(mCurrentOptionIndex).getID())
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startFlashcardActivity()
    {
        Log.f("")
        val data = FlashcardDataObject(
            mDetailItemInformationResult.getContentsList()[mCurrentOptionIndex].getID(),
            mDetailItemInformationResult.getContentsList()[mCurrentOptionIndex].getName(),
            mDetailItemInformationResult.getContentsList()[mCurrentOptionIndex].getSubName(),
            VocabularyType.VOCABULARY_CONTENTS
        )

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.FLASHCARD)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /**
     * 녹음기 화면으로 이동
     */
    private fun startRecordPlayerActivity()
    {
        Log.f("")
        val recordIntentParamsObject = RecordIntentParamsObject(mDetailItemInformationResult.getContentsList().get(mCurrentOptionIndex))

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.RECORD_PLAYER)
            .setData(recordIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startVocabularyActivity()
    {
        Log.f("")
        var title = ""
        title = CommonUtils.getInstance(mContext).getVocabularyTitleName(mDetailItemInformationResult.getContentsList().get(mCurrentOptionIndex))

        val myVocabularyResult = MyVocabularyResult(
            mDetailItemInformationResult.getContentsList().get(mCurrentOptionIndex).getID(),
            title,
            VocabularyType.VOCABULARY_CONTENTS
        )

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.VOCABULARY)
            .setData(myVocabularyResult)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun showBottomStoryItemDialog()
    {
        Log.f("getThumbnailUrl() : " + mDetailItemInformationResult.getContentsList().get(mCurrentOptionIndex).getThumbnailUrl())
        Log.f("mCurrentOptionIndex() : $mCurrentOptionIndex")
        mBottomContentItemOptionDialog = BottomContentItemOptionDialog(mContext, mDetailItemInformationResult.getContentsList().get(mCurrentOptionIndex))
        mBottomContentItemOptionDialog
            .setPosition(mDetailItemInformationResult.getContentsList().get(mCurrentOptionIndex).getIndex())
            .setIndexColor(seriesColor)
            .setItemOptionListener(mStoryDetailOptionListener)
            .setView()
        mBottomContentItemOptionDialog.show()
    }

    private fun showBottomBookAddDialog()
    {
        mBottomBookAddDialog = BottomBookAddDialog(mContext)
        mBottomBookAddDialog.setCancelable(true)
        mBottomBookAddDialog.setBookshelfData(mMainInformationResult.getBookShelvesList())
        mBottomBookAddDialog.setBookSelectListener(mBookAddListener)
        mBottomBookAddDialog.show()
    }

    /**
     * 마이크 권한 허용 요청 다이얼로그
     * - 녹음기 기능 사용을 위해
     */
    private fun showChangeRecordPermissionDialog()
    {
        mTemplateAlertDialog = TemplateAlertDialog(mContext)
        mTemplateAlertDialog.setMessage(mContext.resources.getString(R.string.message_record_permission))
        mTemplateAlertDialog.setButtonType(DialogButtonType.BUTTON_2)
        mTemplateAlertDialog.setButtonText(mContext.resources.getString(R.string.text_cancel), mContext.resources.getString(R.string.text_change_permission))
        mTemplateAlertDialog.setDialogListener(mPermissionDialogListener)
        mTemplateAlertDialog.show()
    }

    private fun releaseDialog()
    {
        mIntroduceSeriesTabletDialog?.dismiss()
        mIntroduceSeriesTabletDialog = null
    }

    private val seriesColor : String
        private get()
        {
            var result = ""
            Log.f("Type : " + mCurrentSeriesBaseResult.getSeriesType())
            Log.f("single : " + mDetailItemInformationResult.isSingleSeries)
            if(mCurrentSeriesBaseResult.getSeriesType().equals(Common.CONTENT_TYPE_SONG) === false
                && mDetailItemInformationResult.isSingleSeries === false)
            {
                result = mCurrentSeriesBaseResult.statusBarColor
            }
            return result
        }

    /**
     * 컨텐츠의 책장 리스트에서 나의단어장으로 컨텐츠를 추가해서 갱신할때 사용하는 메소드 ( 추가됨으로써 서버쪽의 해당 책장의 정보를 갱신하기 위해 사용 )
     * 예) 책장 ID , 컨텐츠의 개수, 책장 컬러 등등
     * @param result 서버쪽에서 받은 결과 책장 정보
     */
    private fun updateBookshelfData(result : MyBookshelfResult)
    {
        for(i in 0 until mMainInformationResult.getBookShelvesList().size)
        {
            if(mMainInformationResult.getBookShelvesList().get(i).getID().equals(result.getID()))
            {
                Log.f("update Index :$i")
                mMainInformationResult.getBookShelvesList().set(i, result)
            }
        }
        CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult)
        MainObserver.updatePage(Common.PAGE_MY_BOOKS)
    }

    private fun showIntroduceSeriesDialog()
    {
        Log.f("")
        mIntroduceSeriesTabletDialog = IntroduceSeriesTabletDialog(mContext, mIntroduceSeriesInformationResult!!)
        mIntroduceSeriesTabletDialog!!.show()
    }

    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String) {}

        override fun onRunningEnd(code : String, `object` : Any)
        {
            val result : BaseResult = `object` as BaseResult
            Log.f("code : " + code + ", status : " + result.getStatus())
            if(result.getStatus() === BaseResult.SUCCESS_CODE_OK)
            {
                if(code === Common.COROUTINE_SERIES_CONTENTS_LIST_INFO)
                {
                    mStoryDetailListContractView.hideContentListLoading()
                    mDetailItemInformationResult = (result as DetailItemInformationBaseObject).getData()
                    mMainHandler.sendEmptyMessage(MESSAGE_SET_STORY_DETAIL_LIST)
                }
                else if(code == Common.COROUTINE_CODE_BOOKSHELF_CONTENTS_ADD)
                {
                    mStoryDetailListContractView.hideLoading()
                    val myBookshelfResult : MyBookshelfResult = (`object` as BookshelfBaseObject).getData()
                    updateBookshelfData(myBookshelfResult)

                    mStoryDetailItemAdapter.initSelectedData()
                    mStoryDetailListContractView.hideFloatingToolbarLayout()
                    val messsage = Message.obtain()
                    messsage.what = MESSAGE_COMPLETE_CONTENTS_ADD
                    messsage.obj = mContext.resources.getString(R.string.message_success_save_contents_in_bookshelf)
                    messsage.arg1 = Activity.RESULT_OK
                    mMainHandler.sendMessageDelayed(messsage, Common.DURATION_NORMAL)
                }
                else if(code == Common.COROUTINE_CODE_INTRODUCE_SERIES)
                {
                    mStoryDetailListContractView.hideLoading()
                    mIntroduceSeriesInformationResult = (result as IntroduceSeriesBaseObject).getData()
                    showIntroduceSeriesDialog()
                }
            } else
            {
                if(result.isDuplicateLogin)
                { //중복 로그인 시 재시작
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initAutoIntroSequence()
                } else if(result.isAuthenticationBroken)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initScene()
                } else
                {
                    if(code == Common.COROUTINE_SERIES_CONTENTS_LIST_INFO)
                    {
                        Log.f("FAIL ASYNC_CODE_SERIES_CONTENTS_LIST_INFO")
                        mStoryDetailListContractView.hideContentListLoading()
                        Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                        (mContext as AppCompatActivity).onBackPressed()
                    }
                    else if(code == Common.COROUTINE_CODE_BOOKSHELF_CONTENTS_ADD)
                    {
                        Log.f("FAIL ASYNC_CODE_BOOKSHELF_CONTENTS_ADD")
                        mStoryDetailListContractView.hideLoading()
                        val messsage = Message.obtain()
                        messsage.what = MESSAGE_COMPLETE_CONTENTS_ADD
                        messsage.obj = result.getMessage()
                        messsage.arg1 = Activity.RESULT_CANCELED
                        mMainHandler.sendMessageDelayed(messsage, Common.DURATION_SHORT)
                    }
                }
            }
        }

        override fun onRunningCanceled(code : String) {}

        override fun onRunningProgress(code : String, progress : Int) {}

        override fun onRunningAdvanceInformation(code : String, `object` : Any) {}

        override fun onErrorListener(code : String, message : String) {}
    }

    private val mDetailItemListener : DetailItemListener = object : DetailItemListener
    {
        override fun onItemClickThumbnail(index : Int)
        {
            Log.f("index : $index")
            startCurrentSelectMovieActivity(index)
        }

        override fun onItemClickOption(index : Int)
        {
            Log.f("index : $index")
            mCurrentOptionIndex = index
            showBottomStoryItemDialog()
        }

        override fun onItemSelectCount(count : Int)
        {
            Log.f("count : $count")
            if(Feature.IS_FREE_USER === false)
            {
                if(count == 0)
                {
                    mStoryDetailListContractView.hideFloatingToolbarLayout()
                }
                else
                {
                    if(count == 1)
                    {
                        mStoryDetailListContractView.showFloatingToolbarLayout()
                    }
                    mStoryDetailListContractView.setFloatingToolbarPlayCount(count)
                }
            }
        }
    }
    private val mStoryDetailOptionListener : ItemOptionListener = object : ItemOptionListener
    {
        override fun onClickQuiz()
        {
            Log.f("")
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_QUIZ, Common.DURATION_SHORT)
        }

        override fun onClickTranslate()
        {
            Log.f("")
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_TRANSLATE, Common.DURATION_SHORT)
        }

        override fun onClickVocabulary()
        {
            Log.f("")
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_VOCABULARY, Common.DURATION_SHORT)
        }

        override fun onClickBookshelf()
        {
            Log.f("")
            mSendBookshelfAddList.clear()
            mSendBookshelfAddList.add(mDetailItemInformationResult.getContentsList().get(mCurrentOptionIndex))
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_BOOKSHELF_ADD_ITEM_DIALOG, Common.DURATION_SHORT)
        }

        override fun onClickEbook()
        {
            Log.f("")
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_EBOOK, Common.DURATION_SHORT)
        }

        override fun onClickGameStarwords()
        {
            Log.f("")
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_GAME_STARWORDS, Common.DURATION_SHORT)
        }

        override fun onClickGameCrossword()
        {
            Log.f("")
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_GAME_CROSSWORD, Common.DURATION_SHORT)
        }

        override fun onClickFlashCard()
        {
            Log.f("")
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_FLASHCARD, Common.DURATION_SHORT)
        }

        override fun onClickRecordPlayer()
        {
            Log.f("")
            if (CommonUtils.getInstance(mContext).checkRecordPermission() == false)
            {
                showChangeRecordPermissionDialog()
            }
            else
            {
                mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_RECORD_PLAYER, Common.DURATION_SHORT)
            }
        }

        override fun onErrorMessage(message : String)
        {
            Log.f("message : $message")
            mStoryDetailListContractView.showErrorMessage(message)
        }
    }

    private val mBookAddListener : BookAddListener = object : BookAddListener
    {
        override fun onClickBook(index : Int)
        {
            Log.f("index : $index")
            mCurrentBookshelfAddResult = mMainInformationResult.getBookShelvesList().get(index)
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_REQUEST_CONTENTS_ADD, Common.DURATION_SHORT)
        }
    }

    private val mPermissionDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(messageType : Int)
        {
        }

        override fun onChoiceButtonClick(buttonType : DialogButtonType, messageType : Int)
        {
            Log.f("messageType : $messageType, buttonType : $buttonType")
            if(buttonType == DialogButtonType.BUTTON_1)
            {
                // [취소] 컨텐츠 사용 불가 메세지 표시
                mStoryDetailListContractView.showErrorMessage(mContext.getString(R.string.message_warning_record_permission))
            }
            else if(buttonType == DialogButtonType.BUTTON_2)
            {
                // [권한 변경하기] 앱 정보 화면으로 이동
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", mContext.packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                mContext.startActivity(intent)
            }
        }
    }
}