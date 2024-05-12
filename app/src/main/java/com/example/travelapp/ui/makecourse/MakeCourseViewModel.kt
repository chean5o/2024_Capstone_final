package com.example.travelapp.ui.makecourse

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MakeCourseViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "코스 생성 화면"
    }
    val text: LiveData<String> = _text
}