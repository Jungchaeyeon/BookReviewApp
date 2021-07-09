package com.jcy.ch12_bookreview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.jcy.ch12_bookreview.adapter.BookAdapter
import com.jcy.ch12_bookreview.adapter.HistoryAdapter
import com.jcy.ch12_bookreview.api.BookService
import com.jcy.ch12_bookreview.databinding.ActivityMainBinding
import com.jcy.ch12_bookreview.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: BookAdapter
    private lateinit var historyAdapter : HistoryAdapter
    private lateinit var bookService: BookService
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBookRecyclerView()
        initHistoryRecyclerView()

        db = getAppDatabase(this)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://book.interpark.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        bookService = retrofit.create(BookService::class.java)

        bookService.getBestSellerBooks(getString(R.string.interparkAPIKey))
            .enqueue(object: Callback<BestSellerDto>{
                override fun onResponse(
                    call: Call<BestSellerDto>,
                    response: Response<BestSellerDto>
                ) {
                    //todo 성공처리
                    if(response.isSuccessful.not()){
                        Log.e(TAG,"NOT SUCCESS!")
                        return
                    }
                    response.body()?.let{
                        Log.d(TAG, it.toString())
                        it.books.forEach { book ->
                            Log.d(TAG, book.toString())
                        }
                        adapter.submitList(it.books) //adapter에서 currentList가 BookList로 바뀌게 된다.
                    }
                }
                override fun onFailure(call: Call<BestSellerDto>, t: Throwable) {
                    //todo 실패처리
                    Log.e(TAG,t.toString())
                }
            })
    }
    private fun search(keyword: String){
        bookService.getBooksByName(getString(R.string.interparkAPIKey),keyword)
            .enqueue(object: Callback<SearchBookDto>{
                override fun onResponse(
                    call: Call<SearchBookDto>,
                    response: Response<SearchBookDto>
                ) {
                    hideHistoryView()
                    saveSearchKeyword(keyword)
                    if(response.isSuccessful.not()){
                        Log.e(TAG,"NOT SUCCESS!")
                        return
                    }
                    response.body()?.let{
                        Log.d(TAG, it.toString())
                        it.books.forEach { book ->
                            Log.d(TAG, book.toString())
                        }
                        adapter.submitList(response.body()?.books.orEmpty()) //리사이클러뷰 갱신, 없으면 빈 리스트
                    }
                }
                override fun onFailure(call: Call<SearchBookDto>, t: Throwable) {
                    hideHistoryView()
                    Log.e(TAG,t.toString())
                }
            })
    }
    private fun initBookRecyclerView(){
        adapter = BookAdapter(itemClickedListener = {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("bookModel", it)
            startActivity(intent)
        })

        binding.bookRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bookRecyclerView.adapter = adapter
    }
    private fun initHistoryRecyclerView(){
        historyAdapter = HistoryAdapter(historyDeleteClickedListener = {
            deleteSearchKeyword(it)
        })
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter
        initSearchEditText()
    }
    private fun initSearchEditText(){
        binding.searchEditText.setOnKeyListener{ v, keyCode, event ->
            if(keyCode == KeyEvent.KEYCODE_ENTER && event.action == MotionEvent.ACTION_DOWN){
                search(binding.searchEditText.text.toString())
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        binding.searchEditText.setOnTouchListener { v, event ->
            if(event.action == MotionEvent.ACTION_DOWN){
                showHistoryView()
            }
            return@setOnTouchListener false
        }
    }
    private fun deleteSearchKeyword(keyword: String){
        Thread{
            db.historyDao().delete(keyword = keyword)
            showHistoryView()
        }.start()
    }
    private fun showHistoryView(){
        Thread{
            val keywords = db.historyDao().getAll().reversed()
            runOnUiThread {
                binding.historyRecyclerView.isVisible = true
                historyAdapter.submitList(keywords.orEmpty())
            }
        }.start()
        binding.historyRecyclerView.isVisible = true
    }
    private fun hideHistoryView(){
        binding.historyRecyclerView.isVisible = false
    }
    private fun saveSearchKeyword(keyword: String){
        Thread{
            db.historyDao().insertHistory(History(null, keyword))
        }.start()
    }
    companion object{
        private const val TAG ="MainActivity"
    }
}