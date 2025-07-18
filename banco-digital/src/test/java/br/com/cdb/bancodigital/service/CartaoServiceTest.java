package br.com.cdb.bancodigital.service;

import br.com.cdb.bancodigital.dto.mapper.CartaoMapper;
import br.com.cdb.bancodigital.dto.request.*;
import br.com.cdb.bancodigital.dto.response.CartaoCreditoResponseDTO;
import br.com.cdb.bancodigital.dto.response.CartaoDebitoResponseDTO;
import br.com.cdb.bancodigital.dto.response.ContaResponseDTO;
import br.com.cdb.bancodigital.entity.*;
import br.com.cdb.bancodigital.entity.enums.StatusCartao;
import br.com.cdb.bancodigital.service.exception.BusinessException;
import br.com.cdb.bancodigital.service.exception.DataIntegrityException;
import br.com.cdb.bancodigital.service.exception.ObjectNotFoundException;
import br.com.cdb.bancodigital.repository.CartaoRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartaoServiceTest {

    private AutoCloseable mocks;

    @InjectMocks
    private CartaoService service;

    @Mock
    private CartaoRepository cartaoRepository;

    @Mock
    private ContaService contaService;

    @Mock
    private CartaoMapper cartaoMapper;

    private ContaCorrente contaCorrente;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        contaCorrente = new ContaCorrente(cliente);
        contaCorrente.setNumero(100L);
        contaCorrente.setSaldo(BigDecimal.valueOf(1000));
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void criarCartaoDeCredito_DeveRetornarDto_QuandoDadosValidos() {
        CartaoCreditoCreateRequestDTO dtoReq = new CartaoCreditoCreateRequestDTO();
        dtoReq.setNumeroConta(100L);
        dtoReq.setSenha("1234");
        dtoReq.setLimiteCredito(BigDecimal.valueOf(5000));

        when(contaService.findById(100L)).thenReturn(contaCorrente);
        CartaoCredito cartaoEnt = new CartaoCredito();
        cartaoEnt.setConta(contaCorrente);
        cartaoEnt.setSenha("1234");
        cartaoEnt.setNumero("5" + "0".repeat(15));
        cartaoEnt.setStatus(StatusCartao.ATIVO);
        cartaoEnt.setLimiteCredito(dtoReq.getLimiteCredito());
        when(cartaoRepository.save(any(CartaoCredito.class))).thenReturn(cartaoEnt);

        CartaoCreditoResponseDTO dtoRes = new CartaoCreditoResponseDTO();
        when(cartaoMapper.toCreditoDto(cartaoEnt)).thenReturn(dtoRes);

        CartaoCreditoResponseDTO result = service.criarCartaoDeCredito(dtoReq);

        assertSame(dtoRes, result);
        verify(cartaoRepository).save(argThat(c ->
                c instanceof CartaoCredito &&
                        c.getSenha().equals("1234") &&
                        ((CartaoCredito)c).getLimiteCredito().equals(dtoReq.getLimiteCredito()) &&
                        c.getStatus()==StatusCartao.ATIVO
        ));
    }

    @Test
    void criarCartaoDebito_DeveRetornarDto_QuandoDadosValidos() {
        CartaoDebitoCreateRequestDTO dtoReq = new CartaoDebitoCreateRequestDTO();
        dtoReq.setNumeroConta(100L);
        dtoReq.setSenha("4321");
        dtoReq.setLimiteDiario(BigDecimal.valueOf(300));

        when(contaService.findById(100L)).thenReturn(contaCorrente);
        CartaoDebito cartaoEnt = new CartaoDebito();
        cartaoEnt.setConta(contaCorrente);
        cartaoEnt.setSenha("4321");
        cartaoEnt.setNumero("5" + "1".repeat(15));
        cartaoEnt.setLimiteDiario(dtoReq.getLimiteDiario());
        when(cartaoRepository.save(any(CartaoDebito.class))).thenReturn(cartaoEnt);

        CartaoDebitoResponseDTO dtoRes = new CartaoDebitoResponseDTO();
        when(cartaoMapper.toDebitoDto(cartaoEnt)).thenReturn(dtoRes);

        CartaoDebitoResponseDTO result = service.criarCartaoDebito(dtoReq);

        assertSame(dtoRes, result);
        verify(cartaoRepository).save(argThat(c ->
                c instanceof CartaoDebito &&
                        c.getSenha().equals("4321") &&
                        ((CartaoDebito)c).getLimiteDiario().equals(dtoReq.getLimiteDiario())
        ));
    }

    @Test
    void fazerPagamento_Credito_DeveAcrescentarGasto_QuandoSaldoDisponivel() {
        CartaoCredito cc = new CartaoCredito();
        cc.setId(2L);
        cc.setStatus(StatusCartao.ATIVO);
        cc.setLimiteCredito(BigDecimal.valueOf(1000));
        cc.setValorGastoMes(BigDecimal.valueOf(200));
        when(cartaoRepository.findById(2L)).thenReturn(Optional.of(cc));

        PagamentoRequestDTO pagDto = new PagamentoRequestDTO();
        pagDto.setValor(BigDecimal.valueOf(300));
        service.fazerPagamento(2L, pagDto);

        assertEquals(BigDecimal.valueOf(500), cc.getValorGastoMes());
        verify(cartaoRepository).save(cc);
    }

    @Test
    void fazerPagamento_Credito_DeveLancarException_QuandoLimiteInsuficiente() {
        CartaoCredito cc = new CartaoCredito();
        cc.setId(2L);
        cc.setStatus(StatusCartao.ATIVO);
        cc.setLimiteCredito(BigDecimal.valueOf(100));
        cc.setValorGastoMes(BigDecimal.valueOf(50));
        when(cartaoRepository.findById(2L)).thenReturn(Optional.of(cc));

        PagamentoRequestDTO pagDto = new PagamentoRequestDTO();
        pagDto.setValor(BigDecimal.valueOf(1000));
        assertThrows(BusinessException.class,
                () -> service.fazerPagamento(2L, pagDto));
    }

    @Test
    void fazerPagamento_Debito_DeveChamarSacarEAtualizarGasto() {
        CartaoDebito cd = new CartaoDebito();
        cd.setId(3L);
        cd.setStatus(StatusCartao.ATIVO);
        cd.setConta(contaCorrente);
        cd.setLimiteDiario(BigDecimal.valueOf(500));
        cd.setGastoDiario(BigDecimal.valueOf(100));

        when(cartaoRepository.findById(3L)).thenReturn(Optional.of(cd));

        // Corrigido: sacar() retorna ContaResponseDTO
        when(contaService.sacar(100L, BigDecimal.valueOf(200)))
                .thenReturn(new ContaResponseDTO());

        PagamentoRequestDTO pagDto = new PagamentoRequestDTO();
        pagDto.setValor(BigDecimal.valueOf(200));

        service.fazerPagamento(3L, pagDto);

        assertEquals(BigDecimal.valueOf(300), cd.getGastoDiario());
        verify(cartaoRepository).save(cd);
    }

    @Test
    void fazerPagamento_DeveLancarException_QuandoCartaoInativo() {
        CartaoCredito cc = new CartaoCredito();
        cc.setId(4L);
        cc.setStatus(StatusCartao.INATIVO);
        when(cartaoRepository.findById(4L)).thenReturn(Optional.of(cc));

        PagamentoRequestDTO pagDto = new PagamentoRequestDTO();
        pagDto.setValor(BigDecimal.ONE);
        assertThrows(BusinessException.class,
                () -> service.fazerPagamento(4L, pagDto));
    }

    @Test
    void ajustarLimiteCredito_DeveAtualizar_QuandoCartaoCredito() {
        CartaoCredito cc = new CartaoCredito();
        cc.setId(5L);
        cc.setLimiteCredito(BigDecimal.valueOf(100));
        when(cartaoRepository.findById(5L)).thenReturn(Optional.of(cc));
        when(cartaoRepository.save(cc)).thenReturn(cc);
        CartaoCreditoResponseDTO dtoRes = new CartaoCreditoResponseDTO();
        when(cartaoMapper.toCreditoDto(cc)).thenReturn(dtoRes);

        LimiteRequestDTO limDto = new LimiteRequestDTO();
        limDto.setNovoLimite(BigDecimal.valueOf(500));
        CartaoCreditoResponseDTO result = service.ajustarLimiteCredito(5L, limDto);

        assertSame(dtoRes, result);
        assertEquals(BigDecimal.valueOf(500), cc.getLimiteCredito());
    }

    @Test
    void ajustarLimiteCredito_DeveLancarException_QuandoNaoCredito() {
        CartaoDebito cd = new CartaoDebito();
        cd.setId(6L);
        when(cartaoRepository.findById(6L)).thenReturn(Optional.of(cd));

        LimiteRequestDTO limDto = new LimiteRequestDTO();
        limDto.setNovoLimite(BigDecimal.ZERO);
        assertThrows(BusinessException.class,
                () -> service.ajustarLimiteCredito(6L, limDto));
    }

    @Test
    void ajustarLimiteDebito_DeveAtualizar_QuandoCartaoDebito() {
        CartaoDebito cd = new CartaoDebito();
        cd.setId(7L);
        cd.setLimiteDiario(BigDecimal.valueOf(200));
        when(cartaoRepository.findById(7L)).thenReturn(Optional.of(cd));
        when(cartaoRepository.save(cd)).thenReturn(cd);
        CartaoDebitoResponseDTO dtoRes = new CartaoDebitoResponseDTO();
        when(cartaoMapper.toDebitoDto(cd)).thenReturn(dtoRes);

        LimiteRequestDTO limDto = new LimiteRequestDTO();
        limDto.setNovoLimite(BigDecimal.valueOf(1000));
        CartaoDebitoResponseDTO result = service.ajustarLimiteDiarioDebito(7L, limDto);

        assertSame(dtoRes, result);
        assertEquals(BigDecimal.valueOf(1000), cd.getLimiteDiario());
    }

    @Test
    void ajustarLimiteDebito_DeveLancarException_QuandoNaoDebito() {
        CartaoCredito cc = new CartaoCredito();
        cc.setId(8L);
        when(cartaoRepository.findById(8L)).thenReturn(Optional.of(cc));

        LimiteRequestDTO limDto = new LimiteRequestDTO();
        limDto.setNovoLimite(BigDecimal.ZERO);
        assertThrows(BusinessException.class,
                () -> service.ajustarLimiteDiarioDebito(8L, limDto));
    }

    @Test
    void findByConta_DeveRetornarDtos_MisturandoTipos() {
        CartaoCredito cc = new CartaoCredito();
        cc.setId(9L);
        CartaoDebito cd = new CartaoDebito();
        cd.setId(10L);
        when(contaService.findById(100L)).thenReturn(contaCorrente);
        when(cartaoRepository.findByContaNumero(100L)).thenReturn(List.of(cc, cd));

        CartaoCreditoResponseDTO dtoC = new CartaoCreditoResponseDTO();
        CartaoDebitoResponseDTO dtoD = new CartaoDebitoResponseDTO();
        when(cartaoMapper.toCreditoDto(cc)).thenReturn(dtoC);
        when(cartaoMapper.toDebitoDto(cd)).thenReturn(dtoD);

        List<Object> list = service.findByConta(100L);

        assertEquals(2, list.size());
        assertSame(dtoC, list.get(0));
        assertSame(dtoD, list.get(1));
    }

    @Test
    void findByConta_DeveLancarObjectNotFound_QuandoNaoExisteConta() {
        when(contaService.findById(100L)).thenThrow(new ObjectNotFoundException("x"));

        assertThrows(ObjectNotFoundException.class,
                () -> service.findByConta(100L));
    }

    @Test
    void findById_DeveLancarObjectNotFound_QuandoNaoEncontrado() {
        when(cartaoRepository.findById(55L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class,
                () -> service.findById(55L));
    }

    @Test
    void alterarEstadoCC_DeveAtualizar_QuandoCredito() {
        CartaoCredito cc = new CartaoCredito();
        cc.setId(11L);
        cc.setStatus(StatusCartao.INATIVO);
        when(cartaoRepository.findById(11L)).thenReturn(Optional.of(cc));
        when(cartaoRepository.save(cc)).thenReturn(cc);
        CartaoCreditoResponseDTO dtoRes = new CartaoCreditoResponseDTO();
        when(cartaoMapper.toCreditoDto(cc)).thenReturn(dtoRes);

        AlterarEstadoCartaoRequestDTO estadoDto = new AlterarEstadoCartaoRequestDTO();
        estadoDto.setNovoEstado(StatusCartao.ATIVO);
        CartaoCreditoResponseDTO result = service.alterarEstadoCC(11L, estadoDto);

        assertSame(dtoRes, result);
        assertEquals(StatusCartao.ATIVO, cc.getStatus());
    }

    @Test
    void alterarEstadoCC_DeveLancarException_QuandoNaoCredito() {
        CartaoDebito cd = new CartaoDebito();
        cd.setId(12L);
        when(cartaoRepository.findById(12L)).thenReturn(Optional.of(cd));

        AlterarEstadoCartaoRequestDTO estadoDto = new AlterarEstadoCartaoRequestDTO();
        estadoDto.setNovoEstado(StatusCartao.ATIVO);
        assertThrows(BusinessException.class,
                () -> service.alterarEstadoCC(12L, estadoDto));
    }

    @Test
    void alterarEstadoCD_DeveAtualizar_QuandoDebito() {
        CartaoDebito cd = new CartaoDebito();
        cd.setId(13L);
        cd.setStatus(StatusCartao.INATIVO);
        when(cartaoRepository.findById(13L)).thenReturn(Optional.of(cd));
        when(cartaoRepository.save(cd)).thenReturn(cd);
        CartaoDebitoResponseDTO dtoRes = new CartaoDebitoResponseDTO();
        when(cartaoMapper.toDebitoDto(cd)).thenReturn(dtoRes);

        AlterarEstadoCartaoRequestDTO estadoDto = new AlterarEstadoCartaoRequestDTO();
        estadoDto.setNovoEstado(StatusCartao.ATIVO);
        CartaoDebitoResponseDTO result = service.alterarEstadoCD(13L, estadoDto);

        assertSame(dtoRes, result);
        assertEquals(StatusCartao.ATIVO, cd.getStatus());
    }

    @Test
    void alterarEstadoCD_DeveLancarException_QuandoNaoDebito() {
        CartaoCredito cc = new CartaoCredito();
        cc.setId(14L);
        when(cartaoRepository.findById(14L)).thenReturn(Optional.of(cc));

        AlterarEstadoCartaoRequestDTO estadoDto = new AlterarEstadoCartaoRequestDTO();
        estadoDto.setNovoEstado(StatusCartao.ATIVO);
        assertThrows(BusinessException.class,
                () -> service.alterarEstadoCD(14L, estadoDto));
    }

    @Test
    void delete_DeveExcluir_QuandoInativo() {
        CartaoCredito cc = new CartaoCredito();
        cc.setId(15L);
        cc.setStatus(StatusCartao.INATIVO);
        when(cartaoRepository.findById(15L)).thenReturn(Optional.of(cc));
        doNothing().when(cartaoRepository).delete(cc);
        doNothing().when(cartaoRepository).flush();

        assertDoesNotThrow(() -> service.delete(15L));
        verify(cartaoRepository).delete(cc);
        verify(cartaoRepository).flush();
    }

    @Test
    void delete_DeveLancarDataIntegrityException_QuandoAtivo() {
        CartaoDebito cd = new CartaoDebito();
        cd.setId(16L);
        cd.setStatus(StatusCartao.ATIVO);
        when(cartaoRepository.findById(16L)).thenReturn(Optional.of(cd));

        assertThrows(DataIntegrityException.class,
                () -> service.delete(16L));
    }

    @Test
    void delete_DeveLancarDataIntegrityException_QuandoViolacaoBanco() {
        CartaoCredito cc = new CartaoCredito();
        cc.setId(17L);
        cc.setStatus(StatusCartao.INATIVO);
        when(cartaoRepository.findById(17L)).thenReturn(Optional.of(cc));
        doThrow(new org.springframework.dao.DataIntegrityViolationException("fk"))
                .when(cartaoRepository).flush();

        assertThrows(DataIntegrityException.class,
                () -> service.delete(17L));
    }
}
