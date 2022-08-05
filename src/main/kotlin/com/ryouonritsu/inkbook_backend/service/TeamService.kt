package com.ryouonritsu.inkbook_backend.service

import com.ryouonritsu.inkbook_backend.entity.Team

/**
 *
 * @author WuKunchao
 */
interface TeamService {
    fun createNewTeam(team: Team)
    fun updateTeam(teamId: String, teamName: String, teamInfo: String)
    fun addMemberIntoTeam(userId: String, teamId: String, userPerm: String)
    fun checkPerm(userId: String, teamId: String): String?
    fun updatePerm(userId: String, teamId: String, userPerm: String)
    fun deleteTeam(teamId: String)
    fun searchTeamByUserId(userId: String): List<Map<String, String>>?
    fun searchMemberByTeamId(teamId: String): List<Map<String, String>>?
    fun deleteMemberByUserId(userId: String, teamId: String)
    fun searchTeamByTeamId(teamId: String): Map<String, String>?
    fun getRecentViewList(user_id: String): List<Map<String, String>>?
    fun checkRecentView(user_id: String, teamId: String): String
    fun updateRecentView(user_id: String, teamId: String, lastViewedTime: String)
    fun addRecentView(user_id: String, teamId: String, lastViewedTime: String)
}