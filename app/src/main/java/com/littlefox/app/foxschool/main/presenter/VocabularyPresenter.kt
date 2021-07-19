package com.littlefox.app.foxschool.main.presenter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Message
import android.os.Parcelable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.bookshelf.ManagementBooksData
import com.littlefox.app.foxschool.`object`.data.vocabulary.VocabularySelectData
import com.littlefox.app.foxschool.`object`.result.VocabularyContentsBaseObject
import com.littlefox.app.foxschool.`object`.result.VocabularyShelfBaseObject
import com.littlefox.app.foxschool.`object`.result.VocabularyShelfListItemBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyDataResult
import com.littlefox.app.foxschool.adapter.VocabularyItemListAdapter
import com.littlefox.app.foxschool.adapter.listener.VocabularyItemListener
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.coroutine.VocabularyContentsAddCoroutine
import com.littlefox.app.foxschool.coroutine.VocabularyContentsDeleteCoroutine
import com.littlefox.app.foxschool.coroutine.VocabularyContentsListCoroutine
import com.littlefox.app.foxschool.coroutine.VocabularyShelfListCoroutine
import com.littlefox.app.foxschool.dialog.BottomBookAddDialog
import com.littlefox.app.foxschool.dialog.BottomIntervalSelectDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.BookAddListener
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.dialog.listener.IntervalSelectListener
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.main.contract.VocabularyContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log
import java.util.*

class VocabularyPresenter : VocabularyContract.Presenter
{
    companion object
    {
        private const val DIALOG_EVENT_DELETE_VOCABULARY_CONTENTS : Int = 10001
        private const val REQUEST_CODE_UPDATE_VOCABULARY : Int          = 1001
        private const val MESSAGE_REQUEST_VOCABULARY_DETAIL_LIST : Int  = 100
        private const val MESSAGE_SETTING_LIST : Int                    = 101
        private const val MESSAGE_COMPLETE_CONTENTS : Int               = 102
        private const val MESSAGE_PLAY_LIST_ITEM : Int                  = 103
        private const val MESSAGE_NOTIFY_DATA_ALL : Int                 = 104
        private const val MESSAGE_NOTIFY_DATA_SELECT : Int              = 105
    }

    private lateinit var mMainHandler : WeakReferenceHandler
    private lateinit var mVocabularyContractView : VocabularyContract.View
    private var mVocabularyItemList : ArrayList<VocabularyDataResult> = ArrayList<VocabularyDataResult>()
    private var mRequestItemList : ArrayList<VocabularyDataResult> = ArrayList<VocabularyDataResult>()
    private var mSelectedPlayItemList : ArrayList<VocabularyDataResult> = ArrayList<VocabularyDataResult>()
    private var mVocabularyContentsListCoroutine : VocabularyContentsListCoroutine? = null
    private var mVocabularyShelfListCoroutine : VocabularyShelfListCoroutine? = null
    private var mVocabularyContentsAddCoroutine : VocabularyContentsAddCoroutine? = null
    private var mVocabularyContentsDeleteCoroutine : VocabularyContentsDeleteCoroutine? = null
    private var mMediaPlayer : MediaPlayer? = null
    private var mAudioAttributes : AudioAttributes? = null
    private lateinit var mCurrentMyVocabularyResult : MyVocabularyResult
    private lateinit var mVocabularyItemListAdapter : VocabularyItemListAdapter
    private lateinit var mVocabularySelectData : VocabularySelectData
    private var mBottomIntervalSelectDialog : BottomIntervalSelectDialog? = null
    private var mBottomBookAddDialog : BottomBookAddDialog? = null
    private lateinit var mMainInformationResult : MainInformationResult
    private lateinit var mCurrentVocabularyAddResult : MyVocabularyResult
    private val mManagementBooksData : ManagementBooksData? = null
    private var mTemplateAlertDialog : TemplateAlertDialog? = null
    private var mCurrentIntervalSecond = 2
    private var mCurrentPlayIndex = 0
    private var isSequencePlay = false
    private var isPause = false
    private lateinit var mContext : Context;

    constructor(context : Context)
    {
        mContext = context;
        mCurrentMyVocabularyResult = (mContext as AppCompatActivity).getIntent().getParcelableExtra<Parcelable>(Common.INTENT_VOCABULARY_DATA) as MyVocabularyResult
        mVocabularySelectData = VocabularySelectData()
        mMainHandler = WeakReferenceHandler(mContext as MessageHandlerCallback)
        Log.f("TYPE : " + mCurrentMyVocabularyResult.getVocabularyType())
        mVocabularyContractView = mContext as VocabularyContract.View
        mVocabularyContractView.initView()
        mVocabularyContractView.setTitle(mCurrentMyVocabularyResult.getName())
        mVocabularyContractView.initFont()
        mVocabularyContractView.setBottomWordsActionType(mCurrentMyVocabularyResult.getVocabularyType())
        Log.f("onCreate")
        init()
        setupMediaPlayer()
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_REQUEST_VOCABULARY_DETAIL_LIST, Common.DURATION_LONG)
    }

    private fun init()
    {
        mCurrentIntervalSecond = CommonUtils.getInstance(mContext).getSharedPreferenceInteger(Common.PARAMS_VOCABULARY_INTERVAL, mCurrentIntervalSecond)
        mVocabularyContractView.setBottomIntervalValue(mCurrentIntervalSecond)
        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        mVocabularyContractView.showContentListLoading()
    }

    override fun resume()
    {
        Log.f("")
        if(isPause)
        {
            isPause = false
            setVocabularyControlPlay()
        }
    }

    override fun pause()
    {
        Log.f("")
        isPause = true
        isSequencePlay = false
        mMainHandler.removeMessages(MESSAGE_PLAY_LIST_ITEM)
    }

    override fun destroy()
    {
        Log.f("")
        mVocabularyContentsListCoroutine?.cancel()
        mVocabularyContentsListCoroutine = null

        mVocabularyShelfListCoroutine?.cancel()
        mVocabularyShelfListCoroutine = null

        mVocabularyContentsAddCoroutine?.cancel()
        mVocabularyContentsAddCoroutine = null

        mVocabularyContentsDeleteCoroutine?.cancel()
        mVocabularyContentsDeleteCoroutine = null

        mMainHandler.removeCallbacksAndMessages(null)
        releaseAudio()
    }

    override fun acvitityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        Log.f("requestCode : $requestCode, resultCode : $resultCode")
        when(requestCode)
        {
            REQUEST_CODE_UPDATE_VOCABULARY -> if(resultCode == Activity.RESULT_OK)
            {
                val bookName : String = data!!.getStringExtra(Common.INTENT_MODIFY_VOCABULARY_NAME)
                Log.f("bookName : $bookName")
                mVocabularyContractView!!.setTitle(bookName)
            }
        }
    }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_REQUEST_VOCABULARY_DETAIL_LIST ->
            if(mCurrentMyVocabularyResult.getVocabularyType() === VocabularyType.VOCABULARY_CONTENTS)
            {
                requestVocabularyContentsListAsync()
            }
            else if(mCurrentMyVocabularyResult.getVocabularyType() === VocabularyType.VOCABULARY_SHELF)
            {
                requestVocabularyShelfListAsync()
            }
            MESSAGE_SETTING_LIST ->
            {
                measureContentsViewSize()
                mVocabularyContractView.hideContentListLoading()
                initRecyclerView()
            }
            MESSAGE_COMPLETE_CONTENTS ->
            if(msg.arg1 == Activity.RESULT_OK)
            {
                mVocabularyContractView!!.showSuccessMessage((msg.obj as String))
            }
            else
            {
                mVocabularyContractView!!.showErrorMessage((msg.obj as String))
            }
            MESSAGE_PLAY_LIST_ITEM ->
            {
                setStatusCurrentItem()
                startAudio(mSelectedPlayItemList)
            }
            MESSAGE_NOTIFY_DATA_ALL ->
            {
                mVocabularyContractView.hideContentListLoading()
                mVocabularyContractView.showListView(mVocabularyItemListAdapter)
                mVocabularyItemListAdapter.notifyDataListChanged(mVocabularyItemList, false)
            }
            MESSAGE_NOTIFY_DATA_SELECT ->
            {
                mVocabularyContractView.hideContentListLoading()
                mVocabularyContractView.showListView(mVocabularyItemListAdapter)
                mVocabularyItemListAdapter.notifyDataListChanged(mSelectedPlayItemList, true)
                setStatusCurrentItem()
                mMainHandler.sendEmptyMessageDelayed(MESSAGE_PLAY_LIST_ITEM, Common.DURATION_NORMAL)
            }
        }
    }

    override fun onClickMenuSelectAll()
    {
        Log.f("")
        mVocabularySelectData.setSelectAll()
        mVocabularyContractView.checkIconStatusMenu(mVocabularySelectData)
        mVocabularyItemListAdapter.notifySelectContents(mVocabularySelectData)
    }

    override fun onClickMenuWord()
    {
        Log.f("")
        mVocabularySelectData.setSelectWord()
        mVocabularyContractView.checkIconStatusMenu(mVocabularySelectData)
        mVocabularyItemListAdapter.notifySelectContents(mVocabularySelectData)
    }

    override fun onClickMenuMeaning()
    {
        Log.f("")
        mVocabularySelectData.setSelectMeaning()
        mVocabularyContractView.checkIconStatusMenu(mVocabularySelectData)
        mVocabularyItemListAdapter.notifySelectContents(mVocabularySelectData)
    }

    override fun onClickMenuExample()
    {
        Log.f("")
        mVocabularySelectData.setSelectExample()
        mVocabularyContractView.checkIconStatusMenu(mVocabularySelectData)
        mVocabularyItemListAdapter.notifySelectContents(mVocabularySelectData)
    }

    override fun onClickBottomInterval()
    {
        Log.f("")
        showBottomIntervalDialog()
    }

    override fun onClickBottomPlayAction()
    {
        if(mVocabularyItemListAdapter.selectedCount <= 0)
        {
            Log.f("Not Select ITEM")
            mVocabularyContractView.showErrorMessage(mContext.resources.getString(R.string.message_not_have_play_vocabulary))
            return
        }
        isSequencePlay = !isSequencePlay
        setVocabularyControlPlay()
    }

    override fun onClickBottomPutInVocabularyShelf()
    {
        Log.f("")
        if(Feature.IS_FREE_USER)
        {
            mVocabularyContractView.showErrorMessage(mContext.resources.getString(R.string.message_payment_service_login))
            return
        }
        if(Feature.IS_REMAIN_DAY_END_USER)
        {
            mVocabularyContractView.showErrorMessage(mContext.resources.getString(R.string.message_payment_service_paid_using))
            return
        }
        Log.f("Select Count : " + mVocabularyItemListAdapter.selectedCount)
        if(mVocabularyItemListAdapter.selectedCount > 0)
        {
            showBottomVocabularyAddDialog()
        }
        else
        {
            mVocabularyContractView.showErrorMessage(mContext.resources.getString(R.string.message_select_words_put_in_vocabulary))
        }
    }

    override fun onClickBottomDeleteInVocabularyShelf()
    {
        Log.f("SelectCount : " + mVocabularyItemListAdapter.selectedCount)
        if(mVocabularyItemListAdapter.selectedCount > 0)
        {
            mRequestItemList.clear()
            mRequestItemList = mVocabularyItemListAdapter.selectedList
            showVocabularyContentDeleteDialog()
        }
        else
        {
            mVocabularyContractView.showErrorMessage(mContext.resources.getString(R.string.message_select_words_delete_in_vocabulary))
        }
    }

    override fun onClickBottomSelectAll()
    {
        Log.f("")
        mVocabularyItemListAdapter.setSelectedAllData()
        mVocabularyContractView.setBottomPlayItemCount(mVocabularyItemListAdapter.selectedCount)
    }

    override fun onClickBottomRemoveAll()
    {
        Log.f("")
        mVocabularyItemListAdapter.initSelectedData()
        mVocabularyContractView.setBottomPlayItemCount(mVocabularyItemListAdapter.selectedCount)
    }

    /**
     * onBindHolder가 끝난후 호출. 뜻, 예문, 단어의 애니메이션 동작이 스크롤시 동작하지않게 하기위해 사용
     */
    override fun onListLayoutChangedComplete()
    {
        try
        {
            mVocabularyItemListAdapter.initChangedDataValue()
        }
        catch(e : Exception) { }
    }

    private fun setVocabularyControlPlay()
    {
        Log.f("isSequencePlay : $isSequencePlay")
        if(isSequencePlay)
        {
            Log.f("Vocabulary Sound Play")
            mVocabularyContractView.setBottomPlayStatus()
            mCurrentPlayIndex = 0
            mSelectedPlayItemList = mVocabularyItemListAdapter.selectedList
            mVocabularyContractView.showContentListLoading()
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_NOTIFY_DATA_SELECT, Common.DURATION_NORMAL)
        }
        else
        {
            Log.f("Vocabulary Sound Stop")
            mMainHandler.removeCallbacksAndMessages(null)
            mVocabularyContractView.setBottomStopStatus()
            mVocabularyContractView.setBottomPlayItemCount(mVocabularyItemListAdapter.selectedCount)
            mVocabularyContractView.scrollPosition(0)
            mSelectedPlayItemList.clear()
            mVocabularyContractView.showContentListLoading()
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_NOTIFY_DATA_ALL, Common.DURATION_NORMAL)
        }
    }

    private fun requestVocabularyContentsListAsync()
    {
        Log.f("Vocabulary ID : " + mCurrentMyVocabularyResult.getContentID())
        mVocabularyContentsListCoroutine = VocabularyContentsListCoroutine(mContext)
        mVocabularyContentsListCoroutine?.setData(mCurrentMyVocabularyResult.getContentID())
        mVocabularyContentsListCoroutine?.asyncListener = mAsyncListener
        mVocabularyContentsListCoroutine?.execute()
    }

    private fun requestVocabularyShelfListAsync()
    {
        Log.f("Vocabulary ID : " + mCurrentMyVocabularyResult.getID())
        mVocabularyShelfListCoroutine = VocabularyShelfListCoroutine(mContext)
        mVocabularyShelfListCoroutine?.setData(mCurrentMyVocabularyResult.getID())
        mVocabularyShelfListCoroutine?.asyncListener = mAsyncListener
        mVocabularyShelfListCoroutine?.execute()
    }

    private fun requestVocabularyContentsAddAsync()
    {
        Log.f("Vocabulary ID : " + mCurrentVocabularyAddResult.getID())
        Log.f("Vocabulary Contents ID : " + mCurrentMyVocabularyResult.getContentID())
        mVocabularyContentsAddCoroutine = VocabularyContentsAddCoroutine(mContext)
        mVocabularyContentsAddCoroutine?.setData(mCurrentMyVocabularyResult.getContentID(), mCurrentVocabularyAddResult.getID(), mRequestItemList)
        mVocabularyContentsAddCoroutine?.asyncListener = mAsyncListener
        mVocabularyContentsAddCoroutine?.execute()
    }

    private fun requestVocabularyContentsDeleteAsync()
    {
        Log.f("Vocabulary ID : " + mCurrentMyVocabularyResult.getID())
        mVocabularyContentsDeleteCoroutine = VocabularyContentsDeleteCoroutine(mContext)
        mVocabularyContentsDeleteCoroutine?.setData(mCurrentMyVocabularyResult.getID(), mRequestItemList)
        mVocabularyContentsDeleteCoroutine?.asyncListener = mAsyncListener
        mVocabularyContentsDeleteCoroutine?.execute()
    }

    private fun showBottomIntervalDialog()
    {
        mBottomIntervalSelectDialog = BottomIntervalSelectDialog(mContext, mCurrentIntervalSecond)
        mBottomIntervalSelectDialog?.setCancelable(true)
        mBottomIntervalSelectDialog?.setOnIntervalSelectListener(mIntervalSelectListener)
        mBottomIntervalSelectDialog?.show()
    }

    private fun showBottomVocabularyAddDialog()
    {
        mBottomBookAddDialog = BottomBookAddDialog(mContext)
        mBottomBookAddDialog?.setCancelable(true)
        mBottomBookAddDialog?.setVocabularyData(mMainInformationResult.getVocabulariesList())
        mBottomBookAddDialog?.setBookSelectListener(mBookAddListener)
        mBottomBookAddDialog?.show()
    }

    private fun showVocabularyContentDeleteDialog()
    {
        mTemplateAlertDialog = TemplateAlertDialog(mContext)
        mTemplateAlertDialog?.setMessage(mContext.resources.getString(R.string.message_question_delete_contents_in_vocabulary))
        mTemplateAlertDialog?.setButtonType(DialogButtonType.BUTTON_2)
        mTemplateAlertDialog?.setDialogEventType(DIALOG_EVENT_DELETE_VOCABULARY_CONTENTS)
        mTemplateAlertDialog?.setDialogListener(mDialogListener)
        mTemplateAlertDialog?.show()
    }

    /**
     * 예문이나 뜻의 길이에 따라 컨텐츠 뷰 크기가 동적으로 변경되어하는 작업을 담당하는 변수
     */
    private fun measureContentsViewSize()
    {
        val fontSize : Float =
            if(CommonUtils.getInstance(mContext).checkTablet)
                CommonUtils.getInstance(mContext).getPixel(28.0f)
            else
                CommonUtils.getInstance(mContext).getPixel(38.0f)
        var widthSize : Float =
            if(CommonUtils.getInstance(mContext).checkTablet)
                CommonUtils.getInstance(mContext).getPixel(864.0f)
            else
                CommonUtils.getInstance(mContext).getPixel(940.0f)
        var meaningLineSize = 0
        var ExampleLineSize = 0
        var meaningText = ""
        var htmlRemovedExampleText : String? = ""
        if(Feature.IS_MINIMUM_DISPLAY_SIZE)
        {
            Log.f("IS_MINIMUM_DISPLAY_SIZE")
            widthSize = CommonUtils.getInstance(mContext).getPixel(364.0f)
        }
        val paint = Paint()
        paint.typeface = Font.getInstance(mContext).getRobotoRegular()
        paint.textSize = fontSize.toFloat()
        var i = 0
        while(i < mVocabularyItemList.size)
        {
            meaningText = mVocabularyItemList[i].getMeaningText()
            htmlRemovedExampleText = CommonUtils.getInstance(mContext).removeHtmlTag(mVocabularyItemList[i].getExampleText())
            if(meaningText.equals("")
                    || htmlRemovedExampleText.equals(""))
            {
                Log.f("meaningText == empty or htmlRemovedExampleText empty : index = $i")
                mVocabularyItemList.removeAt(i--)
                i++
                continue
            }
            meaningLineSize = CommonUtils.getInstance(mContext).splitWordsIntoStringsThatFit(meaningText, widthSize, paint).size
            ExampleLineSize = CommonUtils.getInstance(mContext).splitWordsIntoStringsThatFit(htmlRemovedExampleText, widthSize, paint).size
            mVocabularyItemList[i].setContentViewSize(CommonUtils.getInstance(mContext).getVocabularyContentViewSize(meaningLineSize + ExampleLineSize + 1))
            i++
        }
    }

    /**
     * 컨텐츠의 단어장 리스트에서 나의단어장으로 컨텐츠를 추가해서 갱신할때 사용하는 메소드 ( 추가됨으로써 서버쪽의 해당 단어장의 정보를 갱신하기 위해 사용 )
     * 예) 단어장 ID , 단어의 개수, 단어 컬러 등등
     * @param result 서버쪽에서 받은 결과 단어장 정보
     */
    private fun updateVocabularyData(result : MyVocabularyResult)
    {
        for(i in 0 until mMainInformationResult.getVocabulariesList().size)
        {
            if(mMainInformationResult.getVocabulariesList().get(i).getID().equals(result.getID()))
            {
                mMainInformationResult.getVocabulariesList().set(i, result)
            }
        }
        CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult)
        MainObserver.updatePage(Common.PAGE_MY_BOOKS)
    }

    private fun deleteRequestData()
    {
        for(deleteItem in mRequestItemList)
        {
            for(i in mVocabularyItemList.indices)
            {
                if(deleteItem.getID() == mVocabularyItemList[i].getID())
                {
                    mVocabularyItemList.removeAt(i)
                    break
                }
            }
        }
    }

    /**
     * 단어장에서 요청 데이터를 삭제후 화면 및 메인정보를 Syncronize 한다.
     */
    private fun refreshVocabularyItemData()
    {
        deleteRequestData()
        val mainInformationResult : MainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        for(i in 0 until mainInformationResult.getVocabulariesList().size)
        {
            if(mCurrentMyVocabularyResult.getID().equals(mainInformationResult.getVocabulariesList().get(i).getID()))
            {
                mainInformationResult.getVocabulariesList().get(i).setWordCount(mVocabularyItemList.size)
                break
            }
        }
        CommonUtils.getInstance(mContext).saveMainData(mainInformationResult)
        MainObserver.updatePage(Common.PAGE_MY_BOOKS)
        mVocabularyItemListAdapter.notifyDataListChanged(mVocabularyItemList, false)
    }

    private fun initRecyclerView()
    {
        Log.f("")
        mVocabularyItemListAdapter = VocabularyItemListAdapter(mContext)
        mVocabularyItemListAdapter.setData(mVocabularyItemList)
        mVocabularyItemListAdapter.setOnVocabularyListener(mVocabularyItemListener)
        mVocabularyContractView!!.showListView(mVocabularyItemListAdapter)
    }

    private fun setupMediaPlayer()
    {
        mMediaPlayer = MediaPlayer()
        mMediaPlayer?.setOnCompletionListener(object : MediaPlayer.OnCompletionListener
        {
            override fun onCompletion(mediaPlayer : MediaPlayer)
            {
                Log.f("audio complete ")
                if(isSequencePlay)
                {
                    Log.f("isSequencePlay currentIndex : " + mCurrentPlayIndex + " , list size : " + mSelectedPlayItemList.size)
                    if(mCurrentPlayIndex >= mSelectedPlayItemList.size - 1)
                    {
                        mCurrentPlayIndex = 0
                    }
                    else
                    {
                        mCurrentPlayIndex += 1
                    }
                    mMainHandler.sendEmptyMessageDelayed(MESSAGE_PLAY_LIST_ITEM, mCurrentIntervalSecond * Common.SECOND + Common.DURATION_SHORT)
                }
            }
        })
    }

    private fun setStatusCurrentItem()
    {
        Log.f("mCurrentPlayIndex : $mCurrentPlayIndex")
        mVocabularyItemListAdapter.notifyPlayItem(mCurrentPlayIndex)
        mVocabularyContractView.scrollPosition(mCurrentPlayIndex)
    }

    private fun startAudio(playList : ArrayList<VocabularyDataResult>)
    {
        Log.f("startAudio")
        if(mMediaPlayer != null)
        {
            mMediaPlayer?.reset()
        }
        else
        {
            mMediaPlayer = MediaPlayer()
        }
        try
        {
            Log.f("Play Word : " + playList[mCurrentPlayIndex].getWordText())
            Log.f("Play URL : " + playList[mCurrentPlayIndex].getSoundURL())
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                if(mAudioAttributes == null)
                {
                    mAudioAttributes = AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build()
                }
                mMediaPlayer?.setAudioAttributes(mAudioAttributes)
            }
            else
            {
                mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            }
            mMediaPlayer?.setDataSource(playList[mCurrentPlayIndex].getSoundURL())
            mMediaPlayer?.prepareAsync()
            mMediaPlayer?.setOnPreparedListener(object : MediaPlayer.OnPreparedListener
            {
                override fun onPrepared(mediaPlayer : MediaPlayer)
                {
                    mMediaPlayer?.start()
                }
            })
        }
        catch(e : Exception)
        {
            Log.f("Exception : " + e.message)
        }
    }

    private fun releaseAudio()
    {
        mMediaPlayer?.stop()
        mMediaPlayer?.release()
        mMediaPlayer = null
        mAudioAttributes = null
    }

    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String)
        {
        }

        override fun onRunningEnd(code : String, `object` : Any)
        {
            val result : BaseResult = `object` as BaseResult
            Log.f("code : " + code + ", status : " + result.getStatus())
            if(result.getStatus() === BaseResult.SUCCESS_CODE_OK)
            {
                if(code == Common.COROUTINE_CODE_VOCABULARY_CONTENTS_LIST)
                {
                    mVocabularyItemList = (`object` as VocabularyContentsBaseObject).getData()
                    mMainHandler.sendEmptyMessage(MESSAGE_SETTING_LIST)
                }
                else if(code == Common.COROUTINE_CODE_VOCABULARY_SHELF)
                {
                    mVocabularyItemList = (`object` as VocabularyShelfListItemBaseObject).getData()!!.wordsList()
                    mMainHandler.sendEmptyMessage(MESSAGE_SETTING_LIST)
                }
                else if(code == Common.COROUTINE_CODE_VOCABULARY_CONTENTS_ADD)
                {
                    mVocabularyContractView.hideLoading()
                    val myVocabularyResult : MyVocabularyResult = (`object` as VocabularyShelfBaseObject).getData()
                    updateVocabularyData(myVocabularyResult)
                    mVocabularyItemListAdapter.initSelectedData()
                    mVocabularyContractView.setBottomPlayItemCount(mVocabularyItemListAdapter.selectedCount)

                    val message = Message.obtain()
                    message.what = MESSAGE_COMPLETE_CONTENTS
                    message.obj = mContext.resources.getString(R.string.message_success_save_contents_in_vocabulary)
                    message.arg1 = Activity.RESULT_OK
                    mMainHandler.sendMessageDelayed(message, Common.DURATION_NORMAL)
                }
                else if(code == Common.COROUTINE_CODE_VOCABULARY_CONTENTS_DELETE)
                {
                    mVocabularyContractView.hideLoading()
                    refreshVocabularyItemData()
                    mVocabularyItemListAdapter.initSelectedData()
                    mVocabularyContractView!!.setBottomPlayItemCount(mVocabularyItemListAdapter.selectedCount)
                    val message = Message.obtain()
                    message.what = MESSAGE_COMPLETE_CONTENTS
                    message.obj = mContext.resources.getString(R.string.message_success_delete_contents)
                    message.arg1 = Activity.RESULT_OK
                    mMainHandler.sendMessageDelayed(message, Common.DURATION_NORMAL)
                }
            }
            else
            {
                if(result.isDuplicateLogin)
                { //중복 로그인 시 재시작
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
                    if(code == Common.COROUTINE_CODE_VOCABULARY_CONTENTS_LIST
                            || code == Common.COROUTINE_CODE_VOCABULARY_SHELF)
                    {
                        mVocabularyContractView!!.hideContentListLoading()
                        Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                        (mContext as AppCompatActivity).onBackPressed()
                    }
                    else if(code == Common.COROUTINE_CODE_VOCABULARY_CONTENTS_ADD)
                    {
                        Log.f("FAIL ASYNC_CODE_VOCABULARY_CONTENTS_ADD")
                        mVocabularyContractView!!.hideLoading()
                        val messsage = Message.obtain()
                        messsage.what = MESSAGE_COMPLETE_CONTENTS
                        messsage.obj = result.getMessage()
                        messsage.arg1 = Activity.RESULT_CANCELED
                        mMainHandler!!.sendMessageDelayed(messsage, Common.DURATION_SHORT)
                    }
                }
            }
        }

        override fun onRunningCanceled(code : String)
        {
        }

        override fun onRunningProgress(code : String, progress : Int)
        {
        }

        override fun onRunningAdvanceInformation(code : String, `object` : Any)
        {
        }

        override fun onErrorListener(code : String, message : String)
        {
        }
    }
    private val mVocabularyItemListener : VocabularyItemListener = object : VocabularyItemListener
    {
        override fun onClickSoundPlay(position : Int)
        {
            Log.f("position : $position")
            mCurrentPlayIndex = position
            startAudio(mVocabularyItemList)
        }

        override fun onItemSelectCount(count : Int)
        {
            Log.f("count : $count")
            mVocabularyContractView!!.setBottomPlayItemCount(count)
        }
    }
    private val mIntervalSelectListener : IntervalSelectListener = object : IntervalSelectListener
    {
        override fun onClickIntervalSecond(second : Int)
        {
            Log.f("second : $second")
            mCurrentIntervalSecond = second
            CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_VOCABULARY_INTERVAL, mCurrentIntervalSecond)
            mVocabularyContractView!!.setBottomIntervalValue(mCurrentIntervalSecond)
        }
    }
    private val mBookAddListener : BookAddListener = object : BookAddListener
    {
        override fun onClickBook(index : Int)
        {
            Log.f("index : $index")
            mCurrentVocabularyAddResult = mMainInformationResult.getVocabulariesList().get(index)
            mRequestItemList.clear()
            mRequestItemList = mVocabularyItemListAdapter.selectedList
            mVocabularyContractView.showLoading()
            requestVocabularyContentsAddAsync()
        }
    }
    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int)
        {
        }

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int)
        {
            if(eventType == DIALOG_EVENT_DELETE_VOCABULARY_CONTENTS)
            {
                if(buttonType == DialogButtonType.BUTTON_2)
                {
                    mVocabularyContractView.showLoading()
                    requestVocabularyContentsDeleteAsync()
                }
            }
        }
    }


}