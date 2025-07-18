package br.com.cdb.bancodigital.service;

import br.com.cdb.bancodigital.dto.mapper.ContaMapper;
import br.com.cdb.bancodigital.dto.response.ContaResponseDTO;
import br.com.cdb.bancodigital.entity.Cliente;
import br.com.cdb.bancodigital.entity.Conta;
import br.com.cdb.bancodigital.entity.ContaCorrente;
import br.com.cdb.bancodigital.entity.ContaPoupanca;
import br.com.cdb.bancodigital.entity.enums.TipoConta;
import br.com.cdb.bancodigital.service.exception.BusinessException;
import br.com.cdb.bancodigital.service.exception.DataIntegrityException;
import br.com.cdb.bancodigital.service.exception.ObjectNotFoundException;
import br.com.cdb.bancodigital.repository.ContaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContaService {
    private final ContaRepository contaRepository;
    private final ClienteService clienteService;
    private final ContaMapper contaMapper;

    public ContaService(ContaRepository contaRepository, ClienteService clienteService, ContaMapper contaMapper) {
        this.contaRepository = contaRepository;
        this.clienteService = clienteService;
        this.contaMapper = contaMapper;
    }

    protected Conta findById(Long numero) {
        return contaRepository.findById(numero)
                .orElseThrow(() -> new ObjectNotFoundException("Conta não encontrada! Número: " + numero));
    }

    @Transactional(readOnly = true)
    public ContaResponseDTO getAccountDetails(Long numeroConta) {
        Conta conta = findById(numeroConta);
        return contaMapper.toContaResponseDTO(conta);
    }

    @Transactional
    public ContaResponseDTO abrirConta(Long clienteId, TipoConta tipoConta) {
        Cliente cliente = clienteService.findById(clienteId);
        Conta novaConta;

        if (tipoConta == TipoConta.CORRENTE) {
            novaConta = new ContaCorrente(cliente);
            ((ContaCorrente) novaConta).setTaxaManutencao(cliente.getCategoriaCliente().getTaxaManutencao());
        } else {
            novaConta = new ContaPoupanca(cliente);
            ((ContaPoupanca) novaConta).setTaxaRendimentoAnual(cliente.getCategoriaCliente().getTaxaRendimento());
        }
        novaConta.setAgencia(1001L);
        novaConta = contaRepository.save(novaConta);
        return contaMapper.toContaResponseDTO(novaConta);
    }

    @Transactional
    public ContaResponseDTO depositar(Long numeroConta, BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor do depósito deve ser positivo.");
        }
        Conta conta = findById(numeroConta);
        conta.setSaldo(conta.getSaldo().add(valor));
        conta = contaRepository.save(conta);
        return contaMapper.toContaResponseDTO(conta);
    }

    @Transactional
    public ContaResponseDTO sacar(Long numeroConta, BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor do saque deve ser positivo.");
        }
        Conta conta = findById(numeroConta);
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new BusinessException("Saldo insuficiente para saque.");
        }
        conta.setSaldo(conta.getSaldo().subtract(valor));
        conta = contaRepository.save(conta);
        return contaMapper.toContaResponseDTO(conta);
    }

    @Transactional
    public void transferir(Long numeroOrigem, Long numeroDestino, BigDecimal valor) {
        if (numeroOrigem.equals(numeroDestino)) {
            throw new BusinessException("A conta de origem e destino não podem ser a mesma.");
        }
        sacar(numeroOrigem, valor);
        depositar(numeroDestino, valor);
    }

    @Transactional(readOnly = true)
    public List<ContaResponseDTO> findByClienteId(Long clienteId) {
        clienteService.findById(clienteId);
        return contaRepository.findByClienteId(clienteId)
                .stream()
                .map(contaMapper::toContaResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long numero) {
        Conta conta = findById(numero);
        if(!conta.getCartoes().isEmpty()){
            throw new DataIntegrityException("Não é possível excluir a conta com cartões ativos.");
        }
        try {
            contaRepository.delete(conta);
            contaRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityException(
                    "Não é possível excluir conta com dados relacionados.");
        }
    }
}
