package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.`object`.result.introduceSeries.IntroduceSeriesInformationResult
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class IntroduceSeriesContract
{
    interface View: BaseContract.View
    {
        fun showIntroduceSeriesData(result: IntroduceSeriesInformationResult)
    }

    interface Presenter: BaseContract.Presenter { }
}