package com.ryouonritsu.inkbook_backend.service

import com.ryouonritsu.inkbook_backend.entity.Team

/**
 *
 * @author WuKunchao
 */
interface TeamService {
    fun createNewTeam(team: Team)
}