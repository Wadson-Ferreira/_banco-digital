package br.com.cdb.bancodigital.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("CREDITO")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CartaoCredito extends Cartao {

    @Column(name = "limite_credito", nullable = false, precision = 12, scale = 2)
    private BigDecimal limiteCredito;

    @Column(name = "valor_gasto_mes", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorGastoMes = BigDecimal.ZERO;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "apolice_id", unique = true)
    private Apolice apolice;
}
