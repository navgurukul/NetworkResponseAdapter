package com.navgurukul.networkresponseadapter

import com.navgurukul.networkresponse.NetworkResponse
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for NetworkResponse sealed class
 * 
 * These tests verify the library's core functionality
 */
class NetworkResponseTest {

    @Test
    fun `test Success response creation`() {
        val data = listOf("item1", "item2")
        val response = NetworkResponse.Success(
            body = data,
            code = 200,
            headers = null
        )
        
        assertEquals(data, response.body)
        assertEquals(200, response.code)
        assertNull(response.headers)
    }

    @Test
    fun `test ServerError response creation`() {
        val errorBody = "Error message"
        val response = NetworkResponse.ServerError<String>(
            body = errorBody,
            code = 404,
            headers = null
        )
        
        assertEquals(errorBody, response.body)
        assertEquals(404, response.code)
        assertNotNull(response.error)
    }

    @Test
    fun `test NetworkError response creation`() {
        val exception = java.io.IOException("Network timeout")
        val response = NetworkResponse.NetworkError(exception)
        
        assertEquals(exception, response.error)
        assertEquals("Network timeout", response.error.message)
    }

    @Test
    fun `test UnknownError response creation`() {
        val exception = RuntimeException("Unknown error")
        val response = NetworkResponse.UnknownError(
            error = exception,
            code = 500,
            headers = null
        )
        
        assertEquals(exception, response.error)
        assertEquals(500, response.code)
    }

    @Test
    fun `test Error interface implementation`() {
        val serverError: NetworkResponse.Error = NetworkResponse.ServerError<String>(
            body = "Error",
            code = 500,
            headers = null
        )
        
        assertNotNull(serverError.error)
        
        val networkError: NetworkResponse.Error = NetworkResponse.NetworkError(
            java.io.IOException("Network error")
        )
        
        assertNotNull(networkError.error)
    }

    @Test
    fun `test when expression with Success`() {
        val response: NetworkResponse<String, String> = NetworkResponse.Success(
            body = "Success data",
            code = 200,
            headers = null
        )
        
        val result = when (response) {
            is NetworkResponse.Success -> "success"
            is NetworkResponse.ServerError -> "server_error"
            is NetworkResponse.NetworkError -> "network_error"
            is NetworkResponse.UnknownError -> "unknown_error"
        }
        
        assertEquals("success", result)
    }

    @Test
    fun `test when expression with Error interface`() {
        val response: NetworkResponse<String, String> = NetworkResponse.ServerError(
            body = "Error",
            code = 404,
            headers = null
        )
        
        val result = when (response) {
            is NetworkResponse.Success -> "success"
            is NetworkResponse.Error -> "error"
        }
        
        assertEquals("error", result)
    }
}
