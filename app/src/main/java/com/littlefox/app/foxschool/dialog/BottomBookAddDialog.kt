package com.littlefox.app.foxschool.dialog

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.dialog.listener.BookAddListener
import com.littlefox.app.foxschool.enumerate.BookColor
import com.littlefox.app.foxschool.enumerate.BookType
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout


import java.util.*

class BottomBookAddDialog : BottomSheetDialog
{
    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindView(R.id._booksAddList)
    lateinit var _BooksAddList : RecyclerView

    private var mBooksType : BookType = BookType.BOOKSHELF
    private var mBooksListAdapter : BooksListAdapter? = null
    private var mBookAddListener : BookAddListener? = null
    private var mMyBookshelfResultList : ArrayList<MyBookshelfResult> = ArrayList<MyBookshelfResult>()
    private var mMyVocabularyResultList : ArrayList<MyVocabularyResult> = ArrayList<MyVocabularyResult>()
    private var isFullScreen : Boolean = false
    private var isLandScapeMode : Boolean = false
    private val mContext : Context

    constructor(context : Context) : super(context)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if(CommonUtils.getInstance(context).checkTablet)
        {
            setContentView(R.layout.dialog_bookshelf_add_option_tablet)
        }
        else
        {
            setContentView(R.layout.dialog_bookshelf_add_option)
        }
        ButterKnife.bind(this)
        mContext = context
    }

    protected override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        if(CommonUtils.getInstance(mContext).checkTablet)
        {
            getWindow()!!.setLayout(CommonUtils.getInstance(mContext).getPixel(800), ViewGroup.LayoutParams.MATCH_PARENT)
        }
        else
        {
            // 폰일 때 플래시카드에서 사용하는 BottomDialog 팝업 크기 변경
            if (isLandScapeMode)
            {
                getWindow()!!.setLayout(CommonUtils.getInstance(mContext).getPixel(1000), ViewGroup.LayoutParams.MATCH_PARENT)
            }
        }

        if(isFullScreen)
        {
            getWindow()!!.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }


    fun setBookshelfData(list : ArrayList<MyBookshelfResult>)
    {
        Log.f("size ; " + list.size)
        mBooksType = BookType.BOOKSHELF
        mMyBookshelfResultList = list
        initView()
        initFont()
        initRecyclerView()
    }

    fun setVocabularyData(list : ArrayList<MyVocabularyResult>)
    {
        Log.f("size ; " + list.size)
        mBooksType = BookType.VOCABULARY
        mMyVocabularyResultList = list
        initView()
        initFont()
        initRecyclerView()
    }

    override fun onBackPressed()
    {
        dismiss()
    }

    fun setFullScreen()
    {
        isFullScreen = true
    }

    fun setLandScapeMode()
    {
        isLandScapeMode = true
    }

    fun setBookSelectListener(bookAddListener : BookAddListener?)
    {
        mBookAddListener = bookAddListener
    }

    private fun initView()
    {
        if(mBooksType === BookType.BOOKSHELF)
        {
            _TitleText.setText(mContext.resources.getString(R.string.text_select_bookshelf))
        }
        else
        {
            _TitleText.setText(mContext.resources.getString(R.string.text_select_vocabulary))
        }
    }

    private fun initFont()
    {
        _TitleText.setTypeface(Font.getInstance(mContext).getRobotoMedium())
    }

    private fun initRecyclerView()
    {
        val params : LinearLayout.LayoutParams = _BooksAddList.getLayoutParams() as LinearLayout.LayoutParams

        if(CommonUtils.getInstance(mContext).checkTablet)
            params.height = CommonUtils.getInstance(mContext).getHeightPixel(578)
        else
            params.height = CommonUtils.getInstance(mContext).getHeightPixel(699)
        _BooksAddList.setLayoutParams(params)
        mBooksListAdapter = BooksListAdapter()
        val linearLayoutManager = LinearLayoutManager(mContext)
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL)
        _BooksAddList.setLayoutManager(linearLayoutManager)
        _BooksAddList.setAdapter(mBooksListAdapter)
    }

    inner class BooksListAdapter : RecyclerView.Adapter<BooksListAdapter.ViewHolder?>()
    {
        override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
        {
            val view : View
            if(CommonUtils.getInstance(mContext).checkTablet)
            {
                view = LayoutInflater.from(mContext).inflate(R.layout.mybooks_add_item_tablet_dialog, parent, false)
            }
            else
            {
                view = LayoutInflater.from(mContext).inflate(R.layout.mybooks_add_item, parent, false)
            }
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder : ViewHolder, position : Int)
        {
            Log.f("position : $position")
            val color : BookColor
            if(mBooksType === BookType.BOOKSHELF)
            {
                color = CommonUtils.getInstance(mContext).getBookColorType(mMyBookshelfResultList[position].getColor())
                holder._IconBooksImage.setImageResource(R.drawable.icon_bookshelf)
                holder._BooksTitleText.setText(mMyBookshelfResultList[position].getName().toString() + "(" + mMyBookshelfResultList[position].getContentsCount() + ")")
            }
            else
            {
                color = CommonUtils.getInstance(mContext).getBookColorType(mMyVocabularyResultList[position].getColor())
                holder._IconBooksImage.setImageResource(R.drawable.icon_voca)
                holder._BooksTitleText.setText(mMyVocabularyResultList[position].getName().toString() + "(" + mMyVocabularyResultList[position].getWordCount() + ")")
            }
            holder._BooksEnterButton.visibility = View.GONE
            holder._CoverBooksImage.setImageResource(CommonUtils.getInstance(mContext)!!.getBookResource(color))
            holder._BaseLayout.setOnClickListener(View.OnClickListener {
                mBookAddListener!!.onClickBook(position)
                dismiss()
            })
        }

        override fun getItemCount() : Int
        {
            if(mBooksType === BookType.BOOKSHELF)
                return mMyBookshelfResultList.size
            else
                return mMyVocabularyResultList.size
        }

        inner class ViewHolder : RecyclerView.ViewHolder
        {
            @BindView(R.id._baseLayout)
            lateinit var _BaseLayout : ScalableLayout

            @BindView(R.id._coverBooksImage)
            lateinit var _CoverBooksImage : ImageView

            @BindView(R.id._booksEnterButton)
            lateinit var _BooksEnterButton : ImageView

            @BindView(R.id._iconBooksImage)
            lateinit var _IconBooksImage : ImageView

            @BindView(R.id._booksTitle)
            lateinit var _BooksTitleText : TextView

            constructor(view : View) : super(view)
            {
                ButterKnife.bind(this, view)
                _BooksTitleText.setTypeface(Font.getInstance(mContext).getRobotoMedium())
            }
        }
    }
}