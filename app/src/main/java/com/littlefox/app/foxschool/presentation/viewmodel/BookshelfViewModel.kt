package com.littlefox.app.foxschool.presentation.viewmodel

import android.content.Context
import com.littlefox.app.foxschool.api.viewmodel.api.BookshelfApiViewModel
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.ArrayList
import javax.inject.Inject

@HiltViewModel
class BookshelfViewModel @Inject constructor(private val apiViewModel : BookshelfApiViewModel) : BaseViewModel()
{
    companion object
    {
        const val DIALOG_EVENT_DELETE_BOOKSHELF_CONTENTS : Int      = 10001
        const val DIALOG_EVENT_WARNING_RECORD_PERMISSION : Int      = 10002
    }

    private val _isContentsLoading = MutableSharedFlow<Boolean>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val isContentsLoading : SharedFlow<Boolean> = _isContentsLoading

    private val _contentsList = MutableSharedFlow<ArrayList<ContentsBaseResult>>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val contentsList: SharedFlow<ArrayList<ContentsBaseResult>> = _contentsList

    private val _bookshelfTitle = MutableSharedFlow<String>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val bookshelfTitle : SharedFlow<String> = _bookshelfTitle




    override fun init(context : Context)
    {
        TODO("Not yet implemented")
    }

    override fun onHandleViewEvent(event : BaseEvent)
    {
        TODO("Not yet implemented")
    }

    override fun onHandleApiObserver()
    {
        TODO("Not yet implemented")
    }

    override fun resume()
    {
        TODO("Not yet implemented")
    }

    override fun pause()
    {
        TODO("Not yet implemented")
    }

    override fun destroy()
    {
        TODO("Not yet implemented")
    }

}