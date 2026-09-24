package com.nova.messenger.native2

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface NovaApi {
    @POST("api.php")
    suspend fun login(
        @Query("route") route: String = "auth/login",
        @Body body: LoginBody
    ): AuthResponse

    @POST("api.php")
    suspend fun register(
        @Query("route") route: String = "auth/register",
        @Body body: RegisterBody
    ): AuthResponse

    @GET("api.php")
    suspend fun me(@Query("route") route: String = "me"): MeResponse

    @PATCH("api.php")
    suspend fun updateMe(
        @Query("route") route: String = "me",
        @Body body: ProfileUpdateBody
    ): ProfileUpdateResponse

    @GET("api.php")
    suspend fun conversations(
        @Query("route") route: String = "conversations"
    ): ConversationsResponse

    @GET("api.php")
    suspend fun messages(
        @Query("route") route: String,
        @Query("before") before: Long? = null
    ): MessagesResponse

    @POST("api.php")
    suspend fun sendMessage(
        @Query("route") route: String,
        @Body body: SendMessageBody
    ): MessageEnvelope

    @PATCH("api.php")
    suspend fun editMessage(
        @Query("route") route: String,
        @Body body: EditMessageBody
    ): MessageEnvelope

    @DELETE("api.php")
    suspend fun deleteMessage(
        @Query("route") route: String
    ): MessageEnvelope

    @POST("api.php")
    suspend fun reactMessage(
        @Query("route") route: String,
        @Body body: ReactionBody
    ): MessageEnvelope

    @POST("api.php")
    suspend fun markRead(
        @Query("route") route: String,
        @Body body: ReadBody
    ): OkResponse

    @POST("api.php")
    suspend fun typing(
        @Query("route") route: String,
        @Body body: TypingBody
    ): OkResponse

    @POST("api.php")
    suspend fun heartbeat(
        @Query("route") route: String = "heartbeat",
        @Body body: Map<String, String> = emptyMap()
    ): OkResponse

    @Multipart
    @POST("api.php")
    suspend fun upload(
        @Query("route") route: String = "upload",
        @Part file: MultipartBody.Part
    ): UploadResponse

    @POST("api.php")
    suspend fun registerAndroidPush(
        @Query("route") route: String = "push/android/subscribe",
        @Body body: PushBody
    ): PushResponse

    @DELETE("api.php")
    suspend fun unregisterAndroidPush(
        @Query("route") route: String = "push/android/subscribe",
        @Body body: PushBody
    ): OkResponse

    @GET("api.php")
    suspend fun friends(@Query("route") route: String = "friends"): FriendsResponse

    @GET("api.php")
    suspend fun searchUsers(
        @Query("route") route: String = "friends/search",
        @Query("q") query: String
    ): SearchUsersResponse

    @POST("api.php")
    suspend fun requestFriend(
        @Query("route") route: String = "friends/request",
        @Body body: FriendRequestBody
    ): FriendRequestResult

    @POST("api.php")
    suspend fun respondFriend(
        @Query("route") route: String = "friends/respond",
        @Body body: FriendRespondBody
    ): FriendRespondResult

    @POST("api.php")
    suspend fun removeFriend(
        @Query("route") route: String = "friends/remove",
        @Body body: FriendRemoveBody
    ): FriendRemoveResult

    @POST("api.php")
    suspend fun openDm(
        @Query("route") route: String = "conversations/dm",
        @Body body: DmBody
    ): DmResponse
}
