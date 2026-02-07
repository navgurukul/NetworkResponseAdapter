package com.navgurukul.networkresponseadapter

import com.navgurukul.networkresponse.NetworkResponse
import retrofit2.http.GET

/**
 * Example API service to demonstrate library usage
 */
data class User(
    val id: Int,
    val name: String,
    val email: String
)

data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

data class ApiError(
    val message: String,
    val code: Int
)

interface TestApiService {
    @GET("users")
    suspend fun getUsers(): NetworkResponse<List<User>, ApiError>
    
    @GET("posts")
    suspend fun getPosts(): NetworkResponse<List<Post>, ApiError>
}
