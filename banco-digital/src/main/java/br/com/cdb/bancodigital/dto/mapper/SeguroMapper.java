package br.com.cdb.bancodigital.dto.mapper;

import br.com.cdb.bancodigital.dto.request.SeguroRequestDTO;
import br.com.cdb.bancodigital.dto.response.SeguroResponseDTO;
import br.com.cdb.bancodigital.entity.Seguro;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SeguroMapper {
    Seguro toEntity(SeguroRequestDTO dto);

    SeguroResponseDTO toResponseDTO(Seguro seguro);

    void updateEntityFromDto(SeguroRequestDTO dto, @MappingTarget Seguro seguro);
}