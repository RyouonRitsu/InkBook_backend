package com.ryouonritsu.inkbook_backend.service.impl

import com.ryouonritsu.inkbook_backend.dao.TeamDao
import com.ryouonritsu.inkbook_backend.entity.Team
import com.ryouonritsu.inkbook_backend.service.TeamService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 *
 * @author WuKunchao
 */
@Service
class TeamServiceImpl : TeamService {
    @Autowired
    lateinit var teamDao: TeamDao

    override fun createNewTeam(team: Team) = teamDao.createNewTeam(team)

    override fun addMemberIntoTeam(
        userId: String,
        teamId: String,
        userPerm: String
    ) = teamDao.addMemberIntoTeam(userId, teamId, userPerm)

    override fun checkPerm(userId: String, teamId: String) = teamDao.checkPerm(userId, teamId)

    override fun updatePerm(
        userId: String,
        teamId: String,
        userPerm: String
    ) = teamDao.updatePerm(userId, teamId, userPerm)

    override fun deleteTeam(user_id: String, teamId: String) = teamDao.deleteTeam(user_id, teamId)

    override fun searchTeamByUserId(userId: String) = teamDao.searchTeamByUserId(userId)

    override fun searchMemberByTeamId(teamId: String) = teamDao.searchMemberByTeamId(teamId)

    override fun deleteMemberByUserId(userId: String, teamId: String) = teamDao.deleteMemberByUserId(userId, teamId)

    override fun updateTeam(
        teamId: String,
        teamName: String,
        teamInfo: String
    ) = teamDao.updateTeam(teamId, teamName, teamInfo)

    override fun searchTeamByTeamId(teamId: String) = teamDao.searchTeamByTeamId(teamId)

    override fun addRecentView(
        user_id: String,
        teamId: String,
        lastViewedTime: String
    ) = teamDao.addRecentView(user_id, teamId, lastViewedTime)

    override fun getRecentViewList(user_id: String) = teamDao.getRecentViewList(user_id)

    override fun checkRecentView(user_id: String, teamId: String) = teamDao.checkRecentView(user_id, teamId)

    override fun updateRecentView(
        user_id: String,
        teamId: String,
        lastViewedTime: String
    ) = teamDao.updateRecentView(user_id, teamId, lastViewedTime)
}