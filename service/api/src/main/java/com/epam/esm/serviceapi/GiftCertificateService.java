package com.epam.esm.serviceapi;

import com.epam.esm.serviceapi.dto.GiftCertificateDto;

public interface GiftCertificateService extends BaseService<GiftCertificateDto> {
    boolean update(GiftCertificateDto giftCertificateDto);
}