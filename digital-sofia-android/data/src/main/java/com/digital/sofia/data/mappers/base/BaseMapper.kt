/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.data.mappers.base

// TODO mapper example with mapstruct
// class ExampleMapper :
//    BaseMapper<Data, Data>() {
//    @Mapper(config = StrictMapperConfig::class)
//    fun interface ModelMapper {
//        fun map(from: Data): Data
//    }
//    override fun map(from: Data): Data {
//        return Mappers.getMapper(ModelMapper::class.java).map(from)
//    }
//}
// or example for custom mapper of list
// @Mapper(config = StrictMapperConfig::class)
// fun interface ExampleMapper {
//    @Mapping(target = "list", qualifiedByName = ["mapList"])
//    fun map(from: Data): Data
//    fun mapItem(data: Data): Data
//    @Named("mapList") use const
//    fun mapList(responses: List<Data>?): List<Data>? {
//        return responses?.map(this::mapItem)
//    }
// }

abstract class BaseMapper<From, To> {

    abstract fun map(from: From): To

    open fun mapList(fromList: List<From>): List<To> {
        return fromList.mapTo(ArrayList(fromList.size), this::map)
    }

}