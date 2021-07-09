package com.jcy.ch12_bookreview.api


import retrofit2.http.GET
import retrofit2.http.Query
import com.jcy.ch12_bookreview.model.BestSellerDto
import com.jcy.ch12_bookreview.model.SearchBookDto
import retrofit2.Call

interface BookService {

    @GET("/api/search.api?output=json")
    fun getBooksByName(
        @Query("key") apiKey: String,
        @Query("query") keyword: String
    ) : Call<SearchBookDto>

    @GET("/api/bestSeller.api?output=json&categoryId=100")
    fun getBestSellerBooks(
        @Query("key") apiKey: String
    ): Call<BestSellerDto>
}