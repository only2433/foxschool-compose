package com.littlefox.app.foxschool.main.presenter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.BookshelfBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.base.SearchListBaseObject
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.content.DetailItemInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.adapter.DetailListItemAdapter
import com.littlefox.app.foxschool.adapter.listener.DetailItemListener
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.coroutine.BookshelfContentAddCoroutine
import com.littlefox.app.foxschool.coroutine.SearchListCoroutine
import com.littlefox.app.foxschool.dialog.BottomBookAddDialog
import com.littlefox.app.foxschool.dialog.BottomContentItemOptionDialog
import com.littlefox.app.foxschool.dialog.listener.BookAddListener
import com.littlefox.app.foxschool.dialog.listener.ItemOptionListener
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
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
    }

    private lateinit var mContext : Context
    private lateinit var mSearchListContractView : SearchListContract.View
    private var mCurrentSearchListBaseObject : SearchListBaseObject? = null
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
    private lateinit var mDetailItemInformationResult : DetailItemInformationResult
    private lateinit var mBottomBookAddDialog : BottomBookAddDialog
    private lateinit var mMainInformationResult : MainInformationResult
    private lateinit var mMainHandler : WeakReferenceHandler
    private val mSendBookshelfAddList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var mCurrentOptionIndex = 0
    private var mCurrentPlayIndex = 0

    constructor(context : Context)
    {
        mContext = context
        mMainHandler = WeakReferenceHandler(context as MessageHandlerCallback)
        mSearchListContractView = mContext as SearchListContract.View
        mSearchListContractView.initFont()
        mSearchListContractView.initView()

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
        if(mCurrentSearchListBaseObject!!.isLastPage())
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

    override fun acvitityResult(requestCode : Int, resultCode : Int, data : Intent?) { }

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
            mCurrentSearchListBaseObject = null
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
        mSearchItemList.addAll(mCurrentSearchListBaseObject!!.getSearchList())
        initRecyclerView()
    }

    /**
     * 데이터 초기화
     */
    private fun clearData()
    {
        mRequestPagePosition = 1
        mCurrentSearchListBaseObject = null
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
            Log.f("mSearchListItemAdapter == null")
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
        mBookshelfContentAddCoroutine = BookshelfContentAddCoroutine(mContext)
        mBookshelfContentAddCoroutine!!.setData(mCurrentBookshelfAddResult!!.getID(), data)
        mBookshelfContentAddCoroutine!!.asyncListener = mAsyncListener
        mBookshelfContentAddCoroutine!!.execute()
    }

    /**
     * 검색 리스트 요청
     */
    private fun requestSearchListAsync()
    {
        /**
         * NULL 이면 처음 검색 했거나 , 아니면 타입이 변경된 경우다. 해당 상황에만 로딩 다이얼로그를 보여준다.
         */
        if(mCurrentSearchListBaseObject != null)
        {
            mRequestPagePosition = mCurrentSearchListBaseObject!!.getCurrentPageIndex() + 1
        }
        else
        {
            mSearchListContractView.showContentsListLoading()
        }

        Log.f("mCurrentKeyword : $mCurrentKeyword")
        Log.f("position : $mRequestPagePosition, searchType : $mCurrentSearchType")

        mSearchListCoroutine = SearchListCoroutine(mContext)
        mSearchListCoroutine?.setData(
            mCurrentKeyword,
            mRequestPagePosition,
            MAX_PER_PAGE_COUNT,
            mCurrentSearchType
        )
        mSearchListCoroutine?.asyncListener = mAsyncListener
        mSearchListCoroutine?.execute()
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
        mBottomBookAddDialog = BottomBookAddDialog(mContext)
        mBottomBookAddDialog.setCancelable(true)
        mBottomBookAddDialog.setBookshelfData(mMainInformationResult.getBookShelvesList())
        mBottomBookAddDialog.setBookSelectListener(mBookAddListener)
        mBottomBookAddDialog.show()
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
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.PLAYER)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setData(sendItemList)
            .startActivity()
    }

    /**
     * 퀴즈 화면으로 이동
     */
    private fun startQuizActivity()
    {
        Log.f("Quiz ID : " + mSearchItemList[mCurrentOptionIndex].getID())
        // TODO : QUIZ 화면작업 끝난 후 풀어주기
//        IntentManagementFactory.getInstance()
//            .readyActivityMode(ActivityMode.QUIZ)
//            .setData(mSearchItemList[mCurrentOptionIndex].getID())
//            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
//            .startActivity()
    }

    /**
     * 해석 화면으로 이동
     */
    private fun startOriginTranslateActivity()
    {
        Log.f("")
        // TODO : WEBVIEW_ORIGIN_TRANSLATE 화면작업 끝난 후 풀어주기
//        IntentManagementFactory.getInstance()
//            .readyActivityMode(ActivityMode.WEBVIEW_ORIGIN_TRANSLATE)
//            .setData(mSearchItemList[mCurrentOptionIndex].getID())
//            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
//            .startActivity()
    }

    /**
     * eBook 화면으로 이동
     */
    private fun startEbookActivity()
    {
        Log.f("")
        // TODO : WEBVIEW_EBOOK 화면작업 끝난 후 풀어주기
//        IntentManagementFactory.getInstance()
//            .readyActivityMode(ActivityMode.WEBVIEW_EBOOK)
//            .setData(mSearchItemList[mCurrentOptionIndex].getID())
//            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
//            .startActivity()
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

        // TODO : VOCABULARY 화면작업 끝난 후 풀어주기
//        IntentManagementFactory.getInstance()
//            .readyActivityMode(ActivityMode.VOCABULARY)
//            .setData(myVocabularyResult)
//            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
//            .startActivity()
    }

    /**
     * 스타워즈 게임화면으로 이동
     */
    private fun startGameStarwordsActivity()
    {
        Log.f("")
        // TODO : WEBVIEW_GAME_STARWORDS 화면작업 끝난 후 풀어주기
//        IntentManagementFactory.getInstance()
//            .readyActivityMode(ActivityMode.WEBVIEW_GAME_STARWORDS)
//            .setData(mSearchItemList[mCurrentOptionIndex].getID())
//            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
//            .startActivity()
    }

    /**
     * 크로스워드 게임화면으로 이동
     */
    private fun startGameCrosswordActivity()
    {
        Log.f("")
        // TODO : WEBVIEW_GAME_CROSSWORD 화면작업 끝난 후 풀어주기
//        IntentManagementFactory.getInstance()
//            .readyActivityMode(ActivityMode.WEBVIEW_GAME_CROSSWORD)
//            .setData(mSearchItemList[mCurrentOptionIndex].getID())
//            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
//            .startActivity()
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
            TODO("Not yet implemented")
        }

        override fun onErrorMessage(message : String)
        {
            Log.f("message : $message")
            mSearchListContractView.showErrorMessage(message)
        }
    }

    /**
     * 통신 이벤트 리스너
     */
    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String?) { }

        override fun onRunningEnd(code : String?, mObject : Any?)
        {
            val result : BaseResult = mObject as BaseResult

            Log.f("code : " + code + ", status : " + result.getStatus())

            if(result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                if(code == Common.COROUTINE_CODE_SEARCH_LIST)
                {
                    mSearchListContractView.hideContentsListLoading()
                    mCurrentSearchListBaseObject = mObject as SearchListBaseObject
                    notifyData()
                }
                else if(code == Common.COROUTINE_CODE_BOOKSHELF_CONTENTS_ADD)
                {
                    mSearchListContractView.hideLoading()
                    val myBookshelfResult : MyBookshelfResult = (mObject as BookshelfBaseObject).getData()
                    updateBookshelfData(myBookshelfResult)

                    val messsage = Message.obtain()
                    messsage.what = MESSAGE_COMPLETE_CONTENTS_ADD
                    messsage.obj = mContext.resources.getString(R.string.message_success_save_contents_in_bookshelf)
                    messsage.arg1 = Activity.RESULT_OK
                    mMainHandler.sendMessageDelayed(messsage, Common.DURATION_NORMAL)
                }
            }
            else
            {
                if(result.isAuthenticationBroken)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
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
                        val messsage = Message.obtain()
                        messsage.what = MESSAGE_COMPLETE_CONTENTS_ADD
                        messsage.obj = result.getMessage()
                        messsage.arg1 = Activity.RESULT_CANCELED
                        mMainHandler.sendMessageDelayed(messsage, Common.DURATION_SHORT)
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