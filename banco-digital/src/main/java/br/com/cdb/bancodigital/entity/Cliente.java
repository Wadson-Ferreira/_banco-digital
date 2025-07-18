package br.com.cdb.bancodigital.entity;

import br.com.cdb.bancodigital.entity.enums.CategoriaCliente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 11, nullable = false, unique = true)
    private String cpf;

    @Column(length = 100, nullable = false)
    private String nome;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "rua",       column = @Column(name = "end_rua",       nullable = false, length = 100)),
            @AttributeOverride(name = "numero",    column = @Column(name = "end_numero",    nullable = false, length = 10)),
            @AttributeOverride(name = "complemento", column = @Column(name = "end_complemento", length = 50)),
            @AttributeOverride(name = "bairro",    column = @Column(name = "end_bairro",    nullable = false, length = 50)),
            @AttributeOverride(name = "cidade",    column = @Column(name = "end_cidade",    nullable = false, length = 50)),
            @AttributeOverride(name = "estado",    column = @Column(name = "end_estado",    nullable = false, length = 2)),
            @AttributeOverride(name = "cep",       column = @Column(name = "end_cep",       nullable = false, length = 8))
    })
    private Endereco endereco;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoriaClientes", nullable = false, length = 20)
    private CategoriaCliente categoriaCliente;

    @OneToMany(
            mappedBy = "cliente",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Conta> contas = new ArrayList<>();
}
