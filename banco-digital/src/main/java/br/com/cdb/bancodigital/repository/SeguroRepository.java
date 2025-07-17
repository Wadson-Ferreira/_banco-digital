package br.com.cdb.bancodigital.repository;

import br.com.cdb.bancodigital.entity.Seguro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeguroRepository extends JpaRepository<Seguro, Long> {
}
