package com.tutorapp.showTutors

sealed class Routes (val route:String){
    object ShowTutors: Routes("showTutors")
    object TutorProfile: Routes("TutorProfile/{id}/{name}/{course}/{phone}/{reviews_score}")

}