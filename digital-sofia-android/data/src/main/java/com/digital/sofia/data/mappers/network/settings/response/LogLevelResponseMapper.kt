package com.digital.sofia.data.mappers.network.settings.response

import com.digital.sofia.data.mappers.base.BaseMapper
import com.digital.sofia.data.models.network.user.LogLevelResponse
import com.digital.sofia.domain.models.user.LogLevelModel

class LogLevelResponseMapper: BaseMapper<LogLevelResponse, LogLevelModel>() {
    override fun map(from: LogLevelResponse): LogLevelModel {
        return with(from) {
            LogLevelModel(level = level)
        }
    }
}