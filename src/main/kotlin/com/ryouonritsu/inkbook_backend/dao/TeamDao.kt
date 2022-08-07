package com.ryouonritsu.inkbook_backend.dao

import com.ryouonritsu.inkbook_backend.entity.Team
import org.apache.ibatis.annotations.Mapper

/**
 *
 * @author WuKunchao
 */
@Mapper
interface TeamDao {
    fun createNewTeam(team: Team)
    fun updateTeam(teamId: String, teamName: String, teamInfo: String)
    fun addMemberIntoTeam(userId: String, teamId: String, userPerm: String)
    fun checkPerm(userId: String, teamId: String): String?
    fun updatePerm(userId: String, teamId: String, userPerm: String)
    fun deleteTeam(user_id: String, teamId: String)
    fun searchTeamByUserId(userId: String): List<Map<String, String>>?
    fun searchMemberByTeamId(teamId: String): List<Map<String, String>>?
    fun deleteMemberByUserId(userId: String, teamId: String)
    fun searchTeamByTeamId(teamId: String): Map<String, String>?
    fun addRecentView(user_id: String, teamId: String, lastViewedTime: String)
    fun getRecentViewList(user_id: String): List<Map<String, String>>?
    fun checkRecentView(user_id: String, teamId: String): String?
    fun updateRecentView(user_id: String, teamId: String, lastViewedTime: String)
}