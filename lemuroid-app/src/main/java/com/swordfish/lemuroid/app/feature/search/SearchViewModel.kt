package com.swordfish.lemuroid.app.feature.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.swordfish.lemuroid.app.utils.livedata.CombinedLiveData
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game

class SearchViewModel(private val retrogradeDb: RetrogradeDatabase) : ViewModel() {

    class Factory(val retrogradeDb: RetrogradeDatabase) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(retrogradeDb) as T
        }
    }

    val queryString = MutableLiveData<String>()

    val searchResults: LiveData<PagedList<Game>> = Transformations.switchMap(queryString) {
        LivePagedListBuilder(retrogradeDb.gameSearchDao().search(it), 20).build()
    }

    val emptyViewVisible = CombinedLiveData(queryString, searchResults) { query, results ->
        query?.isNotBlank() == true && results?.isEmpty() == true
    }
}
