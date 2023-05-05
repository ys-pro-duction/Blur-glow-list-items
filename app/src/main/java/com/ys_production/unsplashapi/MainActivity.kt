package com.ys_production.unsplashapi
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.core.ImageTranscoderType
import com.facebook.imagepipeline.core.MemoryChunkType
import com.ys_production.unsplashapi.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    interface UnsplashApi {
        @GET("/search/photos")
        fun searchPhotos(
            @Query("query") query: String?,
            @Query("page") page: Int,
            @Query("per_page") perPage: Int,
            @Query("client_id") clientId: String?,
        ): Call<UnsplashResponse?>?
    }

    private var recyclerView: RecyclerView? = null
    private var adapter: UnsplashAdapter? = null
    private val photos: MutableList<UnsplashPhoto> = ArrayList<UnsplashPhoto>()
    private var unsplashApi: UnsplashApi? = null
    private val apiKey = "4557F9AwsPRINHf-cdHlW4Es66NLFT9Sc6AOpG3yLWs"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Fresco.initialize(
            applicationContext,
            ImagePipelineConfig.newBuilder(applicationContext)
                .setMemoryChunkType(MemoryChunkType.BUFFER_MEMORY)
                .setImageTranscoderType(ImageTranscoderType.JAVA_TRANSCODER)
                .experiment().setNativeCodeDisabled(true)
                .build())
        // Create the Retrofit instance
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.unsplash.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Create the UnsplashApi instance
        unsplashApi = retrofit.create(UnsplashApi::class.java)

        // Set up the RecyclerView
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView?.layoutManager = GridLayoutManager(this, 2)
        adapter = UnsplashAdapter(this, photos)
        recyclerView?.adapter = adapter

        // Set up the search bar
        val searchView = findViewById<SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        // Clear the current list of photos
        photos.clear()

        // Make a call to the Unsplash API
        unsplashApi?.searchPhotos(query, 1, 30, apiKey)
            ?.enqueue(object : Callback<UnsplashResponse?> {
                override fun onResponse(
                    call: Call<UnsplashResponse?>,
                    response: Response<UnsplashResponse?>
                ) {
                    if (response.isSuccessful) { // assuming "response" is an instance of UnsplashResponse

                        // Add the photos to the list
                        response.body()?.getResults()?.let { photos.addAll(it) }
                        adapter?.notifyDataSetChanged()
                    } else {
                        Log.e("MainActivity", "Error: " + response.code())
                    }
                }

                override fun onFailure(call: Call<UnsplashResponse?>, t: Throwable) {
                    Log.e("MainActivity", "Error: " + t.message)
                }
            })
        return false
    }
    fun UnsplashResponse.getResults(): List<UnsplashPhoto> {
        return this.results
    }


    override fun onQueryTextChange(newText: String): Boolean {
        return false
    }
}