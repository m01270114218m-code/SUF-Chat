package com.voicerooms.app.util

/** نتيجة موحّدة لكل عمليات الشبكة */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: String? = null) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}

inline fun <T> runCatchingApi(block: () -> T): ApiResult<T> =
    try {
        ApiResult.Success(block())
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "خطأ غير معروف", e.javaClass.simpleName)
    }

/** ترجمة أخطاء Supabase إلى رسائل عربية مفهومة */
fun translateError(message: String?): String {
    val m = message ?: return "حدث خطأ غير متوقع"
    return when {
        m.contains("WRONG_PASSWORD") -> "كلمة المرور غير صحيحة"
        m.contains("ROOM_LOCKED") -> "الغرفة مقفلة حاليًا"
        m.contains("ROOM_FULL") -> "الغرفة ممتلئة"
        m.contains("USER_BANNED") -> "أنت محظور من هذه الغرفة"
        m.contains("SEAT_TAKEN") -> "المقعد محجوز"
        m.contains("MIC_LOCKED") -> "المايك مقفل في هذه الغرفة"
        m.contains("INSUFFICIENT_BALANCE") -> "رصيدك غير كافٍ"
        m.contains("NOT_AUTHORIZED") -> "ليس لديك صلاحية"
        m.contains("Invalid login credentials") -> "بيانات الدخول غير صحيحة"
        m.contains("already registered") -> "البريد مسجّل بالفعل"
        m.contains("Password should be") -> "كلمة المرور ضعيفة (6 أحرف على الأقل)"
        m.contains("Unable to resolve host") || m.contains("UnknownHost") -> "تحقق من اتصال الإنترنت"
        else -> m
    }
}
