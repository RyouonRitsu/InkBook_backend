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

    override fun deleteTeam(teamId: String) = teamDao.deleteTeam(teamId)

    override fun searchTeamByUserId(userId: String) = teamDao.searchTeamByUserId(userId)

    override fun searchMemberByTeamId(teamId: String) = teamDao.searchMemberByTeamId(teamId)
}