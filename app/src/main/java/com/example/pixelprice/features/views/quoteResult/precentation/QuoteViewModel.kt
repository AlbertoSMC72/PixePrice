package com.example.pixelprice.features.views.quoteResult.precentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixelprice.features.views.quoteResult.data.model.QuoteDTO
import com.example.pixelprice.features.views.quoteResult.domain.GetQuoteUseCase
import kotlinx.coroutines.launch


class QuoteViewModel : ViewModel() {

    private val getQuoteUseCase = GetQuoteUseCase()

    private val _quote = MutableLiveData<QuoteDTO?>()
    val quote: LiveData<QuoteDTO?> = _quote

    private val _error = MutableLiveData<String>("")
    val error: LiveData<String> = _error

    fun loadQuote(projectId: Int) {
        viewModelScope.launch {
            val result = getQuoteUseCase(projectId)
            result.onSuccess {
                _quote.value = it
            }.onFailure {
                _error.value = it.message ?: "Error al cargar cotización"
            }
        }
    }
}
