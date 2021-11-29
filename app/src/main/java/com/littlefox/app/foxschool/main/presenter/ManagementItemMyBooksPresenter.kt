package com.littlefox.app.foxschool.main.presenter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Message
import android.os.Parcelable
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.bookshelf.ManagementBooksData
import com.littlefox.app.foxschool.`object`.result.BookshelfBaseObject
import com.littlefox.app.foxschool.`object`.result.VocabularyShelfBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.coroutine.*
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.enumerate.BookColor
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.MyBooksType
import com.littlefox.app.foxschool.main.contract.ManagementItemMyBooksContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log

/**
 * 책장/단어장 관리 Presenter
 */
class ManagementItemMyBooksPresenter : ManagementItemMyBooksContract.Presenter
{
    companion object
    {
        private const val MESSAGE_DELETE_BOOKSHELF_SUCCESS : Int    = 101
        private const val MESSAGE_DELETE_VOCABULARY_SUCCESS : Int   = 102

        private const val DIALOG_EVENT_DELETE_BOOKSHELF : Int       = 10001
        private const val DIALOG_EVENT_DELETE_VOCABULARY : Int      = 10002

        private const val MAX_NAME_SIZE : Int = 15
    }

    private lateinit var mContext : Context
    private var mMainHandler : WeakReferenceHandler? = null
    private lateinit var mManagementItemMyBooksContractView : ManagementItemMyBooksContract.View
    private var mTemplateAlertDialog : TemplateAlertDialog? = null

    private var mBookshelfCreateCoroutine : BookshelfCreateCoroutine? = null
    private var mBookshelfUpdateCoroutine : BookshelfUpdateCoroutine? = null
    private var mBookshelfDeleteCoroutine : BookshelfDeleteCoroutine? = null

    private var mVocabularyCreateCoroutine : VocabularyCreateCoroutine? = null
    private var mVocabularyUpdateCoroutine : VocabularyUpdateCoroutine? = null
    private var mVocabularyDeleteCoroutine : VocabularyDeleteCoroutine? = null

    private var mMainInformationResult : MainInformationResult? = null
    private lateinit var mManagementBooksData : ManagementBooksData
    private var mSelectBookColor : BookColor? = null
    private var mBookName : String? = null

    constructor(context : Context)
    {
        mContext = context
        mMainHandler = WeakReferenceHandler(mContext as MessageHandlerCallback)
        mManagementItemMyBooksContractView = mContext as ManagementItemMyBooksContract.View
        mManagementItemMyBooksContractView.initView()
        mManagementItemMyBooksContractView.initFont()

        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        mManagementBooksData = (mContext as AppCompatActivity).intent.getParcelableExtra(Common.INTENT_MANAGEMENT_MYBOOKS_DATA)!!
        mSelectBookColor = BookColor.RED

        if(mManagementBooksData.getName() != "")
        {
            mManagementItemMyBooksContractView.setBooksName(mManagementBooksData.getName())
        }

        if(mManagementBooksData.getColor() != "")
        {
            mSelectBookColor = CommonUtils.getInstance(mContext).getBookColorType(mManagementBooksData.getColor())
        }

        mManagementItemMyBooksContractView.settingBookColor(mSelectBookColor!!)
        mManagementItemMyBooksContractView.settingLayoutView(mManagementBooksData.getBooksType())

        Log.f("onCreate")

        init()
    }

    private fun init()
    {
        when(mManagementBooksData.getBooksType())
        {
            MyBooksType.BOOKSHELF_ADD, MyBooksType.VOCABULARY_ADD ->
            {
                mManagementItemMyBooksContractView.setCancelButtonAction(false)
            }
            MyBooksType.BOOKSHELF_MODIFY, MyBooksType.VOCABULARY_MODIFY ->
            {
                mManagementItemMyBooksContractView.setCancelButtonAction(true)
            }
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

        mBookshelfCreateCoroutine?.cancel()
        mBookshelfCreateCoroutine = null

        mBookshelfUpdateCoroutine?.cancel()
        mBookshelfUpdateCoroutine = null

        mBookshelfDeleteCoroutine?.cancel()
        mBookshelfDeleteCoroutine = null

        mVocabularyCreateCoroutine?.cancel()
        mVocabularyCreateCoroutine = null

        mVocabularyUpdateCoroutine?.cancel()
        mVocabularyUpdateCoroutine = null

        mVocabularyDeleteCoroutine?.cancel()
        mVocabularyDeleteCoroutine = null
    }

    override fun activityResult(requestCode : Int, resultCode : Int, data : Intent?) { }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_DELETE_BOOKSHELF_SUCCESS,
            MESSAGE_DELETE_VOCABULARY_SUCCESS ->
            {
                (mContext as AppCompatActivity).finish()
            }
        }
    }

    override fun onSelectSaveButton(bookName : String)
    {
        if(bookName == "")
        {
            mManagementItemMyBooksContractView.showErrorMessage(mContext.resources.getString(R.string.message_warning_empty_bookshelf_name))
            return
        }
        else if(bookName.length > MAX_NAME_SIZE)
        {
            mManagementItemMyBooksContractView.showErrorMessage(mContext.resources.getString(R.string.message_warning_add_bookshelf_maximum_15_word))
            return
        }

        mBookName = bookName

        mManagementItemMyBooksContractView.showLoading()

        Log.f("bookName : " + mBookName + ", type : " + mManagementBooksData.getBooksType())
        when(mManagementBooksData.getBooksType())
        {
            MyBooksType.BOOKSHELF_ADD -> requestBookshelfCreateAsync()
            MyBooksType.BOOKSHELF_MODIFY -> requestBookshelfUpdateAsync()
            MyBooksType.VOCABULARY_ADD -> requestVocabularyCreateAsync()
            MyBooksType.VOCABULARY_MODIFY -> requestVocabularyUpdateAsync()
        }
    }

    override fun onSelectCloseButton()
    {
        (mContext as AppCompatActivity).onBackPressed()
    }

    override fun onSelectBooksItem(color : BookColor)
    {
        mSelectBookColor = color
    }

    /**
     * 취소/삭제 버튼 클릭 이벤트
     */
    override fun onCancelActionButton()
    {
        Log.f("Book Type : " + mManagementBooksData.getBooksType())
        when(mManagementBooksData.getBooksType())
        {
            MyBooksType.BOOKSHELF_ADD, MyBooksType.VOCABULARY_ADD -> (mContext as AppCompatActivity).finish()
            MyBooksType.BOOKSHELF_MODIFY -> showTemplateAlertDialog(
                mContext.resources.getString(R.string.message_delete_bookshelf),
                DIALOG_EVENT_DELETE_BOOKSHELF,
                DialogButtonType.BUTTON_2
            )
            MyBooksType.VOCABULARY_MODIFY -> showTemplateAlertDialog(
                mContext.resources.getString(R.string.message_delete_vocabulary),
                DIALOG_EVENT_DELETE_VOCABULARY,
                DialogButtonType.BUTTON_2
            )
        }
    }

    /**
     * 책장 생성
     */
    private fun requestBookshelfCreateAsync()
    {
        Log.f("")
        mBookshelfCreateCoroutine = BookshelfCreateCoroutine(mContext)
        mBookshelfCreateCoroutine!!.setData(mBookName, mSelectBookColor)
        mBookshelfCreateCoroutine!!.asyncListener = mAsyncListener
        mBookshelfCreateCoroutine!!.execute()
    }

    /**
     * 책장 수정
     */
    private fun requestBookshelfUpdateAsync()
    {
        Log.f("")
        mBookshelfUpdateCoroutine = BookshelfUpdateCoroutine(mContext)
        mBookshelfUpdateCoroutine!!.setData(mManagementBooksData.getID(), mBookName, mSelectBookColor)
        mBookshelfUpdateCoroutine!!.asyncListener = mAsyncListener
        mBookshelfUpdateCoroutine!!.execute()
    }

    /**
     * 책장 삭제
     */
    private fun requestBookshelfDeleteAsync()
    {
        Log.f("Delete Bookshelf ID : " + mManagementBooksData.getID())
        mBookshelfDeleteCoroutine = BookshelfDeleteCoroutine(mContext)
        mBookshelfDeleteCoroutine!!.setData(mManagementBooksData.getID())
        mBookshelfDeleteCoroutine!!.asyncListener = mAsyncListener
        mBookshelfDeleteCoroutine!!.execute()
    }

    /**
     * 단어장 생성
     */
    private fun requestVocabularyCreateAsync()
    {
        Log.f("")
        mVocabularyCreateCoroutine = VocabularyCreateCoroutine(mContext)
        mVocabularyCreateCoroutine!!.setData(mBookName, mSelectBookColor)
        mVocabularyCreateCoroutine!!.asyncListener = mAsyncListener
        mVocabularyCreateCoroutine!!.execute()
    }

    /**
     * 단어장 수정
     */
    private fun requestVocabularyUpdateAsync()
    {
        Log.f("")
        mVocabularyUpdateCoroutine = VocabularyUpdateCoroutine(mContext)
        mVocabularyUpdateCoroutine!!.setData(mManagementBooksData.getID(), mBookName, mSelectBookColor)
        mVocabularyUpdateCoroutine!!.asyncListener = mAsyncListener
        mVocabularyUpdateCoroutine!!.execute()
    }

    /**
     * 단어장 삭제
     */
    private fun requestVocabularyDeleteAsync()
    {
        Log.f("Delete Vocabulary ID : " + mManagementBooksData.getID())
        mVocabularyDeleteCoroutine = VocabularyDeleteCoroutine(mContext)
        mVocabularyDeleteCoroutine!!.setData(mManagementBooksData.getID())
        mVocabularyDeleteCoroutine!!.asyncListener = mAsyncListener
        mVocabularyDeleteCoroutine!!.execute()
    }

    /**
     * 책장 데이터 업데이트
     */
    private fun updateBookshelfData(result : MyBookshelfResult)
    {
        for(i in mMainInformationResult!!.getBookShelvesList().indices)
        {
            if(mMainInformationResult!!.getBookShelvesList()[i].getID() == mManagementBooksData.getID())
            {
                Log.f("update Bookshelf Index :$i")
                mMainInformationResult!!.getBookShelvesList()[i] = result
            }
        }
    }

    /**
     * 책장 삭제
     */
    private fun deleteCurrentBookshelfData()
    {
        for(i in mMainInformationResult!!.getBookShelvesList().indices)
        {
            if(mMainInformationResult!!.getBookShelvesList()[i].getID() == mManagementBooksData.getID())
            {
                Log.f("Delete Bookshelf Index :" + i + ", ID : " + mManagementBooksData.getID())
                mMainInformationResult!!.getBookShelvesList().removeAt(i)
            }
        }
    }

    /**
     * 단어장 데이터 업데이트
     */
    private fun updateVocabularyData(result : MyVocabularyResult)
    {
        for(i in mMainInformationResult!!.getVocabulariesList().indices)
        {
            if(mMainInformationResult!!.getVocabulariesList()[i].getID() == mManagementBooksData.getID())
            {
                Log.f("update Vocabulary Index :$i")
                mMainInformationResult!!.getVocabulariesList()[i] = result
            }
        }
    }

    /**
     * 단어장 삭제
     */
    private fun deleteCurrentVocabularyData()
    {
        for(i in mMainInformationResult!!.getVocabulariesList().indices)
        {
            if(mMainInformationResult!!.getVocabulariesList()[i].getID() == mManagementBooksData.getID())
            {
                Log.f("Delete Vocabulary Index :" + i + ", ID : " + mManagementBooksData.getID())
                mMainInformationResult!!.getVocabulariesList().removeAt(i)
            }
        }
    }

    private fun showTemplateAlertDialog(message : String, eventType : Int, buttonType : DialogButtonType)
    {
        mTemplateAlertDialog = TemplateAlertDialog(mContext)
        mTemplateAlertDialog!!.setMessage(message)
        mTemplateAlertDialog!!.setDialogEventType(eventType)
        mTemplateAlertDialog!!.setButtonType(buttonType)
        mTemplateAlertDialog!!.setDialogListener(mDialogListener)
        mTemplateAlertDialog!!.setGravity(Gravity.LEFT)
        mTemplateAlertDialog!!.show()
    }

    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String) { }

        override fun onRunningEnd(code : String, `object` : Any)
        {
            mManagementItemMyBooksContractView.hideLoading()
            val result : BaseResult = `object` as BaseResult
            Log.f("code : " + code + ", status : " + result.getStatus())

            if(result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                if(code == Common.COROUTINE_CODE_BOOKSHELF_CREATE)
                {
                    Log.f("")
                    val myBookshelfResult : MyBookshelfResult = (`object` as BookshelfBaseObject).getData()
                    mMainInformationResult!!.getBookShelvesList().add(myBookshelfResult)
                    CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult!!)
                    MainObserver.updatePage(Common.PAGE_MY_BOOKS)
                    (mContext as AppCompatActivity).finish()
                }
                else if(code == Common.COROUTINE_CODE_BOOKSHELF_UPDATE)
                {
                    val myBookshelfResult : MyBookshelfResult = (`object` as BookshelfBaseObject).getData()
                    updateBookshelfData(myBookshelfResult)
                    CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult!!)
                    MainObserver.updatePage(Common.PAGE_MY_BOOKS)

                    val intent = Intent()
                    intent.putExtra(Common.INTENT_MODIFY_BOOKSHELF_NAME, myBookshelfResult.getName())
                    (mContext as AppCompatActivity).setResult(Activity.RESULT_OK, intent)
                    (mContext as AppCompatActivity).finish()
                }
                else if(code == Common.COROUTINE_CODE_BOOKSHELF_DELETE)
                {
                    deleteCurrentBookshelfData()
                    CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult!!)
                    MainObserver.updatePage(Common.PAGE_MY_BOOKS)
                    mMainHandler!!.sendEmptyMessageDelayed(MESSAGE_DELETE_BOOKSHELF_SUCCESS, Common.DURATION_SHORT)
                }
                else if(code == Common.COROUTINE_CODE_VOCABULARY_CREATE)
                {
                    val myVocabularyResult : MyVocabularyResult = (`object` as VocabularyShelfBaseObject).getData()
                    mMainInformationResult!!.getVocabulariesList().add(myVocabularyResult)
                    CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult!!)
                    MainObserver.updatePage(Common.PAGE_MY_BOOKS)
                    (mContext as AppCompatActivity).finish()
                }
                else if(code == Common.COROUTINE_CODE_VOCABULARY_UPDATE)
                {
                    val myVocabularyResult : MyVocabularyResult = (`object` as VocabularyShelfBaseObject).getData()
                    updateVocabularyData(myVocabularyResult)
                    CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult!!)
                    MainObserver.updatePage(Common.PAGE_MY_BOOKS)

                    val intent = Intent()
                    intent.putExtra(Common.INTENT_MODIFY_VOCABULARY_NAME, myVocabularyResult.getName())
                    (mContext as AppCompatActivity).setResult(Activity.RESULT_OK, intent)
                    (mContext as AppCompatActivity).finish()
                }
                else if(code == Common.COROUTINE_CODE_VOCABULARY_DELETE)
                {
                    deleteCurrentVocabularyData()
                    CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult!!)
                    MainObserver.updatePage(Common.PAGE_MY_BOOKS)
                    mMainHandler!!.sendEmptyMessageDelayed(MESSAGE_DELETE_VOCABULARY_SUCCESS, Common.DURATION_SHORT)
                }
            }
            else
            {
                if(result.isDuplicateLogin)
                {
                    //중복 로그인 시 재시작
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
                    mManagementItemMyBooksContractView.showErrorMessage(result.getMessage())
                }
            }
        }

        override fun onRunningCanceled(code : String) { }

        override fun onRunningProgress(code : String, progress : Int) { }

        override fun onRunningAdvanceInformation(code : String, `object` : Any) { }

        override fun onErrorListener(code : String, message : String) { }
    }


    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int) {}

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int)
        {
            Log.f("eventType : $eventType, buttonType : $buttonType")
            when(eventType)
            {
                DIALOG_EVENT_DELETE_BOOKSHELF ->
                {
                    if(buttonType == DialogButtonType.BUTTON_2)
                    {
                        mManagementItemMyBooksContractView.showLoading()
                        requestBookshelfDeleteAsync()
                    }
                }
                DIALOG_EVENT_DELETE_VOCABULARY ->
                {
                    if(buttonType == DialogButtonType.BUTTON_2)
                    {
                        mManagementItemMyBooksContractView.showLoading()
                        requestVocabularyDeleteAsync()
                    }
                }
            }
        }
    }
}