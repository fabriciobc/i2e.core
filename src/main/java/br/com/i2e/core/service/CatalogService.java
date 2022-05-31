package br.com.i2e.core.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.com.i2e.common.dto.PeriodParamenterDTO;
import br.com.i2e.common.enums.I2EStatus;
import br.com.i2e.common.enums.I2EType;
import br.com.i2e.common.model.I2EMessage;
import br.com.i2e.common.model.catalog.Marca;
import br.com.i2e.common.model.catalog.Produto;
import br.com.i2e.common.util.JsonUtils;
import br.com.i2e.core.repository.I2EMessageRepository;
import br.com.i2e.core.repository.BrandRepository;
import br.com.i2e.core.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service@Transactional
public class CatalogService {

	private static String I2E_BACKEND_REQUEST_QUEUE = "i2e.request.queue"; 
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private I2EMessageRepository msgRepository;
	@Autowired
	private ProductRepository prdRepository;
	@Autowired
	private BrandRepository marcaRepository;
	
	/**
	 * Fetch catalog from backend system
	 */
	public void fetchCatalog() {
		
		doRequest( I2EType.REFRESH_CATALOG_BY_PERIOD, new PeriodParamenterDTO( 
				LocalDate.now().minus( 1, ChronoUnit.YEARS ), LocalDate.now() ) );
	}
	
	public void fetchBrands() {
		
		doRequest( I2EType.BRANDS_BY_PERIOD, new PeriodParamenterDTO( 
				LocalDate.now().minus( 10, ChronoUnit.YEARS ), LocalDate.now() ) );
	}
	
	private void doRequest( I2EType type, Object params ) {
		var msg = I2EMessage.get(); 
		msg.setType( type );
		msg.setStatus( I2EStatus.SENT );
		try {
			msg.setParameters( JsonUtils.toJson( params ) );

			log.info( "Sendding {} requesto to Backend ", type );
			rabbitTemplate.convertAndSend( I2E_BACKEND_REQUEST_QUEUE, JsonUtils.toJson( msg ) );
			
			msgRepository.save( msg );
		} catch ( JsonProcessingException | AmqpException e ) {
			
			e.printStackTrace();
			log.error( "Error on {} request to Backend", type );
			msg.setError( e.toString() );
			msgRepository.save( msg );
		}
	}
	
	public void updateProduct( I2EMessage msg ) {
		var newPrd = JsonUtils.fromJson( msg.getResponse(), Produto.class );
		var prd = prdRepository.findByCodigoSKU( newPrd.getCodigoSKU() );
		
		if ( prd != null ) {
			
			if ( newPrd.getDataCadastro() != null && newPrd.getDataCadastro().isAfter( prd.getDataCadastro() )  ) {
			
				if ( newPrd.getDataCadastro() != null ) {
					
					prd.setDataCadastro( newPrd.getDataCadastro() );
				}
				prd.setDescricao( newPrd.getDescricao() );
				prd.setDescricaoResumida( newPrd.getDescricaoResumida() );
				prd.setCodigoSKU( newPrd.getCodigoSKU() );
				prd.setCodigoBarras( newPrd.getCodigoBarras() );
				prd.setPeso( newPrd.getPeso() );
				prd.setNcm( newPrd.getNcm() );
				prd.setComprimento( newPrd.getComprimento() );
				prd.setAltura( newPrd.getAltura() );
				prd.setLargura( newPrd.getLargura() );
				if (newPrd.getMarca() != null) {
				
					setProductBrand( prd, newPrd.getMarca() );
				}
				prd.setAltura( newPrd.getAltura() );
				prd.setAltura( newPrd.getAltura() );
			}
		} else {

			if ( newPrd.getDataCadastro() == null ) {
				
				newPrd.setDataCadastro( LocalDate.now() );
			}
			
			if (newPrd.getMarca() != null) {
				
				setProductBrand( newPrd, newPrd.getMarca() );
			}
			
			if ( newPrd.getFotos() != null ) {
				newPrd.getFotos().forEach( f -> {
					log.debug( "newPrd {} ft {} ft.getProduto {}", newPrd, f, f.getProduto() );
					
					f.setProduto( newPrd );
				});
			}
			
			
			
			prdRepository.save( newPrd );
		}
	}

	private void setProductBrand( Produto prd, Marca newBrand ) {
		var marca = marcaRepository.findByCodigo( newBrand.getCodigo() );
		if ( marca == null ) {
			
			throw new IllegalArgumentException(" Brand not found: " + newBrand.getCodigo() ); 
		}
		prd.setMarca( marca );
	}

	public void updateBrand( I2EMessage msg ) {
		var newBrand = JsonUtils.fromJson( msg.getResponse(), Marca.class );
		var brand = marcaRepository.findByCodigo( newBrand.getCodigo() );
		
		if ( brand != null ) {
			
			brand.setNome( newBrand.getNome() );
			brand.setFabricante( newBrand.getFabricante() );
			marcaRepository.save( brand );
		} else {
			
			marcaRepository.save( newBrand );
		}
	}
	
//	@Bean
	public CommandLineRunner testFetchCatalog() {
		return (String[] args) -> {
			this.fetchCatalog();
		};
	}
	
//	@Bean
	public CommandLineRunner testFetchBrands() {
		return (String[] args) -> {
			this.fetchBrands();
		};
	}
}