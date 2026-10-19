
package com.linguaflow.linguaflow.repository;

import com.linguaflow.linguaflow.entity.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QRCodeRepository
        extends JpaRepository<QRCode, Integer> {

    boolean existsByQrCodeValue(String qrCodeValue);
}
