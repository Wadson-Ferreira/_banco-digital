package br.com.cdb.bancodigital.entity.enums;

import java.math.BigDecimal;
import java.util.Arrays;

public enum CategoriaClientes {
    COMUM(BigDecimal.valueOf(12.00), BigDecimal.valueOf(1.5)),
    SUPER(BigDecimal.valueOf(8.00), BigDecimal.valueOf(2.0)),
    PREMIUM(BigDecimal.valueOf(0.00), BigDecimal.valueOf(3.6));

    private final BigDecimal taxaManutencao;
    private final BigDecimal taxaRendimento;

    CategoriaClientes(BigDecimal taxaManutencao, BigDecimal taxaRendimento) {
        this.taxaManutencao = taxaManutencao;
        this.taxaRendimento = taxaRendimento;
    }

    public BigDecimal getTaxaManutencao() {
        return taxaManutencao;
    }

    public BigDecimal getTaxaRendimento() {
        return taxaRendimento;
    }

    public static boolean categoriaValida(String cat) {
        return Arrays.stream(values())
                .anyMatch(c -> c.name().equalsIgnoreCase(cat));
    }
}