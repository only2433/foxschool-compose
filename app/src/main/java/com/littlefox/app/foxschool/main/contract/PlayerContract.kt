package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.adapter.PlayerListAdapter
import com.littlefox.app.foxschool.adapter.PlayerSpeedListAdapter
import com.littlefox.app.foxschool.main.contract.base.BaseContract

/**
 * Created by only340 on 2018-03-21.
 */
class PlayerContract
{
    interface View : BaseContract.View
    {
        fun initPlayListView(adapter : PlayerListAdapter, position : Int)
        fun initPlaySpeedListView(adapter : PlayerSpeedListAdapter)
        fun showMovieLoading()
        fun hideMovieLoading()
        fun initMovieLayout()
        fun settingSpeedTextLayout(speedIndex : Int, isMenuHide : Boolean)
        fun settingCoachmarkView(type : String)
        fun initCaptionText()
        fun setMovieTitle(title : String)
        fun setCaptionText(text : String)
        fun setRemainMovieTime(remainTime : String)
        fun setCurrentMovieTime(currentTime : String)
        fun setRemainPreviewTime(remainTime : Int)
        fun setSeekProgress(progress : Int)
        fun setDownloadProgress(progress : Int)
        fun setMaxProgress(maxProgress : Int)
        fun enablePlayMovie(isPlaying : Boolean)
        fun enableLockMenu(newLockMode : Boolean)
        fun setLockCountTime(second : Int)
        fun showPreviewUserStartView()
        fun showPreviewUserEndView()
        fun showPaymentUserStartView()
        fun showPaymentUserEndView()
        fun settingPaymentEndView(isEbookAvailable : Boolean, isQuizAvailable : Boolean, isVocabularyAvailable : Boolean, isTranslateAvailable : Boolean, isNextButtonVisible : Boolean)
        fun PlayFirstIndexMovie()
        fun PlayNormalIndexMovie()
        fun PlayLastIndexMovie()
        fun PlayOneItemMovie()
        fun checkSupportCaptionView(isSupport : Boolean)
        fun settingPlayerOption(isEnableCaption : Boolean, isEnablePage : Boolean)
        fun enableRepeatView(isOn : Boolean)
        fun scrollPosition(position : Int)

        /**
         * 현재 보이는 Page 라인의 정보를 알려주는 메소드
         * @param startIndex 처음 시작 Index 숫자
         * @param maxPageCount 전체 최대 페이지 개수
         */
        fun settingCurrentPageLine(startIndex : Int, maxPageCount : Int)

        /**
         * 현재 페이지의 index를 가리키기 위해 호출.
         * @param page 해당 Page
         */
        fun enableCurrentPage(page : Int)

        /**
         * 페이지 뷰가 타이틀 영상 시간동안엔 나오면 안되기 때문에 사용
         * @param isActivate TRUE : 페이지뷰 보임, FALSE : 페이지뷰 가림
         */
        fun activatePageView(isActivate : Boolean)
        fun enableSpeedButton()
        fun disableSpeedButton()
        fun disablePortraitOptionButton()
        fun enablePortraitOptionButton()
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onCloseButton()
        fun onHandlePlayButton()
        fun onActivateLockButton()
        fun onUnActivateLockButton()
        fun onNextButton()
        fun onPrevButton()
        fun onReplayButton()
        fun onStartTrackingSeek()
        fun onStopTrackingSeek(progress : Int)
        fun onChangeOrientation(orientation : Int)
        fun onPaymentButton()
        fun onRepeatButton()
        fun onClickCurrentMovieOptionButton()
        fun onClickCurrentMovieEbookButton()
        fun onClickCurrentMovieQuizButton()
        fun onClickCurrentMovieVocabularyButton()
        fun onClickCurrentMovieTranslateButton()
        fun onNextMovieButton()
        fun onPageByPageIndex(index : Int)
        fun onCoachMarkNeverSeeAgain(type : String)

        /**
         * 해당 페이지 라인의 처음 페이지 (예: 1,2,3,4,5 라면 1 , 6,7,8,9,10 이면 6)
         * @param startPage
         */
        fun onMovePrevPage(startPage : Int)

        /**
         * 해당 페이지 라인의 마지막 페이지 (예: 1,2,3,4,5 라면 5 , 6,7,8,9,10 이면 10)
         * @param lastPage
         */
        fun onMoveNextPage(lastPage : Int)
        fun onClickCaptionButton(isEnable : Boolean)
        fun onClickPageByPageButton(isEnable : Boolean)
    }
}