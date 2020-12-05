package com.example.core.data.source.remote

import android.util.Log
import com.example.core.data.source.remote.network.ApiService
import com.example.core.data.source.remote.network.ApiResponse
import com.example.core.data.source.remote.response.GameDetailResponse
import com.example.core.data.source.remote.response.GameDeveloperResponse
import com.example.core.data.source.remote.response.ListGameResponses
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val apiService: ApiService) {
    fun getGames(page : Int, perPage : Int): Single<ListGameResponses> {
        return apiService.getGames(page = page,perPage = perPage)
    }

    fun getDeveloper(): Flowable<ApiResponse<List<GameDeveloperResponse>>> {
        val resultData = PublishSubject.create<ApiResponse<List<GameDeveloperResponse>>>()

        val client = apiService.getDeveloper()

        client
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .take(1)
            .subscribe({ response ->
                val dataArray = response.results
                resultData.onNext(if (dataArray.isNotEmpty()) ApiResponse.Success(dataArray) else ApiResponse.Empty)
            }, { error ->
                resultData.onNext(ApiResponse.Error(error.message.toString()))
                Log.e("RemoteDataSource", error.toString())
            })
        return resultData.toFlowable(BackpressureStrategy.LATEST)
    }

    fun getGamesDetail(id : Int) : Flowable<ApiResponse<GameDetailResponse>>{
        val resultData = PublishSubject.create<ApiResponse<GameDetailResponse>>()

        val client = apiService.getGamesDetail(id=id)

        client.subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .take(1)
            .subscribe({response->
                val dataArray = response
                Log.i("DETAILGAME Remote",dataArray.toString())
                resultData.onNext(if (dataArray != null) ApiResponse.Success(dataArray) else ApiResponse.Empty)
            },{error ->
                Log.i("DETAILGAME Remote",error.message.toString())

                resultData.onNext(ApiResponse.Error(error.message.toString()))
            })
        return resultData.toFlowable(BackpressureStrategy.LATEST)
    }
}
