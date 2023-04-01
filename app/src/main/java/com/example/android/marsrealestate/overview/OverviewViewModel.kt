/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.android.marsrealestate.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.marsrealestate.network.MarsApi
import com.example.android.marsrealestate.network.MarsApiFilter
import com.example.android.marsrealestate.network.MarsProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class MarsApiStatus {
    ERROR, LOADING, DONE
}

class OverviewViewModel : ViewModel() {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    // The internal MutableLiveData String that stores the status of the most recent request
    private val _status = MutableLiveData<MarsApiStatus>()
    val status: LiveData<MarsApiStatus> get() = _status

    private val _marsProperties = MutableLiveData<List<MarsProperty>>()
    val marsProperties: LiveData<List<MarsProperty>> get() = _marsProperties

    private val _navigateToPropertyDetails = MutableLiveData<MarsProperty>()
    val navigateToPropertyDetails: LiveData<MarsProperty> get() = _navigateToPropertyDetails

    /**
     * Call getMarsRealEstateProperties() on init so we can display status immediately.
     */
    init {
        getMarsRealEstateProperties(MarsApiFilter.SHOW_ALL)
    }

    /**
     * Sets the value of the status LiveData to the Mars API status.
     */
    private fun getMarsRealEstateProperties(filter: MarsApiFilter) {

        coroutineScope.launch {

            var getPropertiesDeffered = MarsApi.retrofitService.getProperties(filter.value)

            try {
                _status.value = MarsApiStatus.LOADING
                var listResult = getPropertiesDeffered.await()
                _status.value = MarsApiStatus.DONE
                _marsProperties.value = listResult

            } catch (e: Exception) {
                _status.value = MarsApiStatus.ERROR
                _marsProperties.value = ArrayList()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun navigateToSelectedProperty(marsProperty: MarsProperty){
        _navigateToPropertyDetails.value = marsProperty
    }

    fun doneNavigatingToPropertyDetails(){
        _navigateToPropertyDetails.value = null
    }

    fun updateFilter(filter: MarsApiFilter){
        getMarsRealEstateProperties(filter)
    }

}
