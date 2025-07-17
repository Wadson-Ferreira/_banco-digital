package br.com.cdb.bancodigital.dto.mapper;

import br.com.cdb.bancodigital.dto.response.ContaResponseDTO;
import br.com.cdb.bancodigital.entity.Conta;
import br.com.cdb.bancodigital.entity.ContaCorrente;
import br.com.cdb.bancodigital.entity.ContaPoupanca;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ContaMapper {

    @Mapping(source = "cliente.id", target = "clienteId")
    @Mapping(target = "taxaManutencao", ignore = true)
    @Mapping(target = "taxaRendimentoAnual", ignore = true)
    ContaResponseDTO toContaResponseDTO(Conta conta);

    @AfterMapping
    default void addSpecificAccountDetails(Conta conta, @MappingTarget ContaResponseDTO dto) {
        if (conta instanceof ContaCorrente) {
            dto.setTaxaManutencao(((ContaCorrente) conta).getTaxaManutencao());
        } else if (conta instanceof ContaPoupanca) {
            dto.setTaxaRendimentoAnual(((ContaPoupanca) conta).getTaxaRendimentoAnual());
        }
    }
}