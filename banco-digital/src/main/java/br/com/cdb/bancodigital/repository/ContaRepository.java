package br.com.cdb.bancodigital.repository;

import br.com.cdb.bancodigital.entity.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContaRepository extends JpaRepository<Conta, Long> {
    List<Conta> findByClienteId(Long clienteId);
}
