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
import com.littlefox.app.foxschool.`object`.data.webview.WebviewIntentParamsObject
import com.littlefox.app.foxschool.`object`.result.BookshelfBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.SearchListBaseObject
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.content.DetailItemInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.`object`.result.search.SearchListResult
import com.littlefox.app.foxschool.adapter.DetailListItemAdapter
import com.littlefox.app.foxschool.adapter.listener.DetailItemListener
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.coroutine.BookshelfContentAddCoroutine
import com.littlefox.app.foxschool.coroutine.SearchListCoroutine
import com.littlefox.app.foxschool.dialog.BottomBookAddDialog
import com.littlefox.app.foxschool.dialog.BottomContentItemOptionDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.BookAddListener
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.dialog.listener.ItemOptionListener
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.main.contract.SearchListContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log

class SearchListPresenter : SearchListContract.Presenter
{
    companion object
    {
        private const val MAX_PER_PAGE_COUNT : Int                      = 30

        private const val MESSAGE_SHOW_BOOKSHELF_ADD_ITEM_DIALOG : Int  = 101
        private const val MESSAGE_START_QUIZ : Int                      = 102
        private const val MESSAGE_START_TRANSLATE : Int                 = 103
        private const val MESSAGE_START_EBOOK : Int                     = 104
        private const val MESSAGE_START_VOCABULARY : Int                = 105
        private const val MESSAGE_REQUEST_CONTENTS_ADD : Int            = 106
        private const val MESSAGE_COMPLETE_CONTENTS_ADD : Int           = 107
        private const val MESSAGE_START_GAME_STARWORDS : Int            = 108
        private const val MESSAGE_START_GAME_CROSSWORD : Int            = 109
        private const val MESSAGE_START_FLASHCARD : Int                 = 110
        private const val MESSAGE_START_RECORD_PLAYER : Int             = 111
    }

    private lateinit var mContext : Context
    private lateinit var mSearchListContractView : SearchListContract.View
    private var mCurrentSearchListBaseResult : SearchListResult? = null
    private val mSearchItemList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var mCurrentBookshelfAddResult : MyBookshelfResult? = null

    /**
     * 검색의 타입. ALL = "" , Stories = S , Songs = M
     */
    private var mCurrentSearchType : String = Common.CONTENT_TYPE_ALL
    private var mCurrentKeyword = ""
    private var mRequestPagePosition = 1
    private var mSearchListItemAdapter : DetailListItemAdapter? = null
    private var mSearchListCoroutine : SearchListCoroutine? = null
    private var mBookshelfContentAddCoroutine : BookshelfContentAddCoroutine? = null
    private lateinit var mBottomContentItemOptionDialog : BottomContentItemOptionDialog
    private lateinit var mBottomBookAddDialog : BottomBookAddDialog
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog
    private lateinit var mMainInformationResult : MainInformationResult
    private lateinit var mMainHandler : WeakReferenceHandler
    private val mSendBookshelfAddList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var mCurrentOptionIndex = 0
    private var mCurrentPlayIndex = 0

    constructor(context : Context)
    {
        mContext = context
        mMainHandler = WeakReferenceHandler(context as MessageHandlerCallback)
        mSearchListContractView = (mContext as SearchListContract.View).apply {
            initView()
            initFont()
        }
        Log.f("onCreate")
        init()
    }

    private fun init()
    {
        Log.f("")
        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
    }

    /**
     * 재조회
     */
    override fun requestRefresh()
    {
        Log.f("")
        if(mCurrentSearchListBaseResult!!.isLastPage())
        {
            Log.f("LAST PAGE")
            mSearchListContractView.showErrorMessage(mContext.resources.getString(R.string.message_last_page))
            mSearchListContractView.cancelRefreshView()
        } else
        {
            requestSearchListAsync()
        }
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
        mSearchListCoroutine?.cancel()
        mSearchListCoroutine = null
        mBookshelfContentAddCoroutine?.cancel()
        mBookshelfContentAddCoroutine = null
        mMainHandler.removeCallbacksAndMessages(null)
    }

    override fun activityResult(requestCode : Int, resultCode : Int, data : Intent?) { }

    /** ====================== LifeCycle end ====================== */

    /** ====================== onClick ====================== */

    /**
     * 검색 타입 클릭 이벤트 (전체/동화/동요)
     */
    override fun onClickSearchType(type : String)
    {
        if(mCurrentSearchType == type)
        {
            return
        }
        Log.f("type : $type")
        mCurrentSearchType = type

        if(mCurrentKeyword != "")
        {
            mRequestPagePosition = 1
            mCurrentSearchListBaseResult = null
            mSearchItemList.clear()
            if(mSearchListItemAdapter != null)
            {
                mSearchListItemAdapter!!.notifyDataSetChanged()
            }
            requestSearchListAsync()
        }
    }

    /**
     * 검색 아이콘 클릭 이벤트
     */
    override fun onClickSearchExecute(keyword : String)
    {
        Log.f("keyword : $keyword")
        if(keyword.trim().length < 2)
        {
            mSearchListContractView.showErrorMessage(mContext.resources.getString(R.string.message_warning_search_input_2_or_more))
            return
        }
        clearData()
        mCurrentKeyword = keyword
        requestSearchListAsync()
    }

    /**
     * 메세지 전달 이벤트
     */
    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_SHOW_BOOKSHELF_ADD_ITEM_DIALOG -> showBottomBookAddDialog()
            MESSAGE_START_QUIZ -> startQuizActivity()
            MESSAGE_START_TRANSLATE -> startOriginTranslateActivity()
            MESSAGE_START_VOCABULARY -> startVocabularyActivity()
            MESSAGE_START_EBOOK -> startEbookActivity()
            MESSAGE_REQUEST_CONTENTS_ADD ->
            {
                mSearchListContractView.showLoading()
                requestBookshelfContentsAddAsync(mSendBookshelfAddList)
            }
            MESSAGE_COMPLETE_CONTENTS_ADD ->
            if(msg.arg1 == Activity.RESULT_OK)
            {
                mSearchListContractView.showSuccessMessage((msg.obj as String))
            }
            else
            {
                mSearchListContractView.showErrorMessage((msg.obj as String))
            }
            MESSAGE_START_GAME_STARWORDS -> startGameStarwordsActivity()
            MESSAGE_START_GAME_CROSSWORD -> startGameCrosswordActivity()
            MESSAGE_START_FLASHCARD -> startFlashcardActivity()
            MESSAGE_START_RECORD_PLAYER -> startRecordPlayerActivity()
        }
    }

    /**
     * 데이터 수신 알림 (리스트 세팅)
     */
    private fun notifyData()
    {
        Log.f("")
        mSearchListContractView.hideContentsListLoading()
        mSearchListContractView.cancelRefreshView()
        mSearchItemList.addAll(mCurrentSearchListBaseResult!!.getSearchList())
        initRecyclerView()
    }

    /**
     * 데이터 초기화
     */
    private fun clearData()
    {
        mRequestPagePosition = 1
        mCurrentSearchListBaseResult = null
        mCurrentKeyword = ""
        mSearchItemList.clear()
        mSearchListItemAdapter?.notifyDataSetChanged()
    }

    /**
     * 리스트 세팅
     */
    private fun initRecyclerView()
    {
        if(mSearchListItemAdapter == null)
        {
            // 초기 생성
            Log.f("mSearchListItemAdapter create")
            mSearchListItemAdapter = DetailListItemAdapter(mContext)
                .setData(mSearchItemList)
                .setFullName()
                .setSelectDisable().setBottomViewDisable()
                .setDetailItemListener(mDetailItemListener)
            mSearchListContractView.showSearchListView(mSearchListItemAdapter!!)
        }
        else
        {
            // 데이터 변경
            Log.f("mSearchListItemAdapter notifyDataSetChanged")
            mSearchListItemAdapter!!.setData(mSearchItemList)
            if(mRequestPagePosition == 1)
            {
                mSearchListContractView.showSearchListView(mSearchListItemAdapter!!)
            }
            mSearchListItemAdapter!!.notifyDataSetChanged()
        }
    }

    /**
     * 책장담기 요청
     */
    private fun requestBookshelfContentsAddAsync(data : java.util.ArrayList<ContentsBaseResult>)
    {
        Log.f("")
        mBookshelfContentAddCoroutine = BookshelfContentAddCoroutine(mContext).apply {
            setData(mCurrentBookshelfAddResult!!.getID(), data)
            asyncListener = mAsyncListener
            execute()
        }
    }

    /**
     * 검색 리스트 요청
     */
    private fun requestSearchListAsync()
    {
        /**
         * NULL 이면 처음 검색 했거나 , 아니면 타입이 변경된 경우다. 해당 상황에만 로딩 다이얼로그를 보여준다.
         */
        if(mCurrentSearchListBaseResult != null)
        {
            mRequestPagePosition = mCurrentSearchListBaseResult!!.getCurrentPageIndex() + 1
        }
        else
        {
            mSearchListContractView.showContentsListLoading()
        }

        Log.f("mCurrentKeyword : $mCurrentKeyword")
        Log.f("position : $mRequestPagePosition, searchType : $mCurrentSearchType")

        mSearchListCoroutine = SearchListCoroutine(mContext).apply {
            setData(
                mCurrentKeyword,
                mRequestPagePosition,
                MAX_PER_PAGE_COUNT,
                mCurrentSearchType
            )
            asyncListener = mAsyncListener
            execute()
        }

    }

    /**
     * 컨텐츠 옵션 다이얼로그 표시
     */
    private fun showBottomStoryItemDialog()
    {
        mBottomContentItemOptionDialog = BottomContentItemOptionDialog(mContext, mSearchItemList[mCurrentOptionIndex])
        mBottomContentItemOptionDialog
            .setItemOptionListener(mItemOptionListener)
            .setFullName()
            .setView()
        mBottomContentItemOptionDialog.show()
    }

    /**
     * 책장담기 다이얼로그 표시
     */
    private fun showBottomBookAddDialog()
    {
        mBottomBookAddDialog = BottomBookAddDialog(mContext).apply {
            setCancelable(true)
            setBookshelfData(mMainInformationResult.getBookShelvesList())
            setBookSelectListener(mBookAddListener)
            show()
        }
    }

    /**
     * 마이크 권한 허용 요청 다이얼로그
     * - 녹음기 기능 사용을 위해
     */
    private fun showChangeRecordPermissionDialog()
    {
        mTemplateAlertDialog = TemplateAlertDialog(mContext).apply {
            setMessage(mContext.resources.getString(R.string.message_record_permission))
            setButtonType(DialogButtonType.BUTTON_2)
            setButtonText(mContext.resources.getString(R.string.text_cancel), mContext.resources.getString(R.string.text_change_permission))
            setDialogListener(mPermissionDialogListener)
            show()
        }
    }

    /** ====================== StartActivity ====================== */
    /**
     * 플레이어 화면으로 이동
     */
    private fun startCurrentSelectMovieActivity()
    {
        Log.f("Movie ID : " + mSearchItemList[mCurrentPlayIndex].getID())
        val sendItemList = java.util.ArrayList<ContentsBaseResult>()
        sendItemList.add(mSearchItemList[mCurrentPlayIndex])
        val playerParamsObject = PlayerIntentParamsObject(sendItemList)

        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.PLAYER)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setData(playerParamsObject)
            .startActivity()
    }

    /**
     * 퀴즈 화면으로 이동
     */
    private fun startQuizActivity()
    {
        Log.f("Quiz ID : " + mSearchItemList[mCurrentOptionIndex].getID())
        val quizIntentParamsObject : QuizIntentParamsObject = QuizIntentParamsObject(mSearchItemList[mCurrentOptionIndex].getID())
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.QUIZ)
            .setData(quizIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /**
     * 해석 화면으로 이동
     */
    private fun startOriginTranslateActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_ORIGIN_TRANSLATE)
            .setData(mSearchItemList[mCurrentOptionIndex].getID())
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /**
     * eBook 화면으로 이동
     */
    private fun startEbookActivity()
    {
        Log.f("")
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(mSearchItemList[mCurrentOptionIndex].getID())

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_EBOOK)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /**
     * 단어장 화면으로 이동
     */
    private fun startVocabularyActivity()
    {
        Log.f("")
        val title = CommonUtils.getInstance(mContext).getVocabularyTitleName(mSearchItemList[mCurrentOptionIndex])
        val myVocabularyResult = MyVocabularyResult(
            mSearchItemList[mCurrentOptionIndex].getID(),
            title,
            VocabularyType.VOCABULARY_CONTENTS)

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.VOCABULARY)
            .setData(myVocabularyResult)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /**
     * 스타워즈 게임화면으로 이동
     */
    private fun startGameStarwordsActivity()
    {
        Log.f("")
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(mSearchItemList[mCurrentOptionIndex].getID())

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_STARWORDS)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /**
     * 크로스워드 게임화면으로 이동
     */
    private fun startGameCrosswordActivity()
    {
        Log.f("")
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(mSearchItemList[mCurrentOptionIndex].getID())

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_CROSSWORD)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /**
     * 플래시카드 학습화면으로 이동
     */
    private fun startFlashcardActivity()
    {
        Log.f("")
        val data = FlashcardDataObject(
            mSearchItemList[mCurrentOptionIndex].getID(),
            mSearchItemList[mCurrentOptionIndex].getName(),
            mSearchItemList[mCurrentOptionIndex].getSubName(),
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
        val recordIntentParamsObject = RecordIntentParamsObject(mSearchItemList[mCurrentOptionIndex])

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.RECORD_PLAYER)
            .setData(recordIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /**
     * 책장 업데이트
     */
    private fun updateBookshelfData(result : MyBookshelfResult)
    {
        for(i in mMainInformationResult.getBookShelvesList().indices)
        {
            if(mMainInformationResult.getBookShelvesList()[i].getID() == result.getID())
            {
                Log.f("update Index :$i")
                mMainInformationResult.getBookShelvesList()[i] = result
            }
        }
        CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult)
        MainObserver.updatePage(Common.PAGE_MY_BOOKS)
    }

    /** ====================== set Listener ====================== */

    /**
     * 책장 이벤트 리스너
     */
    private val mBookAddListener : BookAddListener = object : BookAddListener
    {
        override fun onClickBook(index : Int)
        {
            mCurrentBookshelfAddResult = mMainInformationResult.getBookShelvesList()[index]
            Log.f("Add Item : " + mCurrentBookshelfAddResult!!.getName())
            mMainHandler.sendEmptyMessageDelayed(
                MESSAGE_REQUEST_CONTENTS_ADD,
                Common.DURATION_SHORT
            )
        }
    }

    /**
     * 리스트 아이템 이벤트 리스너
     */
    private val mDetailItemListener = object : DetailItemListener
    {
        override fun onItemClickThumbnail(index : Int)
        {
            Log.f("index : $index")
            mCurrentPlayIndex = index
            startCurrentSelectMovieActivity()
        }

        override fun onItemClickOption(index : Int)
        {
            Log.f("index : $index")
            mCurrentOptionIndex = index
            showBottomStoryItemDialog()
        }

        override fun onItemSelectCount(count : Int) { }
    }

    /**
     * 컨텐츠 옵션 다이얼로그 이벤트 리스너
     */
    private val mItemOptionListener : ItemOptionListener = object : ItemOptionListener
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
            mSendBookshelfAddList.add(mSearchItemList.get(mCurrentOptionIndex))
            mBottomContentItemOptionDialog.dismiss()
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
            mSearchListContractView.showErrorMessage(message)
        }
    }

    /**
     * 녹음기 권한요청 다이얼로그 이벤트 리스너
     */
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
                mSearchListContractView.showErrorMessage(mContext.getString(R.string.message_warning_record_permission))
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

    /**
     * 통신 이벤트 리스너
     */
    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String?) { }

        override fun onRunningEnd(code : String?, `object` : Any?)
        {
            val result : BaseResult = `object` as BaseResult

            Log.f("code : " + code + ", status : " + result.getStatus())

            if(result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                if(code == Common.COROUTINE_CODE_SEARCH_LIST)
                {
                    mSearchListContractView.hideContentsListLoading()
                    mCurrentSearchListBaseResult = (`object` as SearchListBaseObject).getData()
                    notifyData()
                }
                else if(code == Common.COROUTINE_CODE_BOOKSHELF_CONTENTS_ADD)
                {
                    mSearchListContractView.hideLoading()
                    val myBookshelfResult : MyBookshelfResult = (`object` as BookshelfBaseObject).getData()
                    updateBookshelfData(myBookshelfResult)

                    val message = Message.obtain().apply {
                        what = MESSAGE_COMPLETE_CONTENTS_ADD
                        obj = mContext.resources.getString(R.string.message_success_save_contents_in_bookshelf)
                        arg1 = Activity.RESULT_OK
                    }
                    mMainHandler.sendMessageDelayed(message, Common.DURATION_NORMAL)
                }
            }
            else
            {
                if(result.isAuthenticationBroken)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initScene()
                }
                else
                {
                    if(code == Common.COROUTINE_CODE_SEARCH_LIST)
                    {
                        mSearchListContractView.hideContentsListLoading()
                        mSearchListContractView.showErrorMessage(result.getMessage())
                    }
                    else if(code == Common.COROUTINE_CODE_BOOKSHELF_CONTENTS_ADD)
                    {
                        Log.f("FAIL ASYNC_CODE_BOOKSHELF_CONTENTS_ADD")
                        mSearchListContractView.hideLoading()
                        val message = Message.obtain().apply {
                            what = MESSAGE_COMPLETE_CONTENTS_ADD
                            obj = result.getMessage()
                            arg1 = Activity.RESULT_CANCELED
                        }
                        mMainHandler.sendMessageDelayed(message, Common.DURATION_SHORT)
                    }
                }
            }
        }

        override fun onRunningCanceled(code : String?) { }

        override fun onRunningProgress(code : String?, progress : Int?) { }

        override fun onRunningAdvanceInformation(code : String?, `object` : Any?) { }

        override fun onErrorListener(code : String?, message : String?) { }
    }
}