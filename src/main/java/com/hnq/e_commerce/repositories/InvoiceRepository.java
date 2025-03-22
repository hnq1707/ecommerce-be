package com.hnq.e_commerce.repositories;

import com.hnq.e_commerce.entities.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    Page<Invoice> findByIsPaid(boolean isPaid, Pageable pageable);
}
