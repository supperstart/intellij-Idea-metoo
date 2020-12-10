package com.metoo.foundation.dao;
import org.springframework.stereotype.Repository;

import com.metoo.core.base.GenericDAO;
import com.metoo.foundation.domain.Payment;
@Repository("paymentDAO")
public class PaymentDAO extends GenericDAO<Payment> {

}