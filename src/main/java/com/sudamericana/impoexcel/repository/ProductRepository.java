package com.sudamericana.impoexcel.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sudamericana.impoexcel.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Page<Product> findByCodigo(String codigo, Pageable pageable);
    
    List<Product> findByFechaCreacionBetween(Date fechaInicio, Date fechaFin);
    
    List<Product> findByCodigoContainingIgnoreCase(String codigo);
    
    Page<Product> findByCodigoContainingIgnoreCase(String codigo, Pageable pageable);
    
    Page<Product> findByMarcaContainingIgnoreCase(String marca, Pageable pageable);
    
    Page<Product> findByProveedorContainingIgnoreCase(String proveedor, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.codigo IN :codigos OR UPPER(p.marca) LIKE UPPER(CONCAT('%', :marca, '%')) OR UPPER(p.proveedor) LIKE UPPER(CONCAT('%', :proveedor, '%'))")
    Page<Product> findByCodigoInOrMarcaContainingOrProveedorContaining(
            @Param("codigos") List<String> codigos,
            @Param("marca") String marca,
            @Param("proveedor") String proveedor,
            Pageable pageable);

            @Query("SELECT p FROM Product p WHERE " +
            "REPLACE(LOWER(p.codigo), ' ', '') LIKE LOWER(CONCAT('%', REPLACE(:codigoSinEspacios, ' ', ''), '%'))")
    List<Product> findByCodigoSinEspaciosContaining(@Param("codigoSinEspacios") String codigoSinEspacios);
}

