package com.littlefox.app.foxschool.main.presenter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Message
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.bookshelf.ManagementBooksData
import com.littlefox.app.foxschool.`object`.data.flashcard.FlashcardDataObject
import com.littlefox.app.foxschool.`object`.data.iac.AwakeItemData
import com.littlefox.app.foxschool.`object`.data.quiz.QuizIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.webview.WebviewIntentParamsObject
import com.littlefox.app.foxschool.`object`.result.BookshelfBaseObject
import com.littlefox.app.foxschool.`object`.result.LoginBaseObject
import com.littlefox.app.foxschool.`object`.result.MainInformationBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.main.InAppCompaignResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesBaseResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.adapter.MainFragmentSelectionPagerAdapter
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.coroutine.BookshelfContentAddCoroutine
import com.littlefox.app.foxschool.coroutine.MainInformationCoroutine
import com.littlefox.app.foxschool.dialog.BottomBookAddDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.BookAddListener
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.enumerate.*
import com.littlefox.app.foxschool.fragment.MainMyBooksFragment
import com.littlefox.app.foxschool.fragment.MainSongFragment
import com.littlefox.app.foxschool.fragment.MainStoryFragment
import com.littlefox.app.foxschool.iac.IACController
import com.littlefox.app.foxschool.main.contract.MainContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.app.foxschool.viewmodel.MainMyBooksFragmentDataObserver
import com.littlefox.app.foxschool.viewmodel.MainPresenterDataObserver
import com.littlefox.app.foxschool.viewmodel.MainSongFragmentDataObserver
import com.littlefox.app.foxschool.viewmodel.MainStoryFragmentDataObserver
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log
import java.util.*

class MainPresenter : MainContract.Presenter
{
    companion object
    {
        private const val MESSAGE_START_QUIZ : Int                      = 100
        private const val MESSAGE_SHOW_BOOKSHELF_ADD_ITEM_DIALOG : Int  = 101
        private const val MESSAGE_REQUEST_CONTENTS_ADD : Int            = 102
        private const val MESSAGE_COMPLETE_CONTENTS_ADD : Int           = 103
        private const val MESSAGE_START_FOXSCHOOL_NEWS : Int            = 104
        private const val MESSAGE_START_LOGIN : Int                     = 105
        private const val MESSAGE_START_MY_INFORMATION : Int            = 106
        private const val MESSAGE_START_LEARNING_LOG : Int              = 107
        private const val MESSAGE_START_APP_USE_GUIDE : Int             = 108
        private const val MESSAGE_START_ADD_USER : Int                  = 109
        private const val MESSAGE_START_ORIGIN_TRANSLATE : Int          = 110
        private const val MESSAGE_START_VOCABULARY : Int                = 111
        private const val MESSAGE_START_EBOOK : Int                     = 112
        private const val MESSAGE_START_PUBLISH_SCHEDULE : Int          = 113
        private const val MESSAGE_START_ATTENDANCE : Int                = 114
        private const val MESSAGE_START_1ON1_ASK : Int                  = 115
        private const val MESSAGE_START_FAQ : Int                       = 116
        private const val MESSAGE_START_RESULT_SERIES : Int             = 117
        private const val MESSAGE_START_LOGOUT : Int                    = 118
        private const val MESSAGE_START_GAME_STARWORDS : Int            = 119
        private const val MESSAGE_START_GAME_CROSSWORD : Int            = 120
        private const val MESSAGE_START_FLASHCARD : Int                 = 121
        private const val MESSAGE_START_HOMEWORK : Int                  = 122
        private const val MESSAGE_START_RECORD_HISTORY : Int            = 123
        private const val MESSAGE_APP_SERVER_ERROR : Int                = 124

        private const val DIALOG_EVENT_IAC : Int                        = 10001
        private const val DIALOG_EVENT_LOGOUT : Int                     = 10002
        private const val DIALOG_EVENT_APP_END : Int                    = 10003
        private const val DIALOG_EVENT_NOT_HAVE_CLASS : Int             = 10004
    }

    private lateinit var mMainContractView : MainContract.View
    private lateinit var mMainFragmentSelectionPagerAdapter : MainFragmentSelectionPagerAdapter
    private lateinit var mFragmentList : List<Fragment>
    private lateinit var mMainInformationResult : MainInformationResult
    private var mLoginInformationResult : LoginInformationResult ?= null
    private lateinit var mMainHandler : WeakReferenceHandler
    private lateinit var mBottomBookAddDialog : BottomBookAddDialog
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog
    private lateinit var mCurrentDetailOptionResult : ContentsBaseResult
    private var mBookshelfContentAddCoroutine : BookshelfContentAddCoroutine? = null
    private var mMainInformationCoroutine : MainInformationCoroutine? = null
    private var mManagementBooksData : ManagementBooksData? = null
    private var mCurrentBookshelfAddResult : MyBookshelfResult? = null
    private val mSendBookshelfAddList : ArrayList<ContentsBaseResult?> = ArrayList<ContentsBaseResult?>()
    private var mIACController : IACController? = null
    private lateinit var mAwakeItemData : AwakeItemData
    private lateinit var mMainStoryFragmentDataObserver : MainStoryFragmentDataObserver
    private lateinit var mMainSongFragmentDataObserver : MainSongFragmentDataObserver
    private lateinit var mMainMyBooksFragmentDataObserver : MainMyBooksFragmentDataObserver
    private lateinit var mMainPresenterDataObserver : MainPresenterDataObserver
    private lateinit var mContext : Context

    constructor(context : Context)
    {
        mContext = context
        mMainHandler = WeakReferenceHandler(mContext as MessageHandlerCallback)
        mMainContractView = (mContext as MainContract.View).apply {
            initView()
            initFont()
        }
        Log.f("onCreate")
        init()
    }

    private fun init()
    {
        MainObserver.clearAll()
        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        mMainFragmentSelectionPagerAdapter = MainFragmentSelectionPagerAdapter((mContext as AppCompatActivity).getSupportFragmentManager())
        mMainFragmentSelectionPagerAdapter.addFragment(MainStoryFragment.instance)
        mMainFragmentSelectionPagerAdapter.addFragment(MainSongFragment.instance)
        mMainFragmentSelectionPagerAdapter.addFragment(MainMyBooksFragment.instance)

        mFragmentList = mMainFragmentSelectionPagerAdapter.pagerFragmentList
        mMainContractView.initViewPager(mMainFragmentSelectionPagerAdapter)
        mLoginInformationResult = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult?
        mMainContractView.settingUserInformation(mLoginInformationResult,
            mMainInformationResult.isUpdateHomework,
            mMainInformationResult.isUpdateNews)
        initIACInformation()

        mMainStoryFragmentDataObserver = ViewModelProviders.of(mContext as AppCompatActivity)
            .get(MainStoryFragmentDataObserver::class.java)
        mMainSongFragmentDataObserver = ViewModelProviders.of(mContext as AppCompatActivity)
            .get(MainSongFragmentDataObserver::class.java)
        mMainMyBooksFragmentDataObserver = ViewModelProviders.of(mContext as AppCompatActivity)
            .get(MainMyBooksFragmentDataObserver::class.java)
        mMainPresenterDataObserver = ViewModelProviders.of(mContext as AppCompatActivity)
            .get(MainPresenterDataObserver::class.java)
        setupMainStoryFragmentListener()
        setupMainSongFragmentListener()
        setupMainMyBooksFragmentListener()
        setAppExecuteDate()
    }

    private fun setAppExecuteDate()
    {
        val date : String = CommonUtils.getInstance(mContext).getTodayDateText()
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_APP_EXECUTE_DATE, date)
        Log.f("date : $date")
    }

    private fun notifyDataChangeAllFragment()
    {
        mMainPresenterDataObserver.notifyDataChangeAll(FragmentDataMode.CREATE, mMainInformationResult)
        mMainContractView.settingUserInformation(mLoginInformationResult,
            mMainInformationResult.isUpdateHomework,
            mMainInformationResult.isUpdateNews)
    }

    private fun initIACInformation()
    {
        if(isVisibleIACData)
        {
            Log.f("IAC VISIBLE")
            showIACInformationDialog(mMainInformationResult.getInAppCompaignInformation()!!)
        }
    }

    private val isVisibleIACData : Boolean
        private get()
        {
            var result = false
            try
            {
                if(mMainInformationResult.getInAppCompaignInformation() != null)
                {
                    mIACController = CommonUtils.getInstance(mContext).getPreferenceObject(
                        Common.PARAMS_IAC_CONTROLLER_INFORMATION,
                        IACController::class.java
                    ) as IACController

                    if(mIACController == null)
                    {
                        Log.f("IACController == null")
                        mIACController = IACController()
                    }

                    if(mMainInformationResult.getInAppCompaignInformation()!!.isButton2Use)
                    {
                        if(mMainInformationResult.getInAppCompaignInformation()!!.getButton2Mode()
                                .equals(Common.IAC_AWAKE_CODE_ALWAYS_VISIBLE))
                        {
                            mAwakeItemData = AwakeItemData(
                                mMainInformationResult.getInAppCompaignInformation()!!.getID(),
                                System.currentTimeMillis(),
                                Common.IAC_AWAKE_CODE_ALWAYS_VISIBLE,
                                0
                            )
                        }
                        else if(mMainInformationResult.getInAppCompaignInformation()!!.getButton2Mode()
                                .equals(Common.IAC_AWAKE_CODE_SPECIAL_DATE_VISIBLE))
                        {
                            mAwakeItemData = AwakeItemData(
                                mMainInformationResult.getInAppCompaignInformation()!!.getID(),
                                System.currentTimeMillis(),
                                Common.IAC_AWAKE_CODE_SPECIAL_DATE_VISIBLE,
                                mMainInformationResult.getInAppCompaignInformation()!!.getNotDisplayDays()
                            )
                        }
                        else if(mMainInformationResult.getInAppCompaignInformation()!!.getButton2Mode()
                                .equals(Common.IAC_AWAKE_CODE_ONCE_VISIBLE))
                        {
                            mAwakeItemData = AwakeItemData(
                                mMainInformationResult.getInAppCompaignInformation()!!.getID(),
                                System.currentTimeMillis(),
                                Common.IAC_AWAKE_CODE_ONCE_VISIBLE,
                                0
                            )
                        }
                    }
                    else
                    {
                        mAwakeItemData = AwakeItemData(
                            mMainInformationResult.getInAppCompaignInformation()!!.getID(),
                            System.currentTimeMillis(),
                            Common.IAC_AWAKE_CODE_ONCE_VISIBLE,
                            0
                        )
                    }
                }
                else
                {
                    return false
                }
            } catch(e : NullPointerException)
            {
                return result
            }
            result = mIACController?.isAwake(mAwakeItemData)!!
            return result
        }

    override fun resume()
    {
        Log.f("")
        updateUserInformation()
        updateFragment()
    }

    override fun pause()
    {
        Log.f("")
    }

    override fun destroy()
    {
        Log.f("")
        mBookshelfContentAddCoroutine?.cancel()
        mBookshelfContentAddCoroutine = null
        mMainInformationCoroutine?.cancel()
        mMainInformationCoroutine = null
        mMainHandler.removeCallbacksAndMessages(null)
    }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_START_QUIZ -> startQuizAcitiviy()
            MESSAGE_SHOW_BOOKSHELF_ADD_ITEM_DIALOG -> showBottomBookAddDialog()
            MESSAGE_REQUEST_CONTENTS_ADD ->
            {
                mMainContractView.showLoading()
                requestBookshelfContentsAddAsync(mSendBookshelfAddList)
            }
            MESSAGE_COMPLETE_CONTENTS_ADD ->
            if(msg.arg1 == Activity.RESULT_OK)
            {
                mMainContractView.showSuccessMessage(msg.obj as String)
            }
            else
            {
                mMainContractView.showErrorMessage(msg.obj as String)
            }
            MESSAGE_START_LOGIN -> startLoginActivity()
            MESSAGE_START_MY_INFORMATION -> startMyInformationActivity()
            MESSAGE_START_LEARNING_LOG -> startLearningLogActivity()
            MESSAGE_START_APP_USE_GUIDE -> startAppUseGuideActivity()
            MESSAGE_START_ADD_USER -> startAddUserActivity()
            MESSAGE_START_ORIGIN_TRANSLATE -> startOriginTranslateActivity()
            MESSAGE_START_VOCABULARY -> startVocabularyActivity()
            MESSAGE_START_EBOOK -> startEbookActivity()
            MESSAGE_START_PUBLISH_SCHEDULE -> startWebviewPublishScheduleActivity()
            MESSAGE_START_ATTENDANCE -> startWebviewAttendanceActivity()
            MESSAGE_START_1ON1_ASK -> startInquireActivity()
            MESSAGE_START_FAQ -> startFAQActivity()
            MESSAGE_START_RESULT_SERIES -> startSelectSeriesActivity(msg.obj as String)
            MESSAGE_START_LOGOUT -> showTemplateAlertDialog(
                mContext.resources.getString(R.string.message_try_logout),
                DIALOG_EVENT_LOGOUT,
                DialogButtonType.BUTTON_2
            )
            MESSAGE_START_GAME_STARWORDS -> startGameStarwordsActivity()
            MESSAGE_START_GAME_CROSSWORD -> startGameCrosswordActivity()
            MESSAGE_START_FLASHCARD -> startFlashcardActivity()
            MESSAGE_START_HOMEWORK -> startHomeworkManageActivity()
            MESSAGE_START_FOXSCHOOL_NEWS -> startFoxSchoolNewsActivity()
            MESSAGE_START_RECORD_HISTORY -> startRecordHistoryActivity()
            MESSAGE_APP_SERVER_ERROR ->
            {
                Log.f("== Server Error  ==")
                Toast.makeText(mContext, mContext.resources.getString(R.string.message_warning_app_server_error), Toast.LENGTH_LONG).show()
                (mContext as AppCompatActivity).finish()
                IntentManagementFactory.getInstance().initScene()
            }
        }
    }

    override fun onClickMenuLogin()
    {
        Log.f("")
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_LOGIN, Common.DURATION_SHORT)
    }

    override fun onClickMenuMyInformation()
    {
        Log.f("")
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_MY_INFORMATION, Common.DURATION_SHORT)
    }

    override fun onClickMenuAppUseGuide()
    {
        Log.f("")
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_APP_USE_GUIDE, Common.DURATION_SHORT)
    }

    override fun onClickMenuAddUser()
    {
        Log.f("")
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_ADD_USER, Common.DURATION_SHORT)
    }

    override fun onClickMenu1On1Ask()
    {
        Log.f("")
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_1ON1_ASK, Common.DURATION_SHORT)
    }

    override fun onClickFoxschoolNews()
    {
        Log.f("")
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_FOXSCHOOL_NEWS, Common.DURATION_SHORT)
    }

    override fun onClickMenuFAQ()
    {
        Log.f("")
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_FAQ, Common.DURATION_SHORT)
    }


    override fun onClickMenuPublishSchedule()
    {
        Log.f("")
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_PUBLISH_SCHEDULE, Common.DURATION_SHORT)
    }

    override fun onClickMenuAttendance()
    {
        Log.f("")
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_ATTENDANCE, Common.DURATION_SHORT)
    }


    override fun onClickMenuLogout()
    {
        Log.f("")
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_LOGOUT, Common.DURATION_SHORT)
    }

    override fun onClickMenuLearningLog()
    {
        Log.f("")
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_LEARNING_LOG, Common.DURATION_SHORT)
    }

    override fun onClickRecordHistory()
    {
        Log.f("")
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_RECORD_HISTORY, Common.DURATION_SHORT)
    }

    /**
     * 메뉴 숙제관리 클릭 이벤트
     */
    override fun onClickMenuHomeworkManage()
    {
        Log.f("")
        if (CommonUtils.getInstance(mContext).isTeacherMode == false)
        {
            // 학생
            if(mLoginInformationResult!!.getSchoolInformation().isHaveClass())
            {
                // 학급정보 있는 경우 화면 이동
                mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_HOMEWORK, Common.DURATION_SHORT)
            }
            else
            {
                showTemplateAlertDialog(
                    mContext.resources.getString(R.string.message_warning_not_have_class_student),
                    DIALOG_EVENT_NOT_HAVE_CLASS,
                    DialogButtonType.BUTTON_1
                )
            }
        }
        else
        {
            // 선생님
            if(mLoginInformationResult!!.getUserInformation().isHaveClass() == false)
            {
                // 학급 정보 없을 때
                showTemplateAlertDialog(
                    mContext.resources.getString(R.string.message_warning_not_have_class_teacher),
                    DIALOG_EVENT_NOT_HAVE_CLASS,
                    DialogButtonType.BUTTON_1
                )
            }
            else
            {
                mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_HOMEWORK, Common.DURATION_SHORT)
            }
        }
    }

    override fun onClickMenuTeacherManual()
    {
        Log.f("")
        mMainContractView.showDownloadMessage(mContext.resources.getString(R.string.message_download_teacher_manual))
        CommonUtils.getInstance(mContext).downloadFileToExternalPublicDir(
            mMainInformationResult.getFileInformation()!!.getTeacherManualLink(),
            Common.FILE_TEACHER_MANUAL
        )
    }

    override fun onClickMenuHomeNewsPaper()
    {
        Log.f("")
        mMainContractView.showDownloadMessage(mContext.resources.getString(R.string.message_download_home_newspaper))
        CommonUtils.getInstance(mContext).downloadFileToExternalPublicDir(
            mMainInformationResult.getFileInformation()!!.getHomeNewsPaperLink(),
            Common.FILE_HOME_NEWSPAPER
        )
    }

    override fun onClickSearch()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.SEARCH)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    override fun onBackPressed()
    {
        Log.f("Check End App")
        showTemplateAlertDialog(
            mContext.resources.getString(R.string.message_check_end_app),
            DIALOG_EVENT_APP_END,
            DialogButtonType.BUTTON_2
        )
    }

    private fun startFoxSchoolNewsActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.FOXSCHOOL_NEWS)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startFAQActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.FAQS)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startInquireActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.INQUIRE)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startWebviewPublishScheduleActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_PUBLISH_SCHEDULE)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startWebviewAttendanceActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_ATTENDANCE)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startGameStarwordsActivity()
    {
        Log.f("")
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(mCurrentDetailOptionResult.id)

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_STARWORDS) //  .setData(mCurrentDetailOptionResult.getID())
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startGameCrosswordActivity()
    {
        Log.f("")
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(mCurrentDetailOptionResult.id)

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_CROSSWORD) // .setData(mCurrentDetailOptionResult.getID())
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startFlashcardActivity()
    {
        Log.f("")
        val data = FlashcardDataObject(
            mCurrentDetailOptionResult.id,
            mCurrentDetailOptionResult.name,
            mCurrentDetailOptionResult.sub_name,
            VocabularyType.VOCABULARY_CONTENTS
        )

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.FLASHCARD)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startHomeworkManageActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.HOMEWORK_MANAGE)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startRecordHistoryActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.RECORD_HISTORY)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun showTemplateAlertDialog(message : String, eventType : Int, buttonType : DialogButtonType)
    {
        mTemplateAlertDialog = TemplateAlertDialog(mContext).apply {
            setMessage(message)
            setDialogEventType(eventType)
            setButtonType(buttonType)
            setDialogListener(mDialogListener)
            setGravity(Gravity.LEFT)
            show()
        }
    }

    private fun startSelectSeriesActivity(seriesID : String)
    {
        Log.f("seriesID : $seriesID")
        var result : SeriesBaseResult? = null
        for(i in 0 until mMainInformationResult.getMainStoryInformation().getContentByLevelToList().size)
        {
            if(mMainInformationResult.getMainStoryInformation().getContentByLevelToList().get(i).getDisplayID().equals(seriesID))
            {
                result = mMainInformationResult.getMainStoryInformation().getContentByLevelToList().get(i)
                result.setSeriesType(Common.CONTENT_TYPE_STORY)
                break
            }
        }
        if(result != null)
        {
            for(i in 0 until mMainInformationResult.getMainSongInformationList().size)
            {
                if(mMainInformationResult.getMainSongInformationList().get(i).getDisplayID().equals(seriesID))
                {
                    result = mMainInformationResult.getMainSongInformationList().get(i)
                    result.setSeriesType(Common.CONTENT_TYPE_SONG)
                    break
                }
            }
        }
        if(result == null)
        {
            return
        }
        result.setTransitionType(TransitionType.SLIDE_VIEW)
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.SERIES_DETAIL_LIST)
            .setData(result)
            .setAnimationMode(AnimationMode.METERIAL_ANIMATION)
            .startActivity()
    }


    private fun startQuizAcitiviy()
    {
        Log.f("")
        var quizIntentParamsObject : QuizIntentParamsObject = QuizIntentParamsObject(mCurrentDetailOptionResult.id)
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.QUIZ)
            .setData(quizIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startNewsArticleActivity(articleID : String)
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.FOXSCHOOL_NEWS)
            .setData(articleID)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startLoginActivity()
    {
        Log.f("")
        val isLoginFromMain = true
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.LOGIN)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setData(isLoginFromMain)
            .startActivity()
    }

    private fun startMyInformationActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.MY_INFORMATION)
            .setData("N")
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startLearningLogActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_LEARNING_LOG)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startAddUserActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.MY_INFORMATION)
            .setData("Y")
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startOriginTranslateActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_ORIGIN_TRANSLATE)
            .setData(mCurrentDetailOptionResult.id)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startEbookActivity()
    {
        Log.f("")
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(mCurrentDetailOptionResult.id)

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_EBOOK)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startVocabularyActivity()
    {
        Log.f("")
        val title : String = mCurrentDetailOptionResult.getVocabularyName()
        val myVocabularyResult = MyVocabularyResult(mCurrentDetailOptionResult.id, title, VocabularyType.VOCABULARY_CONTENTS)
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.VOCABULARY)
            .setData(myVocabularyResult)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startAppUseGuideActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.APP_USE_GUIDE)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }


    private fun updateUserInformation()
    {
        Log.f("update Status : " + MainObserver.isUpdateUserStatus())
        if(MainObserver.isUpdateUserStatus())
        {
            mLoginInformationResult = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult
            mMainContractView.settingUserInformation(mLoginInformationResult,
                mMainInformationResult.isUpdateHomework,
                mMainInformationResult.isUpdateNews)

            MainObserver.clearUserStatus()
        }
    }

    private fun updateFragment()
    {
        Log.i("size : " + MainObserver.getUpdatePageList().size)
        if(MainObserver.getUpdatePageList().size > 0)
        {
            mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
            for(page in MainObserver.getUpdatePageList())
            {
                Log.f("update page : $page")
                when(page)
                {
                    Common.PAGE_STORY -> mMainPresenterDataObserver.updateStoryData.setValue(mMainInformationResult)
                    Common.PAGE_SONG -> mMainPresenterDataObserver.updateSongData.setValue(mMainInformationResult)
                    Common.PAGE_MY_BOOKS -> mMainPresenterDataObserver.updateMyBooksData.setValue(mMainInformationResult)
                }
            }
            MainObserver.clearAll()
        }
    }

    private fun showBottomBookAddDialog()
    {
        mBottomBookAddDialog = BottomBookAddDialog(mContext).apply {
            setCancelable(true)
            setBookshelfData(mMainInformationResult.getBookShelvesList())
            setBookSelectListener(mBookAddListener)
            show()
        }

    }

    private fun showIACInformationDialog(result : InAppCompaignResult)
    {
        mTemplateAlertDialog = TemplateAlertDialog(mContext).apply {
            setTitle(result.getTitle())
            setMessage(result.getContent())
            if(result.isButton1Use == false)
            {
                setButtonText(result.getButton2Text())
            } else
            {
                setButtonText(result.getButton1Text(), result.getButton2Text())
            }
            setDialogEventType(DIALOG_EVENT_IAC)
            setDialogListener(mDialogListener)
            show()
        }

    }

    private fun requestBookshelfContentsAddAsync(data : ArrayList<ContentsBaseResult?>)
    {
        Log.f("")
        mBookshelfContentAddCoroutine = BookshelfContentAddCoroutine(mContext).apply {
            setData(mCurrentBookshelfAddResult?.getID(), data)
            asyncListener = mAsyncListener
            execute()
        }
    }


    private fun requestMainInformationAsync()
    {
        Log.f("")
        mMainInformationCoroutine = MainInformationCoroutine(mContext).apply {
            asyncListener = mAsyncListener
            execute()
        }
    }

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



    private fun setupMainStoryFragmentListener()
    {
        mMainStoryFragmentDataObserver.storyLevelsItemData.observe(mContext as AppCompatActivity,
            Observer<Pair<SeriesInformationResult, View>> {seriesInformationResultViewPair ->
                Log.f("onClick StoryLevelsItem")
                val pair = Pair<View, String>(seriesInformationResultViewPair.second, Common.STORY_DETAIL_LIST_HEADER_IMAGE)

                seriesInformationResultViewPair.first.setTransitionType(TransitionType.PAIR_IMAGE)
                seriesInformationResultViewPair.first.setSeriesType(Common.CONTENT_TYPE_STORY)
                IntentManagementFactory.getInstance()
                    .readyActivityMode(ActivityMode.SERIES_DETAIL_LIST)
                    .setData(seriesInformationResultViewPair.first as SeriesBaseResult)
                    .setViewPair(pair)
                    .setAnimationMode(AnimationMode.METERIAL_ANIMATION)
                    .startActivity()
            })

        mMainStoryFragmentDataObserver.storyCategoryItemData.observe(mContext as AppCompatActivity,
            Observer<Pair<SeriesInformationResult, View>> {seriesInformationResultViewPair ->
                Log.f("onClick StoryCategoryItem")
                val pair = Pair<View, String>(seriesInformationResultViewPair.second, Common.CATEGORY_DETAIL_LIST_HEADER_IMAGE)

                seriesInformationResultViewPair.first.setTransitionType(TransitionType.PAIR_IMAGE)
                IntentManagementFactory.getInstance()
                    .readyActivityMode(ActivityMode.STORY_CATEGORY_LIST)
                    .setData(seriesInformationResultViewPair.first as SeriesBaseResult)
                    .setViewPair(pair)
                    .setAnimationMode(AnimationMode.METERIAL_ANIMATION)
                    .startActivity()
            })
    }

    private fun setupMainSongFragmentListener()
    {
        mMainSongFragmentDataObserver.songCategoryItemData.observe(mContext as AppCompatActivity,
            Observer<Pair<SeriesInformationResult, View>> {seriesInformationResultViewPair ->
                Log.f("onClick SongCategoriesItem")
                val pair = Pair<View, String>(seriesInformationResultViewPair.second, Common.STORY_DETAIL_LIST_HEADER_IMAGE)
                seriesInformationResultViewPair.first.setTransitionType(TransitionType.PAIR_IMAGE)
                seriesInformationResultViewPair.first.setSeriesType(Common.CONTENT_TYPE_SONG)
                IntentManagementFactory.getInstance()
                    .readyActivityMode(ActivityMode.SERIES_DETAIL_LIST)
                    .setData(seriesInformationResultViewPair.first as SeriesBaseResult)
                    .setViewPair(pair).setAnimationMode(AnimationMode.METERIAL_ANIMATION)
                    .startActivity()
            })
    }

    private fun setupMainMyBooksFragmentListener()
    {
        mMainMyBooksFragmentDataObserver.addBookshelfData.observe(mContext as AppCompatActivity,
            Observer<Boolean> {
                Log.f("onAddBookshelf")
                if(mMainInformationResult.getBookShelvesList().size > Common.MAX_BOOKSHELF_SIZE)
                {
                    mMainContractView.showErrorMessage(mContext.resources.getString(R.string.message_maximum_bookshelf))
                }
                else
                {
                    mManagementBooksData = ManagementBooksData(MyBooksType.BOOKSHELF_ADD)
                    IntentManagementFactory.getInstance()
                        .readyActivityMode(ActivityMode.MANAGEMENT_MYBOOKS)
                        .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                        .setData(mManagementBooksData)
                        .startActivity()
                }
            })

        mMainMyBooksFragmentDataObserver.addVocabularyData.observe(mContext as AppCompatActivity,
            Observer<Boolean> {
                Log.f("onAddVocabulary")
                if(mMainInformationResult.getVocabulariesList().size > Common.MAX_VOCABULARY_SIZE)
                {
                    mMainContractView.showErrorMessage(mContext.resources.getString(R.string.message_maximum_vocabulary))
                } else
                {
                    mManagementBooksData = ManagementBooksData(MyBooksType.VOCABULARY_ADD)
                    IntentManagementFactory.getInstance()
                        .readyActivityMode(ActivityMode.MANAGEMENT_MYBOOKS)
                        .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                        .setData(mManagementBooksData).startActivity()
                }
            })

        mMainMyBooksFragmentDataObserver.enterBookshelfListData.observe(mContext as AppCompatActivity,
            Observer<Int> {index ->
                Log.f("onEnterBookshelfList : $index")
                if(mMainInformationResult.getBookShelvesList().get(index).getContentsCount() > 0)
                {
                    Log.f("Enter Bookshelf : " + mMainInformationResult.getBookShelvesList().get(index).getName())
                    IntentManagementFactory.getInstance()
                        .readyActivityMode(ActivityMode.BOOKSHELF)
                        .setData(mMainInformationResult.getBookShelvesList().get(index))
                        .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                        .startActivity()
                }
                else
                {
                    Log.f("Empty Bookshelf")
                    mMainContractView.showErrorMessage(mContext.resources.getString(R.string.message_empty_bookshelf_contents))
                }
            })

        mMainMyBooksFragmentDataObserver.enterVocabularyListData.observe(mContext as AppCompatActivity,
            Observer<Int> {index ->
                Log.f("onEnterVocabularyList : $index")
                if(mMainInformationResult.getVocabulariesList().get(index).getWordCount() > 0)
                {
                    Log.f("Enter Vocabulary : " + mMainInformationResult.getVocabulariesList().get(index).getName())
                    mMainInformationResult.getVocabulariesList().get(index)
                        .setVocabularyType(VocabularyType.VOCABULARY_SHELF)
                    IntentManagementFactory.getInstance()
                        .readyActivityMode(ActivityMode.VOCABULARY)
                        .setData(mMainInformationResult.getVocabulariesList().get(index))
                        .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                        .startActivity()
                } else
                {
                    Log.f("Empty Vocabulary")
                    mMainContractView.showErrorMessage(mContext.resources.getString(R.string.message_empty_vocabulary_contents))
                }
            })

        mMainMyBooksFragmentDataObserver.settingBookshelfData.observe(
            mContext as AppCompatActivity,
            Observer<Int> {index ->
                Log.f("onSettingBookshelf : $index")
                val data : MyBookshelfResult = mMainInformationResult.getBookShelvesList().get(index)
                Log.f("ID : " + data.getID().toString() + ", Name : " + data.getName().toString() + ", Color : " + data.getColor())
                mManagementBooksData = ManagementBooksData(data.getID(), data.getName(), data.getColor(), MyBooksType.BOOKSHELF_MODIFY)
                IntentManagementFactory.getInstance()
                    .readyActivityMode(ActivityMode.MANAGEMENT_MYBOOKS)
                    .setData(mManagementBooksData)
                    .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                    .startActivity()
            })

        mMainMyBooksFragmentDataObserver.settingVocabularyData.observe(mContext as AppCompatActivity,
            Observer<Int> {index ->
                Log.f("onSettingVocabulary : $index")
                val data : MyVocabularyResult =
                    mMainInformationResult.getVocabulariesList().get(index)
                Log.f("ID : " + data.getID().toString() + ", Name : " + data.getName().toString() + ", Color : " + data.getColor())
                mManagementBooksData = ManagementBooksData(data.getID(), data.getName(), data.getColor(), MyBooksType.VOCABULARY_MODIFY)
                IntentManagementFactory.getInstance()
                    .readyActivityMode(ActivityMode.MANAGEMENT_MYBOOKS)
                    .setData(mManagementBooksData)
                    .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                    .startActivity()
            })
    }


    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String) {}

        override fun onRunningEnd(code : String, `object` : Any)
        {
            val result : BaseResult? = `object` as BaseResult?

            if(result == null)
            {
                return
            }

            Log.f("code : " + code + ", status : " + result.getStatus())
            if(result.getStatus() === BaseResult.SUCCESS_CODE_OK)
            {
                if(code == Common.COROUTINE_CODE_BOOKSHELF_CONTENTS_ADD)
                {
                    mMainContractView.hideLoading()
                    val myBookshelfResult : MyBookshelfResult = (`object` as BookshelfBaseObject).getData()
                    updateBookshelfData(myBookshelfResult)
                    updateFragment()
                    val messsage = Message.obtain().apply {
                        what = MESSAGE_COMPLETE_CONTENTS_ADD
                        obj = mContext.resources.getString(R.string.message_success_save_contents_in_bookshelf)
                        arg1 = Activity.RESULT_OK
                    }
                    mMainHandler.sendMessageDelayed(messsage, Common.DURATION_NORMAL)
                }
                else if(code == Common.COROUTINE_CODE_ME)
                {
                    mLoginInformationResult = (result as LoginBaseObject).getData()
                    CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_API_INFORMATION, mLoginInformationResult)
                    requestMainInformationAsync()
                }
                else if(code == Common.COROUTINE_CODE_MAIN)
                {
                    mMainContractView.hideLoading()
                    mMainInformationResult = (`object` as MainInformationBaseObject).getData()
                    CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult)
                    notifyDataChangeAllFragment()
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
                    mMainContractView.hideLoading()
                    if(code == Common.COROUTINE_CODE_BOOKSHELF_CONTENTS_ADD)
                    {
                        val message = Message.obtain().apply {
                            what = MESSAGE_COMPLETE_CONTENTS_ADD
                            obj = result.getMessage()
                            arg1 = Activity.RESULT_CANCELED
                        }

                        mMainHandler.sendMessageDelayed(message, Common.DURATION_SHORT)
                    }
                    else if(code == Common.COROUTINE_CODE_MAIN)
                    {
                        Log.f("MAIN ERROR")
                        (mContext as AppCompatActivity).finish()
                        Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    }
                    else
                    {
                        mMainContractView.showErrorMessage(result.getMessage())
                    }
                }
            }
        }

        override fun onRunningCanceled(code : String) {}

        override fun onRunningProgress(code : String, progress : Int) {}

        override fun onRunningAdvanceInformation(code : String, `object` : Any) {}

        override fun onErrorListener(code : String, message : String)
        {
            mMainHandler.sendEmptyMessage(MESSAGE_APP_SERVER_ERROR)
        }
    }

    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int)
        {
            when(eventType)
            {
                DIALOG_EVENT_IAC ->
                {
                    Log.f("IAC Link move")
                    if(mMainInformationResult.getInAppCompaignInformation()?.getButton1Mode().equals(Common.INAPP_CAMPAIGN_MODE_NEWS))
                    {
                        Log.f("새소식 articleID : " + mMainInformationResult.getInAppCompaignInformation()?.getArticleID())
                        startNewsArticleActivity(java.lang.String.valueOf(mMainInformationResult.getInAppCompaignInformation()?.getArticleID()))
                    }
                    else
                    {
                        CommonUtils.getInstance(mContext).startLinkMove(mMainInformationResult.getInAppCompaignInformation()?.getButton1Link())
                        mIACController?.setPositiveButtonClick()
                        CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_IAC_CONTROLLER_INFORMATION, mIACController)
                    }
                }
            }
        }

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int)
        {
            Log.f("eventType : $eventType, buttonType : $buttonType")
            when(eventType)
            {
                DIALOG_EVENT_IAC ->
                if(buttonType == DialogButtonType.BUTTON_1)
                {
                    Log.f("IAC Link move")
                    if(mMainInformationResult.getInAppCompaignInformation()?.getButton1Mode().equals(Common.INAPP_CAMPAIGN_MODE_NEWS))
                    {
                        Log.f("articleID : " + mMainInformationResult.getInAppCompaignInformation()?.getArticleID())
                        startNewsArticleActivity(java.lang.String.valueOf(mMainInformationResult.getInAppCompaignInformation()?.getArticleID()))
                    }
                    else
                    {
                        CommonUtils.getInstance(mContext).startLinkMove(mMainInformationResult.getInAppCompaignInformation()?.getButton1Link())
                        mIACController?.setPositiveButtonClick()
                        CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_IAC_CONTROLLER_INFORMATION, mIACController)
                    }
                }
                else if(buttonType == DialogButtonType.BUTTON_2)
                {
                    Log.f("IAC Cancel")
                    mIACController?.setCloseButtonClick()
                    mIACController?.setSaveIACInformation(mAwakeItemData)
                    CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_IAC_CONTROLLER_INFORMATION, mIACController)
                }
                DIALOG_EVENT_LOGOUT ->
                if(buttonType == DialogButtonType.BUTTON_2)
                {
                    Log.f("============ LOGOUT COMPLETE ============")
                    IntentManagementFactory.getInstance().initScene()
                }
                DIALOG_EVENT_APP_END ->
                if(buttonType == DialogButtonType.BUTTON_2)
                {
                    Log.f("============ APP END ============")
                    (mContext as AppCompatActivity).finish()
                }
            }
        }
    }

    private val mBookAddListener : BookAddListener = object : BookAddListener
    {
        override fun onClickBook(index : Int)
        {
            Log.f("")
            mCurrentBookshelfAddResult = mMainInformationResult.getBookShelvesList().get(index)
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_REQUEST_CONTENTS_ADD, Common.DURATION_SHORT)
        }
    }
}