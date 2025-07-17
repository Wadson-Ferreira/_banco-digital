package br.com.cdb.bancodigital.entity;

import br.com.cdb.bancodigital.entity.enums.TipoConta;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conta")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_conta", discriminatorType = DiscriminatorType.STRING)
public abstract class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    protected Long numero;

    @Column(nullable = false)
    protected Long agencia;

    @Column(nullable = false, precision = 12, scale = 2)
    protected BigDecimal saldo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    protected Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conta", insertable = false, updatable = false)
    protected TipoConta tipoConta;

    @OneToMany(
            mappedBy = "conta",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    protected List<Cartao> cartoes = new ArrayList<>();

    public Conta(Cliente cliente, TipoConta tipoConta) {
        this.cliente   = cliente;
        this.tipoConta = tipoConta;
        this.saldo     = BigDecimal.ZERO;
        this.cartoes   = new ArrayList<>();
    }
}
