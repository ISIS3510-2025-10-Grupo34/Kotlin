package com.tutorapp.remote

import com.tutorapp.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @Multipart
    @POST("register/")
    suspend fun register(
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part profilePicture: MultipartBody.Part?,
        @Part idPicture: MultipartBody.Part?): Response<ApiResponse>

    @POST("login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("studentprofile/")
    suspend fun studentProfile(@Body request: StudentProfileRequest): Response<StudentProfileResponse>

    @GET("universities/")
    suspend fun universities(): Response<UniversitiesResponse>

    @GET("get-area-of-expertise/")
    suspend fun aoes(): Response<AoesResponse>

    @POST("majors/")
    suspend fun majors(@Body university: String): Response<MajorsResponse>

    @GET("tutoring-sessions-with-names/")
    suspend fun tutoringSessions(): Response<List<TutoringSession>>

    @GET("search-results/")
    suspend fun getSearchResults(): Response<SearchResultResponse>

    @GET("course-estimate-price/")
    suspend fun getPriceEstimation(
        @Query("tutorId") tutorId: Int,
        @Query("courseUniversityName") courseUniversityName: String
    ): Response<PriceEstimationResponse>

    @POST("tutoring-sessions/")
    suspend fun postTutoringSession(@Body request: PostTutoringSessionRequest): Response<PostTutoringSessionResponse>

    @GET("search-results-filter/")
    suspend fun getSearchResultsFilter(): Response<SearchResultFilterResponse>

    @POST("increase-filter-count/")
    suspend fun increaseFilterCount(@Body request: PostFilterCounterIncreaseRequest): Response<PostFilterCounterIncreaseResponse>

    @GET("tutorprofile/")
    suspend fun getTutorProfile(@Query("tutorId") tutorId: Int): Response<GetTutorProfileResponse>

    @POST("time-to-book/")
    suspend fun postTimeToBook(@Body request: PostTimeToBookRequest): Response<PostTimeToBookResponse>

    @POST("profile-creation-time/")
    suspend fun postTimeTosSignUp(@Body request: PostTimeToSignUpRequest)

    @POST("tutor-profile-load-time/")
    suspend fun postTutorProfileLoadTime(@Body request: PostTutorProfileLoadTimeRequest): Response<PostTutorProfileLoadTimeResponse>

    @GET("tutoring-sessions-to-review/")
    suspend fun getTutoringSessionsToReview(
        @Query("studentId") studentId: Int
    ): Response<GetTutoringSessionsToReviewResponse>

    @POST("submit-review/")
    suspend fun postReview(@Body request: PostReviewRequest): Response<PostReviewResponse>

    @POST("send-notification/")
    suspend fun postNotification(@Body request: Notification):Response<SendNotificationResponse>

    @POST("get-notifications/")
    suspend fun getNotifications(@Body universityName:GetNotificationsRequest): Response<List<Notification>>

    @GET("booking-time")
    suspend fun bookingTime()

    @POST("email-check/")
    suspend fun email(@Body email: String): Response<ApiResponse>

    @POST("review-percentage/")
    suspend fun reviewPercentage(@Body id: String): Response<ReviewPercentageResponse>

    @GET("time-to-book-insight/")
    suspend fun getTimeToBookInsight(
        @Query("tutorId") tutorId: Int,
    ): Response<GetTimeToBookInsightResponse>

    @GET("similar-tutors-reviews/{tutorId}")
    suspend fun getSimilarTutorsReviews(@Path("tutorId") tutorId: Int): Response<SimilarTutorsResponse>

    @POST("book-tutoring-session/")
    suspend fun bookTutoringSession(@Body request: BookTutoringSessionRequest): Response<ApiResponse>

    @GET("booked-sessions/")
    suspend fun getBookedSessions(@Query("id") userId: Int): Response<BookedSessionsResponse>

    @GET("record-login/")
    suspend fun recordLogin(@Query("id") userId: Int): Response<ApiResponse>

    @POST("update-gamification/")
    suspend fun updateGamification(@Body request: UpdateGamificationRequest): Response<ApiResponse>

    @GET("leaderboard/")
    suspend fun getLeaderboard(): Response<List<LeaderboardEntry>>

    @GET("gamification-profile/{userId}/")
    suspend fun getGamificationProfile(
        @Path("userId") userId: Int
    ): Response<GamificationProfileResponse>

    @GET("top-posting-times/")
    suspend fun getTopPostingTimes(): Response<List<GetTopPostingTimesResponse>>

    @GET("booked-sessions-v2/")
    suspend fun getBookedSessionsV2(
        @Query("id") studentId: String
    ): Response<List<BookedSessionResponse>>

    @GET("most-demanded-subject/")
    suspend fun getMostDemandedSubject(): Response<GetMostDemandedSubjectResponse>

}
