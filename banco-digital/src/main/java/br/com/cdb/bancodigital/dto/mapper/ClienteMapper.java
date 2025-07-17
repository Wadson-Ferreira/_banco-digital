package br.com.cdb.bancodigital.dto.mapper;

import br.com.cdb.bancodigital.dto.request.ClienteCreateRequestDTO;
import br.com.cdb.bancodigital.dto.request.ClienteUpdateRequestDTO;
import br.com.cdb.bancodigital.dto.response.ClienteResponseDTO;
import br.com.cdb.bancodigital.entity.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {ContaMapper.class, EnderecoMapper.class})
public interface ClienteMapper {
    Cliente toCliente(ClienteCreateRequestDTO dto);

    ClienteResponseDTO toClienteResponseDTO(Cliente cliente);

    void updateClienteFromDto(ClienteUpdateRequestDTO dto, @MappingTarget Cliente cliente);
}