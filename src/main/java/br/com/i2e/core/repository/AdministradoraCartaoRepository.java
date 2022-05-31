package br.com.i2e.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.i2e.common.model.order.AdministradoraCartao;

public interface AdministradoraCartaoRepository extends JpaRepository<AdministradoraCartao, Long>  {

	public AdministradoraCartao findByCodigo(String codigo);
}