package com.gymmanagement.medicine;

import com.gymmanagement.common.mapper.CentralMapperConfig;
import com.gymmanagement.medicine.dto.MedicineResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class MedicineMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", expression = "java(medicine.getClient().getUser().getFullName())")
    @Mapping(target = "dieticianId", source = "dietician.id")
    public abstract MedicineResponse toResponse(Medicine medicine);
}
