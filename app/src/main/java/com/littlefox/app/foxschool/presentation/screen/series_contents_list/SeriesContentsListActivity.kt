package com.littlefox.app.foxschool.presentation.screen.series_contents_list

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.`object`.result.story.SeriesBaseResult
import com.littlefox.app.foxschool.presentation.screen.series_contents_list.phone.SeriesContentsScreenV
import com.littlefox.app.foxschool.presentation.viewmodel.SeriesContentsListViewModel
import com.littlefox.logmonitor.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SeriesContentsListActivity : BaseActivity()
{
    private val viewModel: SeriesContentsListViewModel by viewModels()
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)

        viewModel.init(this)
        setupObserverViewModel()


        setContent{
            SeriesContentsScreenV(
                viewModel = viewModel,
                onEvent = viewModel::onHandleViewEvent
            )
        }
    }

    override fun setupObserverViewModel()
    {
        lifecycleScope.launch {
            viewModel.isLoading.collect{ loading ->
                Log.i("loading : $loading")
                if(loading)
                {
                    showLoading()
                }
                else
                {
                    hideLoading()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.toast.collect{ message ->
                Log.i("message : $message")
                Toast.makeText(this@SeriesContentsListActivity, message, Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launch {
            viewModel.successMessage.collect{ message ->

                Log.i("message : $message")
                CommonUtils.getInstance(this@SeriesContentsListActivity).showSuccessMessage(message)
            }
        }

        lifecycleScope.launch {
            viewModel.errorMessage.collect{ message ->

                Log.i("message : $message")
                CommonUtils.getInstance(this@SeriesContentsListActivity).showErrorMessage(message)
            }
        }

        lifecycleScope.launch {
            viewModel.statusBarColor.collect{ color ->
                setStatusBar(color)
            }
        }
    }



    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }

    private fun setStatusBar(statusColor : String)
    {
        CommonUtils.getInstance(this).setStatusBar(Color.parseColor(statusColor))
    }
}