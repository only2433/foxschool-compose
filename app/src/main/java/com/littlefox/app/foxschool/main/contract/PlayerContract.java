package com.littlefox.app.foxschool.main.contract;

import com.littlefox.app.foxschool.adapter.PlayerListAdapter;
import com.littlefox.app.foxschool.main.contract.base.BaseContract;



/**
 * Created by only340 on 2018-03-21.
 */

public class PlayerContract
{
    public interface View extends BaseContract.View
    {
        public void initPlayListView(PlayerListAdapter adapter, int position);
        public void initPlaySpeedListView(PlayerSpeedListAdapter adapter);
        public void showLoading();
        public void hideLoading();
        public void showMovieLoading();
        public void hideMovieLoading();
        public void initMovieLayout();
        public void settingSpeedTextLayout(int speedIndex, boolean isMenuHide);
        public void settingCoachmarkView(String type);
        public void initCaptionText();
        public void setMovieTitle(String title);
        public void setCaptionText(String text);
        public void setRemainMovieTime(String remainTime);
        public void setCurrentMovieTime(String currentTime);
        public void setRemainPreviewTime(int remainTime);
        public void setSeekProgress(int progress);
        public void setDownloadProgress(int progress);
        public void setMaxProgress(int maxProgress);
        public void enablePlayMovie(boolean isPlaying);
        public void enableLockMenu(boolean newLockMode);
        public void setLockCountTime(int second);
        public void showPreviewUserStartView();
        public void showPreviewUserEndView();
        public void showPaymentUserStartView();
        public void showPaymentUserEndView();
        public void settingPaymentEndView(boolean isEbookAvailable, boolean isQuizAvailable, boolean isVocabularyAvailable, boolean isTranslateAvailable, boolean isNextButtonVisible);
        public void PlayFirstIndexMovie();
        public void PlayNormalIndexMovie();
        public void PlayLastIndexMovie();
        public void PlayOneItemMovie();
        public void checkSupportCaptionView(boolean isSupport);
        public void settingPlayerOption(boolean isEnableCaption, boolean isEnablePage);
        public void enableRepeatView(boolean isOn);
        public void scrollPosition(int position);
        public void showSuccessMessage(String message);
        public void showErrorMessage(String message);

        /**
         * 현재 보이는 Page 라인의 정보를 알려주는 메소드
         * @param startIndex 처음 시작 Index 숫자
         * @param maxPageCount 전체 최대 페이지 개수
         */
        public void settingCurrentPageLine(int startIndex, int maxPageCount);

        /**
         * 현재 페이지의 index를 가리키기 위해 호출.
         * @param page 해당 Page
         */
        public void enableCurrentPage(int page);

        /**
         * 페이지 뷰가 타이틀 영상 시간동안엔 나오면 안되기 때문에 사용
         * @param isActivate TRUE : 페이지뷰 보임, FALSE : 페이지뷰 가림
         */
        public void activatePageView(boolean isActivate);

        public void enableSpeedButton();
        public void disableSpeedButton();

        public void disablePortraitOptionButton();
        public void enablePortraitOptionButton();
    }

    public interface Presenter extends BaseContract.Presenter
    {
        public void onCloseButton();
        public void onHandlePlayButton();
        public void onActivateLockButton();
        public void onUnActivateLockButton();
        public void onNextButton();
        public void onPrevButton();
        public void onReplayButton();
        public void onStartTrackingSeek();
        public void onStopTrackingSeek(int progress);
        public void onChangeOrientation(int orientation);
        public void onPaymentButton();
        public void onRepeatButton();
        public void onClickCurrentMovieOptionButton();
        public void onClickCurrentMovieEbookButton();
        public void onClickCurrentMovieQuizButton();
        public void onClickCurrentMovieVocabularyButton();
        public void onClickCurrentMovieTranslateButton();
        public void onNextMovieButton();
        public void onPageByPageIndex(int index);
        public void onCoachMarkNeverSeeAgain(String type);

        /**
         * 해당 페이지 라인의 처음 페이지 (예: 1,2,3,4,5 라면 1 , 6,7,8,9,10 이면 6)
         * @param startPage
         */
        public void onMovePrevPage(int startPage);

        /**
         * 해당 페이지 라인의 마지막 페이지 (예: 1,2,3,4,5 라면 5 , 6,7,8,9,10 이면 10)
         * @param lastPage
         */
        public void onMoveNextPage(int lastPage);

        public void onClickCaptionButton(boolean isEnable);
        public void onClickPageByPageButton(boolean isEnable);
    }

}
