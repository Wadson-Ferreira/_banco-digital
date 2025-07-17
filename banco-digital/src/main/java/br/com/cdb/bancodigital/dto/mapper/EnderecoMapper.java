package br.com.cdb.bancodigital.dto.mapper;

import br.com.cdb.bancodigital.dto.externo.BrasilApiCepDTO;
import br.com.cdb.bancodigital.dto.request.EnderecoRequestDTO;
import br.com.cdb.bancodigital.dto.response.EnderecoResponseDTO;
import br.com.cdb.bancodigital.entity.Endereco;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnderecoMapper {

    Endereco toEndereco(EnderecoRequestDTO dto);

    EnderecoResponseDTO toEnderecoResponseDTO(Endereco endereco);

    @Mapping(source = "rua", target = "rua")
    @Mapping(source = "bairro", target = "bairro")
    @Mapping(source = "cidade", target = "cidade")
    @Mapping(source = "estado", target = "estado")
    @Mapping(source = "cep", target = "cep")

    EnderecoRequestDTO toEnderecoRequestDTO(BrasilApiCepDTO brasilApiCepDTO);
}