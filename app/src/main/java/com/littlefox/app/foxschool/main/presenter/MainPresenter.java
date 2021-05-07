package com.littlefox.app.foxschool.main.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.littlefox.library.system.async.listener.AsyncListener;
import com.littlefox.logmonitor.Log;

import net.littlefox.lf_app_fragment.R;
import net.littlefox.lf_app_fragment.adapter.MainFragmentSelectionPagerAdapter;
import net.littlefox.lf_app_fragment.billing.BillingClientHelper;
import net.littlefox.lf_app_fragment.billing.IBillingClientListener;
import net.littlefox.lf_app_fragment.common.Common;
import net.littlefox.lf_app_fragment.common.CommonUtils;
import net.littlefox.lf_app_fragment.common.Feature;
import net.littlefox.lf_app_fragment.common.LittlefoxLocale;
import net.littlefox.lf_app_fragment.coroutine.AuthMeCoroutine;
import net.littlefox.lf_app_fragment.coroutine.BookshelfContentAddCoroutine;
import net.littlefox.lf_app_fragment.coroutine.ChangeUserCoroutine;
import net.littlefox.lf_app_fragment.coroutine.InAppBillingCoroutine;
import net.littlefox.lf_app_fragment.coroutine.MainInformationCoroutine;
import net.littlefox.lf_app_fragment.dialog.BottomBookAddDialog;
import net.littlefox.lf_app_fragment.dialog.BottomItemOptionDialog;
import net.littlefox.lf_app_fragment.dialog.TempleteAlertDialog;
import net.littlefox.lf_app_fragment.dialog.listener.BookAddListener;
import net.littlefox.lf_app_fragment.dialog.listener.DialogListener;
import net.littlefox.lf_app_fragment.dialog.listener.ItemOptionListener;
import net.littlefox.lf_app_fragment.enumitem.ActivityMode;
import net.littlefox.lf_app_fragment.enumitem.AnimationMode;
import net.littlefox.lf_app_fragment.enumitem.FragmentDataMode;
import net.littlefox.lf_app_fragment.enumitem.MyBooksType;
import net.littlefox.lf_app_fragment.enumitem.PaymentType;
import net.littlefox.lf_app_fragment.enumitem.VocabularyType;
import net.littlefox.lf_app_fragment.firebase.GoogleAnalyticsHelper;
import net.littlefox.lf_app_fragment.fragment.MainClassFragment;
import net.littlefox.lf_app_fragment.fragment.MainHomeFragment;
import net.littlefox.lf_app_fragment.fragment.MainHomeFragmentTablet;
import net.littlefox.lf_app_fragment.fragment.MainMyBooksFragment;
import net.littlefox.lf_app_fragment.fragment.MainSongFragment;
import net.littlefox.lf_app_fragment.fragment.MainStoryFragment;
import net.littlefox.lf_app_fragment.handler.WeakReferenceHandler;
import net.littlefox.lf_app_fragment.handler.callback.MessageHandlerCallback;
import net.littlefox.lf_app_fragment.iac.IACController;
import net.littlefox.lf_app_fragment.main.contract.MainContract;
import net.littlefox.lf_app_fragment.management.IntentManagementFactory;
import net.littlefox.lf_app_fragment.object.data.ManagementBooksData;
import net.littlefox.lf_app_fragment.object.data.iac.AwakeItemData;
import net.littlefox.lf_app_fragment.object.result.base.BaseResult;
import net.littlefox.lf_app_fragment.object.result.base.BookshelfBaseObject;
import net.littlefox.lf_app_fragment.object.result.base.MainInformationBaseObject;
import net.littlefox.lf_app_fragment.object.result.base.PaymentBaseObject;
import net.littlefox.lf_app_fragment.object.result.base.UserInformationBaseObject;
import net.littlefox.lf_app_fragment.object.result.common.ContentsBaseResult;
import net.littlefox.lf_app_fragment.object.result.common.SeriesBaseResult;
import net.littlefox.lf_app_fragment.object.result.common.SeriesInformationResult;
import net.littlefox.lf_app_fragment.object.result.login.UserInformationResult;
import net.littlefox.lf_app_fragment.object.result.main.BannerInformationResult;
import net.littlefox.lf_app_fragment.object.result.main.InAppCompaignResult;
import net.littlefox.lf_app_fragment.object.result.main.MainInformationResult;
import net.littlefox.lf_app_fragment.object.result.main.MyBookshelfResult;
import net.littlefox.lf_app_fragment.object.result.main.MyVocabularyResult;
import net.littlefox.lf_app_fragment.object.viewModel.MainClassFragmentDataObserver;
import net.littlefox.lf_app_fragment.object.viewModel.MainHomeFragmentDataObserver;
import net.littlefox.lf_app_fragment.object.viewModel.MainMyBooksFragmentDataObserver;
import net.littlefox.lf_app_fragment.object.viewModel.MainPresenterDataObserver;
import net.littlefox.lf_app_fragment.object.viewModel.MainSongFragmentDataObserver;
import net.littlefox.lf_app_fragment.object.viewModel.MainStoryFragmentDataObserver;
import net.littlefox.lf_app_fragment.observer.MainObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class MainPresenter implements MainContract.Presenter {
    private static final int MESSAGE_START_QUIZ                     = 100;
    private static final int MESSAGE_SHOW_BOOKSHELF_ADD_ITEM_DIALOG = 101;
    private static final int MESSAGE_REQUEST_CONTENTS_ADD           = 102;
    private static final int MESSAGE_COMPLETE_CONTENTS_ADD          = 103;
    private static final int MESSAGE_START_NEWS                     = 104;
    private static final int MESSAGE_START_LOGIN                    = 105;
    private static final int MESSAGE_START_TESTIMONIAL              = 106;
    private static final int MESSAGE_START_MY_INFORMATION           = 107;
    private static final int MESSAGE_START_LEARNING_LOG             = 108;
    private static final int MESSAGE_START_APP_USE_GUIDE            = 109;
    private static final int MESSAGE_START_ADD_USER                 = 110;
    private static final int MESSAGE_START_ORIGIN_TRANSLATE         = 111;
    private static final int MESSAGE_START_VOCABULARY               = 112;
    private static final int MESSAGE_START_EBOOK                    = 113;
    private static final int MESSAGE_START_PUBLISH_SCHEDULE         = 114;
    private static final int MESSAGE_START_ATTENDANCE               = 115;
    private static final int MESSAGE_START_PAID                     = 116;
    private static final int MESSAGE_START_1ON1_ASK                 = 117;
    private static final int MESSAGE_START_FAQ                      = 118;
    private static final int MESSAGE_START_PAYMENT_DETAIL           = 119;
    private static final int MESSAGE_START_RESULT_SERIES            = 120;
    private static final int MESSAGE_START_RESTORE                  = 121;
    private static final int MESSAGE_START_STORE                    = 122;
    private static final int MESSAGE_START_LOGOUT                   = 123;
    private static final int MESSAGE_START_GAME_STARWARS            = 124;
    private static final int MESSAGE_START_GAME_CROSSWORD           = 125;
    private static final int MESSAGE_START_CLASS                    = 126;
    private static final int MESSAGE_APP_SERVER_ERROR               = 127;

    private static final int REQUEST_PAYMENT_SUCCESS = 1001;
    private static final int REQUEST_PAYMENT_PAGE = 1002;
    private static final int REQUEST_CODE_GO_LOGIN = 1004;
    private static final int REQUEST_CODE_STUDY_GUIDE = 1005;

    private static final int DIALOG_EVENT_IAC = 10001;
    private static final int DIALOG_EVENT_LOGOUT = 10002;
    private static final int DIALOG_EVENT_APP_END = 10003;

    private Context mContext;
    private MainContract.View mMainContractView = null;
    private MainFragmentSelectionPagerAdapter mMainFragmentSelectionPagerAdapter = null;
    private List<Fragment> mFragmentList;
    private MainInformationResult mMainInformationResult = null;
    private UserInformationResult mUserInformationResult = null;
    private WeakReferenceHandler mMainHandler = null;
    private BottomItemOptionDialog mBottomItemOptionDialog = null;
    private BottomBookAddDialog mBottomBookAddDialog = null;
    private TempleteAlertDialog mTempleteAlertDialog = null;
    private ContentsBaseResult mCurrentDetailOptionResult = null;

    private BookshelfContentAddCoroutine mBookshelfContentAddCoroutine = null;
    private MainInformationCoroutine mMainInformationCoroutine = null;
    private AuthMeCoroutine mAuthMeCoroutine = null;
    private ChangeUserCoroutine mChangeUserCoroutine = null;
    private InAppBillingCoroutine mInAppBillingCoroutine = null;

    private ManagementBooksData mManagementBooksData = null;
    private MyBookshelfResult mCurrentBookshelfAddResult = null;
    private ArrayList<ContentsBaseResult> mSendBookshelfAddList = new ArrayList<ContentsBaseResult>();
    private IACController mIACController = null;
    private AwakeItemData mAwakeItemData = null;

    private PaymentBaseObject mPaymentBaseObject = null;
    private String mReceiptData = "";
    private MainHomeFragmentDataObserver mMainHomeFragmentDataObserver;
    private MainStoryFragmentDataObserver mMainStoryFragmentDataObserver;
    private MainSongFragmentDataObserver mMainSongFragmentDataObserver;
    private MainMyBooksFragmentDataObserver mMainMyBooksFragmentDataObserver;
    private MainClassFragmentDataObserver mMainClassFragmentDataObserver;
    private MainPresenterDataObserver mMainPresenterDataObserver;

    private BillingClientHelper mBillingClientHelper = null;

    public MainPresenter(Context context) {
        mContext = context;
        mMainContractView = (MainContract.View) mContext;
        mMainHandler = new WeakReferenceHandler((MessageHandlerCallback) mContext);
        mMainContractView.initView();
        mMainContractView.initFont();
        Log.f("onCreate");
        init();
    }

    private void init() {
        Log.f("IS TABLET : " + Feature.IS_TABLET);
        MainObserver.getInstance().clearAll();
        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData();
        mMainFragmentSelectionPagerAdapter = new MainFragmentSelectionPagerAdapter(((AppCompatActivity) mContext).getSupportFragmentManager());

        if (Feature.IS_TABLET) {
            mMainFragmentSelectionPagerAdapter.addFragment(MainHomeFragmentTablet.getInstance());
        } else {
            mMainFragmentSelectionPagerAdapter.addFragment(MainHomeFragment.getInstance());
        }
        mMainFragmentSelectionPagerAdapter.addFragment(MainStoryFragment.getInstance());
        mMainFragmentSelectionPagerAdapter.addFragment(MainSongFragment.getInstance());
        mMainFragmentSelectionPagerAdapter.addFragment(MainMyBooksFragment.getInstance());

        if (Feature.IS_SUPPORT_LITTLEFOX_CLASS) {
            if (mMainInformationResult.getClassMainResult() != null) {
                mMainFragmentSelectionPagerAdapter.addFragment(MainClassFragment.getInstance());
            }
        }
        mFragmentList = mMainFragmentSelectionPagerAdapter.getPagerFragmentList();

        mMainContractView.initViewPager(mMainFragmentSelectionPagerAdapter);
        mUserInformationResult = (UserInformationResult) CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, UserInformationResult.class);
        mMainContractView.settingUserInformation(mUserInformationResult);

        initIACInformation();
        initPayment();

        mMainHomeFragmentDataObserver = ViewModelProviders.of((AppCompatActivity) mContext).get(MainHomeFragmentDataObserver.class);
        mMainStoryFragmentDataObserver = ViewModelProviders.of((AppCompatActivity) mContext).get(MainStoryFragmentDataObserver.class);
        mMainSongFragmentDataObserver = ViewModelProviders.of((AppCompatActivity) mContext).get(MainSongFragmentDataObserver.class);
        mMainMyBooksFragmentDataObserver = ViewModelProviders.of((AppCompatActivity) mContext).get(MainMyBooksFragmentDataObserver.class);
        mMainClassFragmentDataObserver = ViewModelProviders.of((AppCompatActivity) mContext).get(MainClassFragmentDataObserver.class);
        mMainPresenterDataObserver = ViewModelProviders.of((AppCompatActivity) mContext).get(MainPresenterDataObserver.class);

        setupMainHomeFragmentListener();
        setupMainStoryFragmentListener();
        setupMainSongFragmentListener();
        setupMainMyBooksFragmentListener();
        setupMainClassFragmentListener();
        setAppExecuteDate();
    }

    private void setAppExecuteDate()
    {
        String date = CommonUtils.getInstance(mContext).getTodayDate();
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_APP_EXECUTE_DATE, date);
        Log.f("date : "+ date);
    }

    private void notifyDataChangeAllFragment() {
        mMainPresenterDataObserver.notifyDataChangeAll(FragmentDataMode.CREATE, mMainInformationResult);
        mMainContractView.settingUserInformation(mUserInformationResult);
    }

    private void initIACInformation() {
        if (isVisibleIACData()) {
            Log.f("IAC VISIBLE");
            showIACInformationDialog(mMainInformationResult.getInAppCompaignInformation());
        }
    }

    private boolean isVisibleIACData() {
        boolean result = false;
        try {
            if (mMainInformationResult.getInAppCompaignInformation() != null) {
                mIACController = (IACController) CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_IAC_CONTROLLER_INFORMATION, IACController.class);
                if (mIACController == null) {
                    Log.f("IACController == null");
                    mIACController = new IACController();
                }

                if (mMainInformationResult.getInAppCompaignInformation().isButton2Use()) {
                    if (mMainInformationResult.getInAppCompaignInformation().getButton2Mode().equals(Common.IAC_AWAKE_CODE_ALWAYS_VISIBLE)) {
                        mAwakeItemData = new AwakeItemData(mMainInformationResult.getInAppCompaignInformation().getID(),
                                System.currentTimeMillis(),
                                Common.IAC_AWAKE_CODE_ALWAYS_VISIBLE,
                                0);
                    } else if (mMainInformationResult.getInAppCompaignInformation().getButton2Mode().equals(Common.IAC_AWAKE_CODE_SPECIAL_DATE_VISIBLE)) {
                        mAwakeItemData = new AwakeItemData(mMainInformationResult.getInAppCompaignInformation().getID(),
                                System.currentTimeMillis(),
                                Common.IAC_AWAKE_CODE_SPECIAL_DATE_VISIBLE,
                                mMainInformationResult.getInAppCompaignInformation().getNotDisplayDays());
                    } else if ((mMainInformationResult.getInAppCompaignInformation().getButton2Mode().equals(Common.IAC_AWAKE_CODE_ONCE_VISIBLE))) {
                        mAwakeItemData = new AwakeItemData(mMainInformationResult.getInAppCompaignInformation().getID(),
                                System.currentTimeMillis(),
                                Common.IAC_AWAKE_CODE_ONCE_VISIBLE,
                                0);
                    }
                } else {
                    mAwakeItemData = new AwakeItemData(mMainInformationResult.getInAppCompaignInformation().getID(),
                            System.currentTimeMillis(),
                            Common.IAC_AWAKE_CODE_ONCE_VISIBLE,
                            0);
                }
            } else {
                return false;
            }


        } catch (NullPointerException e) {
            return result;
        }

        result = mIACController.isAwake(mAwakeItemData);

        return result;
    }

    @Override
    public void resume() {
        Log.f("");
        updateUserInformation();
        updateFragment();
        checkToGoPayment();
    }

    @Override
    public void pause() {
        Log.f("");
    }

    @Override
    public void destroy() {
        Log.f("");

        if(mBookshelfContentAddCoroutine != null)
        {
            mBookshelfContentAddCoroutine.cancel();
            mBookshelfContentAddCoroutine = null;
        }

        if(mChangeUserCoroutine != null)
        {
            mChangeUserCoroutine.cancel();
            mChangeUserCoroutine = null;
        }

        if (mMainInformationCoroutine != null) {
            mMainInformationCoroutine.cancel();
            mMainInformationCoroutine = null;
        }

        if (mInAppBillingCoroutine != null) {
            mInAppBillingCoroutine.cancel();
            mInAppBillingCoroutine = null;
        }

        mMainHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void acvitityResult(int requestCode, int resultCode, Intent data) {
        Log.f("requestCode : " + requestCode + ", resultCode : " + resultCode);

        switch (requestCode) {
            case REQUEST_PAYMENT_SUCCESS:
                if (resultCode == RESULT_OK) {
                    mMainContractView.showLoading();
                    requestAuthMeAsync();
                } else if (resultCode == Common.RESULT_CODE_REGISTER_COUPON_FROM_REMAIN_DAY_END_USER) {
                    onClickMenuMyInformation();
                }
                break;
            case REQUEST_PAYMENT_PAGE:
                if (resultCode == Common.RESULT_CODE_PAYMENT_PAGE) {
                    enterPaymentPage();
                }
                break;

            case REQUEST_CODE_GO_LOGIN:
                if (resultCode == RESULT_OK) {
                    mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_LOGIN, Common.DURATION_SHORT);
                }
                break;
            case REQUEST_CODE_STUDY_GUIDE:
                if (resultCode == Common.RESULT_CODE_SERIES_LIST) {
                    Message message = Message.obtain();
                    message.what = MESSAGE_START_RESULT_SERIES;
                    message.obj = data.getStringExtra(Common.INTENT_RESULT_SERIES_ID);
                    mMainHandler.sendMessageDelayed(message, Common.DURATION_SHORT);
                }
                break;

        }

    }

    @Override
    public void sendMessageEvent(Message msg) {
        switch (msg.what) {
            case MESSAGE_START_QUIZ:
                startQuizAcitiviy();
                break;
            case MESSAGE_SHOW_BOOKSHELF_ADD_ITEM_DIALOG:
                showBottomBookAddDialog();
                break;
            case MESSAGE_REQUEST_CONTENTS_ADD:
                mMainContractView.showLoading();
                requestBookshelfContentsAddAsync(mSendBookshelfAddList);
                break;
            case MESSAGE_COMPLETE_CONTENTS_ADD:
                if (msg.arg1 == RESULT_OK) {
                    mMainContractView.showSuccessMessage((String) msg.obj);
                } else {
                    mMainContractView.showErrorMessage((String) msg.obj);
                }
                break;
            case MESSAGE_START_NEWS:
                startNewsActivity();
                break;
            case MESSAGE_START_LOGIN:
                startLoginActivity();
                break;
            case MESSAGE_START_TESTIMONIAL:
                startTestimonialActivity();
                break;
            case MESSAGE_START_MY_INFORMATION:
                startMyInformationActivity();
                break;
            case MESSAGE_START_LEARNING_LOG:
                startLearningLogActivity();
                break;
            case MESSAGE_START_APP_USE_GUIDE:
                startAppUseGuideActivity();
                break;
            case MESSAGE_START_ADD_USER:
                startAddUserActivity();
                break;
            case MESSAGE_START_ORIGIN_TRANSLATE:
                startOriginTranslateActivity();
                break;
            case MESSAGE_START_VOCABULARY:
                startVocabularyActivity();
                break;
            case MESSAGE_START_EBOOK:
                startEbookActivity();
                break;
            case MESSAGE_START_PUBLISH_SCHEDULE:
                startWebviewPublishScheduleActivity();
                break;
            case MESSAGE_START_ATTENDANCE:
                startWebviewAttendanceActivity();
                break;
            case MESSAGE_START_PAID:
                startPaymentActivity((PaymentType) msg.obj);
                break;
            case MESSAGE_START_1ON1_ASK:
                startWebview1On1AskActivity();
                break;
            case MESSAGE_START_FAQ:
                startWebviewFAQActivity();
                break;
            case MESSAGE_START_PAYMENT_DETAIL:
                if (Feature.IS_REMAIN_DAY_END_USER) {
                    Log.f("IS_REMAIN_DAY_END_USER Pay Page ----->");

                    if (Feature.IS_CHINESE_MODEL) {
                        return;
                    }
                    startPaymentActivity(PaymentType.ONLY_PAY);
                } else {

                    String url = mUserInformationResult.getMobileUrlPrefix()
                            + Common.SUFFIX_MOBILE_DETAIL_PAYMENT_HEADER
                            + "?token=" + CommonUtils.getInstance(mContext).getSharedPreference(Common.PARAMS_ACCESS_TOKEN, Common.TYPE_PARAMS_STRING)
                            + "&redirect_url=" + mUserInformationResult.getMobileUrlPrefix() + Common.SUFFIX_MOBILE_WEB_DETAIL_PAYMENT_REDIRECT_URL;
                    Log.f("MOBILE_WEB_LINK_DETAIL_PAYMENT ----> " + url);
                    CommonUtils.getInstance(mContext).startLinkMove(url);
                }
                break;
            case MESSAGE_START_RESULT_SERIES:
                startSelectSeriesActivity((String) msg.obj);
                break;
            case MESSAGE_START_LOGOUT:
                showTempleteAlertDialog(
                        mContext.getResources().getString(R.string.message_try_logout),
                        DIALOG_EVENT_LOGOUT,
                        TempleteAlertDialog.DEFAULT_BUTTON_TYPE_2);
                break;
            case MESSAGE_START_RESTORE:
                executeRestoreStatus(true);
                break;
            case MESSAGE_START_STORE:
                startGoToStoreWebPage();
                break;
            case MESSAGE_START_GAME_STARWARS:
                startGameStarwarsActivity();
                break;
            case MESSAGE_START_GAME_CROSSWORD:
                startGameCrosswordActivity();
                break;
            case MESSAGE_START_CLASS:
                startClassActivity();
                break;
            case MESSAGE_APP_SERVER_ERROR:
                Log.f("== Server Error  ==");
                Toast.makeText(mContext, mContext.getResources().getString(R.string.message_warning_app_server_error), Toast.LENGTH_LONG).show();
                ((AppCompatActivity) mContext).finish();
                IntentManagementFactory.getInstance().initScene();
                break;
        }
    }

    @Override
    public void changeUser(int index) {
        Log.f("change ID : " + mUserInformationResult.getUserInformationList().get(index).getID());

        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_TOP_MENU,
                Common.ANALYTICS_ACTION_SELECT_MENU,
                Common.ANALYTICS_LABEL_MODIFIED_USER);


        mMainContractView.showLoading();
        requestChangeUserAsync(mUserInformationResult.getUserInformationList().get(index).getID());

    }

    @Override
    public void onClickMenuLogin() {
        Log.f("");
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_TOP_MENU,
                Common.ANALYTICS_ACTION_SELECT_MENU,
                Common.ANALYTICS_LABEL_LOGIN);

        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_LOGIN, Common.DURATION_SHORT);
    }

    @Override
    public void onClickPaidSignIn() {
        Log.f("");
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_TOP_MENU,
                Common.ANALYTICS_ACTION_SELECT_MENU,
                Common.ANALYTICS_LABEL_USER_SIGN);

        Message message = Message.obtain();
        message.what = MESSAGE_START_PAID;
        message.obj = PaymentType.SIGN_AND_PAY;
        mMainHandler.sendMessageDelayed(message, Common.DURATION_SHORT);
    }

    @Override
    public void onClickMenuNews() {
        Log.f("");
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_TOP_MENU,
                Common.ANALYTICS_ACTION_SELECT_MENU,
                Common.ANALYTICS_LABEL_NEWS);

        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_NEWS, Common.DURATION_SHORT);
    }

    @Override
    public void onClickMenuTestimonial() {
        Log.f("");
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_TOP_MENU,
                Common.ANALYTICS_ACTION_SELECT_MENU,
                Common.ANALYTICS_LABEL_TESTIMONIAL);

        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_TESTIMONIAL, Common.DURATION_SHORT);
    }

    @Override
    public void onClickMenuMyInformation() {
        Log.f("");
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_TOP_MENU,
                Common.ANALYTICS_ACTION_SELECT_MENU,
                Common.ANALYTICS_LABEL_MY_INFORMATION);

        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_MY_INFORMATION, Common.DURATION_SHORT);
    }

    @Override
    public void onClickMenuAppUseGuide() {
        Log.f("");
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_TOP_MENU,
                Common.ANALYTICS_ACTION_SELECT_MENU,
                Common.ANALYTICS_LABEL_APP_USE_GUIDE);

        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_APP_USE_GUIDE, Common.DURATION_SHORT);
    }

    @Override
    public void onClickMenuAddUser() {
        Log.f("");
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_TOP_MENU,
                Common.ANALYTICS_ACTION_SELECT_MENU,
                Common.ANALYTICS_LABEL_ADD_USER);

        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_ADD_USER, Common.DURATION_SHORT);
    }

    @Override
    public void onClickMenu1On1Ask() {
        Log.f("");
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_TOP_MENU,
                Common.ANALYTICS_ACTION_SELECT_MENU,
                Common.ANALYTICS_LABEL_1_ON_1);

        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_1ON1_ASK, Common.DURATION_SHORT);
    }

    @Override
    public void onClickMenuFAQ() {
        Log.f("");
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_TOP_MENU,
                Common.ANALYTICS_ACTION_SELECT_MENU,
                Common.ANALYTICS_LABEL_FAQ);

        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_FAQ, Common.DURATION_SHORT);
    }

    @Override
    public void onClickMenuDetailPaymentInformation() {
        Log.f("");

        if (Feature.IS_REMAIN_DAY_END_USER) {
            GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                    Common.ANALYTICS_CATEGORY_TOP_MENU,
                    Common.ANALYTICS_ACTION_SELECT_MENU,
                    Common.ANALYTICS_LABEL_PAYMENT);
        } else {
            GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                    Common.ANALYTICS_CATEGORY_TOP_MENU,
                    Common.ANALYTICS_ACTION_SELECT_MENU,
                    Common.ANALYTICS_LABEL_DETAIL_VIEW);
        }
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_PAYMENT_DETAIL, Common.DURATION_SHORT);
    }

    @Override
    public void onClickMenuPublishSchedule() {
        Log.f("");
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_TOP_MENU,
                Common.ANALYTICS_ACTION_SELECT_MENU,
                Common.ANALYTICS_LABEL_SCHEDULE);

        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_PUBLISH_SCHEDULE, Common.DURATION_SHORT);
    }

    @Override
    public void onClickMenuAttendance() {
        Log.f("");
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_TOP_MENU,
                Common.ANALYTICS_ACTION_SELECT_MENU,
                Common.ANALYTICS_LABEL_ATTENDANCE);

        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_ATTENDANCE, Common.DURATION_SHORT);
    }

    @Override
    public void onClickMenuRestore() {
        Log.f("");
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_TOP_MENU,
                Common.ANALYTICS_ACTION_SELECT_MENU,
                Common.ANALYTICS_LABEL_RESTORE);

        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_RESTORE, Common.DURATION_SHORT);
    }

    @Override
    public void onClickMenuStore()
    {
        Log.f("");
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_STORE, Common.DURATION_SHORT);
    }

    @Override
    public void onClickMenuLogout() {
        Log.f("");
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_TOP_MENU,
                Common.ANALYTICS_ACTION_SELECT_MENU,
                Common.ANALYTICS_LABEL_LOGOUT);

        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_LOGOUT, Common.DURATION_SHORT);
    }

    @Override
    public void onClickMenuLearningLog() {
        Log.f("");
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_TOP_MENU,
                Common.ANALYTICS_ACTION_SELECT_MENU,
                Common.ANALYTICS_LABEL_LEARNING_LOG);

        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_LEARNING_LOG, Common.DURATION_SHORT);
    }

    @Override
    public void onClickMenuClass() {
        Log.f("");
        GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                Common.ANALYTICS_CATEGORY_TOP_MENU,
                Common.ANALYTICS_ACTION_SELECT_MENU,
                Common.ANALYTICS_LABEL_CLASS);

        mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_CLASS, Common.DURATION_SHORT);
    }

    @Override
    public void onClickSearch() {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.SEARCH)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    @Override
    public void onBackPressed() {
        Log.f("Check End App");
        showTempleteAlertDialog(
                mContext.getResources().getString(R.string.message_check_end_app),
                DIALOG_EVENT_APP_END,
                TempleteAlertDialog.DEFAULT_BUTTON_TYPE_2);
    }


    private void initPayment()
    {
        mBillingClientHelper = BillingClientHelper.getInstance();
        setUpInAppPurchaseListener();
        mBillingClientHelper.init(mContext);

    }

    private void setUpInAppPurchaseListener()
    {
        mBillingClientHelper.setOnBillingClientListener(new IBillingClientListener()
        {
            @Override
            public void onSkuDetailQueryFinished() { }

            @Override
            public void onCheckPurchaseItem()
            {
                Log.f("");
                executeRestoreStatus(false);
            }

            @Override
            public void onPurchaseComplete(@Nullable Purchase purchaseItem) { }

            @Override
            public void onConsumeComplete(BillingResult billingResult, String purchaseToken)
            {
                Log.f("response Code : "+ billingResult.getResponseCode()+", purchaseToken : "+ purchaseToken);
            }

            @Override
            public void inFailure(int status, String reason)
            {
                mMainContractView.showErrorMessage(reason);
            }
        });
    }



    private void startWebviewFAQActivity()
    {
        Log.f("");

        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.WEBVIEW_FAQS)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void startWebview1On1AskActivity()
    {
        Log.f("");

        if(LittlefoxLocale.getInstance().getCurrentLocale().contains(Locale.KOREA.toString())
         && Feature.IS_FREE_USER == false)
        {
            IntentManagementFactory.getInstance()
                    .readyActivityMode(ActivityMode.WEBVIEW_1ON1_ASK)
                    .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                    .startActivity();
        }
        else
        {
            CommonUtils.getInstance(mContext).inquireForDeveloper(Common.DEVELOPER_EMAIL);
        }

    }

    private void startWebviewPublishScheduleActivity()
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.WEBVIEW_PUBLISH_SCHEDULE)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void startWebviewAttendanceActivity()
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.WEBVIEW_ATTENDANCE)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void startGameStarwarsActivity()
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.WEBVIEW_GAME_STARWORDS)
              //  .setData(mCurrentDetailOptionResult.getID())
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void startGameCrosswordActivity()
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.WEBVIEW_GAME_CROSSWORD)
               // .setData(mCurrentDetailOptionResult.getID())
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void startClassActivity()
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.CLASS_MAIN)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void startGoToStoreWebPage()
    {
        Log.f("");
        CommonUtils.getInstance(mContext).startLinkMove(Common.STORE_LINK);
    }

    private void executeRestoreStatus(boolean showMessage)
    {
        mPaymentBaseObject = (PaymentBaseObject) CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_IN_APP_ITEM_INFORMATION, PaymentBaseObject.class);
        mReceiptData = CommonUtils.getInstance(mContext).getSharedPreferenceString(Common.PARAMS_IN_APP_ITEM_RECEIPT, "");

        if(mPaymentBaseObject == null)
        {
            if(showMessage)
            {
                Log.f("Restore Not Need User.");
                mMainContractView.showErrorMessage(mContext.getResources().getString(R.string.message_not_have_payment_history));
            }
        }
        else
        {
            if(Feature.IS_FREE_USER == false)
            {
                if(Feature.IS_REMAIN_DAY_END_USER)
                {
                    mMainContractView.showLoading();
                    Log.f("================ Restore User =============");
                    requestInAppInformationAsync();
                }
                else
                {
                    consumePurchaseData();
                }
            }
            else
            {
                if(showMessage)
                {
                    Log.f("Restore Not Need User.");
                    mMainContractView.showErrorMessage(mContext.getResources().getString(R.string.message_not_have_payment_history));
                }
            }
        }
    }

    private void consumePurchaseData()
    {
        Purchase monthItem = mBillingClientHelper.getPurchasedItemResult(BillingClientHelper.IN_APP_1_MONTH);

        if(monthItem != null)
        {
            Log.f("====== consume Item ========");
            Log.f("item data : "+ monthItem.getOriginalJson());
            CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_IN_APP_ITEM_INFORMATION, null);
            CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IN_APP_ITEM_RECEIPT, "");
            mPaymentBaseObject = null;
            mReceiptData = "";
            mBillingClientHelper.consumeItem(monthItem);
        }
    }

    private void requestInAppInformationAsync()
    {
        Log.f("");

        mInAppBillingCoroutine = new InAppBillingCoroutine(mContext);
        mInAppBillingCoroutine.setData(mPaymentBaseObject.getData(), mReceiptData);
        mInAppBillingCoroutine.setAsyncListener(mAsyncListener);
        mInAppBillingCoroutine.execute();
    }

    private void showTempleteAlertDialog(String message, int eventType, int buttonType)
    {
        mTempleteAlertDialog = new TempleteAlertDialog(mContext);
        mTempleteAlertDialog.setMessage(message);
        mTempleteAlertDialog.setDialogEventType(eventType);
        mTempleteAlertDialog.setButtonType(buttonType);
        mTempleteAlertDialog.setDialogListener(mDialogListener);
        mTempleteAlertDialog.setGravity(Gravity.LEFT);
        mTempleteAlertDialog.show();
    }

    private void startSelectSeriesActivity(String seriesID)
    {
        Log.f("seriesID : "+ seriesID);
        SeriesBaseResult result = null;

        for(int i = 0; i < mMainInformationResult.getMainStoryInformation().getContentByLevelToList().size(); i++)
        {
            if(mMainInformationResult.getMainStoryInformation().getContentByLevelToList().get(i).getDisplayId().equals(seriesID))
            {
                result = mMainInformationResult.getMainStoryInformation().getContentByLevelToList().get(i);
                result.setSeriesType(Common.CONTENT_TYPE_STORY);
                break;
            }
        }

        if(result != null)
        {
            for (int i = 0; i < mMainInformationResult.getMainSongInformation().getContentByCategoriesToList().size(); i++) {
                if (mMainInformationResult.getMainSongInformation().getContentByCategoriesToList().get(i).getDisplayId().equals(seriesID))
                {
                    result = mMainInformationResult.getMainSongInformation().getContentByCategoriesToList().get(i);
                    result.setSeriesType(Common.CONTENT_TYPE_SONG);
                    break;
                }
            }
        }

        if(result == null)
        {
            return;
        }

        result.setTransitionType(Common.TRANSITION_SLIDE_VIEW);
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.SERIES_DETAIL_LIST)
                .setData(result)
                .setAnimationMode(AnimationMode.METERIAL_ANIMATION)
                .startActivity();
    }

    private void startCurrentSelectMovieActivity(ContentsBaseResult result)
    {
        Log.f("result ID : "+ result.getID() +", Name : "+ result.getName());
        ArrayList<ContentsBaseResult> sendItemList = new ArrayList<ContentsBaseResult>();
        sendItemList.add(result);

        IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.PLAYER)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .setData(sendItemList)
                .startActivity();
    }

    private void startQuizAcitiviy()
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.QUIZ)
                .setData(mCurrentDetailOptionResult.getID())
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void startNewsActivity()
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.NEWS)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void startTestimonialActivity()
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.TESTIMONIAL)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void startNewsArticleActivity(String articleID)
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.NEWS)
                .setData(articleID)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void startTestimonialArticleActivity(String articleID)
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.TESTIMONIAL)
                .setData(articleID)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void startLoginActivity()
    {
        Log.f("");
        boolean isLoginFromMain = true;
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.LOGIN)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .setData(isLoginFromMain)
                .startActivity();
    }

    private void startPaymentActivity(PaymentType type)
    {
        Log.f("type : "+type);
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.PAYMENT)
                .setRequestCode(type == PaymentType.SIGN_AND_PAY ? REQUEST_CODE_GO_LOGIN : REQUEST_PAYMENT_SUCCESS)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .setData(type)
                .startActivity();
    }

    private void startMyInformationActivity()
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.MY_INFORMATION)
                .setData("N")
                .setRequestCode(REQUEST_PAYMENT_PAGE)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void startLearningLogActivity()
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.WEBVIEW_LEARNING_LOG)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();

    }

    private void startAddUserActivity()
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.MY_INFORMATION)
                .setData("Y")
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void startOriginTranslateActivity()
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.WEBVIEW_ORIGIN_TRANSLATE)
                .setData(mCurrentDetailOptionResult.getID())
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void startEbookActivity()
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.WEBVIEW_EBOOK)
                .setData(mCurrentDetailOptionResult.getID())
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void startVocabularyActivity()
    {
        Log.f("");
        String title = "";
        title = CommonUtils.getInstance(mContext).getVocabularyTitleName(mCurrentDetailOptionResult);
        MyVocabularyResult myVocabularyResult = new MyVocabularyResult(mCurrentDetailOptionResult.getID(),
                title, VocabularyType.VOCABULARY_CONTENTS);
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.VOCABULARY)
                .setData(myVocabularyResult)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void startAppUseGuideActivity()
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.APP_USE_GUIDE)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void showBottomItemOptionDialog(ContentsBaseResult result)
    {
        mBottomItemOptionDialog = new BottomItemOptionDialog(mContext);
        mBottomItemOptionDialog
                .setFullName()
                .setData(result)
                .setItemOptionListener(mItemOptionListener)
                .setView();
        mBottomItemOptionDialog.show();
    }

    private void updateUserInformation()
    {
        Log.f("update Status : "+ MainObserver.getInstance().isUpdateUserStatus());
        if(MainObserver.getInstance().isUpdateUserStatus())
        {
            mUserInformationResult = (UserInformationResult) CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, UserInformationResult.class);
            mMainContractView.settingUserInformation(mUserInformationResult);
            MainObserver.getInstance().clearUserStatus();
        }
    }

    private void updateFragment()
    {
        Log.i("size : "+MainObserver.getInstance().getUpdatePageList().size());
        if(MainObserver.getInstance().getUpdatePageList().size() > 0)
        {
            mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData();
            Pair<FragmentDataMode, MainInformationResult> result = new Pair<>(FragmentDataMode.UPDATE, mMainInformationResult);
            for(int page : MainObserver.getInstance().getUpdatePageList())
            {
               // ((MainCallback)mFragmentList.get(page)).updateData(FragmentDataMode.UPDATE, mMainInformationResult);
                Log.f("update page : "+page);
                switch(page)
                {
                    case Common.PAGE_HOME:
                        mMainPresenterDataObserver.updateHomeData.setValue(result);
                        break;
                    case Common.PAGE_STORY:
                        mMainPresenterDataObserver.updateStoryData.setValue(mMainInformationResult);
                        break;
                    case Common.PAGE_SONG:
                        mMainPresenterDataObserver.updateSongData.setValue(mMainInformationResult);
                        break;
                    case Common.PAGE_MY_BOOKS:
                        mMainPresenterDataObserver.updateMyBooksData.setValue(mMainInformationResult);
                        break;
                    case Common.PAGE_CLASS:
                        mMainPresenterDataObserver.updateClassData.setValue(mMainInformationResult);
                        break;
                }
            }
            MainObserver.getInstance().clearAll();
        }
    }

    private void checkToGoPayment()
    {
        Log.f("");
        if(MainObserver.getInstance().isEnterPaymentPage())
        {
            Log.f("미리보기 후 사용자가 회비 결제 버튼을 눌렀다.!");
            MainObserver.getInstance().clearEnterPaymentPage();
            enterPaymentPage();
        }
    }

    private void enterPaymentPage()
    {
        Log.f("");
        Message message = Message.obtain();
        message.what = MESSAGE_START_PAID;
        if(Feature.IS_FREE_USER)
        {
            message.obj = PaymentType.SIGN_AND_PAY;
        }
        else
        {
            message.obj = PaymentType.ONLY_PAY;
        }
        mMainHandler.sendMessageDelayed(message, Common.DURATION_NORMAL);
    }

    private void showBottomBookAddDialog()
    {
        mBottomBookAddDialog = new BottomBookAddDialog(mContext);
        mBottomBookAddDialog.setCancelable(true);
        mBottomBookAddDialog.setBookshelfData(mMainInformationResult.getBookShelvesList());
        mBottomBookAddDialog.setBookSelectListener(mBookAddListener);
        mBottomBookAddDialog.show();
    }



    private void showIACInformationDialog(InAppCompaignResult result)
    {
        mTempleteAlertDialog = new TempleteAlertDialog(mContext);
        mTempleteAlertDialog.setTitle(result.getTitle());
        mTempleteAlertDialog.setMessage(result.getContent());

        if(result.isButton1Use() == false)
        {
            mTempleteAlertDialog.setButtonText(result.getButton2Text());
        }
        else
        {
            mTempleteAlertDialog.setButtonText(result.getButton1Text(), result.getButton2Text());
        }

        mTempleteAlertDialog.setDialogEventType(DIALOG_EVENT_IAC);
        mTempleteAlertDialog.setDialogListener(mDialogListener);
        mTempleteAlertDialog.show();
    }

    private void requestBookshelfContentsAddAsync(ArrayList<ContentsBaseResult> data)
    {
        Log.f("");
        mBookshelfContentAddCoroutine = new BookshelfContentAddCoroutine(mContext);
        mBookshelfContentAddCoroutine.setData(
                mCurrentBookshelfAddResult.getID(),
                data);
        mBookshelfContentAddCoroutine.setAsyncListener(mAsyncListener);
        mBookshelfContentAddCoroutine.execute();
    }

    private void requestAuthMeAsync()
    {
        Log.f("");

        mAuthMeCoroutine = new AuthMeCoroutine(mContext);
        mAuthMeCoroutine.setAsyncListener(mAsyncListener);
        mAuthMeCoroutine.execute();
    }

    private void requestChangeUserAsync(String changeUserID)
    {
        Log.f("changeUserID : "+changeUserID);

        mChangeUserCoroutine = new ChangeUserCoroutine(mContext);
        mChangeUserCoroutine.setData(changeUserID);
        mChangeUserCoroutine.setAsyncListener(mAsyncListener);
        mChangeUserCoroutine.execute();
    }

    private void requestMainInformationAsync()
    {
        Log.f("");

        mMainInformationCoroutine = new MainInformationCoroutine(mContext);
        mMainInformationCoroutine.setAsyncListener(mAsyncListener);
        mMainInformationCoroutine.execute();
    }

    private void updateBookshelfData(MyBookshelfResult result)
    {
        for(int i = 0; i < mMainInformationResult.getBookShelvesList().size(); i++)
        {
            if(mMainInformationResult.getBookShelvesList().get(i).getID().equals(result.getID()))
            {
                Log.f("update Index :"+ i);
                mMainInformationResult.getBookShelvesList().set(i, result);
            }
        }
        CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult);
        MainObserver.getInstance().updatePage(Common.PAGE_MY_BOOKS);
    }

    private ArrayList<SeriesInformationResult> getSortFreeSeriesList()
    {
        ArrayList<SeriesInformationResult> result = mMainInformationResult.getMainStoryInformation().getContentByLevelToList();

        Collections.sort(result, new Comparator<SeriesInformationResult>()
        {
            @Override
            public int compare(SeriesInformationResult s0, SeriesInformationResult s1)
            {
                if(s0.getFreeSeriesSortNumber() < s1.getFreeSeriesSortNumber())
                {
                    return -1;
                }
                else if(s0.getFreeSeriesSortNumber() > s1.getFreeSeriesSortNumber())
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
            }
        });
        return result;
    }

    private ArrayList<SeriesInformationResult> getSortFreeSingleList()
    {
        ArrayList<SeriesInformationResult> result = mMainInformationResult.getMainStoryInformation().getContentByLevelToList();

        Collections.sort(result, new Comparator<SeriesInformationResult>()
        {
            @Override
            public int compare(SeriesInformationResult s0, SeriesInformationResult s1)
            {
                if(s0.getFreeSingleSortNumber() < s1.getFreeSingleSortNumber())
                {
                    return -1;
                }
                else if(s0.getFreeSingleSortNumber() > s1.getFreeSingleSortNumber())
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
            }
        });
        return result;
    }

    private void setupMainHomeFragmentListener()
    {
        mMainHomeFragmentDataObserver.playMovieData.observe((AppCompatActivity) mContext, new Observer<ContentsBaseResult>() {
            @Override
            public void onChanged(ContentsBaseResult contentsBaseResult)
            {
                Log.f("onClick HomePlayMovie");
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                        Common.ANALYTICS_CATEGORY_HOME,
                        Common.ANALYTICS_ACTION_PLAY,
                        CommonUtils.getInstance(mContext).getContentsName(contentsBaseResult));
                startCurrentSelectMovieActivity(contentsBaseResult);
            }
        });

        mMainHomeFragmentDataObserver.itemOptionData.observe((AppCompatActivity)mContext, new Observer<ContentsBaseResult>() {
            @Override
            public void onChanged(ContentsBaseResult contentsBaseResult)
            {
                Log.f("onClick HomeItemOption");
                mCurrentDetailOptionResult = contentsBaseResult;
                showBottomItemOptionDialog(contentsBaseResult);
            }
        });

        mMainHomeFragmentDataObserver.freeSeriesStoryData.observe((AppCompatActivity)mContext, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isExecute)
            {
                Log.f("onClick HomeFreeSeriesStory");
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                        Common.ANALYTICS_CATEGORY_HOME,
                        Common.ANALYTICS_ACTION_SELECT_MENU,
                        Common.ANALYTICS_LABEL_FREE_SERIES_STORY);

                ArrayList<SeriesInformationResult> sortList = getSortFreeSeriesList();
                mMainInformationResult.getMainStoryInformation().setContentByLevelToList(sortList);
                CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult);
                MainObserver.getInstance().updatePage(Common.PAGE_STORY);
                updateFragment();
                mMainContractView.setCurrentPage(Common.PAGE_STORY);
            }
        });

        mMainHomeFragmentDataObserver.freeSingleStoryData.observe((AppCompatActivity)mContext, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isExecute)
            {
                Log.f("onClick HomeFreeSingleStory");
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                        Common.ANALYTICS_CATEGORY_HOME,
                        Common.ANALYTICS_ACTION_SELECT_MENU,
                        Common.ANALYTICS_LABEL_FREE_SINGLE_STORY);

                ArrayList<SeriesInformationResult> sortList = getSortFreeSingleList();
                mMainInformationResult.getMainStoryInformation().setContentByLevelToList(sortList);
                CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult);
                MainObserver.getInstance().updatePage(Common.PAGE_STORY);
                updateFragment();
                mMainContractView.setCurrentPage(Common.PAGE_STORY);
            }
        });

        mMainHomeFragmentDataObserver.freeSingleSongData.observe((AppCompatActivity)mContext, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isExecute)
            {
                Log.f("onClick HomeFreeSingleSong");
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                        Common.ANALYTICS_CATEGORY_HOME,
                        Common.ANALYTICS_ACTION_SELECT_MENU,
                        Common.ANALYTICS_LABEL_FREE_SONG);

                mMainContractView.setCurrentPage(Common.PAGE_SONG);
            }
        });

        mMainHomeFragmentDataObserver.storyGuideData.observe((AppCompatActivity)mContext, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isExecute)
            {
                Log.f("onClick HomeStudyGuide");
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                        Common.ANALYTICS_CATEGORY_HOME,
                        Common.ANALYTICS_ACTION_SELECT_MENU,
                        Common.ANALYTICS_LABEL_STUDY_GUIDE);

                IntentManagementFactory.getInstance()
                        .readyActivityMode(ActivityMode.WEBVIEW_STUDY_GUIDE)
                        .setRequestCode(REQUEST_CODE_STUDY_GUIDE)
                        .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                        .startActivity();
            }
        });

        mMainHomeFragmentDataObserver.introduceData.observe((AppCompatActivity) mContext, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isExecute)
            {
                Log.f("onClick HomeIntroduce");
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                        Common.ANALYTICS_CATEGORY_HOME,
                        Common.ANALYTICS_ACTION_SELECT_MENU,
                        Common.ANALYTICS_LABEL_INTRODUCE);

                IntentManagementFactory.getInstance()
                        .readyActivityMode(ActivityMode.WEBVIEW_INTRODUCE)
                        .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                        .setRequestCode(REQUEST_PAYMENT_PAGE)
                        .startActivity();
            }
        });

        mMainHomeFragmentDataObserver.frequencySeekSeriesData.observe((AppCompatActivity) mContext, new Observer<SeriesBaseResult>() {
            @Override
            public void onChanged(SeriesBaseResult seriesBaseResult)
            {
                Log.f("onClick HomeFrequencySeekSeries");

                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                        Common.ANALYTICS_CATEGORY_HOME_FREQUENCY_SERIES,
                        Common.ANALYTICS_ACTION_SELECT_MENU,
                        seriesBaseResult.getSeriesName());

                seriesBaseResult.setTransitionType(Common.TRANSITION_SLIDE_VIEW);
                seriesBaseResult.setSeriesType(Common.CONTENT_TYPE_STORY);
                IntentManagementFactory.getInstance()
                        .readyActivityMode(ActivityMode.SERIES_DETAIL_LIST)
                        .setData(seriesBaseResult)
                        .setAnimationMode(AnimationMode.METERIAL_ANIMATION)
                        .startActivity();
            }
        });

        mMainHomeFragmentDataObserver.lastNewsMoreData.observe((AppCompatActivity) mContext, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isExecute)
            {
                Log.f("onClick HomeLastNewsMore");
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                        Common.ANALYTICS_CATEGORY_HOME,
                        Common.ANALYTICS_ACTION_SELECT_MENU,
                        Common.ANALYTICS_LABEL_NEWS);
                startNewsActivity();
            }
        });

        mMainHomeFragmentDataObserver.testimonialMoreData.observe((AppCompatActivity) mContext, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isExecute)
            {
                Log.f("onClick HomeTestimonialMore");
                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                        Common.ANALYTICS_CATEGORY_HOME,
                        Common.ANALYTICS_ACTION_SELECT_MENU,
                        Common.ANALYTICS_LABEL_TESTIMONIAL);
                startTestimonialActivity();
            }
        });

        mMainHomeFragmentDataObserver.newsArticleData.observe((AppCompatActivity)mContext, new Observer<String>() {
            @Override
            public void onChanged(String articleID)
            {
                Log.f("onClick HomeNewsArticle ID :  : " +articleID);
                startNewsArticleActivity(articleID);
            }
        });

        mMainHomeFragmentDataObserver.testimonialArticleData.observe((AppCompatActivity) mContext, new Observer<String>() {
            @Override
            public void onChanged(String articleID)
            {
                Log.f("onClick HomeTestimonialArticle ID : " +articleID);
                startTestimonialArticleActivity(articleID);
            }
        });

        mMainHomeFragmentDataObserver.homeBannerData.observe((AppCompatActivity) mContext, new Observer<BannerInformationResult>() {
            @Override
            public void onChanged(BannerInformationResult data)
            {
                if(data.getType().equals(Common.INAPP_CAMPAIGN_MODE_NEWS))
                {
                    Log.f("onClick HomeBanner type : 새소식 "+", article ID : "+ data.getArticleNumber());
                    GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                            Common.ANALYTICS_CATEGORY_HOME,
                            Common.ANALYTICS_ACTION_BANNER_SELECT,
                            data.getArticleNumber());
                    startNewsArticleActivity(String.valueOf(data.getArticleNumber()));

                }
                else if(data.getType().equals(Common.INAPP_CAMPAIGN_MODE_TESTIMONIAL))
                {
                    Log.f("onClick HomeBanner type : 활용수기 "+", article ID : "+ data.getArticleNumber());
                    GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                            Common.ANALYTICS_CATEGORY_HOME,
                            Common.ANALYTICS_ACTION_BANNER_SELECT,
                            data.getArticleNumber());
                    startTestimonialArticleActivity(String.valueOf(data.getArticleNumber()));
                }
                else
                {
                    Log.f("onClick HomeBanner type : 링크 "+", url : "+ data.getLinkUrl());
                    GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                            Common.ANALYTICS_CATEGORY_HOME,
                            Common.ANALYTICS_ACTION_BANNER_SELECT,
                            data.getLinkUrl());
                    CommonUtils.getInstance(mContext).startLinkMove(data.getLinkUrl());
                }



            }
        });
    }

    private void setupMainStoryFragmentListener()
    {
        mMainStoryFragmentDataObserver.storyLevelsItemData.observe((AppCompatActivity)mContext, new Observer<Pair<SeriesInformationResult, View>>() {
            @Override
            public void onChanged(Pair<SeriesInformationResult, View> seriesInformationResultViewPair)
            {
                Log.f("onClick StoryLevelsItem");
                Pair<View, String> pair = new Pair<>(seriesInformationResultViewPair.second, Common.STORY_DETAIL_LIST_HEADER_IMAGE);
                seriesInformationResultViewPair.first.setTransitionType(Common.TRANSITION_PAIR_IMAGE);
                seriesInformationResultViewPair.first.setSeriesType(Common.CONTENT_TYPE_STORY);
                IntentManagementFactory.getInstance()
                        .readyActivityMode(ActivityMode.SERIES_DETAIL_LIST)
                        .setData((SeriesBaseResult)seriesInformationResultViewPair.first)
                        .setViewPair(pair)
                        .setAnimationMode(AnimationMode.METERIAL_ANIMATION)
                        .startActivity();
            }
        });

        mMainStoryFragmentDataObserver.storyCategoryItemData.observe((AppCompatActivity)mContext, new Observer<Pair<SeriesInformationResult, View>>() {
            @Override
            public void onChanged(Pair<SeriesInformationResult, View> seriesInformationResultViewPair)
            {
                Log.f("onClick StoryCategoryItem");
                Pair<View, String> pair = new Pair<>(seriesInformationResultViewPair.second, Common.CATEGORY_DETAIL_LIST_HEADER_IMAGE);
                seriesInformationResultViewPair.first.setTransitionType(Common.TRANSITION_PAIR_IMAGE);
                IntentManagementFactory.getInstance()
                        .readyActivityMode(ActivityMode.STORY_CATEGORY_LIST)
                        .setData((SeriesBaseResult)seriesInformationResultViewPair.first)
                        .setViewPair(pair)
                        .setAnimationMode(AnimationMode.METERIAL_ANIMATION)
                        .startActivity();
            }
        });
    }

    private void setupMainSongFragmentListener()
    {
        mMainSongFragmentDataObserver.songCategoryItemData.observe((AppCompatActivity)mContext, new Observer<Pair<SeriesInformationResult, View>>() {
            @Override
            public void onChanged(Pair<SeriesInformationResult, View> seriesInformationResultViewPair)
            {
                Log.f("onClick SongCategoriesItem");
                Pair<View, String> pair = new Pair<>(seriesInformationResultViewPair.second, Common.STORY_DETAIL_LIST_HEADER_IMAGE);
                seriesInformationResultViewPair.first.setTransitionType(Common.TRANSITION_PAIR_IMAGE);
                seriesInformationResultViewPair.first.setSeriesType(Common.CONTENT_TYPE_SONG);
                IntentManagementFactory.getInstance()
                        .readyActivityMode(ActivityMode.SERIES_DETAIL_LIST)
                        .setData((SeriesBaseResult)seriesInformationResultViewPair.first)
                        .setViewPair(pair)
                        .setAnimationMode(AnimationMode.METERIAL_ANIMATION)
                        .startActivity();
            }
        });
    }

    private void setupMainMyBooksFragmentListener()
    {
        mMainMyBooksFragmentDataObserver.addBookshelfData.observe((AppCompatActivity)mContext, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isExecute)
            {
                Log.f("onAddBookshelf");

                if(Feature.IS_REMAIN_DAY_END_USER)
                {
                    Log.f("REMAIN_DAY_END_USER ADD BookShelf Not Support");
                    mMainContractView.showErrorMessage(mContext.getResources().getString(R.string.message_payment_service_paid_using));
                    return;
                }

                if(mMainInformationResult.getBookShelvesList().size() > Common.MAX_BOOKSHELF_SIZE)
                {
                    mMainContractView.showErrorMessage(mContext.getResources().getString(R.string.message_maximum_bookshelf));
                }
                else
                {
                    GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                            Common.ANALYTICS_CATEGORY_BOOKSHELF,
                            Common.ANALYTICS_ACTION_SELECT_MENU,
                            Common.ANALYTICS_LABEL_ADD_BOOKSHELF);

                    mManagementBooksData = new ManagementBooksData(MyBooksType.BOOKSHELF_ADD);
                    IntentManagementFactory.getInstance()
                            .readyActivityMode(ActivityMode.MANAGEMENT_MYBOOKS)
                            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                            .setData(mManagementBooksData)
                            .startActivity();
                }
            }
        });

        mMainMyBooksFragmentDataObserver.addVocabularyData.observe((AppCompatActivity)mContext, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isExecute)
            {
                Log.f("onAddVocabulary");

                if(Feature.IS_REMAIN_DAY_END_USER)
                {
                    Log.f("REMAIN_DAY_END_USER ADD Vocabulary Not Support");
                    mMainContractView.showErrorMessage(mContext.getResources().getString(R.string.message_payment_service_paid_using));
                    return;
                }

                if(mMainInformationResult.getVocabulariesList().size() > Common.MAX_VOCABULARY_SIZE)
                {
                    mMainContractView.showErrorMessage(mContext.getResources().getString(R.string.message_maximum_vocabulary));
                }
                else
                {
                    GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                            Common.ANALYTICS_CATEGORY_VOCABULARY,
                            Common.ANALYTICS_ACTION_SELECT_MENU,
                            Common.ANALYTICS_LABEL_ADD_VOCABULARY);


                    mManagementBooksData = new ManagementBooksData(MyBooksType.VOCABULARY_ADD);
                    IntentManagementFactory.getInstance()
                            .readyActivityMode(ActivityMode.MANAGEMENT_MYBOOKS)
                            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                            .setData(mManagementBooksData)
                            .startActivity();
                }
            }
        });

        mMainMyBooksFragmentDataObserver.enterBookshelfListData.observe((AppCompatActivity)mContext, new Observer<Integer>() {
            @Override
            public void onChanged(Integer index)
            {
                Log.f("onEnterBookshelfList : "+ index);
                if(mMainInformationResult.getBookShelvesList().get(index).getContentsCount() > 0)
                {
                    Log.f("Enter Bookshelf : " + mMainInformationResult.getBookShelvesList().get(index).getName());

                    GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                            Common.ANALYTICS_CATEGORY_BOOKSHELF,
                            Common.ANALYTICS_ACTION_SELECT_BOOKSHELF,
                            mMainInformationResult.getBookShelvesList().get(index).getName());

                    IntentManagementFactory.getInstance()
                            .readyActivityMode(ActivityMode.BOOKSHELF)
                            .setData(mMainInformationResult.getBookShelvesList().get(index))
                            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                            .startActivity();
                }
                else
                {
                    Log.f("Empty Bookshelf");
                    mMainContractView.showErrorMessage(mContext.getResources().getString(R.string.message_empty_bookshelf_contents));
                }
            }
        });

        mMainMyBooksFragmentDataObserver.enterVocabularyListData.observe((AppCompatActivity)mContext, new Observer<Integer>() {
            @Override
            public void onChanged(Integer index)
            {
                Log.f("onEnterVocabularyList : "+ index);
                if(mMainInformationResult.getVocabulariesList().get(index).getWordCount() > 0)
                {
                    Log.f("Enter Vocabulary : " + mMainInformationResult.getVocabulariesList().get(index).getName());
                    GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                            Common.ANALYTICS_CATEGORY_VOCABULARY,
                            Common.ANALYTICS_ACTION_SELECT_VOCABULARY,
                            mMainInformationResult.getVocabulariesList().get(index).getName());

                    mMainInformationResult.getVocabulariesList().get(index).setVocabularyType(VocabularyType.VOCABULARY_SHELF);
                    IntentManagementFactory.getInstance()
                            .readyActivityMode(ActivityMode.VOCABULARY)
                            .setData(mMainInformationResult.getVocabulariesList().get(index))
                            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                            .startActivity();
                }
                else
                {
                    Log.f("Empty Vocabulary");
                    mMainContractView.showErrorMessage(mContext.getResources().getString(R.string.message_empty_vocabulary_contents));
                }
            }
        });

        mMainMyBooksFragmentDataObserver.settingBookshelfData.observe((AppCompatActivity)mContext, new Observer<Integer>() {
            @Override
            public void onChanged(Integer index)
            {
                Log.f("onSettingBookshelf : "+ index);
                MyBookshelfResult data = mMainInformationResult.getBookShelvesList().get(index);
                Log.f("ID : "+data.getID()+", Name : "+data.getName()+", Color : "+ data.getColor());

                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                        Common.ANALYTICS_CATEGORY_BOOKSHELF,
                        Common.ANALYTICS_ACTION_SELECT_MENU,
                        Common.ANALYTICS_LABEL_MODIFIED_BOOKSHELF);

                mManagementBooksData = new ManagementBooksData(
                        data.getID(),
                        data.getName(),
                        data.getColor(),
                        MyBooksType.BOOKSHELF_MODIFY );

                IntentManagementFactory.getInstance()
                        .readyActivityMode(ActivityMode.MANAGEMENT_MYBOOKS)
                        .setData(mManagementBooksData)
                        .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                        .startActivity();
            }
        });

        mMainMyBooksFragmentDataObserver.settingVocabularyData.observe((AppCompatActivity)mContext, new Observer<Integer>() {
            @Override
            public void onChanged(Integer index)
            {
                Log.f("onSettingVocabulary : "+ index);
                MyVocabularyResult data = mMainInformationResult.getVocabulariesList().get(index);
                Log.f("ID : "+data.getID()+", Name : "+data.getName()+", Color : "+ data.getColor());

                GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                        Common.ANALYTICS_CATEGORY_VOCABULARY,
                        Common.ANALYTICS_ACTION_SELECT_MENU,
                        Common.ANALYTICS_LABEL_MODIFIED_VOCABULARY);
                mManagementBooksData = new ManagementBooksData(
                        data.getID(),
                        data.getName(),
                        data.getColor(),
                        MyBooksType.VOCABULARY_MODIFY
                );
                IntentManagementFactory.getInstance()
                        .readyActivityMode(ActivityMode.MANAGEMENT_MYBOOKS)
                        .setData(mManagementBooksData)
                        .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                        .startActivity();
            }
        });
    }

    private void setupMainClassFragmentListener()
    {
        mMainClassFragmentDataObserver.myClassData.observe((AppCompatActivity) mContext, new Observer<Void>() {
            @Override
            public void onChanged(Void clickMyClass)
            {
                onClickMyClass();
            }
        });

        mMainClassFragmentDataObserver.classEnrollData.observe((AppCompatActivity) mContext, new Observer<Void>() {
            @Override
            public void onChanged(Void clickEnrollData)
            {
                onClickEnroll();
            }
        });

        mMainClassFragmentDataObserver.classNewsData.observe((AppCompatActivity) mContext, new Observer<Void>() {
            @Override
            public void onChanged(Void clickClassNews)
            {
                onClickNews();
            }
        });

        mMainClassFragmentDataObserver.classWhatData.observe((AppCompatActivity) mContext, new Observer<Void>() {
            @Override
            public void onChanged(Void clickClassWhat)
            {
                onClickClassWhat();
            }
        });

        mMainClassFragmentDataObserver.classHistoryData.observe((AppCompatActivity) mContext, new Observer<Void>() {
            @Override
            public void onChanged(Void clickClassHistory)
            {
                onClickPastMySelfHistory();
            }
        });
    }

    /**
     * 앱 실행날짜 와 클래스 진입 날짜가 동일한지 여부 체크
     */
    private boolean isEqualClassExecuteSyncDate()
    {
        String appExecuteDate = (String) CommonUtils.getInstance(mContext).getSharedPreference(Common.PARAMS_APP_EXECUTE_DATE, Common.TYPE_PARAMS_STRING);
        String classExecuteDate = CommonUtils.getInstance(mContext).getTodayDate();

        Log.f("appExecuteDate  : "+appExecuteDate);
        Log.f("classExecuteDate  : "+classExecuteDate);
        if(appExecuteDate.equals(classExecuteDate))
        {
            Log.f("DATE SYNC SUCCESS");
            return true;
        }
        else
        {
            Log.f("DATE SYNC FAIL");
            return false;
        }
    }

    private void onClickMyClass()
    {
        Log.f("");
        if(isEqualClassExecuteSyncDate() == false)
        {
            ((AppCompatActivity)mContext).finish();
            Toast.makeText(mContext, mContext.getResources().getString(R.string.message_class_execute_date_sync_error), Toast.LENGTH_LONG).show();
            IntentManagementFactory.getInstance().initAutoIntroSequence();
            return;
        }

      /* IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.CLASS_RECORDING_PLAYER)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();*/

       if(mMainInformationResult.getClassMainResult().getCurrentMemberStatus().equals(""))
        {
            mMainContractView.showErrorMessage(mContext.getResources().getString(R.string.message_myself_class_not_enroll));
            return;
        }

        if(mMainInformationResult.getClassMainResult().getCurrentMemberStatus().equals(Common.CLASS_MAIN_STATUS_STUDY_NOT_YET))
        {
            mMainContractView.showErrorMessage(mContext.getResources().getString(R.string.message_myself_class_not_open));
            return;
        }

        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.CLASS_MYSELF)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();


    }

    private void onClickEnroll()
    {
        Log.f("");
        if(isEqualClassExecuteSyncDate() == false)
        {
            ((AppCompatActivity)mContext).finish();
            Toast.makeText(mContext, mContext.getResources().getString(R.string.message_class_execute_date_sync_error), Toast.LENGTH_LONG).show();
            IntentManagementFactory.getInstance().initAutoIntroSequence();
            return;
        }
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.CLASS_ENROLL)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void onClickNews()
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.CLASS_NEWS)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void onClickClassWhat()
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.CLASS_WHAT)
                .setData(mMainInformationResult.getClassMainResult().getCouponImageUrl())
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }

    private void onClickPastMySelfHistory()
    {
        Log.f("");
        IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.CLASS_PAST_MYSELF_HISTORY)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity();
    }


    private ItemOptionListener mItemOptionListener = new ItemOptionListener()
    {
        @Override
        public void onClickQuiz()
        {
            Log.f("");
            GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                    Common.ANALYTICS_CATEGORY_HOME,
                    Common.ANALYTICS_ACTION_QUIZ,
                    CommonUtils.getInstance(mContext).getContentsName(mCurrentDetailOptionResult));
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_QUIZ, Common.DURATION_SHORT);
        }

        @Override
        public void onClickTranslate()
        {
            Log.f("");
            GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                    Common.ANALYTICS_CATEGORY_HOME,
                    Common.ANALYTICS_ACTION_ORIGINAL_TRANSLATE,
                    CommonUtils.getInstance(mContext).getContentsName(mCurrentDetailOptionResult));
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_ORIGIN_TRANSLATE, Common.DURATION_SHORT);
        }

        @Override
        public void onClickVocabulary()
        {
            Log.f("");
            GoogleAnalyticsHelper.getInstance(mContext).sendCurrentEvent(
                    Common.ANALYTICS_CATEGORY_HOME,
                    Common.ANALYTICS_ACTION_VOCABULARY,
                    CommonUtils.getInstance(mContext).getContentsName(mCurrentDetailOptionResult));
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_VOCABULARY, Common.DURATION_SHORT);
        }

        @Override
        public void onClickBookshelf()
        {
            Log.f("");
            mSendBookshelfAddList.clear();
            mSendBookshelfAddList.add(mCurrentDetailOptionResult);
            mBottomItemOptionDialog.dismiss();
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_BOOKSHELF_ADD_ITEM_DIALOG, Common.DURATION_SHORT);
        }

        @Override
        public void onClickEbook()
        {
            Log.f("");
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_EBOOK, Common.DURATION_SHORT);
        }

        @Override
        public void onClickGameStarwords()
        {
            // 스타워즈 액티비티 로 이동
            Log.f("");
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_GAME_STARWARS, Common.DURATION_SHORT);
        }

        @Override
        public void onClickGameCrossword()
        {
            Log.f("");
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_START_GAME_CROSSWORD, Common.DURATION_SHORT);
        }

        @Override
        public void onErrorMessage(String message)
        {
           mMainContractView.showErrorMessage(message);
        }
    };

    private AsyncListener mAsyncListener = new AsyncListener()
    {
        @Override
        public void onRunningStart(String code) { }

        @Override
        public void onRunningEnd(String code, Object object)
        {
            BaseResult result = (BaseResult)object;
            if(result == null)
            {
                return;
            }
            Log.f("code : "+ code+", status : " +result.getStatus());
            if(result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                if(code.equals(Common.ASYNC_CODE_BOOKSHELF_CONTENTS_ADD))
                {
                    mMainContractView.hideLoading();
                    MyBookshelfResult myBookshelfResult = ((BookshelfBaseObject)object).getData();
                    updateBookshelfData(myBookshelfResult);
                    updateFragment();

                    Message messsage = Message.obtain();
                    messsage.what   = MESSAGE_COMPLETE_CONTENTS_ADD;
                    messsage.obj    = mContext.getResources().getString(R.string.message_success_save_contents_in_bookshelf);
                    messsage.arg1   = RESULT_OK;
                    mMainHandler.sendMessageDelayed(messsage, Common.DURATION_NORMAL);
                }
                else if(code.equals(Common.ASYNC_CODE_CHANGE_USER)
                        || code.equals(Common.ASYNC_CODE_ME))
                {
                    mUserInformationResult = ((UserInformationBaseObject) result).getData();
                    CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_API_INFORMATION,
                            mUserInformationResult);

                    requestMainInformationAsync();
                }
                else if(code.equals(Common.ASYNC_CODE_IN_APP_BILLING))
                {
                    consumePurchaseData();
                    mMainContractView.showSuccessMessage(mContext.getResources().getString(R.string.message_success_payment_restore));
                    requestAuthMeAsync();
                }
                else if(code.equals(Common.ASYNC_CODE_MAIN))
                {
                    mMainContractView.hideLoading();
                    mMainInformationResult =  ((MainInformationBaseObject)object).getData();
                    CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult);
                    notifyDataChangeAllFragment();
                }
            }
            else
            {
                if(result.isDuplicateLogin())
                {
                    //중복 로그인 시 재시작
                    ((AppCompatActivity)mContext).finish();
                    Toast.makeText(mContext, result.message, Toast.LENGTH_LONG).show();
                    IntentManagementFactory.getInstance().initAutoIntroSequence();
                }
                else if (result.isAuthenticationBroken())
                {
                    Log.f("== isAuthenticationBroken ==");
                    ((AppCompatActivity) mContext).finish();
                    Toast.makeText(mContext, result.message, Toast.LENGTH_LONG).show();
                    IntentManagementFactory.getInstance().initScene();
                }
                else
                {
                    mMainContractView.hideLoading();
                    if(code.equals(Common.ASYNC_CODE_BOOKSHELF_CONTENTS_ADD))
                    {
                        Message messsage = Message.obtain();
                        messsage.what   = MESSAGE_COMPLETE_CONTENTS_ADD;
                        messsage.obj    = result.getMessage();
                        messsage.arg1   = RESULT_CANCELED;
                        mMainHandler.sendMessageDelayed(messsage, Common.DURATION_SHORT);
                    }
                    else if(code.equals(Common.ASYNC_CODE_MAIN))
                    {
                        Log.f("MAIN ERROR");
                        ((AppCompatActivity) mContext).finish();
                        Toast.makeText(mContext, result.message, Toast.LENGTH_LONG).show();

                    }
                    else
                    {
                        mMainContractView.showErrorMessage(result.message);
                    }
                }
            }
        }

        @Override
        public void onRunningCanceled(String code) { }

        @Override
        public void onRunningProgress(String code, Integer progress) { }

        @Override
        public void onRunningAdvanceInformation(String code, Object object) { }

        @Override
        public void onErrorListener(String code, String message)
        {
            mMainHandler.sendEmptyMessage(MESSAGE_APP_SERVER_ERROR);
        }
    };

    private DialogListener mDialogListener = new DialogListener()
    {
        @Override
        public void onConfirmButtonClick(int eventType)
        {
            switch (eventType)
            {
                case DIALOG_EVENT_IAC:
                    Log.f("IAC Link move");

                    if(mMainInformationResult.getInAppCompaignInformation().getButton1Mode().equals(Common.INAPP_CAMPAIGN_MODE_NEWS))
                    {
                        Log.f("새소식 articleID : " + mMainInformationResult.getInAppCompaignInformation().getArticleID());
                        startNewsArticleActivity(String.valueOf(mMainInformationResult.getInAppCompaignInformation().getArticleID()));
                    }
                    else if(mMainInformationResult.getInAppCompaignInformation().getButton1Mode().equals(Common.INAPP_CAMPAIGN_MODE_TESTIMONIAL))
                    {
                        Log.f("활용수기 articleID : " + mMainInformationResult.getInAppCompaignInformation().getArticleID());
                        startTestimonialArticleActivity(String.valueOf(mMainInformationResult.getInAppCompaignInformation().getArticleID()));
                    }
                    else
                    {
                        CommonUtils.getInstance(mContext).startLinkMove(mMainInformationResult.getInAppCompaignInformation().getButton1Link());
                        mIACController.setPositiveButtonClick();
                        CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_IAC_CONTROLLER_INFORMATION, mIACController);
                    }

                    break;
            }
        }

        @Override
        public void onChoiceButtonClick(int buttonType, int eventType)
        {
            Log.f("eventType : "+eventType+", buttonType : "+ buttonType);
            switch (eventType)
            {
                case DIALOG_EVENT_IAC:
                    if(buttonType == TempleteAlertDialog.DEFAULT_BUTTON_TYPE_1)
                    {
                        Log.f("IAC Link move");
                        if(mMainInformationResult.getInAppCompaignInformation().getButton1Mode().equals(Common.INAPP_CAMPAIGN_MODE_NEWS))
                        {
                            Log.f("articleID : " + mMainInformationResult.getInAppCompaignInformation().getArticleID());
                            startNewsArticleActivity(String.valueOf(mMainInformationResult.getInAppCompaignInformation().getArticleID()));
                        }
                        else if(mMainInformationResult.getInAppCompaignInformation().getButton1Mode().equals(Common.INAPP_CAMPAIGN_MODE_TESTIMONIAL))
                        {
                            Log.f("활용수기 articleID : " + mMainInformationResult.getInAppCompaignInformation().getArticleID());
                            startTestimonialArticleActivity(String.valueOf(mMainInformationResult.getInAppCompaignInformation().getArticleID()));
                        }
                        else
                        {
                            CommonUtils.getInstance(mContext).startLinkMove(mMainInformationResult.getInAppCompaignInformation().getButton1Link());
                            mIACController.setPositiveButtonClick();
                            CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_IAC_CONTROLLER_INFORMATION, mIACController);
                        }

                    }
                    else if(buttonType == TempleteAlertDialog.DEFAULT_BUTTON_TYPE_2)
                    {
                        Log.f("IAC Cancel");
                        mIACController.setCloseButtonClick();
                        mIACController.setSaveIACInformation(mAwakeItemData);
                        CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_IAC_CONTROLLER_INFORMATION, mIACController);
                    }
                    break;

                case DIALOG_EVENT_LOGOUT:
                    if(buttonType == TempleteAlertDialog.DEFAULT_BUTTON_TYPE_2)
                    {
                        Log.f("============ LOGOUT COMPLETE ============");
                        IntentManagementFactory.getInstance().initScene();
                    }
                    break;
                case DIALOG_EVENT_APP_END:
                    if(buttonType == TempleteAlertDialog.DEFAULT_BUTTON_TYPE_2)
                    {
                        Log.f("============ APP END ============");
                        ((AppCompatActivity)mContext).finish();
                    }
                    break;
            }

        }
    };

    private BookAddListener mBookAddListener = new BookAddListener()
    {
        @Override
        public void onClickBook(int index)
        {
            Log.f("");

            mCurrentBookshelfAddResult = mMainInformationResult.getBookShelvesList().get(index);
            mMainHandler.sendEmptyMessageDelayed(MESSAGE_REQUEST_CONTENTS_ADD, Common.DURATION_SHORT);
        }
    };


}
