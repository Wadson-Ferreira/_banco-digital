package br.com.cdb.bancodigital.entity.enums;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum CategoriaCliente {
    COMUM    (new BigDecimal("12.00"), new BigDecimal("1.5")),
    SUPER    (new BigDecimal("8.00"),  new BigDecimal("2.0")),
    PREMIUM  (new BigDecimal("0.00"),  new BigDecimal("3.6"));

    private final BigDecimal taxaManutencao;
    private final BigDecimal taxaRendimento;

    CategoriaCliente(BigDecimal taxaManutencao, BigDecimal taxaRendimento) {
        this.taxaManutencao = taxaManutencao;
        this.taxaRendimento  = taxaRendimento;
    }
}