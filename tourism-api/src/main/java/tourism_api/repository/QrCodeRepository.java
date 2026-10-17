
package tourism_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tourism_api.entity.QrCode;

public interface QrCodeRepository extends JpaRepository<QrCode, Integer> {
}
