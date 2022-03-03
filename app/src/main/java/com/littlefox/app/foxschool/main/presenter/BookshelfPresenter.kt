package com.littlefox.app.foxschool.main.presenter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Message
import android.os.Parcelable
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.flashcard.FlashcardDataObject
import com.littlefox.app.foxschool.`object`.data.player.PlayerIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.quiz.QuizIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.record.RecordIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.webview.WebviewIntentParamsObject
import com.littlefox.app.foxschool.`object`.result.BookshelfListItemBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.adapter.DetailListItemAdapter
import com.littlefox.app.foxschool.adapter.listener.DetailItemListener
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.coroutine.BookshelfContentRemoveCoroutine
import com.littlefox.app.foxschool.coroutine.BookshelfDetailListInformationCoroutine
import com.littlefox.app.foxschool.dialog.BottomContentItemOptionDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.dialog.listener.ItemOptionListener
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.main.contract.BookshelfContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log
import java.util.*

/**
 * 책장 Presenter
 */
class BookshelfPresenter : BookshelfContract.Presenter
{
    companion object
    {
        private const val DIALOG_EVENT_DELETE_BOOKSHELF_CONTENTS : Int      = 10001
        private const val DIALOG_TYPE_WARNING_RECORD_PERMISSION : Int       = 10002

        private const val REQUEST_CODE_UPDATE_BOOKSHELF : Int               = 1001

        private const val MESSAGE_REQUEST_BOOKSHELF_DETAIL_LIST : Int       = 100
        private const val MESSAGE_SET_BOOKSHELF_DETAIL_LIST : Int           = 101
        private const val MESSAGE_REQUEST_BOOKSHELF_CONTENTS_DELETE : Int   = 102
        private const val MESSAGE_COMPLETE_BOOKSHELF_CONTENTS_DELETE : Int  = 103
        private const val MESSAGE_START_QUIZ : Int                          = 104
        private const val MESSAGE_START_TRANSLATE : Int                     = 105
        private const val MESSAGE_START_EBOOK : Int                         = 106
        private const val MESSAGE_START_VOCABULARY : Int                    = 107
        private const val MESSAGE_FINISH_BOOKSHELF : Int                    = 108
        private const val MESSAGE_START_GAME_STARWORDS : Int                = 109
        private const val MESSAGE_START_GAME_CROSSWORD : Int                = 110
        private const val MESSAGE_START_FLASHCARD : Int                     = 111
        private const val MESSAGE_START_RECORD_PLAYER : Int                 = 112
    }

    private lateinit var mContext : Context
    private lateinit var mBookshelfContractView : BookshelfContract.View
    private lateinit var mMainHandler : WeakReferenceHandler
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog

    private var mCurrentMyBookshelfResult : MyBookshelfResult? = null
    private var mBookItemInformationList : ArrayList<ContentsBaseResult>? = null
    private var mBookshelfDetailItemAdapter : DetailListItemAdapter? = null
    private var mBottomContentItemOptionDialog : BottomContentItemOptionDialog? = null

    private var mBookshelfDetailListInformationCoroutine : BookshelfDetailListInformationCoroutine? = null
    private var mBookshelfContentRemoveCoroutine : BookshelfContentRemoveCoroutine? = null

    private var mDeleteBookItemList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var mCurrentPlayIndex : Int = 0
    private var mCurrentOptionIndex : Int = 0

    constructor(context : Context)
    {
        mContext = context
        mCurrentMyBookshelfResult = (mContext as AppCompatActivity).intent.getParcelableExtra<Parcelable>(Common.INTENT_BOOKSHELF_DATA) as MyBookshelfResult
        Log.f("ID : ${mCurrentMyBookshelfResult?.getID()}, Name : ${mCurrentMyBookshelfResult?.getName()}, Color : ${ mCurrentMyBookshelfResult?.getColor()}")
        mMainHandler = WeakReferenceHandler(mContext as MessageHandlerCallback)
        mBookshelfContractView = (mContext as BookshelfContract.View).apply {
            initView()
            initFont()
            setTitle(mCurrentMyBookshelfResult?.getName())
            showContentListLoading()
        }
        Log.f("onCreate")
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_REQUEST_BOOKSHELF_DETAIL_LIST, Common.DURATION_LONG)
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
        mMainHandler.removeCallbacksAndMessages(null)
        mBookshelfDetailListInformationCoroutine?.cancel()
        mBookshelfDetailListInformationCoroutine = null
        mBookshelfContentRemoveCoroutine?.cancel()
        mBookshelfContentRemoveCoroutine = null
    }

    override fun activityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        Log.f("requestCode : $requestCode, resultCode : $resultCode")

        when(requestCode)
        {
            REQUEST_CODE_UPDATE_BOOKSHELF ->
            {
                if(resultCode == Activity.RESULT_OK)
                {
                    val bookName = data!!.getStringExtra(Common.INTENT_MODIFY_BOOKSHELF_NAME)
                    Log.f("bookName : $bookName")
                    mBookshelfContractView.setTitle(bookName)
                }
            }
        }
    }

    override fun sendMessageEvent(msg : Message)
    {
        Log.f("message : ${msg.what}")
        when(msg.what)
        {
            MESSAGE_REQUEST_BOOKSHELF_DETAIL_LIST -> requestBookshelfDetailInformationAsync()
            MESSAGE_SET_BOOKSHELF_DETAIL_LIST -> initContentItemList()
            MESSAGE_REQUEST_BOOKSHELF_CONTENTS_DELETE ->
            {
                mBookshelfContractView.showLoading()
                requestBookshelfRemoveAsync()
            }
            MESSAGE_COMPLETE_BOOKSHELF_CONTENTS_DELETE ->
            {
                if(msg.arg1 == Activity.RESULT_OK)
                {
                    mBookshelfContractView.showSuccessMessage(msg.obj as String)
                } else
                {
                    mBookshelfContractView.showErrorMessage(msg.obj as String)
                }
                if(mBookItemInformationList!!.size == 0)
                {
                    mMainHandler.sendEmptyMessageDelayed(
                        MESSAGE_FINISH_BOOKSHELF,
                        Common.DURATION_NORMAL
                    )
                }
            }
            MESSAGE_START_QUIZ -> startQuizActivity()
            MESSAGE_START_TRANSLATE -> startOriginTranslateActivity()
            MESSAGE_START_EBOOK -> startEbookActivity()
            MESSAGE_START_VOCABULARY -> startVocabularyActivity()
            MESSAGE_FINISH_BOOKSHELF -> (mContext as AppCompatActivity).onBackPressed()
            MESSAGE_START_GAME_STARWORDS -> startGameStarwordsActivity()
            MESSAGE_START_GAME_CROSSWORD -> startGameCrosswordActivity()
            MESSAGE_START_FLASHCARD -> startFlashcardActivity()
            MESSAGE_START_RECORD_PLAYER -> startRecordPlayerActivity()
        }
    }

    /**
     * 전체 선택
     */
    override fun onClickSelectAll()
    {
        Log.f("")
        mBookshelfDetailItemAdapter!!.setSelectedAllData()
    }

    /**
     * 컨텐츠 선택 재생
     */
    override fun onClickSelectPlay()
    {
        Log.f("")
        if(mBookshelfDetailItemAdapter!!.getSelectedList().size > 0)
        {
            val sendItemList = mBookshelfDetailItemAdapter!!.getSelectedList()
            val playerParamsObject = PlayerIntentParamsObject(sendItemList)
            IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.PLAYER)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .setData(playerParamsObject)
                .startActivity()
        }
        else
        {
            mBookshelfContractView.showErrorMessage(mContext.resources.getString(R.string.message_not_selected_contents_list))
        }
    }

    /**
     * 책장 삭제
     */
    override fun onClickRemoveBookshelf()
    {
        Log.f("delete list size : " + mBookshelfDetailItemAdapter!!.getSelectedList().size)

        if(mBookshelfDetailItemAdapter!!.getSelectedList().size > 0)
        {
            mDeleteBookItemList.clear()
            mDeleteBookItemList = mBookshelfDetailItemAdapter!!.getSelectedList()
            showBookshelfContentsDeleteDialog()
        }
    }

    override fun onClickCancel()
    {
        Log.f("")
        mBookshelfDetailItemAdapter!!.initSelectedData()
    }

    private fun initContentItemList()
    {
        mBookshelfDetailItemAdapter = DetailListItemAdapter(mContext).setData(mBookItemInformationList!!).setDetailItemListener(mDetailItemListener)
        mBookshelfContractView.showBookshelfDetailListView(mBookshelfDetailItemAdapter!!)
    }

    private fun refreshBookshelfItemData()
    {
        for(deleteItem in mDeleteBookItemList)
        {
            for(i in mBookItemInformationList!!.indices)
            {
                if(deleteItem.getID() == (mBookItemInformationList!![i].getID()))
                {
                    mBookItemInformationList!!.removeAt(i)
                    break
                }
            }
        }

        val mainInformationResult : MainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        for(i in 0 until mainInformationResult.getBookShelvesList().size)
        {
            if(mCurrentMyBookshelfResult!!.getID() == (mainInformationResult.getBookShelvesList().get(i).getID()))
            {
                mainInformationResult.getBookShelvesList().get(i).setContentsCount(mBookItemInformationList!!.size)
                break
            }
        }
        CommonUtils.getInstance(mContext).saveMainData(mainInformationResult)
        MainObserver.updatePage(Common.PAGE_MY_BOOKS)
        mBookshelfDetailItemAdapter!!.notifyDataListChanged(mBookItemInformationList!!)
    }

    /**
     * ================ 컨텐츠 화면 이동 ================
     */
    private fun startCurrentSelectMovieActivity(index : Int)
    {
        Log.f("")
        mCurrentPlayIndex = index
        val sendItemList = ArrayList<ContentsBaseResult>()
        sendItemList.add(mBookItemInformationList!![mCurrentPlayIndex])
        val playerParamsObject = PlayerIntentParamsObject(sendItemList)
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.PLAYER)
            .setData(playerParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startQuizActivity()
    {
        Log.f("")
        val quizIntentParamsObject = QuizIntentParamsObject(mBookItemInformationList!![mCurrentOptionIndex].getID())
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
            .setData(mBookItemInformationList!![mCurrentOptionIndex].getID())
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startEbookActivity()
    {
        Log.f("")
        val data  = WebviewIntentParamsObject(mBookItemInformationList!![mCurrentOptionIndex].getID())
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_EBOOK)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startVocabularyActivity()
    {
        Log.f("")
        val title = CommonUtils.getInstance(mContext).getVocabularyTitleName(mBookItemInformationList!![mCurrentOptionIndex])
        val myVocabularyResult = MyVocabularyResult(
            mBookItemInformationList!![mCurrentOptionIndex].getID(),
            title,
            VocabularyType.VOCABULARY_CONTENTS
        )
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.VOCABULARY)
            .setData(myVocabularyResult)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startGameStarwordsActivity()
    {
        Log.f("")
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(mBookItemInformationList!![mCurrentOptionIndex].getID())

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_STARWORDS)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startGameCrosswordActivity()
    {
        Log.f("")
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(mBookItemInformationList!![mCurrentOptionIndex].getID())

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_CROSSWORD)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startFlashcardActivity()
    {
        Log.f("")
        val data = FlashcardDataObject(
            mBookItemInformationList!![mCurrentOptionIndex].getID(),
            mBookItemInformationList!![mCurrentOptionIndex].getName(),
            mBookItemInformationList!![mCurrentOptionIndex].getSubName(),
            VocabularyType.VOCABULARY_CONTENTS
        )
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.FLASHCARD)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startRecordPlayerActivity()
    {
        Log.f("")
        val recordIntentParamsObject = RecordIntentParamsObject(mBookItemInformationList!![mCurrentOptionIndex])
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.RECORD_PLAYER)
            .setData(recordIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /**
     * ================ 통신요청 ================
     */
    private fun requestBookshelfDetailInformationAsync()
    {
        Log.f("")
        mBookshelfDetailListInformationCoroutine = BookshelfDetailListInformationCoroutine(mContext).apply {
            setData(mCurrentMyBookshelfResult!!.getID())
            asyncListener = mAsyncListener
            execute()
        }
    }

    private fun requestBookshelfRemoveAsync()
    {
        Log.f("")
        mBookshelfContentRemoveCoroutine = BookshelfContentRemoveCoroutine(mContext).apply {
            setData(mCurrentMyBookshelfResult!!.getID(), mDeleteBookItemList)
            asyncListener = mAsyncListener
            execute()
        }
    }

    /**
     * ================ 다이얼로그 ================
     */
    private fun showBottomBookshelfItemDialog()
    {
        mBottomContentItemOptionDialog = BottomContentItemOptionDialog(mContext, mBookItemInformationList!![mCurrentOptionIndex])
        mBottomContentItemOptionDialog!!
            .setDeleteMode()
            .setFullName()
            .setItemOptionListener(mItemOptionListener)
            .setView()
        mBottomContentItemOptionDialog!!.show()
    }

    private fun showBookshelfContentsDeleteDialog()
    {
        mTemplateAlertDialog = TemplateAlertDialog(mContext).apply {
            setMessage(mContext.resources.getString(R.string.message_question_delete_contents_in_bookshelf))
            setButtonType(DialogButtonType.BUTTON_2)
            setDialogEventType(DIALOG_EVENT_DELETE_BOOKSHELF_CONTENTS)
            setDialogListener(mDialogListener)
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
            setDialogEventType(DIALOG_TYPE_WARNING_RECORD_PERMISSION)
            setButtonType(DialogButtonType.BUTTON_2)
            setButtonText(mContext.resources.getString(R.string.text_cancel), mContext.resources.getString(R.string.text_change_permission))
            setDialogListener(mDialogListener)
            show()
        }
    }

    /**
     * ================ Listener ================
     */
    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String) {}

        override fun onRunningEnd(code : String, `object` : Any)
        {
            val result : BaseResult = `object` as BaseResult
            Log.f("code : " + code + ", status : " + result.getStatus())
            if(result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                if(code == Common.COROUTINE_CODE_BOOKSHELF_DETAIL_LIST_INFO)
                {
                    mBookshelfContractView.hideContentListLoading()
                    mBookItemInformationList = (result as BookshelfListItemBaseObject).getData()
                    mMainHandler.sendEmptyMessage(MESSAGE_SET_BOOKSHELF_DETAIL_LIST)
                }
                else if(code == Common.COROUTINE_CODE_BOOKSHELF_CONTENTS_DELETE)
                {
                    mBookshelfContractView.hideLoading()
                    refreshBookshelfItemData()
                    mBookshelfContractView.hideFloatingToolbarLayout()
                    val message = Message.obtain().apply {
                        what = MESSAGE_COMPLETE_BOOKSHELF_CONTENTS_DELETE
                        obj = mContext.resources.getString(R.string.message_success_delete_contents)
                        arg1 = Activity.RESULT_OK
                    }
                    mMainHandler.sendMessageDelayed(message, Common.DURATION_NORMAL)
                }
            }
            else
            {
                if(result.isDuplicateLogin)
                {
                    // 중복 로그인 시 재시작
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initAutoIntroSequence()
                }
                else if(result.isAuthenticationBroken)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initScene()
                }
                else
                {
                    if(code == Common.COROUTINE_CODE_BOOKSHELF_DETAIL_LIST_INFO)
                    {
                        mBookshelfContractView.hideContentListLoading()
                        Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                        (mContext as AppCompatActivity).onBackPressed()
                    }
                    else if(code == Common.COROUTINE_CODE_BOOKSHELF_CONTENTS_DELETE)
                    {
                        mBookshelfContractView.hideLoading()
                        mBookshelfContractView.hideFloatingToolbarLayout()
                        val message = Message.obtain().apply {
                            what = MESSAGE_COMPLETE_BOOKSHELF_CONTENTS_DELETE
                            obj = result.getMessage()
                            arg1 = Activity.RESULT_CANCELED
                        }
                        mMainHandler.sendMessageDelayed(message, Common.DURATION_NORMAL)
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
            mCurrentPlayIndex = index
            startCurrentSelectMovieActivity(mCurrentPlayIndex)
        }

        override fun onItemClickOption(index : Int)
        {
            Log.f("index : $index")
            mCurrentOptionIndex = index
            showBottomBookshelfItemDialog()
        }

        override fun onItemSelectCount(count : Int)
        {
            if(count == 0)
            {
                mBookshelfContractView.hideFloatingToolbarLayout()
            }
            else
            {
                if(count == 1)
                {
                    mBookshelfContractView.showFloatingToolbarLayout()
                }
                mBookshelfContractView.setFloatingToolbarPlayCount(count)
            }
        }
    }

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
            Log.f("DELETE")
            mDeleteBookItemList.clear()
            mDeleteBookItemList.add(mBookItemInformationList!![mCurrentOptionIndex])
            showBookshelfContentsDeleteDialog()
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
            mBookshelfContractView.showErrorMessage(message)
        }
    }

    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int) {}

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int)
        {
            Log.f("event type : $eventType, buttonType : $buttonType")
            if(eventType == DIALOG_EVENT_DELETE_BOOKSHELF_CONTENTS)
            {
                when(buttonType)
                {
                    DialogButtonType.BUTTON_2 ->
                    {
                        mBookshelfContractView.showLoading()
                        requestBookshelfRemoveAsync()
                    }
                }
            }
            else if(eventType == DIALOG_TYPE_WARNING_RECORD_PERMISSION)
            {
                when(buttonType)
                {
                    DialogButtonType.BUTTON_1 ->
                    {
                        // [취소] 컨텐츠 사용 불가 메세지 표시
                        mBookshelfContractView.showErrorMessage(mContext.getString(R.string.message_warning_record_permission))
                    }
                    DialogButtonType.BUTTON_2 ->
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
    }
}