package com.linguaflow.linguaflow.service;

import com.linguaflow.linguaflow.entity.TouristSite;
import com.linguaflow.linguaflow.repository.CategoryRepository;
import com.linguaflow.linguaflow.repository.NarrationRepository;
import com.linguaflow.linguaflow.repository.QRCodeRepository;
import com.linguaflow.linguaflow.repository.TouristSiteRepository;
import com.linguaflow.linguaflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TouristSiteServiceTest {

    @Mock
    private QRCodeRepository qrCodeRepository;

    @Mock
    private TouristSiteRepository touristSiteRepository;

    @Mock
    private TouristSiteMapper touristSiteMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private NarrationRepository narrationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TouristSiteService touristSiteService;

    @Test
    void deleteTouristSite_withNarrations_throwsAndDoesNotDelete() {
        var site = TouristSite.builder().siteId(42).build();
        when(touristSiteRepository.findById(42)).thenReturn(Optional.of(site));
        when(qrCodeRepository.existsByTouristSite_SiteId(42)).thenReturn(false);
        when(narrationRepository.existsByTouristSite_SiteId(42)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> touristSiteService.deleteTouristSite(42));

        verify(touristSiteRepository, never()).delete(site);
    }

    @Test
    void deleteTouristSite_withoutDependencies_deletesSite() {
        var site = TouristSite.builder().siteId(42).build();
        when(touristSiteRepository.findById(42)).thenReturn(Optional.of(site));
        when(qrCodeRepository.existsByTouristSite_SiteId(42)).thenReturn(false);
        when(narrationRepository.existsByTouristSite_SiteId(42)).thenReturn(false);

        assertDoesNotThrow(() -> touristSiteService.deleteTouristSite(42));

        verify(touristSiteRepository).delete(site);
    }
}
