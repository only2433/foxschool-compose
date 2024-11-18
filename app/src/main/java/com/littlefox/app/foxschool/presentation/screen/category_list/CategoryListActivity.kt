package com.littlefox.app.foxschool.presentation.screen.category_list

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.presentation.screen.category_list.phone.CategoryListScreenV
import com.littlefox.app.foxschool.presentation.viewmodel.CategoryListViewModel
import com.littlefox.logmonitor.Log
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryListActivity : BaseActivity()
{
    private val viewModel: CategoryListViewModel by viewModels()
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)

        viewModel.init(this)
        setupObserverViewModel()
        setContent {
            CategoryListScreenV(
                viewModel = viewModel,
                onEvent = viewModel::onHandleViewEvent
            )
        }
    }

    override fun setupObserverViewModel()
    {
        viewModel.toast.observe(this){ message ->
            Log.i("message : $message")
            Toast.makeText(this@CategoryListActivity, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.successMessage.observe(this){ message ->
            Log.i("message : $message")
            CommonUtils.getInstance(this@CategoryListActivity).showSuccessMessage(message)
        }

        viewModel.errorMessage.observe(this){ message ->
            Log.i("message : $message")
            CommonUtils.getInstance(this@CategoryListActivity).showErrorMessage(message)
        }

        viewModel.statusBarColor.observe(this){ color ->
            setStatusBar(color)
        }

    }

    override fun onResume()
    {
        super.onResume()
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
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