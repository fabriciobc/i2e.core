package br.com.i2e.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.i2e.common.model.order.Venda;

public interface SalesOrderRepository extends JpaRepository<Venda, Long>  {

}