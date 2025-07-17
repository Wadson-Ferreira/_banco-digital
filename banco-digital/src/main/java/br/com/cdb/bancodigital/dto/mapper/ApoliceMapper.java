package br.com.cdb.bancodigital.dto.mapper;

import br.com.cdb.bancodigital.dto.response.ApoliceResponseDTO;
import br.com.cdb.bancodigital.entity.Apolice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApoliceMapper {
    @Mapping(source = "cartaoCoberto.id", target = "cartaoId")
    @Mapping(source = "seguro.tipoSeguro", target = "tipoSeguro")
    ApoliceResponseDTO toDto(Apolice apolice);
}