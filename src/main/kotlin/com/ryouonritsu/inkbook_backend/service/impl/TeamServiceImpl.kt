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
    lateinit var teamDao : TeamDao

    override fun createNewTeam(team: Team) = teamDao.createNewTeam(team)
}