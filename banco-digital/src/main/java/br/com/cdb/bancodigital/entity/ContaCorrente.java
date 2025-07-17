package br.com.cdb.bancodigital.entity;

import br.com.cdb.bancodigital.entity.enums.TipoConta;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("CORRENTE")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class ContaCorrente extends Conta {

    @Column(name = "taxa_manutencao", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxaManutencao;

    public ContaCorrente(Cliente cliente) {
        super(cliente, TipoConta.CORRENTE);
    }
}
