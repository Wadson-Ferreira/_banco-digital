package br.com.cdb.bancodigital.entity;

import br.com.cdb.bancodigital.entity.enums.TipoConta;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("POUPANCA")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class ContaPoupanca extends Conta {

    @Column(name = "taxa_rendimento_anual", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxaRendimentoAnual;

    public ContaPoupanca(Cliente cliente) {
        super(cliente, TipoConta.POUPANCA);
    }
}
