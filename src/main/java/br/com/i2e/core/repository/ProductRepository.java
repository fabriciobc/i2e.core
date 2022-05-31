package br.com.i2e.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.i2e.common.model.catalog.Produto;

public interface ProductRepository extends JpaRepository<Produto, Long> {

	public Produto findByCodigoSKU(String cosidoSku);
}
