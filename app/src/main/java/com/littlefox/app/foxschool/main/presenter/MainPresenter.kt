package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.os.Message
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.littlefox.app.foxschool.`object`.data.bookshelf.ManagementBooksData
import com.littlefox.app.foxschool.`object`.result.common.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.login.UserInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.adapter.MainFragmentSelectionPagerAdapter
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.coroutine.AuthMeCoroutine
import com.littlefox.app.foxschool.coroutine.BookshelfContentAddCoroutine
import com.littlefox.app.foxschool.coroutine.ChangeUserCoroutine
import com.littlefox.app.foxschool.coroutine.MainInformationCoroutine
import com.littlefox.app.foxschool.dialog.BottomBookAddDialog
import com.littlefox.app.foxschool.dialog.BottomItemOptionDialog
import com.littlefox.app.foxschool.dialog.TempleteAlertDialog
import com.littlefox.app.foxschool.fragment.MainMyBooksFragment
import com.littlefox.app.foxschool.fragment.MainSongFragment
import com.littlefox.app.foxschool.fragment.MainStoryFragment
import com.littlefox.app.foxschool.main.contract.MainContract
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.logmonitor.Log

import java.util.*

class MainPresenter(private val mContext : Context) : MainContract.Presenter
{
    companion object
    {
        private const val MESSAGE_START_QUIZ : Int                      = 100
        private const val MESSAGE_SHOW_BOOKSHELF_ADD_ITEM_DIALOG : Int  = 101
        private const val MESSAGE_REQUEST_CONTENTS_ADD : Int            = 102
        private const val MESSAGE_COMPLETE_CONTENTS_ADD : Int           = 103
        private const val MESSAGE_START_NEWS : Int                      = 104
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
        private const val MESSAGE_START_PAID : Int                      = 115
        private const val MESSAGE_START_1ON1_ASK : Int                  = 116
        private const val MESSAGE_START_FAQ : Int                       = 117
        private const val MESSAGE_START_PAYMENT_DETAIL : Int            = 118
        private const val MESSAGE_START_RESULT_SERIES : Int             = 119
        private const val MESSAGE_START_LOGOUT : Int                    = 120
        private const val MESSAGE_START_GAME_STARWARS : Int             = 121
        private const val MESSAGE_START_GAME_CROSSWORD : Int            = 122
        private const val MESSAGE_APP_SERVER_ERROR : Int                = 123

        private const val REQUEST_PAYMENT_SUCCESS : Int                 = 1001
        private const val REQUEST_PAYMENT_PAGE : Int                    = 1002
        private const val REQUEST_CODE_GO_LOGIN : Int                   = 1004
        private const val REQUEST_CODE_STUDY_GUIDE : Int                = 1005

        private const val DIALOG_EVENT_IAC : Int                        = 10001
        private const val DIALOG_EVENT_LOGOUT : Int                     = 10002
        private const val DIALOG_EVENT_APP_END : Int                    = 10003
    }


    private lateinit var mMainContractView : MainContract.View
    private lateinit var mMainFragmentSelectionPagerAdapter : MainFragmentSelectionPagerAdapter
    private lateinit var mFragmentList : List<Fragment>
    private lateinit var mMainInformationResult : MainInformationResult
    private lateinit var mUserInformationResult : UserInformationResult
    private lateinit var mMainHandler : WeakReferenceHandler
    private var mBottomItemOptionDialog : BottomItemOptionDialog? = null
    private var mBottomBookAddDialog : BottomBookAddDialog? = null
    private var mTempleteAlertDialog : TempleteAlertDialog? = null
    private var mCurrentDetailOptionResult : ContentsBaseResult? = null
    private var mBookshelfContentAddCoroutine : BookshelfContentAddCoroutine? = null
    private var mMainInformationCoroutine : MainInformationCoroutine? = null
    private var mAuthMeCoroutine : AuthMeCoroutine? = null
    private var mChangeUserCoroutine : ChangeUserCoroutine? = null
    private var mManagementBooksData : ManagementBooksData? = null
    private var mCurrentBookshelfAddResult : MyBookshelfResult? = null
    private val mSendBookshelfAddList : ArrayList<ContentsBaseResult?> = ArrayList<ContentsBaseResult?>()
    private var mIACController : IACController? = null
    private var mAwakeItemData : AwakeItemData? = null
    private var mPaymentBaseObject : PaymentBaseObject? = null
    private var mReceiptData = ""
    private var mMainHomeFragmentDataObserver : MainHomeFragmentDataObserver? = null
    private var mMainStoryFragmentDataObserver : MainStoryFragmentDataObserver? = null
    private var mMainSongFragmentDataObserver : MainSongFragmentDataObserver? = null
    private var mMainMyBooksFragmentDataObserver : MainMyBooksFragmentDataObserver? = null
    private var mMainClassFragmentDataObserver : MainClassFragmentDataObserver? = null
    private var mMainPresenterDataObserver : MainPresenterDataObserver? = null

    private fun init()
    {
        Log.f("IS TABLET : " + Feature.IS_TABLET)
        MainObserver.clearAll()
        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        mMainFragmentSelectionPagerAdapter =
            MainFragmentSelectionPagerAdapter((mContext as AppCompatActivity).getSupportFragmentManager())

        mMainFragmentSelectionPagerAdapter.addFragment(MainStoryFragment.instance)
        mMainFragmentSelectionPagerAdapter.addFragment(MainSongFragment.instance)
        mMainFragmentSelectionPagerAdapter.addFragment(MainMyBooksFragment.instance)

        mFragmentList = mMainFragmentSelectionPagerAdapter.pagerFragmentList
        mMainContractView.initViewPager(mMainFragmentSelectionPagerAdapter)
        mUserInformationResult = CommonUtils.getInstance(mContext).getPreferenceObject(
            Common.PARAMS_USER_API_INFORMATION, UserInformationResult::class.java) as UserInformationResult
        mMainContractView.settingUserInformation(mUserInformationResult)
        initIACInformation()
        initPayment()
        mMainHomeFragmentDataObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(
            MainHomeFragmentDataObserver::class.java
        )
        mMainStoryFragmentDataObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(
            MainStoryFragmentDataObserver::class.java
        )
        mMainSongFragmentDataObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(
            MainSongFragmentDataObserver::class.java
        )
        mMainMyBooksFragmentDataObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(
            MainMyBooksFragmentDataObserver::class.java
        )
        mMainClassFragmentDataObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(
            MainClassFragmentDataObserver::class.java
        )
        mMainPresenterDataObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(
            MainPresenterDataObserver::class.java
        )
        setupMainHomeFragmentListener()
        setupMainStoryFragmentListener()
        setupMainSongFragmentListener()
        setupMainMyBooksFragmentListener()
        setupMainClassFragmentListener()
        setAppExecuteDate()
    }

    private fun setAppExecuteDate()
    {
        val date : String = CommonUtils.getInstance(mContext).getTodayDate()
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_APP_EXECUTE_DATE, date)
        Log.f("date : $date")
    }

    private fun notifyDataChangeAllFragment()
    {
        mMainPresenterDataObserver.notifyDataChangeAll(
            FragmentDataMode.CREATE,
            mMainInformationResult
        )
        mMainContractView.settingUserInformation(mUserInformationResult)
    }

    private fun initIACInformation()
    {
        if(isVisibleIACData)
        {
            Log.f("IAC VISIBLE")
            showIACInformationDialog(mMainInformationResult.getInAppCompaignInformation())
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
                    if(mMainInformationResult.getInAppCompaignInformation().isButton2Use())
                    {
                        if(mMainInformationResult.getInAppCompaignInformation().getButton2Mode()
                                .equals(Common.IAC_AWAKE_CODE_ALWAYS_VISIBLE)
                        )
                        {
                            mAwakeItemData = AwakeItemData(
                                mMainInformationResult.getInAppCompaignInformation().getID(),
                                System.currentTimeMillis(),
                                Common.IAC_AWAKE_CODE_ALWAYS_VISIBLE,
                                0
                            )
                        } else if(mMainInformationResult.getInAppCompaignInformation()
                                .getButton2Mode().equals(Common.IAC_AWAKE_CODE_SPECIAL_DATE_VISIBLE)
                        )
                        {
                            mAwakeItemData = AwakeItemData(
                                mMainInformationResult.getInAppCompaignInformation().getID(),
                                System.currentTimeMillis(),
                                Common.IAC_AWAKE_CODE_SPECIAL_DATE_VISIBLE,
                                mMainInformationResult.getInAppCompaignInformation()
                                    .getNotDisplayDays()
                            )
                        } else if(mMainInformationResult.getInAppCompaignInformation()
                                .getButton2Mode().equals(Common.IAC_AWAKE_CODE_ONCE_VISIBLE)
                        )
                        {
                            mAwakeItemData = AwakeItemData(
                                mMainInformationResult.getInAppCompaignInformation().getID(),
                                System.currentTimeMillis(),
                                Common.IAC_AWAKE_CODE_ONCE_VISIBLE,
                                0
                            )
                        }
                    } else
                    {
                        mAwakeItemData = AwakeItemData(
                            mMainInformationResult.getInAppCompaignInformation().getID(),
                            System.currentTimeMillis(),
                            Common.IAC_AWAKE_CODE_ONCE_VISIBLE,
                            0
                        )
                    }
                } else
                {
                    return false
                }
            } catch(e : NullPointerException)
            {
                return result
            }
            result = mIACController.isAwake(mAwakeItemData)
            return result
        }

    fun resume()
    {
        Log.f("")
        updateUserInformation()
        updateFragment()
        checkToGoPayment()
    }

    fun pause()
    {
        Log.f("")
    }

    fun destroy()
    {
        Log.f("")
        if(mBookshelfContentAddCoroutine != null)
        {
            mBookshelfContentAddCoroutine.cancel()
            mBookshelfContentAddCoroutine = null
        }
        if(mChangeUserCoroutine != null)
        {
            mChangeUserCoroutine.cancel()
            mChangeUserCoroutine = null
        }
        if(mMainInformationCoroutine != null)
        {
            mMainInformationCoroutine.cancel()
            mMainInformationCoroutine = null
        }
        if(mInAppBillingCoroutine != null)
        {
            mInAppBillingCoroutine.cancel()
            mInAppBillingCoroutine = null
        }
        mMainHandler.removeCallbacksAndMessages(null)
    }

    fun acvitityResult(requestCode : Int, resultCode : Int, data : Intent)
    {
        Log.f("requestCode : $requestCode, resultCode : $resultCode")
        when(requestCode)
        {
            REQUEST_PAYMENT_SUCCESS -> if(resultCode == Activity.RESULT_OK)
            {
                mMainContractView.showLoading()
                requestAuthMeAsync()
            } else if(resultCode == Common.RESULT_CODE_REGISTER_COUPON_FROM_REMAIN_DAY_END_USER)
            {
                onClickMenuMyInformation()
            }
            REQUEST_PAYMENT_PAGE -> if(resultCode == Common.RESULT_CODE_PAYMENT_PAGE)
            {
                enterPaymentPage()
            }
            REQUEST_CODE_GO_LOGIN -> if(resultCode == Activity.RESULT_OK)
            {
                mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_LOGIN, Common.DURATION_SHORT)
            }
            REQUEST_CODE_STUDY_GUIDE -> if(resultCode == Common.RESULT_CODE_SERIES_LIST)
            {
                val message = Message.obtain()
                message.what = MESSAGE_START_RESULT_SERIES
                message.obj = data.getStringExtra(Common.INTENT_RESULT_SERIES_ID)
                mMainHandler.sendMessageDelayed(message, Common.DURATION_SHORT)
            }
        }
    }

    fun sendMessageEvent(msg : Message)
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
            MESSAGE_COMPLETE_CONTENTS_ADD -> if(msg.arg1 == Activity.RESULT_OK)
            {
                mMainContractView.showSuccessMessage(msg.obj as String)
            } else
            {
                mMainContractView.showErrorMessage(msg.obj as String)
            }
            MESSAGE_START_NEWS -> startNewsActivity()
            MESSAGE_START_LOGIN -> startLoginActivity()
            MESSAGE_START_TESTIMONIAL -> startTestimonialActivity()
            MESSAGE_START_MY_INFORMATION -> startMyInformationActivity()
            MESSAGE_START_LEARNING_LOG -> startLearningLogActivity()
            MESSAGE_START_APP_USE_GUIDE -> startAppUseGuideActivity()
            MESSAGE_START_ADD_USER -> startAddUserActivity()
            MESSAGE_START_ORIGIN_TRANSLATE -> startOriginTranslateActivity()
            MESSAGE_START_VOCABULARY -> startVocabularyActivity()
            MESSAGE_START_EBOOK -> startEbookActivity()
            MESSAGE_START_PUBLISH_SCHEDULE -> startWebviewPublishScheduleActivity()
            MESSAGE_START_ATTENDANCE -> startWebviewAttendanceActivity()
            MESSAGE_START_PAID -> startPaymentActivity(msg.obj as PaymentType)
            MESSAGE_START_1ON1_ASK -> startWebview1On1AskActivity()
            MESSAGE_START_FAQ -> startWebviewFAQActivity()
            MESSAGE_START_PAYMENT_DETAIL -> if(Feature.IS_REMAIN_DAY_END_USER)
            {
                Log.f("IS_REMAIN_DAY_END_USER Pay Page ----->")
                if(Feature.IS_CHINESE_MODEL)
                {
                    return
                }
                startPaymentActivity(PaymentType.ONLY_PAY)
            } else
            {
                val url : String =
                    (mUserInformationResult.getMobileUrlPrefix() + Common.SUFFIX_MOBILE_DETAIL_PAYMENT_HEADER).toString() + "?token=" + CommonUtils.getInstance(
                        mContext
                    ).getSharedPreference(Common.PARAMS_ACCESS_TOKEN, Common.TYPE_PARAMS_STRING)
                        .toString() + "&redirect_url=" + mUserInformationResult.getMobileUrlPrefix() + Common.SUFFIX_MOBILE_WEB_DETAIL_PAYMENT_REDIRECT_URL
                Log.f("MOBILE_WEB_LINK_DETAIL_PAYMENT ----> $url")
                CommonUtils.getInstance(mContext).startLinkMove(url)
            }
            MESSAGE_START_RESULT_SERIES -> startSelectSeriesActivity(msg.obj as String)
            MESSAGE_START_LOGOUT -> showTempleteAlertDialog(
                mContext.resources.getString(R.string.message_try_logout),
                DIALOG_EVENT_LOGOUT,
                TempleteAlertDialog.DEFAULT_BUTTON_TYPE_2
            )
            MESSAGE_START_RESTORE -> executeRestoreStatus(true)
            MESSAGE_START_STORE -> startGoToStoreWebPage()
            MESSAGE_START_GAME_STARWARS -> startGameStarwarsActivity()
            MESSAGE_START_GAME_CROSSWORD -> startGameCrosswordActivity()
            MESSAGE_START_CLASS -> startClassActivity()
            MESSAGE_APP_SERVER_ERROR ->
            {
                Log.f("== Server Error  ==")
                Toast.makeText(
                    mContext,
                    mContext.resources.getString(R.string.message_warning_app_server_error),
                    Toast.LENGTH_LONG
                ).show()
                (mContext as AppCompatActivity).finish()
                IntentManagementFactory.getInstance().initScene()
            }
        }
    }

    fun changeUser(index : Int)
    {
        Log.f("change ID : " + mUserInformationResult.getUserInformationList().get(index).getID())
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_TOP_MENU,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            Common.ANALYTICS_LABEL_MODIFIED_USER
        )
        mMainContractView.showLoading()
        requestChangeUserAsync(mUserInformationResult.getUserInformationList().get(index).getID())
    }

    fun onClickMenuLogin()
    {
        Log.f("")
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_TOP_MENU,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            Common.ANALYTICS_LABEL_LOGIN
        )
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_LOGIN, Common.DURATION_SHORT)
    }

    fun onClickPaidSignIn()
    {
        Log.f("")
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_TOP_MENU,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            Common.ANALYTICS_LABEL_USER_SIGN
        )
        val message = Message.obtain()
        message.what = MESSAGE_START_PAID
        message.obj = PaymentType.SIGN_AND_PAY
        mMainHandler.sendMessageDelayed(message, Common.DURATION_SHORT)
    }

    fun onClickMenuNews()
    {
        Log.f("")
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_TOP_MENU,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            Common.ANALYTICS_LABEL_NEWS
        )
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_NEWS, Common.DURATION_SHORT)
    }

    fun onClickMenuTestimonial()
    {
        Log.f("")
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_TOP_MENU,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            Common.ANALYTICS_LABEL_TESTIMONIAL
        )
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_TESTIMONIAL, Common.DURATION_SHORT)
    }

    fun onClickMenuMyInformation()
    {
        Log.f("")
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_TOP_MENU,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            Common.ANALYTICS_LABEL_MY_INFORMATION
        )
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_MY_INFORMATION, Common.DURATION_SHORT)
    }

    fun onClickMenuAppUseGuide()
    {
        Log.f("")
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_TOP_MENU,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            Common.ANALYTICS_LABEL_APP_USE_GUIDE
        )
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_APP_USE_GUIDE, Common.DURATION_SHORT)
    }

    fun onClickMenuAddUser()
    {
        Log.f("")
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_TOP_MENU,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            Common.ANALYTICS_LABEL_ADD_USER
        )
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_ADD_USER, Common.DURATION_SHORT)
    }

    fun onClickMenu1On1Ask()
    {
        Log.f("")
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_TOP_MENU,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            Common.ANALYTICS_LABEL_1_ON_1
        )
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_1ON1_ASK, Common.DURATION_SHORT)
    }

    fun onClickMenuFAQ()
    {
        Log.f("")
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_TOP_MENU,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            Common.ANALYTICS_LABEL_FAQ
        )
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_FAQ, Common.DURATION_SHORT)
    }

    fun onClickMenuDetailPaymentInformation()
    {
        Log.f("")
        if(Feature.IS_REMAIN_DAY_END_USER)
        {
            GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_TOP_MENU,
                Common.ANALYTICS_ACTION_SELECT_MENU,
                Common.ANALYTICS_LABEL_PAYMENT
            )
        } else
        {
            GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_TOP_MENU,
                Common.ANALYTICS_ACTION_SELECT_MENU,
                Common.ANALYTICS_LABEL_DETAIL_VIEW
            )
        }
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_PAYMENT_DETAIL, Common.DURATION_SHORT)
    }

    fun onClickMenuPublishSchedule()
    {
        Log.f("")
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_TOP_MENU,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            Common.ANALYTICS_LABEL_SCHEDULE
        )
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_PUBLISH_SCHEDULE, Common.DURATION_SHORT)
    }

    fun onClickMenuAttendance()
    {
        Log.f("")
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_TOP_MENU,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            Common.ANALYTICS_LABEL_ATTENDANCE
        )
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_ATTENDANCE, Common.DURATION_SHORT)
    }

    fun onClickMenuRestore()
    {
        Log.f("")
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_TOP_MENU,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            Common.ANALYTICS_LABEL_RESTORE
        )
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_RESTORE, Common.DURATION_SHORT)
    }

    fun onClickMenuStore()
    {
        Log.f("")
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_STORE, Common.DURATION_SHORT)
    }

    fun onClickMenuLogout()
    {
        Log.f("")
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_TOP_MENU,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            Common.ANALYTICS_LABEL_LOGOUT
        )
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_LOGOUT, Common.DURATION_SHORT)
    }

    fun onClickMenuLearningLog()
    {
        Log.f("")
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_TOP_MENU,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            Common.ANALYTICS_LABEL_LEARNING_LOG
        )
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_LEARNING_LOG, Common.DURATION_SHORT)
    }

    fun onClickMenuClass()
    {
        Log.f("")
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
            Common.ANALYTICS_CATEGORY_TOP_MENU,
            Common.ANALYTICS_ACTION_SELECT_MENU,
            Common.ANALYTICS_LABEL_CLASS
        )
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_CLASS, Common.DURATION_SHORT)
    }

    fun onClickSearch()
    {
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.SEARCH)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    fun onBackPressed()
    {
        Log.f("Check End App")
        showTempleteAlertDialog(
            mContext.resources.getString(R.string.message_check_end_app),
            DIALOG_EVENT_APP_END,
            TempleteAlertDialog.DEFAULT_BUTTON_TYPE_2
        )
    }

    private fun initPayment()
    {
        mBillingClientHelper = BillingClientHelper.getInstance()
        setUpInAppPurchaseListener()
        mBillingClientHelper.init(mContext)
    }

    private fun setUpInAppPurchaseListener()
    {
        mBillingClientHelper.setOnBillingClientListener(object : IBillingClientListener()
        {
            fun onSkuDetailQueryFinished()
            {
            }

            fun onCheckPurchaseItem()
            {
                Log.f("")
                executeRestoreStatus(false)
            }

            fun onPurchaseComplete(purchaseItem : Purchase?)
            {
            }

            fun onConsumeComplete(billingResult : BillingResult, purchaseToken : String)
            {
                Log.f("response Code : " + billingResult.getResponseCode() + ", purchaseToken : " + purchaseToken)
            }

            fun inFailure(status : Int, reason : String?)
            {
                mMainContractView.showErrorMessage(reason)
            }
        })
    }

    private fun startWebviewFAQActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.WEBVIEW_FAQS)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startWebview1On1AskActivity()
    {
        Log.f("")
        if(LittlefoxLocale.getInstance().getCurrentLocale()
                .contains(Locale.KOREA.toString()) && Feature.IS_FREE_USER === false
        )
        {
            IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.WEBVIEW_1ON1_ASK)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
        } else
        {
            CommonUtils.getInstance(mContext).inquireForDeveloper(Common.DEVELOPER_EMAIL)
        }
    }

    private fun startWebviewPublishScheduleActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_PUBLISH_SCHEDULE)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startWebviewAttendanceActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.WEBVIEW_ATTENDANCE)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startGameStarwarsActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_STARWORDS) //  .setData(mCurrentDetailOptionResult.getID())
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startGameCrosswordActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_CROSSWORD) // .setData(mCurrentDetailOptionResult.getID())
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startClassActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.CLASS_MAIN)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startGoToStoreWebPage()
    {
        Log.f("")
        CommonUtils.getInstance(mContext).startLinkMove(Common.STORE_LINK)
    }

    private fun executeRestoreStatus(showMessage : Boolean)
    {
        mPaymentBaseObject = CommonUtils.getInstance(mContext).getPreferenceObject(
            Common.PARAMS_IN_APP_ITEM_INFORMATION,
            PaymentBaseObject::class.java
        ) as PaymentBaseObject
        mReceiptData = CommonUtils.getInstance(mContext)
            .getSharedPreferenceString(Common.PARAMS_IN_APP_ITEM_RECEIPT, "")
        if(mPaymentBaseObject == null)
        {
            if(showMessage)
            {
                Log.f("Restore Not Need User.")
                mMainContractView.showErrorMessage(mContext.resources.getString(R.string.message_not_have_payment_history))
            }
        } else
        {
            if(Feature.IS_FREE_USER === false)
            {
                if(Feature.IS_REMAIN_DAY_END_USER)
                {
                    mMainContractView.showLoading()
                    Log.f("================ Restore User =============")
                    requestInAppInformationAsync()
                } else
                {
                    consumePurchaseData()
                }
            } else
            {
                if(showMessage)
                {
                    Log.f("Restore Not Need User.")
                    mMainContractView.showErrorMessage(mContext.resources.getString(R.string.message_not_have_payment_history))
                }
            }
        }
    }

    private fun consumePurchaseData()
    {
        val monthItem : Purchase =
            mBillingClientHelper.getPurchasedItemResult(BillingClientHelper.IN_APP_1_MONTH)
        if(monthItem != null)
        {
            Log.f("====== consume Item ========")
            Log.f("item data : " + monthItem.getOriginalJson())
            CommonUtils.getInstance(mContext)
                .setPreferenceObject(Common.PARAMS_IN_APP_ITEM_INFORMATION, null)
            CommonUtils.getInstance(mContext)
                .setSharedPreference(Common.PARAMS_IN_APP_ITEM_RECEIPT, "")
            mPaymentBaseObject = null
            mReceiptData = ""
            mBillingClientHelper.consumeItem(monthItem)
        }
    }

    private fun requestInAppInformationAsync()
    {
        Log.f("")
        mInAppBillingCoroutine = InAppBillingCoroutine(mContext)
        mInAppBillingCoroutine.setData(mPaymentBaseObject.getData(), mReceiptData)
        mInAppBillingCoroutine.setAsyncListener(mAsyncListener)
        mInAppBillingCoroutine.execute()
    }

    private fun showTempleteAlertDialog(message : String, eventType : Int, buttonType : Int)
    {
        mTempleteAlertDialog = TempleteAlertDialog(mContext)
        mTempleteAlertDialog.setMessage(message)
        mTempleteAlertDialog.setDialogEventType(eventType)
        mTempleteAlertDialog.setButtonType(buttonType)
        mTempleteAlertDialog.setDialogListener(mDialogListener)
        mTempleteAlertDialog.setGravity(Gravity.LEFT)
        mTempleteAlertDialog.show()
    }

    private fun startSelectSeriesActivity(seriesID : String)
    {
        Log.f("seriesID : $seriesID")
        var result : SeriesBaseResult? = null
        for(i in 0 until mMainInformationResult.getMainStoryInformation().getContentByLevelToList()
            .size())
        {
            if(mMainInformationResult.getMainStoryInformation().getContentByLevelToList().get(i)
                    .getDisplayId().equals(seriesID)
            )
            {
                result = mMainInformationResult.getMainStoryInformation().getContentByLevelToList()
                    .get(i)
                result.setSeriesType(Common.CONTENT_TYPE_STORY)
                break
            }
        }
        if(result != null)
        {
            for(i in 0 until mMainInformationResult.getMainSongInformation()
                .getContentByCategoriesToList().size())
            {
                if(mMainInformationResult.getMainSongInformation().getContentByCategoriesToList()
                        .get(i).getDisplayId().equals(seriesID)
                )
                {
                    result = mMainInformationResult.getMainSongInformation()
                        .getContentByCategoriesToList().get(i)
                    result.setSeriesType(Common.CONTENT_TYPE_SONG)
                    break
                }
            }
        }
        if(result == null)
        {
            return
        }
        result.setTransitionType(Common.TRANSITION_SLIDE_VIEW)
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.SERIES_DETAIL_LIST)
            .setData(result).setAnimationMode(AnimationMode.METERIAL_ANIMATION).startActivity()
    }

    private fun startCurrentSelectMovieActivity(result : ContentsBaseResult)
    {
        Log.f("result ID : " + result.getID().toString() + ", Name : " + result.getName())
        val sendItemList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
        sendItemList.add(result)
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.PLAYER)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).setData(sendItemList).startActivity()
    }

    private fun startQuizAcitiviy()
    {
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.QUIZ)
            .setData(mCurrentDetailOptionResult.getID())
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startNewsActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.NEWS)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startTestimonialActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.TESTIMONIAL)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startNewsArticleActivity(articleID : String)
    {
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.NEWS)
            .setData(articleID).setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startTestimonialArticleActivity(articleID : String)
    {
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.TESTIMONIAL)
            .setData(articleID).setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startLoginActivity()
    {
        Log.f("")
        val isLoginFromMain = true
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.LOGIN)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).setData(isLoginFromMain)
            .startActivity()
    }

    private fun startPaymentActivity(type : PaymentType)
    {
        Log.f("type : $type")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.PAYMENT)
            .setRequestCode(if(type === PaymentType.SIGN_AND_PAY) REQUEST_CODE_GO_LOGIN else REQUEST_PAYMENT_SUCCESS)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).setData(type).startActivity()
    }

    private fun startMyInformationActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.MY_INFORMATION)
            .setData("N").setRequestCode(REQUEST_PAYMENT_PAGE)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startLearningLogActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.WEBVIEW_LEARNING_LOG)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startAddUserActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.MY_INFORMATION)
            .setData("Y").setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startOriginTranslateActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_ORIGIN_TRANSLATE)
            .setData(mCurrentDetailOptionResult.getID())
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startEbookActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.WEBVIEW_EBOOK)
            .setData(mCurrentDetailOptionResult.getID())
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startVocabularyActivity()
    {
        Log.f("")
        var title = ""
        title = CommonUtils.getInstance(mContext).getVocabularyTitleName(mCurrentDetailOptionResult)
        val myVocabularyResult = MyVocabularyResult(
            mCurrentDetailOptionResult.getID(), title, VocabularyType.VOCABULARY_CONTENTS
        )
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.VOCABULARY)
            .setData(myVocabularyResult).setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startAppUseGuideActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.APP_USE_GUIDE)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun showBottomItemOptionDialog(result : ContentsBaseResult?)
    {
        mBottomItemOptionDialog = BottomItemOptionDialog(mContext)
        mBottomItemOptionDialog.setFullName().setData(result)
            .setItemOptionListener(mItemOptionListener).setView()
        mBottomItemOptionDialog.show()
    }

    private fun updateUserInformation()
    {
        Log.f("update Status : " + MainObserver.getInstance().isUpdateUserStatus())
        if(MainObserver.getInstance().isUpdateUserStatus())
        {
            mUserInformationResult = CommonUtils.getInstance(mContext).getPreferenceObject(
                Common.PARAMS_USER_API_INFORMATION,
                UserInformationResult::class.java
            ) as UserInformationResult
            mMainContractView.settingUserInformation(mUserInformationResult)
            MainObserver.getInstance().clearUserStatus()
        }
    }

    private fun updateFragment()
    {
        Log.i("size : " + MainObserver.getInstance().getUpdatePageList().size())
        if(MainObserver.getInstance().getUpdatePageList().size() > 0)
        {
            mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
            val result : Pair<FragmentDataMode, MainInformationResult?> =
                Pair<FragmentDataMode, MainInformationResult?>(
                    FragmentDataMode.UPDATE,
                    mMainInformationResult
                )
            for(page in MainObserver.getInstance().getUpdatePageList())
            { // ((MainCallback)mFragmentList.get(page)).updateData(FragmentDataMode.UPDATE, mMainInformationResult);
                Log.f("update page : $page")
                when(page)
                {
                    Common.PAGE_HOME -> mMainPresenterDataObserver.updateHomeData.setValue(result)
                    Common.PAGE_STORY -> mMainPresenterDataObserver.updateStoryData.setValue(
                        mMainInformationResult
                    )
                    Common.PAGE_SONG -> mMainPresenterDataObserver.updateSongData.setValue(
                        mMainInformationResult
                    )
                    Common.PAGE_MY_BOOKS -> mMainPresenterDataObserver.updateMyBooksData.setValue(
                        mMainInformationResult
                    )
                    Common.PAGE_CLASS -> mMainPresenterDataObserver.updateClassData.setValue(
                        mMainInformationResult
                    )
                }
            }
            MainObserver.getInstance().clearAll()
        }
    }

    private fun checkToGoPayment()
    {
        Log.f("")
        if(MainObserver.getInstance().isEnterPaymentPage())
        {
            Log.f("미리보기 후 사용자가 회비 결제 버튼을 눌렀다.!")
            MainObserver.getInstance().clearEnterPaymentPage()
            enterPaymentPage()
        }
    }

    private fun enterPaymentPage()
    {
        Log.f("")
        val message = Message.obtain()
        message.what = MESSAGE_START_PAID
        if(Feature.IS_FREE_USER)
        {
            message.obj = PaymentType.SIGN_AND_PAY
        } else
        {
            message.obj = PaymentType.ONLY_PAY
        }
        mMainHandler.sendMessageDelayed(message, Common.DURATION_NORMAL)
    }

    private fun showBottomBookAddDialog()
    {
        mBottomBookAddDialog = BottomBookAddDialog(mContext)
        mBottomBookAddDialog.setCancelable(true)
        mBottomBookAddDialog.setBookshelfData(mMainInformationResult.getBookShelvesList())
        mBottomBookAddDialog.setBookSelectListener(mBookAddListener)
        mBottomBookAddDialog.show()
    }

    private fun showIACInformationDialog(result : InAppCompaignResult)
    {
        mTempleteAlertDialog = TempleteAlertDialog(mContext)
        mTempleteAlertDialog.setTitle(result.getTitle())
        mTempleteAlertDialog.setMessage(result.getContent())
        if(result.isButton1Use() === false)
        {
            mTempleteAlertDialog.setButtonText(result.getButton2Text())
        } else
        {
            mTempleteAlertDialog.setButtonText(result.getButton1Text(), result.getButton2Text())
        }
        mTempleteAlertDialog.setDialogEventType(DIALOG_EVENT_IAC)
        mTempleteAlertDialog.setDialogListener(mDialogListener)
        mTempleteAlertDialog.show()
    }

    private fun requestBookshelfContentsAddAsync(data : ArrayList<ContentsBaseResult?>)
    {
        Log.f("")
        mBookshelfContentAddCoroutine = BookshelfContentAddCoroutine(mContext)
        mBookshelfContentAddCoroutine.setData(
            mCurrentBookshelfAddResult.getID(), data
        )
        mBookshelfContentAddCoroutine.setAsyncListener(mAsyncListener)
        mBookshelfContentAddCoroutine.execute()
    }

    private fun requestAuthMeAsync()
    {
        Log.f("")
        mAuthMeCoroutine = AuthMeCoroutine(mContext)
        mAuthMeCoroutine.setAsyncListener(mAsyncListener)
        mAuthMeCoroutine.execute()
    }

    private fun requestChangeUserAsync(changeUserID : String)
    {
        Log.f("changeUserID : $changeUserID")
        mChangeUserCoroutine = ChangeUserCoroutine(mContext)
        mChangeUserCoroutine.setData(changeUserID)
        mChangeUserCoroutine.setAsyncListener(mAsyncListener)
        mChangeUserCoroutine.execute()
    }

    private fun requestMainInformationAsync()
    {
        Log.f("")
        mMainInformationCoroutine = MainInformationCoroutine(mContext)
        mMainInformationCoroutine.setAsyncListener(mAsyncListener)
        mMainInformationCoroutine.execute()
    }

    private fun updateBookshelfData(result : MyBookshelfResult)
    {
        for(i in 0 until mMainInformationResult.getBookShelvesList().size())
        {
            if(mMainInformationResult.getBookShelvesList().get(i).getID().equals(result.getID()))
            {
                Log.f("update Index :$i")
                mMainInformationResult.getBookShelvesList().set(i, result)
            }
        }
        CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult)
        MainObserver.getInstance().updatePage(Common.PAGE_MY_BOOKS)
    }

    private val sortFreeSeriesList : ArrayList<Any>
        private get()
        {
            val result : ArrayList<SeriesInformationResult> =
                mMainInformationResult.getMainStoryInformation().getContentByLevelToList()
            Collections.sort(result, Comparator<Any> {s0, s1 ->
                if(s0.getFreeSeriesSortNumber() < s1.getFreeSeriesSortNumber())
                {
                    -1
                } else if(s0.getFreeSeriesSortNumber() > s1.getFreeSeriesSortNumber())
                {
                    1
                } else
                {
                    0
                }
            })
            return result
        }
    private val sortFreeSingleList : ArrayList<Any>
        private get()
        {
            val result : ArrayList<SeriesInformationResult> =
                mMainInformationResult.getMainStoryInformation().getContentByLevelToList()
            Collections.sort(result, Comparator<Any> {s0, s1 ->
                if(s0.getFreeSingleSortNumber() < s1.getFreeSingleSortNumber())
                {
                    -1
                } else if(s0.getFreeSingleSortNumber() > s1.getFreeSingleSortNumber())
                {
                    1
                } else
                {
                    0
                }
            })
            return result
        }

    private fun setupMainHomeFragmentListener()
    {
        mMainHomeFragmentDataObserver.playMovieData.observe(
            mContext as AppCompatActivity,
            Observer<Any> {contentsBaseResult ->
                Log.f("onClick HomePlayMovie")
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                    Common.ANALYTICS_CATEGORY_HOME,
                    Common.ANALYTICS_ACTION_PLAY,
                    CommonUtils.getInstance(mContext).getContentsName(contentsBaseResult)
                )
                startCurrentSelectMovieActivity(contentsBaseResult)
            })
        mMainHomeFragmentDataObserver.itemOptionData.observe(
            mContext as AppCompatActivity,
            Observer<Any?> {contentsBaseResult ->
                Log.f("onClick HomeItemOption")
                mCurrentDetailOptionResult = contentsBaseResult
                showBottomItemOptionDialog(contentsBaseResult)
            })
        mMainHomeFragmentDataObserver.freeSeriesStoryData.observe(
            mContext as AppCompatActivity,
            Observer<Boolean?> {
                Log.f("onClick HomeFreeSeriesStory")
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                    Common.ANALYTICS_CATEGORY_HOME,
                    Common.ANALYTICS_ACTION_SELECT_MENU,
                    Common.ANALYTICS_LABEL_FREE_SERIES_STORY
                )
                val sortList : ArrayList<SeriesInformationResult> = sortFreeSeriesList
                mMainInformationResult.getMainStoryInformation().setContentByLevelToList(sortList)
                CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult)
                MainObserver.getInstance().updatePage(Common.PAGE_STORY)
                updateFragment()
                mMainContractView.setCurrentPage(Common.PAGE_STORY)
            })
        mMainHomeFragmentDataObserver.freeSingleStoryData.observe(
            mContext as AppCompatActivity,
            Observer<Boolean?> {
                Log.f("onClick HomeFreeSingleStory")
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                    Common.ANALYTICS_CATEGORY_HOME,
                    Common.ANALYTICS_ACTION_SELECT_MENU,
                    Common.ANALYTICS_LABEL_FREE_SINGLE_STORY
                )
                val sortList : ArrayList<SeriesInformationResult> = sortFreeSingleList
                mMainInformationResult.getMainStoryInformation().setContentByLevelToList(sortList)
                CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult)
                MainObserver.getInstance().updatePage(Common.PAGE_STORY)
                updateFragment()
                mMainContractView.setCurrentPage(Common.PAGE_STORY)
            })
        mMainHomeFragmentDataObserver.freeSingleSongData.observe(
            mContext as AppCompatActivity,
            Observer<Boolean?> {
                Log.f("onClick HomeFreeSingleSong")
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                    Common.ANALYTICS_CATEGORY_HOME,
                    Common.ANALYTICS_ACTION_SELECT_MENU,
                    Common.ANALYTICS_LABEL_FREE_SONG
                )
                mMainContractView.setCurrentPage(Common.PAGE_SONG)
            })
        mMainHomeFragmentDataObserver.storyGuideData.observe(
            mContext as AppCompatActivity,
            Observer<Boolean?> {
                Log.f("onClick HomeStudyGuide")
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                    Common.ANALYTICS_CATEGORY_HOME,
                    Common.ANALYTICS_ACTION_SELECT_MENU,
                    Common.ANALYTICS_LABEL_STUDY_GUIDE
                )
                IntentManagementFactory.getInstance()
                    .readyActivityMode(ActivityMode.WEBVIEW_STUDY_GUIDE)
                    .setRequestCode(REQUEST_CODE_STUDY_GUIDE)
                    .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
            })
        mMainHomeFragmentDataObserver.introduceData.observe(
            mContext as AppCompatActivity,
            Observer<Boolean?> {
                Log.f("onClick HomeIntroduce")
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                    Common.ANALYTICS_CATEGORY_HOME,
                    Common.ANALYTICS_ACTION_SELECT_MENU,
                    Common.ANALYTICS_LABEL_INTRODUCE
                )
                IntentManagementFactory.getInstance()
                    .readyActivityMode(ActivityMode.WEBVIEW_INTRODUCE)
                    .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                    .setRequestCode(REQUEST_PAYMENT_PAGE).startActivity()
            })
        mMainHomeFragmentDataObserver.frequencySeekSeriesData.observe(
            mContext as AppCompatActivity,
            Observer<Any> {seriesBaseResult ->
                Log.f("onClick HomeFrequencySeekSeries")
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                    Common.ANALYTICS_CATEGORY_HOME_FREQUENCY_SERIES,
                    Common.ANALYTICS_ACTION_SELECT_MENU,
                    seriesBaseResult.getSeriesName()
                )
                seriesBaseResult.setTransitionType(Common.TRANSITION_SLIDE_VIEW)
                seriesBaseResult.setSeriesType(Common.CONTENT_TYPE_STORY)
                IntentManagementFactory.getInstance()
                    .readyActivityMode(ActivityMode.SERIES_DETAIL_LIST).setData(seriesBaseResult)
                    .setAnimationMode(AnimationMode.METERIAL_ANIMATION).startActivity()
            })
        mMainHomeFragmentDataObserver.lastNewsMoreData.observe(
            mContext as AppCompatActivity,
            Observer<Boolean?> {
                Log.f("onClick HomeLastNewsMore")
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                    Common.ANALYTICS_CATEGORY_HOME,
                    Common.ANALYTICS_ACTION_SELECT_MENU,
                    Common.ANALYTICS_LABEL_NEWS
                )
                startNewsActivity()
            })
        mMainHomeFragmentDataObserver.testimonialMoreData.observe(
            mContext as AppCompatActivity,
            Observer<Boolean?> {
                Log.f("onClick HomeTestimonialMore")
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                    Common.ANALYTICS_CATEGORY_HOME,
                    Common.ANALYTICS_ACTION_SELECT_MENU,
                    Common.ANALYTICS_LABEL_TESTIMONIAL
                )
                startTestimonialActivity()
            })
        mMainHomeFragmentDataObserver.newsArticleData.observe(
            mContext as AppCompatActivity,
            Observer<String> {articleID ->
                Log.f("onClick HomeNewsArticle ID :  : $articleID")
                startNewsArticleActivity(articleID)
            })
        mMainHomeFragmentDataObserver.testimonialArticleData.observe(
            mContext as AppCompatActivity,
            Observer<String> {articleID ->
                Log.f("onClick HomeTestimonialArticle ID : $articleID")
                startTestimonialArticleActivity(articleID)
            })
        mMainHomeFragmentDataObserver.homeBannerData.observe(
            mContext as AppCompatActivity,
            Observer<Any> {data ->
                if(data.getType().equals(Common.INAPP_CAMPAIGN_MODE_NEWS))
                {
                    Log.f("onClick HomeBanner type : 새소식 " + ", article ID : " + data.getArticleNumber())
                    GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                        Common.ANALYTICS_CATEGORY_HOME,
                        Common.ANALYTICS_ACTION_BANNER_SELECT,
                        data.getArticleNumber()
                    )
                    startNewsArticleActivity(java.lang.String.valueOf(data.getArticleNumber()))
                } else if(data.getType().equals(Common.INAPP_CAMPAIGN_MODE_TESTIMONIAL))
                {
                    Log.f("onClick HomeBanner type : 활용수기 " + ", article ID : " + data.getArticleNumber())
                    GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                        Common.ANALYTICS_CATEGORY_HOME,
                        Common.ANALYTICS_ACTION_BANNER_SELECT,
                        data.getArticleNumber()
                    )
                    startTestimonialArticleActivity(java.lang.String.valueOf(data.getArticleNumber()))
                } else
                {
                    Log.f("onClick HomeBanner type : 링크 " + ", url : " + data.getLinkUrl())
                    GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                        Common.ANALYTICS_CATEGORY_HOME,
                        Common.ANALYTICS_ACTION_BANNER_SELECT,
                        data.getLinkUrl()
                    )
                    CommonUtils.getInstance(mContext).startLinkMove(data.getLinkUrl())
                }
            })
    }

    private fun setupMainStoryFragmentListener()
    {
        mMainStoryFragmentDataObserver.storyLevelsItemData.observe(
            mContext as AppCompatActivity,
            Observer<Pair<Any, View>> {seriesInformationResultViewPair ->
                Log.f("onClick StoryLevelsItem")
                val pair = Pair<View, String>(
                    seriesInformationResultViewPair.second,
                    Common.STORY_DETAIL_LIST_HEADER_IMAGE
                )
                seriesInformationResultViewPair.first.setTransitionType(Common.TRANSITION_PAIR_IMAGE)
                seriesInformationResultViewPair.first.setSeriesType(Common.CONTENT_TYPE_STORY)
                IntentManagementFactory.getInstance()
                    .readyActivityMode(ActivityMode.SERIES_DETAIL_LIST)
                    .setData(seriesInformationResultViewPair.first as SeriesBaseResult)
                    .setViewPair(pair).setAnimationMode(AnimationMode.METERIAL_ANIMATION)
                    .startActivity()
            })
        mMainStoryFragmentDataObserver.storyCategoryItemData.observe(
            mContext as AppCompatActivity,
            Observer<Pair<Any, View>> {seriesInformationResultViewPair ->
                Log.f("onClick StoryCategoryItem")
                val pair = Pair<View, String>(
                    seriesInformationResultViewPair.second,
                    Common.CATEGORY_DETAIL_LIST_HEADER_IMAGE
                )
                seriesInformationResultViewPair.first.setTransitionType(Common.TRANSITION_PAIR_IMAGE)
                IntentManagementFactory.getInstance()
                    .readyActivityMode(ActivityMode.STORY_CATEGORY_LIST)
                    .setData(seriesInformationResultViewPair.first as SeriesBaseResult)
                    .setViewPair(pair).setAnimationMode(AnimationMode.METERIAL_ANIMATION)
                    .startActivity()
            })
    }

    private fun setupMainSongFragmentListener()
    {
        mMainSongFragmentDataObserver.songCategoryItemData.observe(
            mContext as AppCompatActivity,
            Observer<Pair<Any, View>> {seriesInformationResultViewPair ->
                Log.f("onClick SongCategoriesItem")
                val pair = Pair<View, String>(
                    seriesInformationResultViewPair.second,
                    Common.STORY_DETAIL_LIST_HEADER_IMAGE
                )
                seriesInformationResultViewPair.first.setTransitionType(Common.TRANSITION_PAIR_IMAGE)
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
        mMainMyBooksFragmentDataObserver.addBookshelfData.observe(
            mContext as AppCompatActivity,
            Observer<Boolean?> {
                Log.f("onAddBookshelf")
                if(Feature.IS_REMAIN_DAY_END_USER)
                {
                    Log.f("REMAIN_DAY_END_USER ADD BookShelf Not Support")
                    mMainContractView.showErrorMessage(mContext.resources.getString(R.string.message_payment_service_paid_using))
                    return@Observer
                }
                if(mMainInformationResult.getBookShelvesList().size() > Common.MAX_BOOKSHELF_SIZE)
                {
                    mMainContractView.showErrorMessage(mContext.resources.getString(R.string.message_maximum_bookshelf))
                } else
                {
                    GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                        Common.ANALYTICS_CATEGORY_BOOKSHELF,
                        Common.ANALYTICS_ACTION_SELECT_MENU,
                        Common.ANALYTICS_LABEL_ADD_BOOKSHELF
                    )
                    mManagementBooksData = ManagementBooksData(MyBooksType.BOOKSHELF_ADD)
                    IntentManagementFactory.getInstance()
                        .readyActivityMode(ActivityMode.MANAGEMENT_MYBOOKS)
                        .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                        .setData(mManagementBooksData).startActivity()
                }
            })
        mMainMyBooksFragmentDataObserver.addVocabularyData.observe(
            mContext as AppCompatActivity,
            Observer<Boolean?> {
                Log.f("onAddVocabulary")
                if(Feature.IS_REMAIN_DAY_END_USER)
                {
                    Log.f("REMAIN_DAY_END_USER ADD Vocabulary Not Support")
                    mMainContractView.showErrorMessage(mContext.resources.getString(R.string.message_payment_service_paid_using))
                    return@Observer
                }
                if(mMainInformationResult.getVocabulariesList().size() > Common.MAX_VOCABULARY_SIZE)
                {
                    mMainContractView.showErrorMessage(mContext.resources.getString(R.string.message_maximum_vocabulary))
                } else
                {
                    GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                        Common.ANALYTICS_CATEGORY_VOCABULARY,
                        Common.ANALYTICS_ACTION_SELECT_MENU,
                        Common.ANALYTICS_LABEL_ADD_VOCABULARY
                    )
                    mManagementBooksData = ManagementBooksData(MyBooksType.VOCABULARY_ADD)
                    IntentManagementFactory.getInstance()
                        .readyActivityMode(ActivityMode.MANAGEMENT_MYBOOKS)
                        .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                        .setData(mManagementBooksData).startActivity()
                }
            })
        mMainMyBooksFragmentDataObserver.enterBookshelfListData.observe(
            mContext as AppCompatActivity,
            Observer<Int> {index ->
                Log.f("onEnterBookshelfList : $index")
                if(mMainInformationResult.getBookShelvesList().get(index).getContentsCount() > 0)
                {
                    Log.f(
                        "Enter Bookshelf : " + mMainInformationResult.getBookShelvesList()
                            .get(index).getName()
                    )
                    GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                        Common.ANALYTICS_CATEGORY_BOOKSHELF,
                        Common.ANALYTICS_ACTION_SELECT_BOOKSHELF,
                        mMainInformationResult.getBookShelvesList().get(index).getName()
                    )
                    IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.BOOKSHELF)
                        .setData(mMainInformationResult.getBookShelvesList().get(index))
                        .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
                } else
                {
                    Log.f("Empty Bookshelf")
                    mMainContractView.showErrorMessage(mContext.resources.getString(R.string.message_empty_bookshelf_contents))
                }
            })
        mMainMyBooksFragmentDataObserver.enterVocabularyListData.observe(
            mContext as AppCompatActivity,
            Observer<Int> {index ->
                Log.f("onEnterVocabularyList : $index")
                if(mMainInformationResult.getVocabulariesList().get(index).getWordCount() > 0)
                {
                    Log.f(
                        "Enter Vocabulary : " + mMainInformationResult.getVocabulariesList()
                            .get(index).getName()
                    )
                    GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                        Common.ANALYTICS_CATEGORY_VOCABULARY,
                        Common.ANALYTICS_ACTION_SELECT_VOCABULARY,
                        mMainInformationResult.getVocabulariesList().get(index).getName()
                    )
                    mMainInformationResult.getVocabulariesList().get(index)
                        .setVocabularyType(VocabularyType.VOCABULARY_SHELF)
                    IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.VOCABULARY)
                        .setData(mMainInformationResult.getVocabulariesList().get(index))
                        .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
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
                val data : MyBookshelfResult =
                    mMainInformationResult.getBookShelvesList().get(index)
                Log.f(
                    "ID : " + data.getID().toString() + ", Name : " + data.getName()
                        .toString() + ", Color : " + data.getColor()
                )
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                    Common.ANALYTICS_CATEGORY_BOOKSHELF,
                    Common.ANALYTICS_ACTION_SELECT_MENU,
                    Common.ANALYTICS_LABEL_MODIFIED_BOOKSHELF
                )
                mManagementBooksData = ManagementBooksData(
                    data.getID(), data.getName(), data.getColor(), MyBooksType.BOOKSHELF_MODIFY
                )
                IntentManagementFactory.getInstance()
                    .readyActivityMode(ActivityMode.MANAGEMENT_MYBOOKS)
                    .setData(mManagementBooksData).setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                    .startActivity()
            })
        mMainMyBooksFragmentDataObserver.settingVocabularyData.observe(
            mContext as AppCompatActivity,
            Observer<Int> {index ->
                Log.f("onSettingVocabulary : $index")
                val data : MyVocabularyResult =
                    mMainInformationResult.getVocabulariesList().get(index)
                Log.f(
                    "ID : " + data.getID().toString() + ", Name : " + data.getName()
                        .toString() + ", Color : " + data.getColor()
                )
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                    Common.ANALYTICS_CATEGORY_VOCABULARY,
                    Common.ANALYTICS_ACTION_SELECT_MENU,
                    Common.ANALYTICS_LABEL_MODIFIED_VOCABULARY
                )
                mManagementBooksData = ManagementBooksData(
                    data.getID(), data.getName(), data.getColor(), MyBooksType.VOCABULARY_MODIFY
                )
                IntentManagementFactory.getInstance()
                    .readyActivityMode(ActivityMode.MANAGEMENT_MYBOOKS)
                    .setData(mManagementBooksData).setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                    .startActivity()
            })
    }

    private fun setupMainClassFragmentListener()
    {
        mMainClassFragmentDataObserver.myClassData.observe(
            mContext as AppCompatActivity,
            Observer<Void?> {onClickMyClass()})
        mMainClassFragmentDataObserver.classEnrollData.observe(
            mContext as AppCompatActivity,
            Observer<Void?> {onClickEnroll()})
        mMainClassFragmentDataObserver.classNewsData.observe(
            mContext as AppCompatActivity,
            Observer<Void?> {onClickNews()})
        mMainClassFragmentDataObserver.classWhatData.observe(
            mContext as AppCompatActivity,
            Observer<Void?> {onClickClassWhat()})
        mMainClassFragmentDataObserver.classHistoryData.observe(
            mContext as AppCompatActivity,
            Observer<Void?> {onClickPastMySelfHistory()})
    }

    /**
     * 앱 실행날짜 와 클래스 진입 날짜가 동일한지 여부 체크
     */
    private val isEqualClassExecuteSyncDate : Boolean
        private get()
        {
            val appExecuteDate = CommonUtils.getInstance(mContext).getSharedPreference(
                Common.PARAMS_APP_EXECUTE_DATE,
                Common.TYPE_PARAMS_STRING
            ) as String
            val classExecuteDate : String = CommonUtils.getInstance(mContext).getTodayDate()
            Log.f("appExecuteDate  : $appExecuteDate")
            Log.f("classExecuteDate  : $classExecuteDate")
            return if(appExecuteDate == classExecuteDate)
            {
                Log.f("DATE SYNC SUCCESS")
                true
            } else
            {
                Log.f("DATE SYNC FAIL")
                false
            }
        }

    private fun onClickMyClass()
    {
        Log.f("")
        if(isEqualClassExecuteSyncDate == false)
        {
            (mContext as AppCompatActivity).finish()
            Toast.makeText(
                mContext,
                mContext.resources.getString(R.string.message_class_execute_date_sync_error),
                Toast.LENGTH_LONG
            ).show()
            IntentManagementFactory.getInstance().initAutoIntroSequence()
            return
        }

        /* IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.CLASS_RECORDING_PLAYER)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();*/if(mMainInformationResult.getClassMainResult()
            .getCurrentMemberStatus().equals("")
    )
    {
        mMainContractView.showErrorMessage(mContext.resources.getString(R.string.message_myself_class_not_enroll))
        return
    }
        if(mMainInformationResult.getClassMainResult().getCurrentMemberStatus()
                .equals(Common.CLASS_MAIN_STATUS_STUDY_NOT_YET)
        )
        {
            mMainContractView.showErrorMessage(mContext.resources.getString(R.string.message_myself_class_not_open))
            return
        }
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.CLASS_MYSELF)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun onClickEnroll()
    {
        Log.f("")
        if(isEqualClassExecuteSyncDate == false)
        {
            (mContext as AppCompatActivity).finish()
            Toast.makeText(
                mContext,
                mContext.resources.getString(R.string.message_class_execute_date_sync_error),
                Toast.LENGTH_LONG
            ).show()
            IntentManagementFactory.getInstance().initAutoIntroSequence()
            return
        }
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.CLASS_ENROLL)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun onClickNews()
    {
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.CLASS_NEWS)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun onClickClassWhat()
    {
        Log.f("")
        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.CLASS_WHAT)
            .setData(mMainInformationResult.getClassMainResult().getCouponImageUrl())
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun onClickPastMySelfHistory()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.CLASS_PAST_MYSELF_HISTORY)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private val mItemOptionListener : ItemOptionListener = object : ItemOptionListener()
    {
        fun onClickQuiz()
        {
            Log.f("")
            GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_HOME,
                Common.ANALYTICS_ACTION_QUIZ,
                CommonUtils.getInstance(mContext).getContentsName(mCurrentDetailOptionResult)
            )
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_QUIZ, Common.DURATION_SHORT)
        }

        fun onClickTranslate()
        {
            Log.f("")
            GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_HOME,
                Common.ANALYTICS_ACTION_ORIGINAL_TRANSLATE,
                CommonUtils.getInstance(mContext).getContentsName(mCurrentDetailOptionResult)
            )
            mMainHandler.sendEmptyMessageDelayed(
                MESSAGE_START_ORIGIN_TRANSLATE,
                Common.DURATION_SHORT
            )
        }

        fun onClickVocabulary()
        {
            Log.f("")
            GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_HOME,
                Common.ANALYTICS_ACTION_VOCABULARY,
                CommonUtils.getInstance(mContext).getContentsName(mCurrentDetailOptionResult)
            )
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_VOCABULARY, Common.DURATION_SHORT)
        }

        fun onClickBookshelf()
        {
            Log.f("")
            mSendBookshelfAddList.clear()
            mSendBookshelfAddList.add(mCurrentDetailOptionResult)
            mBottomItemOptionDialog.dismiss()
            mMainHandler.sendEmptyMessageDelayed(
                MESSAGE_SHOW_BOOKSHELF_ADD_ITEM_DIALOG,
                Common.DURATION_SHORT
            )
        }

        fun onClickEbook()
        {
            Log.f("")
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_EBOOK, Common.DURATION_SHORT)
        }

        fun onClickGameStarwords()
        { // 스타워즈 액티비티 로 이동
            Log.f("")
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_GAME_STARWARS, Common.DURATION_SHORT)
        }

        fun onClickGameCrossword()
        {
            Log.f("")
            mMainHandler.sendEmptyMessageDelayed(
                MESSAGE_START_GAME_CROSSWORD,
                Common.DURATION_SHORT
            )
        }

        fun onErrorMessage(message : String?)
        {
            mMainContractView.showErrorMessage(message)
        }
    }
    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String)
        {
        }

        override fun onRunningEnd(code : String, `object` : Any)
        {
            val result : BaseResult = `object` as BaseResult ?: return
            Log.f("code : " + code + ", status : " + result.getStatus())
            if(result.getStatus() === BaseResult.SUCCESS_CODE_OK)
            {
                if(code == Common.ASYNC_CODE_BOOKSHELF_CONTENTS_ADD)
                {
                    mMainContractView.hideLoading()
                    val myBookshelfResult : MyBookshelfResult =
                        (`object` as BookshelfBaseObject).getData()
                    updateBookshelfData(myBookshelfResult)
                    updateFragment()
                    val messsage = Message.obtain()
                    messsage.what = MESSAGE_COMPLETE_CONTENTS_ADD
                    messsage.obj =
                        mContext.resources.getString(R.string.message_success_save_contents_in_bookshelf)
                    messsage.arg1 = Activity.RESULT_OK
                    mMainHandler.sendMessageDelayed(messsage, Common.DURATION_NORMAL)
                } else if(code == Common.ASYNC_CODE_CHANGE_USER || code == Common.ASYNC_CODE_ME)
                {
                    mUserInformationResult = (result as UserInformationBaseObject).getData()
                    CommonUtils.getInstance(mContext).setPreferenceObject(
                        Common.PARAMS_USER_API_INFORMATION, mUserInformationResult
                    )
                    requestMainInformationAsync()
                } else if(code == Common.ASYNC_CODE_IN_APP_BILLING)
                {
                    consumePurchaseData()
                    mMainContractView.showSuccessMessage(mContext.resources.getString(R.string.message_success_payment_restore))
                    requestAuthMeAsync()
                } else if(code == Common.ASYNC_CODE_MAIN)
                {
                    mMainContractView.hideLoading()
                    mMainInformationResult = (`object` as MainInformationBaseObject).getData()
                    CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult)
                    notifyDataChangeAllFragment()
                }
            } else
            {
                if(result.isDuplicateLogin())
                { //중복 로그인 시 재시작
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.message, Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initAutoIntroSequence()
                } else if(result.isAuthenticationBroken())
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.message, Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initScene()
                } else
                {
                    mMainContractView.hideLoading()
                    if(code == Common.ASYNC_CODE_BOOKSHELF_CONTENTS_ADD)
                    {
                        val messsage = Message.obtain()
                        messsage.what = MESSAGE_COMPLETE_CONTENTS_ADD
                        messsage.obj = result.getMessage()
                        messsage.arg1 = Activity.RESULT_CANCELED
                        mMainHandler.sendMessageDelayed(messsage, Common.DURATION_SHORT)
                    } else if(code == Common.ASYNC_CODE_MAIN)
                    {
                        Log.f("MAIN ERROR")
                        (mContext as AppCompatActivity).finish()
                        Toast.makeText(mContext, result.message, Toast.LENGTH_LONG).show()
                    } else
                    {
                        mMainContractView.showErrorMessage(result.message)
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
            mMainHandler.sendEmptyMessage(MESSAGE_APP_SERVER_ERROR)
        }
    }
    private val mDialogListener : DialogListener = object : DialogListener()
    {
        fun onConfirmButtonClick(eventType : Int)
        {
            when(eventType)
            {
                DIALOG_EVENT_IAC ->
                {
                    Log.f("IAC Link move")
                    if(mMainInformationResult.getInAppCompaignInformation().getButton1Mode()
                            .equals(Common.INAPP_CAMPAIGN_MODE_NEWS)
                    )
                    {
                        Log.f(
                            "새소식 articleID : " + mMainInformationResult.getInAppCompaignInformation()
                                .getArticleID()
                        )
                        startNewsArticleActivity(
                            java.lang.String.valueOf(
                                mMainInformationResult.getInAppCompaignInformation().getArticleID()
                            )
                        )
                    } else if(mMainInformationResult.getInAppCompaignInformation().getButton1Mode()
                            .equals(Common.INAPP_CAMPAIGN_MODE_TESTIMONIAL)
                    )
                    {
                        Log.f(
                            "활용수기 articleID : " + mMainInformationResult.getInAppCompaignInformation()
                                .getArticleID()
                        )
                        startTestimonialArticleActivity(
                            java.lang.String.valueOf(
                                mMainInformationResult.getInAppCompaignInformation().getArticleID()
                            )
                        )
                    } else
                    {
                        CommonUtils.getInstance(mContext).startLinkMove(
                            mMainInformationResult.getInAppCompaignInformation().getButton1Link()
                        )
                        mIACController.setPositiveButtonClick()
                        CommonUtils.getInstance(mContext).setPreferenceObject(
                            Common.PARAMS_IAC_CONTROLLER_INFORMATION,
                            mIACController
                        )
                    }
                }
            }
        }

        fun onChoiceButtonClick(buttonType : Int, eventType : Int)
        {
            Log.f("eventType : $eventType, buttonType : $buttonType")
            when(eventType)
            {
                DIALOG_EVENT_IAC -> if(buttonType == TempleteAlertDialog.DEFAULT_BUTTON_TYPE_1)
                {
                    Log.f("IAC Link move")
                    if(mMainInformationResult.getInAppCompaignInformation().getButton1Mode()
                            .equals(Common.INAPP_CAMPAIGN_MODE_NEWS)
                    )
                    {
                        Log.f(
                            "articleID : " + mMainInformationResult.getInAppCompaignInformation()
                                .getArticleID()
                        )
                        startNewsArticleActivity(
                            java.lang.String.valueOf(
                                mMainInformationResult.getInAppCompaignInformation().getArticleID()
                            )
                        )
                    } else if(mMainInformationResult.getInAppCompaignInformation().getButton1Mode()
                            .equals(Common.INAPP_CAMPAIGN_MODE_TESTIMONIAL)
                    )
                    {
                        Log.f(
                            "활용수기 articleID : " + mMainInformationResult.getInAppCompaignInformation()
                                .getArticleID()
                        )
                        startTestimonialArticleActivity(
                            java.lang.String.valueOf(
                                mMainInformationResult.getInAppCompaignInformation().getArticleID()
                            )
                        )
                    } else
                    {
                        CommonUtils.getInstance(mContext).startLinkMove(
                            mMainInformationResult.getInAppCompaignInformation().getButton1Link()
                        )
                        mIACController.setPositiveButtonClick()
                        CommonUtils.getInstance(mContext).setPreferenceObject(
                            Common.PARAMS_IAC_CONTROLLER_INFORMATION,
                            mIACController
                        )
                    }
                } else if(buttonType == TempleteAlertDialog.DEFAULT_BUTTON_TYPE_2)
                {
                    Log.f("IAC Cancel")
                    mIACController.setCloseButtonClick()
                    mIACController.setSaveIACInformation(mAwakeItemData)
                    CommonUtils.getInstance(mContext).setPreferenceObject(
                        Common.PARAMS_IAC_CONTROLLER_INFORMATION,
                        mIACController
                    )
                }
                DIALOG_EVENT_LOGOUT -> if(buttonType == TempleteAlertDialog.DEFAULT_BUTTON_TYPE_2)
                {
                    Log.f("============ LOGOUT COMPLETE ============")
                    IntentManagementFactory.getInstance().initScene()
                }
                DIALOG_EVENT_APP_END -> if(buttonType == TempleteAlertDialog.DEFAULT_BUTTON_TYPE_2)
                {
                    Log.f("============ APP END ============")
                    (mContext as AppCompatActivity).finish()
                }
            }
        }
    }
    private val mBookAddListener : BookAddListener = object : BookAddListener()
    {
        fun onClickBook(index : Int)
        {
            Log.f("")
            mCurrentBookshelfAddResult = mMainInformationResult.getBookShelvesList().get(index)
            mMainHandler.sendEmptyMessageDelayed(
                MESSAGE_REQUEST_CONTENTS_ADD,
                Common.DURATION_SHORT
            )
        }
    }


    init
    {
        mMainContractView = mContext as MainContract.View
        mMainHandler = WeakReferenceHandler(mContext as MessageHandlerCallback)
        mMainContractView.initView()
        mMainContractView.initFont()
        Log.f("onCreate")
        init()
    }
}