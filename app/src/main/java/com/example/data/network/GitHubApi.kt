package com.example.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// --- GitHub Data Models ---

data class GitHubUser(
    val login: String,
    val id: Long,
    val avatar_url: String?,
    val name: String?,
    val public_repos: Int,
    val bio: String?,
    val html_url: String
)

data class GitHubRepo(
    val id: Long,
    val name: String,
    val full_name: String,
    val description: String?,
    val html_url: String,
    val stargazers_count: Int,
    val forks_count: Int,
    val language: String?
)

data class GitHubRelease(
    val id: Long,
    val tag_name: String,
    val name: String?,
    val body: String?,
    val assets: List<GitHubAsset>
)

data class GitHubAsset(
    val id: Long,
    val name: String,
    val size: Long,
    val download_count: Int,
    val browser_download_url: String
)

// --- Retrofit API Service Interface ---

interface GitHubService {
    @GET("users/{username}")
    suspend fun getUser(
        @Path("username") username: String,
        @Header("Authorization") authHeader: String? = null
    ): GitHubUser

    @GET("user")
    suspend fun getAuthenticatedUser(
        @Header("Authorization") authHeader: String
    ): GitHubUser

    @GET("users/{username}/repos")
    suspend fun getUserRepos(
        @Path("username") username: String,
        @Header("Authorization") authHeader: String? = null,
        @Query("per_page") perPage: Int = 100,
        @Query("sort") sort: String = "updated"
    ): List<GitHubRepo>

    @GET("user/repos")
    suspend fun getAuthenticatedUserRepos(
        @Header("Authorization") authHeader: String,
        @Query("per_page") perPage: Int = 100,
        @Query("sort") sort: String = "updated"
    ): List<GitHubRepo>

    @GET("repos/{owner}/{repo}/releases")
    suspend fun getReleases(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("Authorization") authHeader: String? = null
    ): List<GitHubRelease>
}

// --- API Client Singleton ---

object GitHubApiClient {
    private const val BASE_URL = "https://api.github.com/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/vnd.github.v3+json")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    val service: GitHubService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GitHubService::class.java)
    }
}
