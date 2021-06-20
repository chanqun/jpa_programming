package study.datajpa.dto

data class MemberDto(
    val id: Long,
    val username: String,
    val teamName: String? = null
)
