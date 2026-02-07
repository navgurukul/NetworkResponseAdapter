package com.navgurukul.networkresponseadapter

import com.navgurukul.networkresponse.NetworkResponse
import com.navgurukul.networkresponse.NetworkResponseAdapterFactory
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Simple test to verify the library works correctly
 * 
 * This demonstrates:
 * 1. Setting up Retrofit with NetworkResponseAdapterFactory
 * 2. Making API calls
 * 3. Handling different response types
 */
object LibraryTest {
    
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addCallAdapterFactory(NetworkResponseAdapterFactory())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val apiService = retrofit.create(TestApiService::class.java)
    
    /**
     * Test basic functionality
     * Run this to verify the library works
     */
    fun runTest() = runBlocking {
        println("🧪 Testing NetworkResponse Adapter Library...")
        println("=" .repeat(50))
        
        // Test 1: Successful API call
        println("\n✅ Test 1: Making API call to get users...")
        when (val response = apiService.getUsers()) {
            is NetworkResponse.Success -> {
                println("✅ SUCCESS! Got ${response.body.size} users")
                println("   Status Code: ${response.code}")
                println("   First user: ${response.body.firstOrNull()?.name}")
            }
            is NetworkResponse.ServerError -> {
                println("❌ Server Error: ${response.code}")
            }
            is NetworkResponse.NetworkError -> {
                println("❌ Network Error: ${response.error.message}")
            }
            is NetworkResponse.UnknownError -> {
                println("❌ Unknown Error: ${response.error.message}")
            }
        }
        
        // Test 2: Another endpoint
        println("\n✅ Test 2: Making API call to get posts...")
        when (val response = apiService.getPosts()) {
            is NetworkResponse.Success -> {
                println("✅ SUCCESS! Got ${response.body.size} posts")
                println("   Status Code: ${response.code}")
                println("   First post: ${response.body.firstOrNull()?.title}")
            }
            is NetworkResponse.ServerError -> {
                println("❌ Server Error: ${response.code}")
            }
            is NetworkResponse.NetworkError -> {
                println("❌ Network Error: ${response.error.message}")
            }
            is NetworkResponse.UnknownError -> {
                println("❌ Unknown Error: ${response.error.message}")
            }
        }
        
        println("\n" + "=".repeat(50))
        println("🎉 Library test completed!")
    }
    
    /**
     * Example: Using simplified error handling
     */
    fun testSimplifiedErrorHandling() = runBlocking {
        println("\n🧪 Testing simplified error handling...")
        
        when (val response = apiService.getUsers()) {
            is NetworkResponse.Success -> {
                println("✅ Got data: ${response.body.size} users")
            }
            is NetworkResponse.Error -> {
                // Handle any error type
                println("❌ Error occurred: ${response.error.message}")
            }
        }
    }
}

/**
 * Main function to run tests
 * You can call this from your MainActivity or a test
 */
fun main() {
    LibraryTest.runTest()
    LibraryTest.testSimplifiedErrorHandling()
}
