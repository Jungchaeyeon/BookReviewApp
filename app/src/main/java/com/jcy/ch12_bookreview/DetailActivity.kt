package com.jcy.ch12_bookreview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.room.Room
import com.bumptech.glide.Glide
import com.jcy.ch12_bookreview.databinding.ActivityDetailBinding
import com.jcy.ch12_bookreview.model.AppDatabase
import com.jcy.ch12_bookreview.model.Book
import com.jcy.ch12_bookreview.model.Review
import com.jcy.ch12_bookreview.model.getAppDatabase

class DetailActivity : AppCompatActivity() {

    private lateinit var  binding : ActivityDetailBinding
    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = getAppDatabase(this)

        val model = intent.getParcelableExtra<Book>("bookModel")

        binding.titleTv.text = model?.title.orEmpty()
        binding.descriptionTv.text = model?.description.orEmpty()

        Glide.with(binding.coverImageView.context)
            .load(model?.coverSmallUrl.orEmpty())
            .into(binding.coverImageView)

        Thread{
            val review = db.reviewDao().getOnReview(model?.id?.toInt() ?: 0)
            runOnUiThread{
                binding.reviewEditText.setText(review?.review.orEmpty())
            }
        }.start()
        binding.saveBtn.setOnClickListener {
            Thread{
                db.reviewDao().saveReview(
                    Review(model?.id?.toInt() ?:0,
                            binding.reviewEditText.text.toString())
                )
            }.start()

            Toast.makeText(this,"리뷰가 기록되었습니다.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}