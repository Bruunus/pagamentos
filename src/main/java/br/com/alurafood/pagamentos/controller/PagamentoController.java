 package br.com.alurafood.pagamentos.controller;

import org.springframework.data.domain.Pageable;
import java.net.URI;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.alurafood.pagamentos.dto.PagamentoDTO;
import br.com.alurafood.pagamentos.service.PagamentoService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {
	
	@Autowired
	private PagamentoService service;
	
	
	 
	/**
	 * Controlador para bucar os pagamentos por paginação
	 * 
	 * @param paginacao
	 * @return
	 */
	@GetMapping
	public Page<PagamentoDTO> listar(@PageableDefault(size = 10) Pageable paginacao) {
	        return service.obterTodos(paginacao);
	}
	
	
	
	
	/**
	 * Recebe requisição e realiza a busca de um pagamento com base em seu id. Necessário passar esse valor no
	 * endpoint, ou seja, na url então por isso o uso necessário da @PathVariable
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public ResponseEntity<PagamentoDTO> detalhar(@PathVariable @NotNull Long id) {
		 PagamentoDTO dto = service.obterPorId(id);
	
		 return ResponseEntity.ok(dto);
	}
	
	
	/**
	 * Recebe requisição para criar um pagamento
	 * @param dto
	 * @param uriBuilder
	 * @return
	 */
    @PostMapping
    public ResponseEntity<PagamentoDTO> cadastrar(@RequestBody @Valid PagamentoDTO dto, UriComponentsBuilder uriBuilder) {
    	PagamentoDTO pagamento = service.criarPagamento(dto);
        URI endereco = uriBuilder.path("/pagamentos/{id}").buildAndExpand(pagamento.getId()).toUri();

        return ResponseEntity.created(endereco).body(pagamento);
    }
    
    
    /**
     * Recebe requisição de atualização.
     * @param id
     * @param dto
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<PagamentoDTO> atualizar(@PathVariable @NotNull Long id, @RequestBody @Valid PagamentoDTO dto) {
    	PagamentoDTO atualizado = service.atualizarPagamento(id, dto);
        return ResponseEntity.ok(atualizado);
    }
    
    
    
    /**
     * Recebe requisição para deletar um pagamento pelo id.
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<PagamentoDTO> remover(@PathVariable @NotNull Long id) {
        service.excluirPagamento(id);
        return ResponseEntity.noContent().build();
    }
    
    
    /**
     * Controller que realiza o pagamento do pedido (Microserviço externo)
     * @param id
     */
    @PatchMapping("/{id}/confirmar")
    @CircuitBreaker(name = "atualizarPedido", fallbackMethod = "pagamentoAutorizadoComIntegracaoPendente")
    public void confirmarPagamento(@PathVariable @NotNull Long id) {
    	service.confimarPagamento(id);
    }
    
    
    /**
     * Fallback
     * @param id
     * @param e
     */
    public void pagamentoAutorizadoComIntegracaoPendente(Long id, Exception e){
        service.alteraStatus(id);
    }
    
    
    
    
    

}
