/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2024 by Roman Kryvolapov
 **/
package com.digital.sofia.data.utils

import org.mapstruct.MapperConfig
import org.mapstruct.ReportingPolicy

// This setting is needed to throw an error when compiling the project if the classes are not identical
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.ERROR)
interface StrictMapperConfig