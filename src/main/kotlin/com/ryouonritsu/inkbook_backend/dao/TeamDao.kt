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
    fun addMemberIntoTeam(userId: String, teamId: String, userPerm: String)
    fun checkPerm(userId: String, teamId: String): String?
    fun deleteTeam(teamId: String)
}