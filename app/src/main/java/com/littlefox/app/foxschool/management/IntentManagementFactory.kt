package com.littlefox.app.foxschool.management

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Message
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.flashcard.FlashcardDataObject
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesBaseResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.main.*
import com.littlefox.app.foxschool.main.webview.*
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.logmonitor.Log

import java.util.*

/**
 * 모든 Intent Activity를 관리하는 클래스
 */
class IntentManagementFactory
{
    private val MESSAGE_EXECUTE_POSSIBLE: Int = 100
    private lateinit var mContext : Context
    private var mCurrentExecuteMode : ActivityMode = ActivityMode.MAIN
    private var mCurrentExecuteAnimationMode : AnimationMode = AnimationMode.NO_ANIATION
    private var mCurrentExecuteRequestCode = -1
    private var mCurrentExecuteData : Any? = null
    private var mCurrentExecuteIntentFlag = -1
    private var mCurrentViewPair : Pair<View, String>? = null
    private var mSync = Any()
    private var isRunning = false
    private val mHandler : Handler = object : Handler()
    {
        override fun handleMessage(msg : Message)
        {
            when(msg.what)
            {
                MESSAGE_EXECUTE_POSSIBLE -> isRunning = false
            }
        }
    }

    fun setCurrentActivity(context : Context)
    {
        mContext = context
    }

    /**
     * 실행할 액티비티의 화면의 코드 ( 무조건 제일 먼저 선언 해야한다. 데이터 초기화 로직 들어가 있음 )
     *
     * @param mode 화면의 코드
     * @return IntentManagementFactory
     */
    fun readyActivityMode(mode : ActivityMode) : IntentManagementFactory
    {
        init()
        mCurrentExecuteMode = mode
        return this
    }

    /**
     * 실행할 액티비티에 애니메이션 모드를 세팅한다.
     *
     * @param animationMode 세팅할 애니메이션 모드
     * @return IntentManagementFactory
     */
    fun setAnimationMode(animationMode : AnimationMode) : IntentManagementFactory
    {
        mCurrentExecuteAnimationMode = animationMode
        return this
    }

    /**
     * 기존 액티비티가 받아야 하는 requestCode 를 세팅한다.
     *
     * @param requestCode 액티비티가 받아야 하는 requestCode
     * @return IntentManagementFactory
     */
    fun setRequestCode(requestCode : Int) : IntentManagementFactory
    {
        mCurrentExecuteRequestCode = requestCode
        return this
    }

    /**
     * 실행할 액티비티에게 데이터를 전달한다.
     *
     * @param object 전달할 데이터
     * @return IntentManagementFactory
     */
    fun setData(`object` : Any?) : IntentManagementFactory
    {
        mCurrentExecuteData = `object`
        return this
    }

    /**
     * 실행할 액티비티의 Intent Type Flag를 설정한다.
     *
     * @param flag Intent Add flag
     * @return IntentManagementFactory
     */
    fun setIntentFlag(flag : Int) : IntentManagementFactory
    {
        mCurrentExecuteIntentFlag = flag
        return this
    }

    fun setViewPair(pair : Pair<View, String>?) : IntentManagementFactory
    {
        mCurrentViewPair = pair
        return this
    }

    /**
     * 액티비티를 실행한다. ( 단, 설정들을 하지 않고 실행시 제대로 동작 안함 )
     */
    fun startActivity()
    {
        Log.f("isRunning : $isRunning")
        if(isRunning)
        {
            return
        }
        synchronized(mSync) {
            isRunning = true
            startActivity(mCurrentExecuteMode, mCurrentExecuteAnimationMode, mCurrentExecuteRequestCode, mCurrentExecuteData, mCurrentExecuteIntentFlag)
            mHandler.sendEmptyMessageDelayed(MESSAGE_EXECUTE_POSSIBLE, Common.DURATION_NORMAL)
        }
    }

    fun initScene()
    {
        Log.f("")
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_AUTO_LOGIN_DATA, "N")
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, "")
        CommonUtils.getInstance(mContext).setPreferenceObject(Common.PARAMS_USER_LOGIN, null)
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_DISPOSABLE_LOGIN, false)
        startActivity(ActivityMode.INTRO, AnimationMode.REVERSE_NORMAL_ANIMATION, -1, null, Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    fun initAutoIntroSequence()
    {
        Log.f("")
        MainObserver.clearAll()
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_DISPOSABLE_LOGIN, true)
        startActivity(ActivityMode.INTRO, AnimationMode.REVERSE_NORMAL_ANIMATION, -1, null, Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    fun clearLogin()
    {
        Log.f("")
        val isLoginFromMain = true
        MainObserver.clearAll()
        startActivity(ActivityMode.LOGIN, AnimationMode.NORMAL_ANIMATION, -1, isLoginFromMain, Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    private fun startActivity(mode : ActivityMode, animationMode : AnimationMode, requestCode : Int, `object` : Any?, addFlag : Int)
    {
        Log.f("executeMode : $mode, requestCode : $requestCode, addFlag : $addFlag")
        mCurrentExecuteAnimationMode = animationMode
        var intent : Intent? = null
        when(mode)
        {
            ActivityMode.INTRO -> intent = Intent(mContext, IntroActivity::class.java)

            ActivityMode.LOGIN ->
            {
                intent = Intent(mContext, LoginActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_IS_LOGIN_FROM_MAIN, `object` as Boolean?)
                }
            }

            ActivityMode.MAIN -> intent = Intent(mContext, MainActivity::class.java)

            ActivityMode.SERIES_DETAIL_LIST ->
            {
                intent = Intent(mContext, SeriesContentsListActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_STORY_SERIES_DATA, `object` as SeriesBaseResult?)
                }
            }

            ActivityMode.STORY_CATEGORY_LIST ->
            {
                intent = Intent(mContext, StoryCategoryListActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_STORY_CATEGORY_DATA, `object` as SeriesBaseResult?)
                }
            }

            ActivityMode.PLAYER ->
            {
                intent = Intent(mContext, PlayerHlsActivity::class.java)
                if(`object` != null)
                {
                    intent.putParcelableArrayListExtra(Common.INTENT_PLAYER_DATA_PARAMS, `object` as ArrayList<ContentsBaseResult?>?)
                }
            }

            ActivityMode.SEARCH -> intent = Intent(mContext, SearchListActivity::class.java)

            ActivityMode.INTRODUCE_SERIES ->
            {
                intent = Intent(mContext, IntroduceSeriesActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_SERIES_INFORMATION_ID, `object` as String?)
                }
            }

            ActivityMode.VOCABULARY ->
            {
                intent = Intent(mContext, VocabularyActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_VOCABULARY_DATA, `object` as MyVocabularyResult?)
                }
            }

            ActivityMode.QUIZ ->
            {
                intent = Intent(mContext, QuizActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_QUIZ_PARAMS, `object` as String?)
                }
            }

            ActivityMode.MY_INFORMATION -> intent = Intent(mContext, MyInformationActivity::class.java)

            ActivityMode.APP_USE_GUIDE -> intent = Intent(mContext, AppUseGuideActivity::class.java)

            ActivityMode.FLASHCARD ->
            {
                intent = Intent(mContext, FlashCardActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_FLASHCARD_DATA, `object` as FlashcardDataObject?)
                }
            }

            ActivityMode.FOXSCHOOL_NEWS -> intent = Intent(mContext, FoxSchoolNewsActivity::class.java)

            ActivityMode.FAQS -> intent = Intent(mContext, FAQActivity::class.java)

            ActivityMode.INQUIRE -> intent = Intent(mContext, InquireActivity::class.java)

            ActivityMode.WEBVIEW_LEARNING_LOG -> intent = Intent(mContext, WebviewLearningLogActivity::class.java)

            ActivityMode.WEBVIEW_POLICY_PRIVACY -> intent = Intent(mContext, WebviewPolicyPrivacyActivity::class.java)

            ActivityMode.WEBVIEW_POLICY_TERMS -> intent = Intent(mContext, WebviewPolicyTermsActivity::class.java)

            ActivityMode.WEBVIEW_GAME_STARWORDS ->
            {
                intent = Intent(mContext, WebviewGameStarwordsActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_GAME_STARWORDS_ID, `object` as String?)
                }
            }

            /*ActivityMode.INTRO ->
                intent = Intent(mContext, IntroActivity::class.java)
            ActivityMode.LOGIN ->
            {
                intent = Intent(mContext, LoginActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_IS_LOGIN_FROM_MAIN, `object` as Boolean?)
                }
            }

            PLAYER ->
            {
                val isProgressivePlay = CommonUtils.getInstance(mContext).getSharedPreference(Common.PARAMS_IS_FORCE_PROGRESSIVE_PLAY, Common.TYPE_PARAMS_BOOLEAN) as Boolean
                if(isProgressivePlay)
                {
                    intent = Intent(mContext, PlayerProgressDownloadActivity::class.java)
                }
                else
                {
                    intent = Intent(mContext, PlayerHlsActivity::class.java)
                }
                intent.putParcelableArrayListExtra(Common.INTENT_PLAYER_DATA_PARAMS, `object` as ArrayList<ContentsBaseResult?>?)
            }
            QUIZ ->
            {
                intent = Intent(mContext, QuizActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_QUIZ_PARAMS, `object` as String?)
                }
            }
            MANAGEMENT_MYBOOKS ->
            {
                intent = Intent(mContext, ManagementMyBooksActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_MANAGEMENT_MYBOOKS_DATA, `object` as ManagementBooksData?)
                }
            }
            SERIES_DETAIL_LIST ->
            {
                intent = Intent(mContext, SeriesContentsListActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_STORY_SERIES_DATA, `object` as SeriesBaseResult?)
                }
            }
            STORY_CATEGORY_LIST ->
            {
                intent = Intent(mContext, StoryCategoryListActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_STORY_CATEGORY_DATA, `object` as SeriesBaseResult?)
                }
            }
            BOOKSHELF ->
            {
                intent = Intent(mContext, BookshelfActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_BOOKSHELF_DATA, `object` as MyBookshelfResult?)
                }
            }
            INTRODUCE_SERIES ->
            {
                intent = Intent(mContext, IntroduceSeriesActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_SERIES_INFORMATION_ID, `object` as String?)
                }
            }
            NEWS ->
            {
                intent = Intent(mContext, NewsActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_NEWS_ARTICLE_ID, `object` as String?)
                }
            }
            TESTIMONIAL ->
            {
                intent = Intent(mContext, TestimonialAcvitity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_TESTIMONIAL_ARTICLE_ID, `object` as String?)
                }
            }
            MY_INFORMATION ->
            {
                intent = Intent(mContext, WebviewMyInformationActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_ADD_USER, `object` as String?)
                }
            }
            APP_USE_GUIDE -> intent = Intent(mContext, AppUseGuideActivity::class.java)
            VOCABULARY ->
            {
                intent = Intent(mContext, VocabularyActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_VOCABULARY_DATA, `object` as MyVocabularyResult?)
                }
            }
            SEARCH -> intent = Intent(mContext, SearchListActivity::class.java)
            WEBVIEW_INTRODUCE -> intent = Intent(mContext, WebviewIntroduceActivity::class.java)
            WEBVIEW_STUDY_GUIDE -> intent = Intent(mContext, WebviewStudyGuideActivity::class.java)
            WEBVIEW_ORIGIN_TRANSLATE ->
            {
                intent = Intent(mContext, WebviewOriginTranslateActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_ORIGIN_TRANSLATE_ID, `object` as String?)
                }
            }
            WEBVIEW_EBOOK ->
            {
                intent = Intent(mContext, WebviewEbookActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_EBOOK_ID, `object` as String?)
                }
            }
            WEBVIEW_ATTENDANCE -> intent = Intent(mContext, WebviewAttendanceActivity::class.java)
            WEBVIEW_PUBLISH_SCHEDULE -> intent = Intent(mContext, WebviewPublishScheduleActivity::class.java)
            WEBVIEW_USER_FIND_INFORMATION ->
            {
                intent = Intent(mContext, WebviewUserFindInformationActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_FIND_INFORMATION, `object` as FindType?)
                }
            }
            WEBVIEW_1ON1_ASK -> intent = Intent(mContext, Webview1On1AskActivity::class.java)
            WEBVIEW_POLICY_PRIVACY -> intent = Intent(mContext, WebviewPolicyPrivacyActivity::class.java)
            WEBVIEW_POLICY_TERMS -> intent = Intent(mContext, WebviewPolicyTermsActivity::class.java)
            WEBVIEW_SERVICE_INTRODUCE -> intent = Intent(mContext, WebviewServiceIntroduceActivity::class.java)
            WEBVIEW_FAQS -> intent = Intent(mContext, WebviewFAQActivity::class.java)
            WEBVIEW_LEARNING_LOG -> intent = Intent(mContext, WebviewLearningLogActivity::class.java)
            WEBVIEW_INACTIVE_ACCOUNT -> intent = Intent(mContext, WebviewInActiveAccountActivity::class.java)
            WEBVIEW_GAME_STARWORDS ->
            {
                intent = Intent(mContext, WebviewGameStarwordsActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_GAME_STARWORDS_ID, `object` as String?)
                }
            }
            WEBVIEW_GAME_CROSSWORD ->
            {
                intent = Intent(mContext, WebviewGameCrosswordActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_GAME_CROSSWORD_ID, `object` as String?)
                }
            }*/
        }
        if(addFlag != -1)
        {
            intent?.addFlags(addFlag)
        }
        when(animationMode)
        {
            AnimationMode.NO_ANIATION ->
                if(requestCode == -1)
                        (mContext as Activity).startActivity(intent)
                else
                        (mContext as Activity).startActivityForResult(intent, requestCode)

            AnimationMode.NORMAL_ANIMATION ->
            {
                if(requestCode == -1)
                        (mContext as Activity).startActivity(intent)
                else
                        (mContext as Activity).startActivityForResult(intent, requestCode)
                (mContext as Activity).overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
            }

            AnimationMode.REVERSE_NORMAL_ANIMATION ->
            {
                if(requestCode == -1)
                        (mContext as Activity).startActivity(intent)
                else
                        (mContext as Activity).startActivityForResult(intent, requestCode)
                (mContext as Activity).overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
            }
            AnimationMode.METERIAL_ANIMATION -> try
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    val options : ActivityOptionsCompat
                    options = if(mCurrentViewPair != null)
                    {
                        ActivityOptionsCompat.makeSceneTransitionAnimation(mContext as Activity, mCurrentViewPair)
                    }
                    else
                    {
                        ActivityOptionsCompat.makeSceneTransitionAnimation(mContext as Activity)
                    }
                    if(requestCode == -1)
                        ActivityCompat.startActivity(mContext as Activity, intent!!, options.toBundle())
                    else
                        ActivityCompat.startActivityForResult(mContext as Activity, intent!!, requestCode, options.toBundle())
                }
                else
                {
                    if(requestCode == -1)
                            (mContext as Activity).startActivity(intent)
                    else
                            (mContext as Activity).startActivityForResult(intent, requestCode)
                    (mContext as Activity).overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
                }
            }
            catch(e : NoSuchMethodError)
            {
                if(requestCode == -1)
                        (mContext as Activity).startActivity(intent)
                else
                        (mContext as Activity).startActivityForResult(intent, requestCode)
                (mContext as Activity).overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
            }
        }
    }

    private fun init()
    {
        mCurrentExecuteMode = ActivityMode.MAIN
        mCurrentExecuteAnimationMode = AnimationMode.NO_ANIATION
        mCurrentExecuteRequestCode = -1
        mCurrentExecuteData = null
        mCurrentExecuteIntentFlag = -1
        mCurrentViewPair = null
    }

    companion object
    {
        var sIntentManagementFactory : IntentManagementFactory? = null
        fun getInstance() : IntentManagementFactory
        {
            if(sIntentManagementFactory == null)
            {
                sIntentManagementFactory = IntentManagementFactory();
            }

            return sIntentManagementFactory!!
        }
    }

}