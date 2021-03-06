package kr.co.gooroomeelite.model

/**
 * @author Gnoss
 * @email silmxmail@naver.com
 * @created 2021-06-15
 * @desc
 */
data class ContentDTO(
    var profileImageUrl : String? = null,
    var nickname : String? = null,
    var userId : String? = null,
    var studyTime : Int = 0,
    var google : Boolean = false,
    var todaystudytime : Int? = 0
)
