package br.com.i2e.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.i2e.common.model.order.Transportadora;

public interface TransportadoraRepository extends JpaRepository<Transportadora, Long>  {
	
	public Transportadora findByCnpj(String cnpj);

}