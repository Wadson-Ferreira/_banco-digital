package br.com.cdb.bancodigital.repository;

import br.com.cdb.bancodigital.entity.Cartao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartaoRepository extends JpaRepository<Cartao, Long> {
    List<Cartao> findByContaNumero(Long contaNumero);
}
