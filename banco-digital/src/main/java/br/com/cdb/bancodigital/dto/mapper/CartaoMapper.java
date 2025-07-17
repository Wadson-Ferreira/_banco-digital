package br.com.cdb.bancodigital.dto.mapper;

import br.com.cdb.bancodigital.dto.response.CartaoCreditoResponseDTO;
import br.com.cdb.bancodigital.dto.response.CartaoDebitoResponseDTO;
import br.com.cdb.bancodigital.entity.CartaoCredito;
import br.com.cdb.bancodigital.entity.CartaoDebito;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartaoMapper {
    @Mapping(source = "conta.numero", target = "contaNumero")
    CartaoCreditoResponseDTO toCreditoDto(CartaoCredito cartao);

    @Mapping(source = "conta.numero", target = "contaNumero")
    CartaoDebitoResponseDTO toDebitoDto(CartaoDebito cartao);
}