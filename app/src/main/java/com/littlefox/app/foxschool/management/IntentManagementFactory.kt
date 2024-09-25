package com.littlefox.app.foxschool.management

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Message
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.bookshelf.ManagementBooksData
import com.littlefox.app.foxschool.`object`.data.flashcard.FlashcardDataObject
import com.littlefox.app.foxschool.`object`.data.player.PlayerIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.quiz.QuizIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.record.RecordIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.webview.WebviewIntentParamsObject
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkCheckingIntentParamsObject
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesBaseResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.FindType
import com.littlefox.app.foxschool.main.*
import com.littlefox.app.foxschool.main.webview.*
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.app.foxschool.presentation.screen.intro.IntroActivity
import com.littlefox.app.foxschool.presentation.screen.login.LoginActivity
import com.littlefox.app.foxschool.presentation.screen.main.MainActivity
import com.littlefox.app.foxschool.presentation.screen.series_contents_list.SeriesContentsListActivity

import com.littlefox.logmonitor.Log

/**
 * 모든 Intent Activity를 관리하는 클래스
 */
class IntentManagementFactory
{
    private val MESSAGE_EXECUTE_POSSIBLE: Int = 100
    private lateinit var mContext : Context
    private var mCurrentExecuteMode : ActivityMode = ActivityMode.MAIN
    private var mCurrentExecuteAnimationMode : AnimationMode = AnimationMode.NO_ANIATION
    private var mCurrentExecuteData : Any? = null
    private var mCurrentExecuteIntentFlag = -1
    private var mCurrentViewPair : Pair<View, String>? = null
    private var mSync = Any()
    private var isRunning = false
    private var mCurrentExecuteResultLauncher : ActivityResultLauncher<Intent?>? = null
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
     * 다음 액티비티에서 받아야할 데이터를 참조하기 위한 ActivityResultLauncher룰 세팅하여 기존의 액티비티에서 값을 받게끔 한다.
     * @param launcher  액티비티에서 받아야할 데이터를 참조하기 위한 ActivityResultLauncher
     * @return IntentManagementFactory
     */
    fun setResultLauncher(launcher : ActivityResultLauncher<Intent?>?) : IntentManagementFactory
    {
        mCurrentExecuteResultLauncher = launcher
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
            startActivity(mCurrentExecuteMode, mCurrentExecuteAnimationMode, mCurrentExecuteResultLauncher, mCurrentExecuteData, mCurrentExecuteIntentFlag)
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
        startActivity(ActivityMode.INTRO, AnimationMode.REVERSE_NORMAL_ANIMATION, null, null, Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    fun initAutoIntroSequence()
    {
        Log.f("")
        MainObserver.clearAll()
        CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_IS_DISPOSABLE_LOGIN, true)
        startActivity(ActivityMode.INTRO, AnimationMode.REVERSE_NORMAL_ANIMATION, null, null, Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    fun clearLogin()
    {
        Log.f("")
        val isLoginFromMain = true
        MainObserver.clearAll()
        startActivity(ActivityMode.LOGIN, AnimationMode.NORMAL_ANIMATION, null, isLoginFromMain, Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    private fun startActivity(mode : ActivityMode, animationMode : AnimationMode, launcher : ActivityResultLauncher<Intent?>?, `object` : Any?, addFlag : Int)
    {
        Log.f("executeMode : $mode, launcher : $launcher, addFlag : $addFlag")
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
                    intent.putExtra(Common.INTENT_PLAYER_DATA_PARAMS, `object` as PlayerIntentParamsObject?)
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

            ActivityMode.BOOKSHELF ->
            {
                intent = Intent(mContext, BookshelfActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_BOOKSHELF_DATA, `object` as MyBookshelfResult?)
                }
            }

            ActivityMode.MANAGEMENT_MYBOOKS ->
            {
                intent = Intent(mContext, ManagementMyBooksActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_MANAGEMENT_MYBOOKS_DATA, `object` as ManagementBooksData?)
                }
            }

            ActivityMode.QUIZ ->
            {
                intent = Intent(mContext, QuizActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_QUIZ_PARAMS, `object` as QuizIntentParamsObject?)
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

            ActivityMode.RECORD_PLAYER ->
            {
                intent = Intent(mContext, RecordPlayerActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_RECORD_PLAYER_DATA, `object` as RecordIntentParamsObject?)
                }
            }

            ActivityMode.HOMEWORK_MANAGE ->
            {
                if (CommonUtils.getInstance(mContext).isTeacherMode)
                {
                    intent = Intent(mContext, TeacherHomeworkManageActivity::class.java)
                }
                else
                {
                    intent = Intent(mContext, StudentHomeworkManageActivity::class.java)
                }
            }

            ActivityMode.HOMEWORK_CHECKING ->
            {
                intent = Intent(mContext, TeacherHomeworkCheckingActivity::class.java)
                if (`object` != null)
                {
                    intent.putExtra(Common.INTENT_HOMEWORK_CHECKING_DATA, `object` as HomeworkCheckingIntentParamsObject?)
                }
            }

            ActivityMode.RECORD_HISTORY -> intent = Intent(mContext, RecordHistoryActivity::class.java)

            ActivityMode.WEBVIEW_LEARNING_LOG -> intent = Intent(mContext, WebviewLearningLogActivity::class.java)

            ActivityMode.WEBVIEW_POLICY_PRIVACY -> intent = Intent(mContext, WebviewPolicyPrivacyActivity::class.java)

            ActivityMode.WEBVIEW_POLICY_TERMS -> intent = Intent(mContext, WebviewPolicyTermsActivity::class.java)

            ActivityMode.WEBVIEW_GAME_STARWORDS ->
            {
                intent = Intent(mContext, WebviewGameStarwordsActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_GAME_STARWORDS_ID, `object` as WebviewIntentParamsObject?)
                }
            }

            ActivityMode.WEBVIEW_GAME_CROSSWORD ->
            {
                intent = Intent(mContext, WebviewGameCrosswordActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_GAME_CROSSWORD_ID, `object` as WebviewIntentParamsObject?)
                }
            }

            ActivityMode.WEBVIEW_EBOOK ->
            {
                intent = Intent(mContext, WebviewEbookActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_EBOOK_DATA, `object` as WebviewIntentParamsObject?)
                }
            }

            ActivityMode.WEBVIEW_ORIGIN_TRANSLATE ->
            {
                intent = Intent(mContext, WebviewOriginTranslateActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_ORIGIN_TRANSLATE_ID, `object` as String?)
                }
            }

            ActivityMode.WEBVIEW_FOXSCHOOL_INTRODUCE -> intent = Intent(mContext, WebviewFoxSchoolIntroduceActivity::class.java)

            ActivityMode.WEBVIEW_USER_FIND_INFORMATION ->
            {
                intent = Intent(mContext, WebviewUserFindInformationActivity::class.java)
                if(`object` != null)
                {
                    intent.putExtra(Common.INTENT_FIND_INFORMATION, `object` as FindType?)
                }
            }
            else ->{}
        }
        if(addFlag != -1)
        {
            intent?.addFlags(addFlag)
        }
        when(animationMode)
        {
            AnimationMode.NO_ANIATION ->
            {
                if(launcher != null)
                {
                    launcher.launch(intent)
                }
                else
                {
                    (mContext as Activity).startActivity(intent)
                }
            }

            AnimationMode.NORMAL_ANIMATION ->
            {
                if(launcher != null)
                {
                    launcher.launch(intent)
                }
                else
                {
                    (mContext as Activity).startActivity(intent)
                }
                (mContext as Activity).overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
            }

            AnimationMode.REVERSE_NORMAL_ANIMATION ->
            {
                if(launcher != null)
                {
                    launcher.launch(intent)
                }
                else
                {
                    (mContext as Activity).startActivity(intent)
                }
                (mContext as Activity).overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
            }

            AnimationMode.METERIAL_ANIMATION ->
            {
                try {
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

                        if(launcher != null)
                        {
                            launcher.launch(intent, options)
                        }
                        else
                        {
                            ActivityCompat.startActivity((mContext as Activity), intent!!, options.toBundle())
                        }
                    }
                    else
                    {
                        if(launcher != null)
                        {
                            launcher.launch(intent)
                        } else
                        {
                            (mContext as Activity).startActivity(intent)
                        }
                        (mContext as Activity).overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
                    }
                }
                catch(e : NoSuchMethodError)
                {
                    if(launcher != null)
                    {
                        launcher.launch(intent)
                    } else
                    {
                        (mContext as Activity).startActivity(intent)
                    }
                    (mContext as Activity).overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
                }
            }
        }
    }

    private fun init()
    {
        mCurrentExecuteMode             = ActivityMode.MAIN
        mCurrentExecuteAnimationMode    = AnimationMode.NO_ANIATION
        mCurrentExecuteData             = null
        mCurrentExecuteIntentFlag       = -1
        mCurrentViewPair                = null
        mCurrentExecuteResultLauncher   = null
    }

    companion object
    {
        var sIntentManagementFactory : IntentManagementFactory? = null
        fun getInstance() : IntentManagementFactory
        {
            if(sIntentManagementFactory == null)
            {
                sIntentManagementFactory = IntentManagementFactory()
            }

            return sIntentManagementFactory!!
        }
    }

}