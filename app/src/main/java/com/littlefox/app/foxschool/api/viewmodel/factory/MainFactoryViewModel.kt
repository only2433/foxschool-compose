package com.littlefox.app.foxschool.api.viewmodel.factory

import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.bookshelf.ManagementBooksData
import com.littlefox.app.foxschool.`object`.data.iac.AwakeItemData
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.main.InAppCompaignResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesBaseResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.adapter.MainFragmentSelectionPagerAdapter
import com.littlefox.app.foxschool.api.base.BaseFactoryViewModel
import com.littlefox.app.foxschool.api.viewmodel.api.MainApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.*
import com.littlefox.app.foxschool.fragment.MainMyBooksFragment
import com.littlefox.app.foxschool.fragment.MainSongFragment
import com.littlefox.app.foxschool.fragment.MainStoryFragment
import com.littlefox.app.foxschool.iac.IACController
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainFactoryViewModel @Inject constructor(private val apiViewModel : MainApiViewModel) : BaseFactoryViewModel()
{
    companion object
    {
        const val DIALOG_TYPE_IAC : Int                        = 10001
        const val DIALOG_TYPE_LOGOUT : Int                     = 10002
        const val DIALOG_TYPE_APP_END : Int                    = 10003
        const val DIALOG_TYPE_NOT_HAVE_CLASS : Int             = 10004
    }

    private val _settingViewPager = SingleLiveEvent<MainFragmentSelectionPagerAdapter>()
    val settingViewPager: LiveData<MainFragmentSelectionPagerAdapter> get() = _settingViewPager

    private val _settingMenuView = SingleLiveEvent<Pair<Boolean, Boolean>>()
    val settingMenuView: LiveData<Pair<Boolean, Boolean>> get() = _settingMenuView

    private val _settingUserInformation = SingleLiveEvent<LoginInformationResult?>()
    val settingUserInformation: LiveData<LoginInformationResult?> get() = _settingUserInformation

    private val _showAppEndDialog = SingleLiveEvent<Void>()
    val showAppEndDialog: LiveData<Void> get() = _showAppEndDialog

    private val _showNoClassStudentDialog = SingleLiveEvent<Void>()
    val showNoClassStudentDialog: LiveData<Void> get() = _showNoClassStudentDialog

    private val _showNoClassTeacherDialog = SingleLiveEvent<Void>()
    val showNoClassTeacherDialog: LiveData<Void> get() = _showNoClassTeacherDialog

    private val _showIACInformationDialog = SingleLiveEvent<InAppCompaignResult>()
    val showIACInformationDialog: LiveData<InAppCompaignResult> get() = _showIACInformationDialog

    // [동화 Fragment] 업데이트
    private val _updateStoryData = SingleLiveEvent<MainInformationResult>()
    val updateStoryData : LiveData<MainInformationResult> get() = _updateStoryData

    // [동요 Fragment] 업데이트
    private val _updateSongData = SingleLiveEvent<MainInformationResult>()
    val updateSongData : LiveData<MainInformationResult> get() = _updateSongData

    // [책장 Fragment] 업데이트
    private val _updateMyBooksData = SingleLiveEvent<MainInformationResult>()
    val updateMyBooksData : LiveData<MainInformationResult> get() = _updateMyBooksData

    private lateinit var mContext : Context

    private lateinit var mMainFragmentSelectionPagerAdapter : MainFragmentSelectionPagerAdapter
    private lateinit var mFragmentList : List<Fragment>

    private var mLoginInformationResult : LoginInformationResult? = null
    private lateinit var mMainInformationResult : MainInformationResult
    private var mIACController : IACController? = null
    private lateinit var mAwakeItemData : AwakeItemData
    private var mManagementBooksData : ManagementBooksData? = null

    override fun init(context : Context)
    {
        mContext = context
        setupViewModelObserver()

        MainObserver.clearAll()

        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        mLoginInformationResult = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult?

        mMainFragmentSelectionPagerAdapter = MainFragmentSelectionPagerAdapter((mContext as AppCompatActivity).getSupportFragmentManager())
        mMainFragmentSelectionPagerAdapter.addFragment(MainStoryFragment.instance)
        mMainFragmentSelectionPagerAdapter.addFragment(MainSongFragment.instance)
        mMainFragmentSelectionPagerAdapter.addFragment(MainMyBooksFragment.instance)
        mFragmentList = mMainFragmentSelectionPagerAdapter.pagerFragmentList

        _settingViewPager.value = mMainFragmentSelectionPagerAdapter
        _settingMenuView.value = Pair(mMainInformationResult.isUpdateHomework, mMainInformationResult.isUpdateNews)
        _settingUserInformation.value = mLoginInformationResult
        initIACInformation()

        setAppExecuteDate()
    }

    override fun setupViewModelObserver() { }

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
    }

    fun onBackPressed()
    {
        Log.f("Check End App")
        _showAppEndDialog.call()
    }

    private fun setAppExecuteDate()
    {
        val date : String = CommonUtils.getInstance(mContext).getTodayDateText()
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_APP_EXECUTE_DATE, date)
        Log.f("date : $date")
    }

    private fun updateUserInformation()
    {
        Log.f("update Status : " + MainObserver.isUpdateUserStatus())
        if(MainObserver.isUpdateUserStatus())
        {
            mLoginInformationResult = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult
            _settingUserInformation.value = mLoginInformationResult
            _settingMenuView.value = Pair(mMainInformationResult.isUpdateHomework, mMainInformationResult.isUpdateNews)
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
                    Common.PAGE_STORY -> _updateStoryData.value = mMainInformationResult
                    Common.PAGE_SONG -> _updateSongData.value = mMainInformationResult
                    Common.PAGE_MY_BOOKS -> _updateMyBooksData.value = mMainInformationResult
                }
            }
            MainObserver.clearAll()
        }
    }

    private fun initIACInformation()
    {
        if(isVisibleIACData)
        {
            Log.f("IAC VISIBLE")
            _showIACInformationDialog.value = mMainInformationResult.getInAppCompaignInformation()!!
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

    fun setLogout()
    {
        Log.f("============ LOGOUT COMPLETE ============")
        IntentManagementFactory.getInstance().initScene()
    }

    fun onClickIACLink(articleID : String)
    {
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.FOXSCHOOL_NEWS)
            .setData(articleID)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    fun onClickIACPositiveButton()
    {
        mIACController?.setPositiveButtonClick()
        CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_IAC_CONTROLLER_INFORMATION, mIACController)
    }

    fun onClickIACCloseButton()
    {
        mIACController?.setCloseButtonClick()
        mIACController?.setSaveIACInformation(mAwakeItemData)
        CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_IAC_CONTROLLER_INFORMATION, mIACController)
    }

    fun onClickMenuMyInformation()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.MY_INFORMATION)
            .setData("N")
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    fun onClickMenuLearningLog()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_LEARNING_LOG)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    fun onClickRecordHistory()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.RECORD_HISTORY)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    fun onClickMenuHomeworkManage()
    {
        Log.f("")
        if (CommonUtils.getInstance(mContext).isTeacherMode == false)
        {
            // 학생
            if(mLoginInformationResult!!.getSchoolInformation().isHaveClass())
            {
                // 학급정보 있는 경우 화면 이동
                IntentManagementFactory.getInstance()
                    .readyActivityMode(ActivityMode.HOMEWORK_MANAGE)
                    .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                    .startActivity()
            }
            else
            {
                _showNoClassStudentDialog.call()
            }
        }
        else
        {
            // 선생님
            if(mLoginInformationResult!!.getUserInformation().isHaveClass())
            {
                // 학급정보 있는 경우 화면 이동
                IntentManagementFactory.getInstance()
                    .readyActivityMode(ActivityMode.HOMEWORK_MANAGE)
                    .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                    .startActivity()
            }
            else
            {
                _showNoClassTeacherDialog.call()
            }
        }
    }

    fun onClickFoxschoolNews()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.FOXSCHOOL_NEWS)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    fun onClickMenuFAQ()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.FAQS)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    fun onClickMenu1On1Ask()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.INQUIRE)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    fun onClickMenuAppUseGuide()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.APP_USE_GUIDE)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    fun onClickMenuTeacherManual()
    {
        Log.f("")
        CommonUtils.getInstance(mContext).downloadFileToExternalPublicDir(
            mMainInformationResult.getFileInformation()!!.getTeacherManualLink(),
            Common.FILE_TEACHER_MANUAL
        )
    }

    fun onClickMenuHomeNewsPaper()
    {
        Log.f("")
        CommonUtils.getInstance(mContext).downloadFileToExternalPublicDir(
            mMainInformationResult.getFileInformation()!!.getHomeNewsPaperLink(),
            Common.FILE_HOME_NEWSPAPER
        )
    }

    fun onClickSearch()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.SEARCH)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /** =============== [동화 Fragment] 이벤트 =============== */
    fun onClickStoryLevelsItem(seriesInformationResult : SeriesInformationResult, selectView : View)
    {
        Log.f("onClick StoryLevelsItem")
        val pair = androidx.core.util.Pair<View, String>(
            selectView,
            Common.STORY_DETAIL_LIST_HEADER_IMAGE
        )

        seriesInformationResult.setTransitionType(TransitionType.PAIR_IMAGE)
        seriesInformationResult.setSeriesType(Common.CONTENT_TYPE_STORY)
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.SERIES_DETAIL_LIST)
            .setData(seriesInformationResult as SeriesBaseResult)
            .setViewPair(pair)
            .setAnimationMode(AnimationMode.METERIAL_ANIMATION)
            .startActivity()
    }

    fun onClickStoryCategoriesItem(seriesInformationResult : SeriesInformationResult, selectView : View)
    {
        Log.f("onClick StoryCategoryItem")
        val pair = androidx.core.util.Pair<View, String>(
            selectView,
            Common.CATEGORY_DETAIL_LIST_HEADER_IMAGE
        )

        seriesInformationResult.setTransitionType(TransitionType.PAIR_IMAGE)
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.STORY_CATEGORY_LIST)
            .setData(seriesInformationResult as SeriesBaseResult)
            .setViewPair(pair)
            .setAnimationMode(AnimationMode.METERIAL_ANIMATION)
            .startActivity()
    }

    /** =============== [동요 Fragment] 이벤트 =============== */
    fun onClickSongCategoriesItem(seriesInformationResult : SeriesInformationResult, selectView : View)
    {
        Log.f("onClick SongCategoriesItem")
        val pair = androidx.core.util.Pair<View, String>(
            selectView,
            Common.STORY_DETAIL_LIST_HEADER_IMAGE
        )
        seriesInformationResult.setTransitionType(TransitionType.PAIR_IMAGE)
        seriesInformationResult.setSeriesType(Common.CONTENT_TYPE_SONG)
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.SERIES_DETAIL_LIST)
            .setData(seriesInformationResult as SeriesBaseResult)
            .setViewPair(pair).setAnimationMode(AnimationMode.METERIAL_ANIMATION)
            .startActivity()
    }

    /** =============== [책장 Fragment] 이벤트 =============== */
    fun onAddBookshelf()
    {
        Log.f("onAddBookshelf")
        if(mMainInformationResult.getBookShelvesList().size > Common.MAX_BOOKSHELF_SIZE)
        {
            _errorMessage.value = mContext.resources.getString(R.string.message_maximum_bookshelf)
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
    }

    fun onAddVocabulary()
    {
        Log.f("onAddVocabulary")
        if(mMainInformationResult.getVocabulariesList().size > Common.MAX_VOCABULARY_SIZE)
        {
            _errorMessage.value = mContext.resources.getString(R.string.message_maximum_vocabulary)
        }
        else
        {
            mManagementBooksData = ManagementBooksData(MyBooksType.VOCABULARY_ADD)
            IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.MANAGEMENT_MYBOOKS)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .setData(mManagementBooksData).startActivity()
        }
    }

    fun onSettingBookshelf(index : Int)
    {
        Log.f("onSettingBookshelf : $index")
        val data : MyBookshelfResult = mMainInformationResult.getBookShelvesList().get(index)
        Log.f("ID : " + data.getID().toString() + ", Name : " + data.getName().toString() + ", Color : " + data.getColor())
        mManagementBooksData = ManagementBooksData(data.getID(), data.getName(), data.getColor(), MyBooksType.BOOKSHELF_MODIFY)
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.MANAGEMENT_MYBOOKS)
            .setData(mManagementBooksData)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    fun onSettingVocabulary(index : Int)
    {
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
    }

    fun onEnterBookshelfList(index : Int)
    {
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
            _errorMessage.value = mContext.resources.getString(R.string.message_empty_bookshelf_contents)
        }
    }

    fun onEnterVocabularyList(index : Int)
    {
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
            _errorMessage.value = mContext.resources.getString(R.string.message_empty_vocabulary_contents)
        }
    }
}