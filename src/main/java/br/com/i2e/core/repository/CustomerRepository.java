package br.com.i2e.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.i2e.common.model.Cliente;


public interface CustomerRepository extends JpaRepository<Cliente, Long>  {

	public Cliente findByCpfCnpj(String cpfCnpj);
}
