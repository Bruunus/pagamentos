package br.com.alurafood.pagamentos.service;

import org.springframework.data.domain.Pageable;

import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import br.com.alurafood.pagamentos.dto.PagamentoDTO;
import br.com.alurafood.pagamentos.http.PedidoClient;
import br.com.alurafood.pagamentos.model.Pagamento;
import br.com.alurafood.pagamentos.model.Status;
import br.com.alurafood.pagamentos.repository.PagamentoRepository;

@Service
public class PagamentoService {

	@Autowired
	private PagamentoRepository repository;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private PedidoClient pedido;
	
	/**
	 * Método para criar um pagamento
	 * @param dto
	 * @return
	 */
	public PagamentoDTO criarPagamento(PagamentoDTO dto) {
        Pagamento pagamento = modelMapper.map(dto, Pagamento.class);
        pagamento.setStatus(Status.CRIADO);
        repository.save(pagamento);

        return modelMapper.map(pagamento, PagamentoDTO.class);
    }
	
	
	/**
	 * Método para realizar paginação do resultado da busca na tabela de pagamentos.
	 * 
	 * @param paginacao
	 * @return
	 */
	 public Page<PagamentoDTO> obterTodos(Pageable paginacao) {
		 return repository
				 .findAll(paginacao)
	             .map(p -> modelMapper.map(p, PagamentoDTO.class));
	 }
	 
	 
	 
	 /**
	  * Implementação para obter um objeto PagamentoDTO por um ID do banco de dados 
	  * usando Spring Data JPA e ModelMapper. 
	  * 
	  * @param id
	  * @return
	  */
	 public PagamentoDTO obterPorId(Long id) {
		 Pagamento pagamento = repository.findById(id)
				 .orElseThrow(() -> new EntityNotFoundException());

		 return modelMapper.map(pagamento, PagamentoDTO.class);
	 }
	 
	 
	 
	 /**
	  * Método para atualizar um pagamento.
	  * 
	  * @param id
	  * @param dto
	  * @return
	  */
	 public PagamentoDTO atualizarPagamento(Long id, PagamentoDTO dto) {
		 
		 Pagamento pagamento = modelMapper.map(dto, Pagamento.class);
	     pagamento.setId(id);
	     pagamento = repository.save(pagamento);
	     
	     return modelMapper.map(pagamento, PagamentoDTO.class);
	 }
			
	
	 /**
	  * Método para excluir um pagamento
	  * 
	  * @param id
	  */
	 public void excluirPagamento(Long id) {
	        repository.deleteById(id);
	    }
	 
	 
	 /**
	  * Método que realiza conmunicação com outro microserviço. Através do método
	  * atualizarPagamento() 
	  * Pede um id para ser passado;
	  * Verifica se este id está presente no banco;
	  * Realiza a modificação para status confirmado;
	  * Salva no banco
	  * Magica aqui: Realiza a atualização do status no microserviço pagamentos
	  * 
	  * Tecnologia do spring-boot Feigh Client - Comunicação Síncrona
	  * @param id
	  */
	 public void confimarPagamento(Long id) {
		 Optional<Pagamento> pagamento = repository.findById(id);
		 
		 if (!pagamento.isPresent()) {
			 throw new EntityNotFoundException();
		 }
		 
		 pagamento.get().setStatus(Status.CONFIRMADO);
		 repository.save(pagamento.get());
		 pedido.atualizaPagamento(pagamento.get().getPedidoId());
		 
		 
	 }


	 /**
	  * Metodo de fallback 
	  * (Para realizar este teste derrube o micro serviço pedidos
	  * @param id
	  */
	public void alteraStatus(Long id) {
		
		Optional<Pagamento> pagamento = repository.findById(id);
		 
		 if (!pagamento.isPresent()) {
			 throw new EntityNotFoundException();
		 }
		 
		 pagamento.get().setStatus(Status.CONFIRMADO_SEM_INTEGRACAO);
		 repository.save(pagamento.get());
		
	}
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	
	
}
