package br.com.i2e.core.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javax.annotation.PostConstruct;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.com.i2e.common.dto.PeriodParamenterDTO;
import br.com.i2e.common.enums.I2EStatus;
import br.com.i2e.common.enums.I2EType;
import br.com.i2e.common.model.Cliente;
import br.com.i2e.common.model.I2EMessage;
import br.com.i2e.common.util.JsonUtils;
import br.com.i2e.core.repository.CustomerRepository;
import br.com.i2e.core.repository.I2EMessageRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service@Transactional
public class CustomerService {
	
	private static String I2E_BACKEND_REQUEST_QUEUE = "i2e.request.queue"; 
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private I2EMessageRepository msgRepository;
	@Autowired
	private CustomerRepository customerRepository;
	@Autowired
	private ModelMapper modelMapper;
	
	@PostConstruct
	public void postInit() {
		
		modelMapper.addMappings(new PropertyMap<Cliente, Cliente>() {
		                @Override
		                protected void configure() {
		                    skip(destination.getId());
		                }
		            });
	}

	public void fetchCustomers() {
		
		doRequest( I2EType.CUSTOMER_BY_PERIOD, new PeriodParamenterDTO( 
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
	
	public void updateCustomer( I2EMessage msg ) {
		var newCusomer = JsonUtils.fromJson( msg.getResponse(), Cliente.class );
		var customer = customerRepository.findByCpfCnpj( newCusomer.getCpfCnpj() );
		
		if ( customer != null ) {
			
			modelMapper.map( newCusomer, customer );
			
			customerRepository.save( customer );
		} else {
			
			customerRepository.save( newCusomer );
		}
	}
	
	@Bean
	public CommandLineRunner testFetchCustomer() {
		
		return (String[] args) -> {
			this.fetchCustomers();
		};
	}
}
