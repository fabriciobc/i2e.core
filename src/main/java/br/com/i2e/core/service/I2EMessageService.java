package br.com.i2e.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.i2e.common.enums.I2EStatus;
import br.com.i2e.common.model.I2EMessage;
import br.com.i2e.core.repository.I2EMessageRepository;

@Service
public class I2EMessageService {
	
	@Autowired
	private CatalogService catalogoService;
	@Autowired
	private CustomerService customerService;
	@Autowired
	private I2EMessageRepository msgReporitory;

	public void receive( I2EMessage msg) {
		switch ( msg.getType() ) {
		case PRODUCT_DETAIL:
			catalogoService.updateProduct( msg );
			break;
		case BRAND_DETAIL:
			catalogoService.updateBrand( msg );
			break;
		case CUSTOMER_DETAIL:
			customerService.updateCustomer( msg );
			break;
		}
		
		msg.setStatus( I2EStatus.PROCESSED );
		msgReporitory.save( msg );
	}
}
