package br.com.cdb.bancodigital.service;

import br.com.cdb.bancodigital.dto.mapper.CartaoMapper;
import br.com.cdb.bancodigital.dto.request.*;
import br.com.cdb.bancodigital.dto.response.CartaoCreditoResponseDTO;
import br.com.cdb.bancodigital.dto.response.CartaoDebitoResponseDTO;
import br.com.cdb.bancodigital.entity.Cartao;
import br.com.cdb.bancodigital.entity.CartaoCredito;
import br.com.cdb.bancodigital.entity.CartaoDebito;
import br.com.cdb.bancodigital.entity.Conta;
import br.com.cdb.bancodigital.entity.enums.StatusCartao;
import br.com.cdb.bancodigital.service.exception.BusinessException;
import br.com.cdb.bancodigital.service.exception.DataIntegrityException;
import br.com.cdb.bancodigital.service.exception.ObjectNotFoundException;
import br.com.cdb.bancodigital.repository.CartaoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CartaoService {

    private final CartaoRepository cartaoRepository;
    private final ContaService contaService;
    private final CartaoMapper cartaoMapper;

    public CartaoService(CartaoRepository cartaoRepository, ContaService contaService, CartaoMapper cartaoMapper) {
        this.cartaoRepository = cartaoRepository;
        this.contaService = contaService;
        this.cartaoMapper = cartaoMapper;
    }

    public CartaoCreditoResponseDTO criarCartaoDeCredito(CartaoCreditoCreateRequestDTO dto) {
        Conta conta = contaService.findById(dto.getNumeroConta());
        CartaoCredito cartaoCredito = new CartaoCredito();
        // Em 1 projeto real, a senha seria criptografada (ex: com Spring Security)
        cartaoCredito.setSenha(dto.getSenha());
        cartaoCredito.setConta(conta);
        cartaoCredito.setNumero(gerarNumeroCartao());
        cartaoCredito.setStatus(StatusCartao.ATIVO);
        cartaoCredito.setLimiteCredito(dto.getLimiteCredito());
        cartaoCredito = cartaoRepository.save(cartaoCredito);
        return cartaoMapper.toCreditoDto(cartaoCredito);
    }

    public CartaoDebitoResponseDTO criarCartaoDebito(CartaoDebitoCreateRequestDTO dto) {
        Conta conta = contaService.findById(dto.getNumeroConta());
        CartaoDebito cartaoDebito = new CartaoDebito();
        cartaoDebito.setSenha(dto.getSenha());
        cartaoDebito.setConta(conta);
        cartaoDebito.setNumero(gerarNumeroCartao());
        cartaoDebito.setLimiteDiario(dto.getLimiteDiario());
        cartaoDebito = cartaoRepository.save(cartaoDebito);
        return cartaoMapper.toDebitoDto(cartaoDebito);
    }

    @Transactional
    public void fazerPagamento(Long cartaoId, PagamentoRequestDTO dto) {
        Cartao cartao = findById(cartaoId);
        if (cartao.getStatus() == StatusCartao.INATIVO) {
            throw new BusinessException("Pagamento recusado: Cartão está inativo.");
        }
        if (cartao instanceof CartaoCredito) {
            pagarComCredito((CartaoCredito) cartao, dto.getValor());
        } else if (cartao instanceof CartaoDebito) {
            pagarComDebito((CartaoDebito) cartao, dto.getValor());
        } else {
            throw new BusinessException("Tipo de cartão desconhecido.");
        }
    }

    private void pagarComCredito(CartaoCredito cartao, BigDecimal valor) {
        BigDecimal limiteDisponivel = cartao.getLimiteCredito().subtract(cartao.getValorGastoMes());
        if (limiteDisponivel.compareTo(valor) < 0) {
            throw new BusinessException("Pagamento recusado: Limite de crédito insuficiente.");
        }
        cartao.setValorGastoMes(cartao.getValorGastoMes().add(valor));
        cartaoRepository.save(cartao);
    }

    private void pagarComDebito(CartaoDebito cartao, BigDecimal valor) {
        BigDecimal limiteDisponivelDiario = cartao.getLimiteDiario().subtract(cartao.getGastoDiario());
        if (limiteDisponivelDiario.compareTo(valor) < 0) {
            throw new BusinessException("Pagamento recusado: Limite de gasto diário excedido.");
        }
        contaService.sacar(cartao.getConta().getNumero(), valor);
        cartao.setGastoDiario(cartao.getGastoDiario().add(valor));
        cartaoRepository.save(cartao);
    }

    @Transactional
    public CartaoCreditoResponseDTO ajustarLimiteCredito(Long cartaoId, LimiteRequestDTO dto) {
        Cartao cartao = findById(cartaoId);
        if (!(cartao instanceof CartaoCredito cc)) {
            throw new BusinessException("Apenas cartões de crédito podem ter o limite de credito ajustado.");
        }
        cc.setLimiteCredito(dto.getNovoLimite());
        cc = cartaoRepository.save(cc);
        return cartaoMapper.toCreditoDto(cc);
    }

    @Transactional
    public CartaoDebitoResponseDTO ajustarLimiteDiarioDebito(Long cartaoId, LimiteRequestDTO dto) {
        Cartao cartao = findById(cartaoId);
        if (!(cartao instanceof CartaoDebito cd)) {
            throw new BusinessException("Apenas cartões de débito podem ter o limite diário ajustado.");
        }
        cd.setLimiteDiario(dto.getNovoLimite());
        cd = cartaoRepository.save(cd);
        return cartaoMapper.toDebitoDto(cd);
    }

    @Transactional(readOnly = true)
    public List<Object> findByConta(Long numeroConta) {
        contaService.findById(numeroConta);
        List<Cartao> cartoes = cartaoRepository.findByContaNumero(numeroConta);
        return cartoes.stream()
                .map(cartao -> {
                    if (cartao instanceof CartaoCredito) {
                        return cartaoMapper.toCreditoDto((CartaoCredito) cartao);
                    } else if (cartao instanceof CartaoDebito) {
                        return cartaoMapper.toDebitoDto((CartaoDebito) cartao);
                    } else {
                        throw new IllegalStateException("Tipo de cartão desconhecido: " + cartao.getClass());
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Cartao findById(Long id) {
        return cartaoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Cartão não encontrado! Id: " + id));
    }

    private String gerarNumeroCartao() {
        Random random = new Random();
        long num = Math.abs(random.nextLong()) % 1_000_000_000_000_000L;
        return String.format("5%015d", num);
    }

    @Transactional
    public CartaoCreditoResponseDTO alterarEstadoCC(Long cartaoId, AlterarEstadoCartaoRequestDTO dto) {
        Cartao cartao = findById(cartaoId);
        if (!(cartao instanceof CartaoCredito cartaoCredito)) {
            throw new BusinessException("ID informado não é de um cartão de crédito");
        }
        cartaoCredito.setStatus(dto.getNovoEstado());
        cartaoCredito = cartaoRepository.save(cartaoCredito);

        return cartaoMapper.toCreditoDto(cartaoCredito);
    }

    @Transactional
    public CartaoDebitoResponseDTO alterarEstadoCD(Long cartaoId, AlterarEstadoCartaoRequestDTO dto) {
        Cartao cartao = findById(cartaoId);
        if (!(cartao instanceof CartaoDebito cartaoDebito)) {
            throw new BusinessException("ID informado não é de um cartão de debito");
        }
        cartaoDebito.setStatus(dto.getNovoEstado());
        cartaoDebito = cartaoRepository.save(cartaoDebito);
        return cartaoMapper.toDebitoDto(cartaoDebito);
    }

    @Transactional
    public void delete(Long id) {
        Cartao cartao = findById(id);
        if(cartao.getStatus() == StatusCartao.ATIVO){
            throw new DataIntegrityException("Não é possível excluir cartões ativos.");
        }
        try {
            cartaoRepository.delete(cartao);
            cartaoRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityException(
                    "Não é possível excluir cartão com dados relacionados.");
        }
    }
}
