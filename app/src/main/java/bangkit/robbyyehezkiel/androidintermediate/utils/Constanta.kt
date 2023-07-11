package bangkit.robbyyehezkiel.androidintermediate.utils

object Constanta {

    enum class AuthPreferences {
        UserUID, UserName, UserEmail, UserToken, UserLastLogin
    }

    enum class DetailStory {
        UserName, ImageURL, ContentDescription, Latitude, Longitude
    }

    enum class LocationPicker {
        IsPicked, Latitude, Longitude
    }

    const val preferenceName = "Settings"
    const val preferenceDefaultValue = "Not Set"

    val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")

    const val CAMERA_PERMISSION_CODE = 10
    const val STORAGE_PERMISSION_CODE = 20
    const val LOCATION_PERMISSION_CODE = 30
    const val tempToken =
        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLWZWNkFCelJ6QzFlOE9RckkiLCJpYXQiOjE2NTEwMjE4MzB9.fNi8G9VXnv8Sg1EHJq2KHOeMg_tbhLuo2Hqd6YMacK4"

    const val TAG_WIDGET = "WIDGET_STORY"
    const val TAG_STORY = "TEST_STORY"
    const val TAG_DOWNLOAD = "TEST_DOWNLOAD"
    const val TAG_AUTH = "TEST_AUTH"
}