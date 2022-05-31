package br.com.i2e.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.i2e.common.model.catalog.Marca;

public interface BrandRepository extends JpaRepository<Marca, Long> {
	
	public Marca findByCodigo( String codigo );
}
